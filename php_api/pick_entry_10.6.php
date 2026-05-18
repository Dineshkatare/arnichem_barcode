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

// Establish Database Connection
if (file_exists(__DIR__ . "/connect_app.php")) {
    include(__DIR__ . "/connect_app.php");
} else {
    $conn = new mysqli($db_host, $db_username, $db_password, $db_name);
}

if (!$conn || $conn->connect_error) {
    $err = (isset($conn->connect_error) && $conn->connect_error) ? $conn->connect_error : 'Unknown connection error';
    echo json_encode(["status" => "error", "message" => "Database connection failed: " . $err]);
    exit();
}

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

    // Fetch customer name for notification payload
    $cust_name_display = $cust_code;
    $name_stmt = $conn->prepare("SELECT name FROM businesspartners WHERE code = ? LIMIT 1");
    if ($name_stmt) {
        $name_stmt->bind_param("s", $cust_code);
        $name_stmt->execute();
        $name_result = $name_stmt->get_result();
        if ($name_row = $name_result->fetch_assoc()) {
            $cust_name_display = $name_row['name'];
        }
        $name_stmt->close();
    }

    // Embed full pick details in data payload so app can render without a second API call
    $link = "https://arnisol.com/intranet/view_business_partner.php?code=" . urlencode($cust_code);
    $notif_extra = [
        'pick_id'    => (string)$srno,
        'srno'       => (string)$srno,
        'code'       => $cust_code,
        'name'       => $cust_name_display,
        'message'    => $final_pick_msg,
        'remarks'    => $remarks,
        'date_added' => date('d-m-Y', strtotime($date_added)),
        'link'       => $link,
    ];

    // Send FCM notification to account role
    $notif_response = sendBroadcastNotification(
        "New Pick Request",
        "Pick Request #$srno - $cust_name_display. Tap to view.",
        "pick_entry",
        $notif_extra,
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
        $error_msg = isset($notif_response['message']) ? $notif_response['message'] : "Unknown notification error";
        echo json_encode([
            "status" => "success", 
            "message" => "Pickup submitted with line tracking, but notification failed.", 
            "srno" => $srno,
            "notif_error" => $error_msg
        ]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Failed to submit", "error" => $conn->error]);
}

$query->close();
$conn->close(); // Close the database connection

// ==========================================
// HELPER FUNCTION - NOW USING CENTRALIZED FCMSender
// ==========================================
function sendBroadcastNotification($title, $body, $event_type = 'broadcast', $extra_data = [], $role_key = null) {
    global $db_host, $db_username, $db_password, $db_name;

    $fcm_file = __DIR__ . '/send_fcm_notification.php';
    if (!file_exists($fcm_file)) {
        error_log("Notification Error: send_fcm_notification.php missing.");
        return ["status" => "error", "message" => "FCM library missing"];
    }
    
    require_once $fcm_file;
    $serviceAccountPath = __DIR__ . '/service-account.json';
    
    $dbConfig = [
        'host' => $db_host,
        'user' => $db_username,
        'pass' => $db_password,
        'name' => $db_name
    ];

    return FCMSender::broadcastToRole($serviceAccountPath, $dbConfig, $title, $body, $role_key, $event_type, $extra_data);
}
?>
