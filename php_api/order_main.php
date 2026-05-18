<?php
// Set the response type to JSON
header('Content-Type: application/json');

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
if (empty($_POST['cust_code']) || empty($_POST['order_msg']) || empty($_POST['user']) || empty($_POST['date_added'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields (cust_code, order_msg, user, or date_added)"]);
    exit();
}

// Retrieve and sanitize the inputs
$cust_code = $_POST['cust_code'];
$order_msg = $_POST['order_msg'];
$user = $_POST['user'];
$remarks = isset($_POST['remarks']) ? $_POST['remarks'] : '';

try {
    $date = new DateTime($_POST['date_added']);
    $date_added = $date->format('Y-m-d'); 
} catch (Exception $e) {
    echo json_encode(["status" => "error", "message" => "Invalid date format for date_added"]);
    exit();
}

$applied_on = date('Y-m-d H:i:s');

$query = $conn->prepare("INSERT INTO order_main (cust_code, date_added, order_msg, timestamp, user, remarks) VALUES (?, ?, ?, ?, ?, ?)");

if ($query) {
    $query->bind_param("ssssss", $cust_code, $date_added, $order_msg, $applied_on, $user, $remarks);

    if ($query->execute()) {
        $srno = $conn->insert_id;

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

        // Embed full order details in data payload so app can render without a second API call
        $link = "https://arnisol.com/intranet/view_business_partner.php?code=" . urlencode($cust_code);
        $notif_extra = [
            'order_id'   => (string)$srno,
            'srno'       => (string)$srno,
            'code'       => $cust_code,
            'name'       => $cust_name_display,
            'message'    => $order_msg,
            'remarks'    => $remarks,
            'date_added' => date('d-m-Y', strtotime($date_added)),
            'link'       => $link,
        ];

        // Send notification ONLY to users with role_key = 'account'
        $notif_response = sendBroadcastNotification(
            "New Order Created",
            "Order #$srno - $cust_name_display. Tap to view.",
            "new_order",
            $notif_extra,
            "account"   // <-- only 'account' role users receive this
        );

        if ($notif_response && isset($notif_response['status']) && $notif_response['status'] === 'success') {
            echo json_encode([
                "status" => "success", 
                "message" => "Order submitted successfully", 
                "srno" => $srno,
                "notif_details" => $notif_response
            ]);
        } else {
            $error_msg = isset($notif_response['message']) ? $notif_response['message'] : "Unknown notification error";
            echo json_encode([
                "status" => "error", 
                "message" => "Order submitted but notification failed. Check error log for details.", 
                "srno" => $srno,
                "notif_error" => $error_msg
            ]);
        }

    } else {
        echo json_encode(["status" => "error", "message" => "Failed to submit order", "error" => $query->error]);
    }
    $query->close();
} else {
    echo json_encode(["status" => "error", "message" => "Failed to prepare SQL statement", "error" => $conn->error]);
}

$conn->close(); 

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
