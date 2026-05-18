<?php
// send_broadcast.php
// Sends FCM notification to users filtered by role_key

header('Content-Type: application/json');

if (isset($_POST['db_host']) && isset($_POST['db_username']) && isset($_POST['db_password']) && isset($_POST['db_name'])) {
    $servername = $_POST['db_host'];
    $username   = $_POST['db_username'];
    $password   = $_POST['db_password'];
    $dbname     = $_POST['db_name'];
} else {
    echo json_encode(array("status" => "error", "message" => "Missing database credentials"));
    exit;
}

$title      = isset($_POST['title'])      ? $_POST['title']      : null;
$body       = isset($_POST['body'])       ? $_POST['body']       : null;
$role_key   = isset($_POST['role_key'])   ? $_POST['role_key']   : null;  // e.g. "account"
$event_type = isset($_POST['event_type']) ? $_POST['event_type'] : 'broadcast';
$extra_data = isset($_POST['extra_data']) ? json_decode($_POST['extra_data'], true) : [];

if (!$title || !$body) {
    echo json_encode(array("status" => "error", "message" => "Missing title or body"));
    exit;
}

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    echo json_encode(array("status" => "error", "message" => "Connection failed: " . $conn->connect_error));
    exit;
}

// Fetch FCM tokens — filter by role_key if provided.
// role_key in DB is stored as comma-separated e.g. "account,manager".
// FIND_IN_SET checks if the requested role exists in that stored list.
if ($role_key) {
    $stmt = $conn->prepare(
        "SELECT username, fcm_token FROM user_fcm_tokens
         WHERE fcm_token IS NOT NULL AND fcm_token != ''
         AND (FIND_IN_SET(?, role_key) > 0 OR role_key LIKE CONCAT('%', ?, '%'))"
    );
    $stmt->bind_param("ss", $role_key, $role_key);
    $stmt->execute();
    $result = $stmt->get_result();
} else {
    // No role filter -> send to ALL (broadcast)
    $result = $conn->query(
        "SELECT username, fcm_token FROM user_fcm_tokens
         WHERE fcm_token IS NOT NULL AND fcm_token != ''"
    );
}

    $fcm_file = __DIR__ . '/send_fcm_notification.php';
    if (!file_exists($fcm_file)) {
        echo json_encode(array("status" => "error", "message" => "Required file missing: " . $fcm_file));
        exit;
    }
    require_once $fcm_file;

    $serviceAccountPath = __DIR__ . '/service-account.json';
    $dbConfig = [
        'host' => $servername,
        'user' => $username,
        'pass' => $password,
        'name' => $dbname
    ];

    $response = FCMSender::broadcastToRole($serviceAccountPath, $dbConfig, $title, $body, $role_key, $event_type, $extra_data);
    echo json_encode($response);
} else {
    echo json_encode(array("status" => "success", "message" => "No users found for role: " . ($role_key ? $role_key : "all")));
}

$conn->close();
?>
