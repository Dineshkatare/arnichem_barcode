<?php

// Check if the required POST variables are set
if (
    isset($_POST['db_host'], $_POST['db_username'], $_POST['db_password'], $_POST['db_name'],
    $_POST['time'], $_POST['emp_id'], $_POST['emp_name'], $_POST['remarks'],
    $_POST['in_out'], $_POST['latitude'], $_POST['longitude'], $_POST['address'], $_POST['entered_by'])
) {
    // Database connection details
    $db_host = $_POST['db_host'];
    $db_username = $_POST['db_username'];
    $db_password = $_POST['db_password'];
    $db_name = $_POST['db_name'];

    // Establish Database Connection
    if (file_exists(__DIR__ . "/connect_app.php")) {
        include(__DIR__ . "/connect_app.php");
    } else {
        $conn = new mysqli($db_host, $db_username, $db_password, $db_name);
    }
    
    header('Content-Type: application/json');

    // Check the connection
    if (!$conn || $conn->connect_error) {
        $err = (isset($conn->connect_error) && $conn->connect_error) ? $conn->connect_error : 'Unknown connection error';
        die(json_encode(array("status" => "error", "message" => "Database connection failed: " . $err)));
    }

    // Get the input values from the request
    $time = mysqli_real_escape_string($conn, $_POST['time']);
    $emp_id = mysqli_real_escape_string($conn, $_POST['emp_id']);
    $emp_name = mysqli_real_escape_string($conn, $_POST['emp_name']);
    $remarks = mysqli_real_escape_string($conn, $_POST['remarks']);
    $in_out_type = mysqli_real_escape_string($conn, $_POST['in_out']);
    $latitude = mysqli_real_escape_string($conn, $_POST['latitude']);
    $longitude = mysqli_real_escape_string($conn, $_POST['longitude']);
    $address = mysqli_real_escape_string($conn, $_POST['address']);
    $entered_by = mysqli_real_escape_string($conn, $_POST['entered_by']);
    $api_name = "attendance_log6.0.php";

    // Date and time settings
    date_default_timezone_set('Asia/Kolkata');
    $main_today_mysql_ymd = date("Y-m-d");
    $main_today_mysql = date("Y-m-d H:i:s");

    // --- CONFIGURATION SWITCH ---
    $save_to_local_server = false;

    // --- Main Logic for Handling File Upload --- 
    if (isset($_FILES["bitmap"]) && $_FILES["bitmap"]["error"] === UPLOAD_ERR_OK) {
        
        // Initialize variables
        $localFilename = null;
        $fileSourcePathForR2 = $_FILES['bitmap']['tmp_name'];
        $originalFilename = $_FILES['bitmap']['name'];
		$mainFilename = "IMG" . rand() . ".jpg";
        
        if ($save_to_local_server) {
            $uploadDir = "../images/attendance_image/";
			$localFilename = $mainFilename;
            $localFilePath = $uploadDir . $localFilename;

            if (move_uploaded_file($_FILES["bitmap"]["tmp_name"], $localFilePath)) {
                $fileSourcePathForR2 = $localFilePath;
            } else {
                $response = ["status" => "error", "message" => "Critical Error: Local file save was enabled but failed."];
                echo json_encode($response);
                exit();
            }
        }

        $fileContent = file_get_contents($fileSourcePathForR2);
        

		
        if ($fileContent === false) {
            $response = ["status" => "error", "message" => "Critical Error: Could not read source file for upload."];
        } else {
            // --- Step 3: Call the Bluehost R2 Upload API with file content ---
           
			$ch = curl_init();
            $uploadApiUrl = 'https://www.arnisol.com/intranet/r2_interface/upload_handler.php';

            $postData = [
                'file_content'  => base64_encode($fileContent),
                'file_name'     => $mainFilename,
                'document_type' => 'attendance_images'
            ];

            curl_setopt($ch, CURLOPT_URL, $uploadApiUrl);
            curl_setopt($ch, CURLOPT_POST, true);
            curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($postData)); // Use http_build_query for safety
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

            $apiResponse = curl_exec($ch);
            $httpcode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
            $curl_error = curl_error($ch);
            curl_close($ch);

            $result = json_decode($apiResponse, true);

            // --- Step 4: Process the API Response ---
            $r2ObjectKey = null;
            if ($httpcode === 200 && isset($result['status']) && $result['status'] === 'success') {
                $r2ObjectKey = $result['r2_object_key'];
            } else {
                $errorMessage = isset($result['message']) ? $result['message'] : 'Unknown API error.';
                if (!empty($curl_error)) { $errorMessage .= " cURL Error: " . $curl_error; }
                error_log("R2 upload failed for " . $originalFilename . ": " . $errorMessage);
            }
			
			
            
            // --- Step 5: Insert the Record into Your Database ---
            $sql_attendance = "INSERT INTO attendance (srno, emp_id, emp_name, date, time, timestamp, entry_type, remarks, GPS_lat, GPS_long, GPS_Address, file_path_image, r2_file_path, entered_by)
                               VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            $stmt = $conn->prepare($sql_attendance);
            if ($stmt) {
                // 'ssssssssssss' represents 12 string parameters for the 12 question marks
                $stmt->bind_param('sssssssssssss', 
                    $emp_id, $emp_name, $main_today_mysql_ymd, $time, $main_today_mysql, 
                    $in_out_type, $remarks, $latitude, $longitude, $address, 
                    $localFilename, 
                    $r2ObjectKey,
                    $entered_by
                );

                if ($stmt->execute()) {
                    $log_id = $conn->insert_id;
                    
                    // Send push notification to HR role
                    $notif_response = sendBroadcastNotification(
                        "Attendance Logged: $in_out_type",
                        "$emp_name marked $in_out_type at $time.",
                        "attendance_entry",
                        ['emp_id' => $emp_id, 'in_out' => $in_out_type, 'log_id' => (string)$log_id],
                        "hr" // Targets users with 'hr' role
                    );

                    $response = [
                        "status" => "success", 
                        "message" => "Attendance logged successfully", 
                        "srno" => $log_id,
                        "notif_sent" => ($notif_response && isset($notif_response['status']) && $notif_response['status'] === 'success'),
                        "notif_error" => (isset($notif_response['status']) && $notif_response['status'] === 'error') ? $notif_response['message'] : null
                    ];
                    
                    // Log to api_calls_new table
                    $sql_api_calls = "INSERT INTO api_calls_new (id, api_name, call_date, email) VALUES (NULL, ?, ?, ?)";
                    $stmt_api = $conn->prepare($sql_api_calls);
                    if ($stmt_api) {
                        $stmt_api->bind_param('sss', $api_name, $main_today_mysql, $entered_by);
                        $stmt_api->execute();
                        $stmt_api->close();
                    }
                } else {
                    $response = ["status" => "error", "message" => "Database execute error: " . $stmt->error];
                }
                $stmt->close();
            } else {
                $response = ["status" => "error", "message" => "Database prepare error: " . $conn->error];
            }
        }
    } else {
        $response = ["status" => "error", "message" => "File 'bitmap' not found or upload error."];
    }

    $conn->close();
} else {
    header('Content-Type: application/json');
    $response = ["status" => "error", "message" => "Required POST variables are not set"];
}

// ==========================================
// HELPER FUNCTION WITH ENHANCED LOGGING
// ==========================================
function sendBroadcastNotification($title, $body, $event_type = 'broadcast', $extra_data = [], $role_key = null) {
    global $db_host, $db_username, $db_password, $db_name;

    $broadcast_api_url = "http://arnichem.co.in/intranet/barcode/APP/app_apis/send_broadcast.php";

    $postData = [
        'db_host'     => $db_host,
        'db_username' => $db_username,
        'db_password' => $db_password,
        'db_name'     => $db_name,
        'title'       => $title,
        'body'        => $body,
        'event_type'  => $event_type,
        'extra_data'  => json_encode($extra_data),
        'role_key'    => $role_key
    ];

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $broadcast_api_url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($postData));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false); 
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
    curl_setopt($ch, CURLOPT_TIMEOUT, 15);

    $response = curl_exec($ch);
    $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $curl_error = curl_error($ch);
    curl_close($ch);

    if ($response === false) {
        error_log("Notification Error: CURL failed. Error: $curl_error");
        return ["status" => "error", "message" => "CURL Connection Error: " . $curl_error];
    }

    if ($http_code !== 200) {
        error_log("Notification Error: Broadcast API returned HTTP $http_code. Response: $response");
        return ["status" => "error", "message" => "Broadcast API returned HTTP $http_code."];
    }

    $decoded_response = json_decode($response, true);
    if (json_last_error() !== JSON_ERROR_NONE) {
        error_log("Notification Error: Failed to decode JSON response. Raw response: $response");
        return ["status" => "error", "message" => "Invalid JSON response from Broadcast API"];
    }

    return $decoded_response;
}

// Send the final response as JSON
echo json_encode($response);
