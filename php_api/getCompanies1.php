<?php
// getCompanies1.php
// Fetch list of companies for the app

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

$conn = new mysqli($servername, $db_user, $db_pass, $dbname);
if ($conn->connect_error) {
    echo json_encode(array("status" => "error", "message" => "Connection failed: " . $conn->connect_error));
    exit;
}

// Ensure companies table exists
$conn->query("CREATE TABLE IF NOT EXISTS companies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE,
    status VARCHAR(50) DEFAULT 'Active'
)");

$result = $conn->query("SELECT id, name, code FROM companies WHERE status = 'Active' ORDER BY name");
$companies = $result->fetch_all(MYSQLI_ASSOC);

echo json_encode(array("status" => "success", "data" => $companies));

$conn->close();
?>
