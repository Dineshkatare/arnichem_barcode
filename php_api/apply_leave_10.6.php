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
    ('$emp_id', '$manager_id', '$from_date', '$to_date', '$type', '$reason', '$applied_on', '$approved_by', '', '$joining_date', '$remarks', '$status')";

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

    // Fetch employee name for notification payload
    $emp_name = "User #$emp_id";
    $emp_stmt = $conn->prepare("SELECT firstname, lastname FROM users WHERE id = ? LIMIT 1");
    if ($emp_stmt) {
        $emp_stmt->bind_param("s", $emp_id);
        $emp_stmt->execute();
        $emp_result = $emp_stmt->get_result();
        if ($emp_row = $emp_result->fetch_assoc()) {
            $emp_name = trim($emp_row['firstname'] . ' ' . $emp_row['lastname']);
        }
        $emp_stmt->close();
    }

    $notif_extra = [
        'leave_id'     => (string)$leave_srno,
        'emp_name'     => $emp_name,
        'leave_type'   => $type,
        'from_date'    => $from_date,
        'to_date'      => $to_date,
        'reason'       => $reason,
        'leave_status' => $status,
    ];

    // Send FCM notification to HR role
    $notif_response = sendBroadcastNotification(
        "New Leave Application",
        "Leave Request #$leave_srno - $emp_name. Tap to view.",
        "leave_request",
        $notif_extra,
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
