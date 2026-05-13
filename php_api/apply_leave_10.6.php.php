<?php
// Retrieve dynamic database connection parameters from POST request
$db_host = $_POST['db_host'];  
$db_username = $_POST['db_username'];  
$db_password = $_POST['db_password'];  
$db_name = $_POST['db_name'];

include("connect_app.php");

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
$approved_on = '0000-00-00 00:00:00'; // Default value for approved_on

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
    ('$emp_id', '$manager_id', '$from_date', '$to_date', '$type', '$reason', '$applied_on', '$approved_by', '$approved_on', '$joining_date', '$remarks', '$status')";

// Execute the query and return the response
if (mysqli_query($conn, $query)) {
	$leave_srno = mysqli_insert_id($conn); 
    //echo json_encode(["status" => "success", "message" => "Leave application submitted successfully".$leave_srno]);
	
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
        // The main goal was successful, so we return a success message to the app.
        error_log("Remote email trigger successful for leave #$leave_srno. Response: $response");
    } else {
        // Even if email fails, the leave was saved. Log the error for debugging.
        error_log("Failed to trigger email for leave #$leave_srno. HTTP Code: $http_code. Response: $response");
    }

    // Send FCM notification to HR role
    $notif_response = sendBroadcastNotification(
        "New Leave Application",
        "Employee #$emp_id applied for leave from $from_date to $to_date.",
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
        echo json_encode([
            "status" => "success", 
            "message" => "Leave application submitted, but notification failed.", 
            "srno" => $leave_srno,
            "notif_error" => isset($notif_response['message']) ? $notif_response['message'] : "Unknown error"
        ]);
    }
} else {
    // Return detailed error message
    echo json_encode(["status" => "error", "message" => "Failed to submit leave application", "error" => mysqli_error($conn)]);
}

mysqli_close($conn); // Close the database connection

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
