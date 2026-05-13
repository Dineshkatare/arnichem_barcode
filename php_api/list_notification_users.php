<?php
header('Content-Type: application/json');

// DB credentials
if (isset($_POST['db_host']) && isset($_POST['db_username']) && isset($_POST['db_password']) && isset($_POST['db_name'])) {
    $servername = $_POST['db_host'];
    $db_user    = $_POST['db_username'];
    $db_pass    = $_POST['db_password'];
    $dbname     = $_POST['db_name'];
} elseif (isset($_GET['db_host'])) { // Fallback for browser testing if needed
    $servername = $_GET['db_host'];
    $db_user    = $_GET['db_username'];
    $db_pass    = $_GET['db_password'];
    $dbname     = $_GET['db_name'];
} else {
    // If no credentials provided, try to use connect_app.php as a default fallback
    if (file_exists(__DIR__ . "/connect_app.php")) {
        include(__DIR__ . "/connect_app.php");
    } else {
        echo json_encode(array("status" => "error", "message" => "Missing database credentials and connect_app.php not found"));
        exit;
    }
}

// Establish Database Connection if not already established by connect_app.php
if (!isset($conn) || !$conn) {
    $conn = new mysqli($servername, $db_user, $db_pass, $dbname);
}

if ($conn->connect_error) {
    echo json_encode(array("status" => "error", "message" => "Connection failed: " . $conn->connect_error));
    exit;
}

$role_filter = isset($_GET['role']) ? $_GET['role'] : null;

if ($role_filter) {
    $stmt = $conn->prepare("SELECT username, device_type, role_key, updated_at FROM user_fcm_tokens WHERE fcm_token IS NOT NULL AND fcm_token != '' AND (FIND_IN_SET(?, role_key) > 0 OR role_key LIKE CONCAT('%', ?, '%'))");
    $stmt->bind_param("ss", $role_filter, $role_filter);
    $stmt->execute();
    $result = $stmt->get_result();
} else {
    $result = $conn->query("SELECT username, device_type, role_key, updated_at FROM user_fcm_tokens WHERE fcm_token IS NOT NULL AND fcm_token != ''");
}

$users = [];
if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $users[] = $row;
    }
}

echo json_encode([
    "status" => "success",
    "total_registered_users" => count($users),
    "filter_applied" => $role_filter ? $role_filter : "None",
    "users" => $users
], JSON_PRETTY_PRINT);

$conn->close();
?>
