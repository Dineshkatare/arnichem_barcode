<?php
// update_notification_status.php

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

$notification_id = isset($_POST['notification_id']) ? $_POST['notification_id'] : null;
$status = isset($_POST['status']) ? $_POST['status'] : 'opened';

if (!$notification_id) {
    echo json_encode(array("status" => "error", "message" => "Missing notification_id"));
    exit;
}

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    echo json_encode(array("status" => "error", "message" => "Connection failed: " . $conn->connect_error));
    exit;
}

// Update status
$stmt = $conn->prepare("UPDATE notification_history SET status = ? WHERE id = ?");
$stmt->bind_param("si", $status, $notification_id);

if ($stmt->execute()) {
    echo json_encode(array("status" => "success", "message" => "Notification status updated"));
} else {
    echo json_encode(array("status" => "error", "message" => "Error updating status: " . $stmt->error));
}

$stmt->close();
$conn->close();
?>
