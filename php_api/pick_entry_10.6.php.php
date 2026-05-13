<?php
// Retrieve dynamic database connection parameters from POST request
$db_host = isset($_POST['db_host']) ? $_POST['db_host'] : '';
$db_username = isset($_POST['db_username']) ? $_POST['db_username'] : '';
$db_password = isset($_POST['db_password']) ? $_POST['db_password'] : '';
$db_name = isset($_POST['db_name']) ? $_POST['db_name'] : '';

// Validate database parameters
if (empty($db_host) || empty($db_username) || empty($db_name)) {
    echo json_encode(["status" => "error", "message" => "Missing database connection parameters"]);
    exit();
}

// Include the database connection file
include("connect_app.php");

// Check for missing required fields
if (empty($_POST['cust_code']) || empty($_POST['pick_msg']) || empty($_POST['user']) || empty($_POST['date_added'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit();
}

// Retrieve and sanitize the inputs
$cust_code = $_POST['cust_code'];
$pick_msg = $_POST['pick_msg'];
$user = $_POST['user'];
$remarks = isset($_POST['remarks']) ? $_POST['remarks'] : '';

// Validate and format the date_added to ensure it's in YYYY-MM-DD format
try {
    $date = new DateTime($_POST['date_added']);
    $date_added = $date->format('Y-m-d'); // Format as YYYY-MM-DD
} catch (Exception $e) {
    echo json_encode(["status" => "error", "message" => "Invalid date format for date_added"]);
    exit();
}

$applied_on = date('Y-m-d H:i:s'); // Current date-time when the leave is applied

// =========================================================================
// NEW LOGIC: Fetch > 25 days cylinders and append them to the Pick Message
// =========================================================================
$fetch_cyls = $conn->prepare("SELECT cyl_code, filled_with, elapsed_days FROM inventory_overdue_daily WHERE cust_code = ? AND elapsed_days > 25 ORDER BY filled_with ASC, elapsed_days DESC");
$fetch_cyls->bind_param("s", $cust_code);
$fetch_cyls->execute();
$result = $fetch_cyls->get_result();

$gasSummary = [];
$hasCylinders = false;

// Group by Gas Type
while ($row = $result->fetch_assoc()) {
    $gas = $row['filled_with'] ? $row['filled_with'] : 'Other';
    if (!isset($gasSummary[$gas])) {
        $gasSummary[$gas] = [];
    }
    $gasSummary[$gas][] = $row['cyl_code'] . ' (' . $row['elapsed_days'] . ' days)';
    $hasCylinders = true;
}
$fetch_cyls->close();

$detailedMsg = "";
if ($hasCylinders) {
    $detailedMsg .= "\n\nAuto-Targeted Aged Cylinders (>25 days):\n";
    foreach ($gasSummary as $gas => $cyls) {
        $detailedMsg .= $gas . " (" . count($cyls) . " units):\n" . implode(', ', $cyls) . "\n\n";
    }
}

// Combine original app message with our detailed cylinder list
$final_pick_msg = trim($pick_msg . "\n" . $detailedMsg. "\nEntry added from App.");
// =========================================================================

// 1. Insert Main Pickup Entry (Using $final_pick_msg)
$query = $conn->prepare("INSERT INTO pick_main (cust_code, date_added, pick_msg, timestamp, user, remarks, status) VALUES (?, ?, ?, ?, ?, ?, 'Pending')");
$query->bind_param("ssssss", $cust_code, $date_added, $final_pick_msg, $applied_on, $user, $remarks);

if ($query->execute()) {
    $srno = $conn->insert_id;

    // 2. AUTO-POPULATE pick_line (Android Compatibility)
    // Since the app doesn't send specific cylinders, we fetch everything > 25 days for this customer
    $line_query = "INSERT INTO pick_line (pick_srno, cyl_code, gas_type, elapsed_days)
                   SELECT ?, cyl_code, filled_with, elapsed_days 
                   FROM inventory_overdue_daily 
                   WHERE cust_code = ? AND elapsed_days > 25";
    
    $lp = $conn->prepare($line_query);
    $lp->bind_param("is", $srno, $cust_code);
    $lp->execute();

    // Send FCM notification to account role
    $notif_response = sendBroadcastNotification(
        "New Pick Request",
        "Pick request for $cust_code added by user #$user.",
        "pick_entry",
        ['pick_id' => (string)$srno, 'cust_code' => $cust_code],
        "account" // Targets users with 'account' role
    );

    if ($notif_response && isset($notif_response['status']) && $notif_response['status'] === 'success') {
        echo json_encode([
            "status" => "success", 
            "message" => "Pickup submitted with line tracking and notification sent", 
            "srno" => $srno,
            "notif_details" => $notif_response
        ]);
    } else {
        echo json_encode([
            "status" => "success", 
            "message" => "Pickup submitted with line tracking, but notification failed.", 
            "srno" => $srno,
            "notif_error" => isset($notif_response['message']) ? $notif_response['message'] : "Unknown error"
        ]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Failed to submit", "error" => $conn->error]);
}

$query->close();
$conn->close(); // Close the database connection

// ==========================================
// HELPER FUNCTION TO SEND NOTIFICATIONS
// ==========================================
function sendBroadcastNotification($title, $body, $event_type = 'broadcast', $extra_data = [], $role_key = null) {
    global $db_host, $db_username, $db_password, $db_name;

    $broadcast_api_url = "http://arnichem.co.in/intranet/barcode/APP/app_apis/send_broadcast.php";

    $postData = [
        'db_host'     => $db_host,
        'db_username' => $db_username,
        'db_password' => $db_password,
        'db_name'     => $db_name,
        'title'       => $title,
        'body'        => $body,
        'event_type'  => $event_type,
        'extra_data'  => json_encode($extra_data),
        'role_key'    => $role_key
    ];

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $broadcast_api_url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($postData));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false); 
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
    curl_setopt($ch, CURLOPT_TIMEOUT, 15);

    $response = curl_exec($ch);
    $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($response === false) {
        return ["status" => "error", "message" => "CURL Error"];
    }

    return json_decode($response, true);
}
?>
