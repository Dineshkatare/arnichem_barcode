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

// Create table if not exists
$sql_create = "CREATE TABLE IF NOT EXISTS notification_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    data TEXT,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('sent', 'delivered', 'opened') DEFAULT 'sent'
)";
$conn->query($sql_create);

// Fetch history
$stmt = $conn->prepare("SELECT id, title, body, data, sent_at, status FROM notification_history WHERE username = ? ORDER BY sent_at DESC LIMIT 50");
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
