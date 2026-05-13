<?php
// fetch_order_by_id.php
// Returns a single order row by srno (order_id) with customer name via JOIN

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

$order_id = isset($_POST['order_id']) ? intval($_POST['order_id']) : 0;
if ($order_id <= 0) {
    echo json_encode(array("status" => "error", "message" => "Missing or invalid order_id"));
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

// Join order_main with businesspartners to get customer name
// businesspartners columns: code (= cust_code), name
$stmt = $conn->prepare(
    "SELECT 
        o.srno,
        o.cust_code,
        COALESCE(b.name, o.cust_code) AS cust_name,
        DATE_FORMAT(o.date_added, '%d-%m-%Y') AS date_added,
        o.order_msg,
        o.remarks,
        o.user
     FROM order_main o
     LEFT JOIN businesspartners b ON b.code = o.cust_code
     WHERE o.srno = ?
     LIMIT 1"
);

if (!$stmt) {
    echo json_encode(array("status" => "error", "message" => "Prepare failed: " . $conn->error));
    $conn->close();
    exit;
}

$stmt->bind_param("i", $order_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows == 0) {
    echo json_encode(array("status" => "error", "message" => "Order not found"));
    $stmt->close();
    $conn->close();
    exit;
}

$row = $result->fetch_assoc();
echo json_encode(array(
    "status"     => "success",
    "srno"       => (string)$row['srno'],
    "code"       => $row['cust_code'],
    "name"       => $row['cust_name'],
    "date_added" => $row['date_added'],
    "message"    => $row['order_msg'],
    "remarks"    => $row['remarks'],
    "user"       => $row['user'],
    "link"       => "https://arnisol.com/intranet/view_business_partner.php?code=" . urlencode($row['cust_code'])
));

$stmt->close();
$conn->close();
?>
