<?php
// get_device_list.php

if (isset($_POST['db_host']) && isset($_POST['db_username']) && isset($_POST['db_password']) && isset($_POST['db_name'])) {
    $servername = $_POST['db_host'];
    $username = $_POST['db_username'];
    $password = $_POST['db_password'];
    $dbname = $_POST['db_name'];
} else {
    include __DIR__ . '/db.php';
}

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
  die("Connection failed: " . $conn->connect_error);
}

$sql = "SELECT device_name, device_no FROM device_list WHERE status='Active' ORDER BY device_name";
$result = $conn->query($sql);

$devices = array();

if ($result->num_rows > 0) {
  // output data of each row
  while($row = $result->fetch_assoc()) {
    $devices[] = $row;
  }
} 

echo json_encode($devices);

$conn->close();
?>
