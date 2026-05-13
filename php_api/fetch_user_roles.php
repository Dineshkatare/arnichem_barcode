<?php
// fetch_user_roles.php

header('Content-Type: application/json');

if (isset($_POST['db_host']) && isset($_POST['db_username']) && isset($_POST['db_password']) && isset($_POST['db_name'])) {
    $servername = $_POST['db_host'];
    $username_db = $_POST['db_username'];
    $password_db = $_POST['db_password'];
    $dbname = $_POST['db_name'];
} else {
    echo json_encode(array("status" => "error", "message" => "Missing database credentials"));
    exit;
}

$username_input = $_POST['username'];

if (!isset($username_input) || empty($username_input)) {
    echo json_encode(array("status" => "error", "message" => "Missing username"));
    exit;
}

// Establish Database Connection
if (file_exists(__DIR__ . "/connect_app.php")) {
    include(__DIR__ . "/connect_app.php");
} else {
    $conn = new mysqli($servername, $username_db, $password_db, $dbname);
}

if (!$conn || $conn->connect_error) {
    $err = (isset($conn->connect_error) && $conn->connect_error) ? $conn->connect_error : 'Unknown connection error';
    echo json_encode(["status" => "error", "message" => "Database connection failed: " . $err]);
    exit();
}

$roles = array();
$stmt = $conn->prepare("SELECT role_key FROM user_notification_roles WHERE user = ?");
if ($stmt) {
    $stmt->bind_param("s", $username_input);
    $stmt->execute();
    $res = $stmt->get_result();
    while ($row = $res->fetch_assoc()) {
        $roles[] = $row['role_key'];
    }
    $stmt->close();

    $role_key_str = implode(",", $roles);

    echo json_encode(array("status" => "success", "roles" => $role_key_str));
} else {
    echo json_encode(array("status" => "error", "message" => "Error preparing statement: " . $conn->error));
}

$conn->close();
?>
