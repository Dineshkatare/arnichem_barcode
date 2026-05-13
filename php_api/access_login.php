<?php
// access_login.php
// User authentication API

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

$email    = isset($_POST['email']) ? trim($_POST['email']) : '';
$password = isset($_POST['password']) ? $_POST['password'] : '';

if (empty($email) || empty($password)) {
    echo json_encode(array("status" => "error", "message" => "Missing email or password"));
    exit;
}

$conn = new mysqli($servername, $db_user, $db_pass, $dbname);
if ($conn->connect_error) {
    echo json_encode(array("status" => "error", "message" => "Connection failed: " . $conn->connect_error));
    exit;
}

// Ensure users table exists
$conn->query("CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)");

// Check user
$stmt = $conn->prepare("SELECT id, name, email, password, role FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();
$user = $result->fetch_assoc();

if ($user) {
    // For production, use password_verify($password, $user['password'])
    // Here we use simple comparison for legacy compatibility as requested
    if ($password === $user['password']) {
        unset($user['password']); // Don't return password
        echo json_encode(array("status" => "success", "message" => "Login successful", "user" => $user));
    } else {
        echo json_encode(array("status" => "error", "message" => "Invalid password"));
    }
} else {
    echo json_encode(array("status" => "error", "message" => "User not found"));
}

$stmt->close();
$conn->close();
?>
