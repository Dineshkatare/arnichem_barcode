<?php
// update_user_device.php

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

$device_name = $_POST['device_name'];
$user_email = $_POST['user_email']; // Or username

if (isset($device_name) && isset($user_email)) {
    
    // Update the device_list table
    $sql = "UPDATE device_list SET user='$user_email' WHERE device_name='$device_name'";

    if ($conn->query($sql) === TRUE) {
        
        // Fetch the device_no for the selected device to return it
        $sql_select = "SELECT device_no FROM device_list WHERE device_name='$device_name'";
        $result = $conn->query($sql_select);
        
        if ($result->num_rows > 0) {
            $row = $result->fetch_assoc();
            $response = array("status" => "success", "device_no" => $row['device_no']);
            echo json_encode($response);
        } else {
             echo json_encode(array("status" => "success", "device_no" => "", "message" => "Device updated but could not fetch number"));
        }

    } else {
      echo json_encode(array("status" => "error", "message" => "Error updating record: " . $conn->error));
    }

} else {
    echo json_encode(array("status" => "error", "message" => "Missing parameters"));
}

$conn->close();
?>
