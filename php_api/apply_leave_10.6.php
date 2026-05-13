<?php
// Retrieve dynamic database connection parameters from POST request
$db_host = $_POST['db_host'];  
$db_username = $_POST['db_username'];  
$db_password = $_POST['db_password'];  
$db_name = $_POST['db_name'];

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
if (!isset($_POST['emp_id']) || !isset($_POST['from_date']) || !isset($_POST['to_date']) || !isset($_POST['type']) || !isset($_POST['reason']) || !isset($_POST['joining_date'])) {
    echo json_encode(["status" => "error", "message" => "Missing fields"]);
    exit();
}

// Use the inputs directly as in the previous approach
$emp_id = $_POST['emp_id'];
$from_date = $_POST['from_date'];
$to_date = $_POST['to_date'];
$type = $_POST['type']; // 'Leave', 'Comp Off', or 'LOP'
$reason = $_POST['reason'];
$joining_date = $_POST['joining_date'];
$applied_on = date('Y-m-d H:i:s'); // Current date-time when the leave is applied

// Fetch the manager_id from the database using the emp_id
$query_manager = "SELECT manager_id FROM users WHERE id = '$emp_id'";
$result_manager = mysqli_query($conn, $query_manager);

if (!$result_manager || mysqli_num_rows($result_manager) == 0) {
    echo json_encode(["status" => "error", "message" => "Manager not found for this employee"]);
    exit();
}

$row_manager = mysqli_fetch_assoc($result_manager);
$manager_id = $row_manager['manager_id'];

// Initially setting status as 'Applied', remarks, and approval fields as empty
$status = 'Applied';
$approved_by = ''; // No approval yet
$approved_on = null;
$remarks = ''; // No remarks yet
$leave_srno = '';

// Insert into the leave_main table
$query = "INSERT INTO leave_main 
    (emp_id, manager_id, from_date, to_date, type, reason, applied_on, approved_by, approved_on, joining_date, remarks, status) 
    VALUES 
    ('$emp_id', '$manager_id', '$from_date', '$to_date', '$type', '$reason', '$applied_on', '$approved_by', NULL, '$joining_date', '$remarks', '$status')";

// Execute the query and return the response
if (mysqli_query($conn, $query)) {
    $leave_srno = mysqli_insert_id($conn); 
    
    $remote_email_url = 'https://www.arnisol.com/intranet/send_leave_email.php'; // URL to the new script
    $secret_key = 'YourSuperSecretKeyHere123!@#'; // Must match the key in the remote script

    $post_data = [
        'leave_srno' => $leave_srno,
        'secret_key' => $secret_key
    ];

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $remote_email_url);
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($post_data));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 15); // Increased timeout for remote script processing

    $response = curl_exec($ch);
    $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($http_code == 200) {
        error_log("Remote email trigger successful for leave #$leave_srno. Response: $response");
    } else {
        error_log("Failed to trigger email for leave #$leave_srno. HTTP Code: $http_code. Response: $response");
    }

    // Send FCM notification to HR role
    $notif_response = sendBroadcastNotification(
        "New Leave Application",
        "Employee applied for leave from $from_date to $to_date. Tap to review.",
        "leave_request",
        ['leave_id' => (string)$leave_srno, 'emp_id' => $emp_id],
        "hr" // Targets users with 'hr' role
    );

    if ($notif_response && isset($notif_response['status']) && $notif_response['status'] === 'success') {
        echo json_encode([
            "status" => "success", 
            "message" => "Leave application submitted and notification sent", 
            "srno" => $leave_srno,
            "notif_details" => $notif_response
        ]);
    } else {
        $error_msg = isset($notif_response['message']) ? $notif_response['message'] : "Unknown notification error";
        echo json_encode([
            "status" => "success", 
            "message" => "Leave application submitted, but notification failed.", 
            "srno" => $leave_srno,
            "notif_error" => $error_msg
        ]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Failed to submit leave application", "error" => mysqli_error($conn)]);
}

mysqli_close($conn); // Close the database connection

// ==========================================
// HELPER FUNCTION WITH ENHANCED LOGGING
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
    $curl_error = curl_error($ch);
    curl_close($ch);

    if ($response === false) {
        error_log("Notification Error: CURL failed. Error: $curl_error");
        return ["status" => "error", "message" => "CURL Connection Error: " . $curl_error];
    }

    if ($http_code !== 200) {
        error_log("Notification Error: Broadcast API returned HTTP $http_code. Response: $response");
        return ["status" => "error", "message" => "Broadcast API returned HTTP $http_code."];
    }

    $decoded_response = json_decode($response, true);
    if (json_last_error() !== JSON_ERROR_NONE) {
        error_log("Notification Error: Failed to decode JSON response. Raw response: $response");
        return ["status" => "error", "message" => "Invalid JSON response from Broadcast API"];
    }

    return $decoded_response;
}
?>
