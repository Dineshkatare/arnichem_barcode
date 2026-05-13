<?php
$db_host = $_POST['db_host'];
$db_username = $_POST['db_username'];
$db_password = $_POST['db_password'];
$db_name = $_POST['db_name'];

include("connect_app.php");

// Receive the whole report as a JSON string in one field
$json_str = $_POST['json_data'];
$data = json_decode($json_str, true);

if (!$data) {
    echo json_encode(array("status" => "error", "msg" => "Invalid JSON data"));
    exit;
}

$cust_code = $data['cust_code'];
$user_email = $data['email'];
$remarks = $data['remarks'];

try {
    // The user provided schema: (no, date_added, cust_code, remarks, email)
    // 'no' is auto_increment.
    $stmt = $conn->prepare("INSERT INTO inventory_holding_crm_main (date_added, cust_code, remarks, email) VALUES (CURDATE(), ?, ?, ?)");
    $stmt->bind_param("sss", $cust_code, $remarks, $user_email);
    $stmt->execute();
    $main_id = $conn->insert_id;

    echo json_encode(array("status" => "success", "msg" => "Report saved successfully", "id" => $main_id));

} catch (Exception $e) {
    echo json_encode(array("status" => "error", "msg" => $e->getMessage()));
}
?>
