<?php
// fetch_username.php
// Fetch user profile details by username/email

header('Content-Type: application/json');

if (isset($_POST['db_host'], $_POST['db_username'], $_POST['db_password'], $_POST['db_name'])) {
    $servername = $_POST['db_host'];
    $db_user    = $_POST['db_username'];
    $db_pass    = $_POST['db_password'];
    $dbname     = $_POST['db_name'];
} else {
    echo json_encode(array("status" => "error", "message" => "Missing database credentials"));
    exit;
}

$username = isset($_POST['username']) ? trim($_POST['username']) : '';

if (empty($username)) {
    echo json_encode(array("status" => "error", "message" => "Missing username"));
    exit;
}

$conn = new mysqli($servername, $db_user, $db_pass, $dbname);
if ($conn->connect_error) {
    echo json_encode(array("status" => "error", "message" => "Connection failed: " . $conn->connect_error));
    exit;
}

$stmt = $conn->prepare("SELECT id, name, email, role FROM users WHERE email = ? OR name = ?");
$stmt->bind_param("ss", $username, $username);
$stmt->execute();
$result = $stmt->get_result();
$user = $result->fetch_assoc();

if ($user) {
    echo json_encode(array("status" => "success", "data" => $user));
} else {
    echo json_encode(array("status" => "error", "message" => "User not found"));
}

$stmt->close();
$conn->close();
?>
