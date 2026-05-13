<?php
// register_fcm_token.php

header('Content-Type: application/json');

// -- 1. Database credentials --------------------------------------------------
if (isset($_POST['db_host']) && isset($_POST['db_username']) && isset($_POST['db_password']) && isset($_POST['db_name'])) {
    $servername = $_POST['db_host'];
    $db_user    = $_POST['db_username'];
    $db_pass    = $_POST['db_password'];
    $dbname     = $_POST['db_name'];
} else {
    echo json_encode(array("status" => "error", "message" => "Missing database credentials"));
    exit;
}

// -- 2. Input params ----------------------------------------------------------
$app_username = isset($_POST['username'])    ? trim($_POST['username'])  : '';
$token        = isset($_POST['fcm_token'])   ? $_POST['fcm_token']       : '';
$device_type  = isset($_POST['device_type']) ? $_POST['device_type']     : 'android';
$role_key     = isset($_POST['role_key'])    ? $_POST['role_key']        : '';

if (empty($app_username) || empty($token)) {
    echo json_encode(array("status" => "error", "message" => "Missing username or fcm_token"));
    exit;
}

// -- 3. Connect ---------------------------------------------------------------
$conn = new mysqli($servername, $db_user, $db_pass, $dbname);
if ($conn->connect_error) {
    echo json_encode(array("status" => "error", "message" => "Connection failed: " . $conn->connect_error));
    exit;
}

// -- 4. Create table if not exists (fresh install) ----------------------------
$conn->query("CREATE TABLE IF NOT EXISTS user_fcm_tokens (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(255) NOT NULL,
    fcm_token   TEXT NOT NULL,
    device_type ENUM('android','ios') DEFAULT 'android',
    role_key    VARCHAR(255) DEFAULT NULL,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_username (username)
)");

// -- 5. Auto-migrate: user_id (INT) -> username (VARCHAR) ---------------------

// 5a. Drop old 'user_id' column if it still exists
$res_uid = $conn->query("SHOW COLUMNS FROM user_fcm_tokens LIKE 'user_id'");
if ($res_uid && $res_uid->num_rows > 0) {
    $conn->query("ALTER TABLE user_fcm_tokens DROP INDEX user_id");  // drop index if named user_id
    $conn->query("ALTER TABLE user_fcm_tokens DROP COLUMN user_id");
}

// 5b. Add 'username' column if missing
$res_uname = $conn->query("SHOW COLUMNS FROM user_fcm_tokens LIKE 'username'");
if ($res_uname && $res_uname->num_rows == 0) {
    $conn->query("ALTER TABLE user_fcm_tokens ADD COLUMN username VARCHAR(255) NOT NULL DEFAULT '' AFTER id");
    $conn->query("ALTER TABLE user_fcm_tokens ADD UNIQUE KEY unique_username (username)");
}

// 5c. Add 'role_key' column if missing
$res_role = $conn->query("SHOW COLUMNS FROM user_fcm_tokens LIKE 'role_key'");
if ($res_role && $res_role->num_rows == 0) {
    $conn->query("ALTER TABLE user_fcm_tokens ADD COLUMN role_key VARCHAR(255) DEFAULT NULL");
}

// -- 6. Insert or Update token (keyed by username) ----------------------------
$stmt = $conn->prepare(
    "INSERT INTO user_fcm_tokens (username, fcm_token, device_type, role_key)
     VALUES (?, ?, ?, ?)
     ON DUPLICATE KEY UPDATE
         fcm_token   = VALUES(fcm_token),
         device_type = VALUES(device_type),
         role_key    = VALUES(role_key)"
);

if (!$stmt) {
    echo json_encode(array("status" => "error", "message" => "Prepare failed: " . $conn->error));
    $conn->close();
    exit;
}

$stmt->bind_param("ssss", $app_username, $token, $device_type, $role_key);

if ($stmt->execute()) {
    echo json_encode(array("status" => "success", "message" => "Token registered successfully"));
} else {
    echo json_encode(array("status" => "error", "message" => "Execute failed: " . $stmt->error));
}

$stmt->close();
$conn->close();
?>
