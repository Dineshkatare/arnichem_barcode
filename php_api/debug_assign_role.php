<?php
// debug_assign_role.php
// Utility to manually assign roles to users in the notification system.

header('Content-Type: application/json');

if (isset($_GET['username']) && isset($_GET['role']) && isset($_GET['db_host'])) {
    $db_host = $_GET['db_host'];
    $db_username = $_GET['db_username'];
    $db_password = $_GET['db_password'];
    $db_name = $_GET['db_name'];
    $target_user = $_GET['username'];
    $target_role = $_GET['role'];
} else {
    echo json_encode([
        "status" => "error", 
        "message" => "Usage: debug_assign_role.php?username=USER&role=ROLE&db_host=...&db_username=...&db_password=...&db_name=...",
        "tip" => "This script adds a role to a user in the user_notification_roles table so they can receive push notifications."
    ]);
    exit;
}

// Establish Database Connection
if (file_exists(__DIR__ . "/connect_app.php")) {
    include(__DIR__ . "/connect_app.php");
} else {
    $conn = new mysqli($db_host, $db_username, $db_password, $db_name);
}

if (!$conn || $conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Connection failed: " . ($conn ? $conn->connect_error : "Unknown")]));
}

// 1. Ensure table exists
$conn->query("CREATE TABLE IF NOT EXISTS user_notification_roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user VARCHAR(255) NOT NULL,
    role_key VARCHAR(255) NOT NULL,
    UNIQUE KEY unique_user_role (user, role_key)
)");

// 2. Insert role
$stmt = $conn->prepare("INSERT IGNORE INTO user_notification_roles (user, role_key) VALUES (?, ?)");
$stmt->bind_param("ss", $target_user, $target_role);

if ($stmt->execute()) {
    $affected = $stmt->affected_rows;
    echo json_encode([
        "status" => "success", 
        "message" => $affected > 0 ? "Role '$target_role' assigned to user '$target_user'." : "User '$target_user' already has role '$target_role'.",
        "next_step" => "IMPORTANT: The user must now Log Out and Log In again in the Android app to sync this change."
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Execute failed: " . $stmt->error]);
}

$stmt->close();
$conn->close();
?>
