<?php
// fetch_attendance_by_id.php
// Returns a single attendance row by srno (log_id)

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

$log_id = isset($_POST['log_id']) ? intval($_POST['log_id']) : 0;
if ($log_id <= 0) {
    echo json_encode(array("status" => "error", "message" => "Missing or invalid log_id"));
    exit;
}

$stmt = $conn->prepare(
    "SELECT 
        srno,
        emp_id,
        emp_name,
        date,
        time,
        entry_type,
        remarks,
        GPS_lat,
        GPS_long,
        GPS_Address,
        r2_file_path
     FROM attendance 
     WHERE srno = ?
     LIMIT 1"
);

if (!$stmt) {
    echo json_encode(array("status" => "error", "message" => "Prepare failed: " . $conn->error));
    $conn->close();
    exit;
}

$stmt->bind_param("i", $log_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows == 0) {
    echo json_encode(array("status" => "error", "message" => "Attendance record not found"));
    $stmt->close();
    $conn->close();
    exit;
}

$row = $result->fetch_assoc();

// Construct R2 public URL
$r2_public_url = "https://pub-92976b3281514757978d30e556488d07.r2.dev/";
$image_url = $row['r2_file_path'] ? $r2_public_url . $row['r2_file_path'] : "";

echo json_encode(array(
    "status"    => "success",
    "log_id"    => (string)$row['srno'],
    "emp_id"    => $row['emp_id'],
    "emp_name"  => $row['emp_name'],
    "date"      => $row['date'],
    "time"      => $row['time'],
    "in_out"    => $row['entry_type'],
    "remarks"   => $row['remarks'],
    "latitude"  => $row['GPS_lat'],
    "longitude" => $row['GPS_long'],
    "address"   => $row['GPS_Address'],
    "image_url" => $image_url
));

$stmt->close();
$conn->close();
?>
