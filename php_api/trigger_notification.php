<?php
// trigger_notification.php
// API to trigger event-based push notifications (New Orders, Leave, Driver Allocation)

header('Content-Type: application/json');

if (isset($_POST['db_host']) && isset($_POST['db_username']) && isset($_POST['db_password']) && isset($_POST['db_name'])) {
    $servername = $_POST['db_host'];
    $username = $_POST['db_username'];
    $password = $_POST['db_password'];
    $dbname = $_POST['db_name'];
} else {
    echo json_encode(array("status" => "error", "message" => "Missing database credentials"));
    exit;
}

$user_id = isset($_POST['user_id']) ? $_POST['user_id'] : null;
$title = isset($_POST['title']) ? $_POST['title'] : null;
$body = isset($_POST['body']) ? $_POST['body'] : null;
$event_type = isset($_POST['event_type']) ? $_POST['event_type'] : 'general'; // new_order, leave_update, etc.
$extra_data = isset($_POST['extra_data']) ? json_decode($_POST['extra_data'], true) : [];

if (!$user_id || !$title || !$body) {
    echo json_encode(array("status" => "error", "message" => "Missing user_id, title, or body"));
    exit;
}

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    echo json_encode(array("status" => "error", "message" => "Connection failed: " . $conn->connect_error));
    exit;
}

// Fetch FCM Token and Numeric ID for the target user
$stmt = $conn->prepare("SELECT id, fcm_token FROM user_fcm_tokens WHERE username = ?");
$stmt->bind_param("s", $user_id); // we treat $user_id as the username string
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    $fcm_token = $row['fcm_token'];
    $numeric_id = $row['id'];

    // Include the sender utility
    $fcm_file = __DIR__ . '/send_fcm_notification.php';
    if (!file_exists($fcm_file)) {
        echo json_encode(array("status" => "error", "message" => "Required file missing: " . $fcm_file));
        exit;
    }
    require_once $fcm_file;
    
    // NOTE: You must provide the correct path to your google-services.json or service account json here
    $serviceAccountPath = __DIR__ . '/service-account.json'; // UPDATE THIS PATH
    
    $dbConfig = [
        'host' => $servername,
        'user' => $username,
        'pass' => $password,
        'name' => $dbname
    ];

    try {
        $sender = new FCMSender($serviceAccountPath, $dbConfig);
        
        $data = array_merge(["event_type" => $event_type], $extra_data);
        
        // Send the notification (Logging is now handled automatically inside sendNotification)
        $response = $sender->sendNotification($fcm_token, $title, $body, $data, $numeric_id);
        
        echo json_encode(array(
            "status" => "success", 
            "message" => "Notification sent and logged successfully",
            "fcm_response" => json_decode($response)
        ));
    } catch (Exception $e) {
        error_log("Trigger Notification Error for user $user_id: " . $e->getMessage());
        echo json_encode(array("status" => "error", "message" => "FCM Error: " . $e->getMessage()));
    }

} else {
    echo json_encode(array("status" => "error", "message" => "No FCM token found for user_id: " . $user_id));
}

$stmt->close();
$conn->close();
?>
