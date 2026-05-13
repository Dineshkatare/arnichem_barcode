<?php
// get_task_masters.php
// Fetch Categories, Priorities, and Effort levels for task creation

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

$user_id = isset($_POST['user']) ? $_POST['user'] : '';

$conn = new mysqli($servername, $db_user, $db_pass, $dbname);
if ($conn->connect_error) {
    echo json_encode(array("status" => "error", "message" => "Connection failed: " . $conn->connect_error));
    exit;
}

$response = array("status" => "success", "data" => array());

// 1. Categories
$categories = [];
$stmt = $conn->prepare("SELECT catg FROM org_catg WHERE user = ? ORDER BY catg");
if ($stmt) {
    $stmt->bind_param("s", $user_id);
    $stmt->execute();
    $res = $stmt->get_result();
    while ($row = $res->fetch_assoc()) {
        $categories[] = $row['catg'];
    }
    $stmt->close();
}
$response['data']['categories'] = $categories;

// 2. Priorities
$response['data']['priorities'] = ['Low', 'Medium', 'High', 'Critical'];

// 3. Effort Levels
$response['data']['effort_levels'] = ['Very Easy', 'Easy', 'Medium', 'Hard', 'Very Hard'];

// 4. Status Options
$response['data']['status_options'] = ['Pending', 'In Progress', 'Review', 'Done', 'Archived'];

echo json_encode($response);
$conn->close();
?>
