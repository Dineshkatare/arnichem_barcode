<?php
error_reporting(0);
require __DIR__ . "/init.php";  // Assuming init.php handles db connection $con

$response = array();

if($con) {
    if(isset($_POST['cust_code']) && isset($_POST['description'])) {
        
        $date_added = date('Y-m-d');
        $cust_code = $_POST['cust_code'];
        $transport_type = $_POST['transport_type'];
        $transport_no = $_POST['transport_no'];
        $driver = $_POST['driver'];
        $empb = $_POST['driver']; // stored as driver/user ID often
        $description = $_POST['description'];
        $remarks = isset($_POST['remarks']) ? $_POST['remarks'] : '';
        $address = isset($_POST['addr']) ? $_POST['addr'] : '';
        $GPS_lat = isset($_POST['lati']) ? $_POST['lati'] : '';
        $GPS_long = isset($_POST['logi']) ? $_POST['logi'] : '';
        $app_entry = 'Y';
        $email = isset($_POST['email']) ? $_POST['email'] : '';

        // Prepare the SQL statement
        // Note: 'no' is AI PK, timestamp is auto, date_added we set.
        $sql = "INSERT INTO empty_cust_main (date_added, cust_code, transport_type, transport_no, driver, empb, description, remarks, address, GPS_lat, GPS_long, app_entry, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        $stmt = $con->prepare($sql);
        
        if ($stmt) {
            $stmt->bind_param("sssssssssssss", $date_added, $cust_code, $transport_type, $transport_no, $driver, $empb, $description, $remarks, $address, $GPS_lat, $GPS_long, $app_entry, $email);
            
            if ($stmt->execute()) {
                // Get the generated ID if needed
                $srno = $stmt->insert_id;
                
                $response['status'] = 'success';
                $response['msg'] = 'Entry added successfully';
                $response['srno'] = $srno;
            } else {
                $response['status'] = 'failure';
                $response['msg'] = 'Failed to insert data: ' . $stmt->error;
            }
            $stmt->close();
        } else {
             $response['status'] = 'failure';
             $response['msg'] = 'Failed to prepare statement: ' . $con->error;
        }

    } else {
        $response['status'] = 'failure';
        $response['msg'] = 'Missing required parameters';
    }
} else {
    $response['status'] = 'failure';
    $response['msg'] = 'Database connection failed';
}

echo json_encode($response);
?>
