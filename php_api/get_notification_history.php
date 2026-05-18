<?php
// get_notification_history.php

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

if (!$user_id) {
    echo json_encode(array("status" => "error", "message" => "Missing user_id"));
    exit;
}

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    echo json_encode(array("status" => "error", "message" => "Connection failed: " . $conn->connect_error));
    exit;
}

// Create table if not exists and migrate
$conn->query("CREATE TABLE IF NOT EXISTS notification_history (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    user_id INT(11),
    title VARCHAR(255),
    body TEXT,
    data TEXT,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('sent','delivered','opened') DEFAULT 'sent'
)");

// Auto-migration: Check if 'username' column exists and rename/change it to 'user_id'
$check_uname = $conn->query("SHOW COLUMNS FROM notification_history LIKE 'username'");
if ($check_uname && $check_uname->num_rows > 0) {
    $conn->query("ALTER TABLE notification_history CHANGE COLUMN username user_id INT(11)");
}

// Ensure status is ENUM
$check_status = $conn->query("SHOW COLUMNS FROM notification_history LIKE 'status'");
if ($check_status && $check_status->num_rows > 0) {
    $status_row = $check_status->fetch_assoc();
    if ($status_row && strpos($status_row['Type'], 'enum') === false) {
         $conn->query("ALTER TABLE notification_history MODIFY COLUMN status ENUM('sent','delivered','opened') DEFAULT 'sent'");
    }
}

// Fetch history
$stmt = $conn->prepare("
    SELECT h.id, h.title, h.body, h.data, h.sent_at, h.status 
    FROM notification_history h
    INNER JOIN user_fcm_tokens t ON h.user_id = t.id
    WHERE t.username = ? 
    ORDER BY h.sent_at DESC 
    LIMIT 50
");
$stmt->bind_param("s", $user_id); // we still use $user_id variable but it contains the username now
$stmt->execute();
$result = $stmt->get_result();

$history = array();
while ($row = $result->fetch_assoc()) {
    $history[] = $row;
}

echo json_encode(array("status" => "success", "data" => $history));

$stmt->close();
$conn->close();
?>
