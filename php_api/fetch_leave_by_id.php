<?php
// fetch_leave_by_id.php
// Returns a single leave row by srno with employee name via JOIN

header('Content-Type: application/json');

// DB credentials
if (isset($_POST['db_host']) && isset($_POST['db_username']) && isset($_POST['db_password']) && isset($_POST['db_name'])) {
    $servername = $_POST['db_host'];
    $db_user    = $_POST['db_username'];
    $db_pass    = $_POST['db_password'];
    $dbname     = $_POST['db_name'];
} else {
    echo json_encode(array("status" => "error", "message" => "Missing database credentials"));
    exit;
}

$leave_id = isset($_POST['leave_id']) ? intval($_POST['leave_id']) : 0;
if ($leave_id <= 0) {
    echo json_encode(array("status" => "error", "message" => "Missing or invalid leave_id"));
    exit;
}

// Establish Database Connection
if (file_exists(__DIR__ . "/connect_app.php")) {
    include(__DIR__ . "/connect_app.php");
} else {
    $conn = new mysqli($servername, $db_user, $db_pass, $dbname);
}

if (!$conn || $conn->connect_error) {
    $err = (isset($conn->connect_error) && $conn->connect_error) ? $conn->connect_error : 'Unknown connection error';
    echo json_encode(["status" => "error", "message" => "Database connection failed: " . $err]);
    exit();
}

// Join leave_main with users to get employee name
$stmt = $conn->prepare(
    "SELECT 
        l.srno,
        l.emp_id,
        CONCAT(u.firstname, ' ', u.lastname) AS emp_name,
        l.from_date,
        l.to_date,
        l.joining_date,
        l.type,
        l.reason,
        l.status,
        l.remarks,
        l.applied_on
     FROM leave_main l
     LEFT JOIN users u ON u.id = l.emp_id
     WHERE l.srno = ?
     LIMIT 1"
);

if (!$stmt) {
    echo json_encode(array("status" => "error", "message" => "Prepare failed: " . $conn->error));
    $conn->close();
    exit;
}

$stmt->bind_param("i", $leave_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows == 0) {
    echo json_encode(array("status" => "error", "message" => "Leave application not found"));
    $stmt->close();
    $conn->close();
    exit;
}

$row = $result->fetch_assoc();
echo json_encode(array(
    "status"       => "success",
    "srno"         => (string)$row['srno'],
    "emp_id"       => $row['emp_id'],
    "emp_name"     => $row['emp_name'],
    "from_date"    => $row['from_date'],
    "to_date"      => $row['to_date'],
    "joining_date" => $row['joining_date'],
    "leave_type"   => $row['type'],
    "reason"       => $row['reason'],
    "leave_status" => $row['status'],
    "remarks"      => $row['remarks'],
    "applied_on"   => $row['applied_on']
));

$stmt->close();
$conn->close();
?>
