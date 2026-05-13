<?php
// Set the response type to JSON
header('Content-Type: application/json');

// Retrieve dynamic database connection parameters from POST request
$db_host = isset($_POST['db_host']) ? $_POST['db_host'] : '';
$db_username = isset($_POST['db_username']) ? $_POST['db_username'] : '';
$db_password = isset($_POST['db_password']) ? $_POST['db_password'] : '';
$db_name = isset($_POST['db_name']) ? $_POST['db_name'] : '';

// Validate database parameters
if (empty($db_host) || empty($db_username) || empty($db_name)) {
    echo json_encode(["status" => "error", "message" => "Missing database connection parameters"]);
    exit();
}

// Establish Database Connection
if (file_exists(__DIR__ . "/connect_app.php")) {
    include(__DIR__ . "/connect_app.php");
} else {
    $conn = new mysqli($db_host, $db_username, $db_password, $db_name);
}

if (!$conn || $conn->connect_error) {
    $err = (isset($conn->connect_error) && $conn->connect_error) ? $conn->connect_error : 'Unknown connection error';
    echo json_encode(["status" => "error", "message" => "Database connection failed: " . $err]);
    exit();
}

// Check for missing required fields
if (empty($_POST['cust_code']) || empty($_POST['order_msg']) || empty($_POST['user']) || empty($_POST['date_added'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields (cust_code, order_msg, user, or date_added)"]);
    exit();
}

// Retrieve and sanitize the inputs
$cust_code = $_POST['cust_code'];
$order_msg = $_POST['order_msg'];
$user = $_POST['user'];
$remarks = isset($_POST['remarks']) ? $_POST['remarks'] : '';

try {
    $date = new DateTime($_POST['date_added']);
    $date_added = $date->format('Y-m-d'); 
} catch (Exception $e) {
    echo json_encode(["status" => "error", "message" => "Invalid date format for date_added"]);
    exit();
}

$applied_on = date('Y-m-d H:i:s');

$query = $conn->prepare("INSERT INTO order_main (cust_code, date_added, order_msg, timestamp, user, remarks) VALUES (?, ?, ?, ?, ?, ?)");

if ($query) {
    $query->bind_param("ssssss", $cust_code, $date_added, $order_msg, $applied_on, $user, $remarks);

    if ($query->execute()) {
        $srno = $conn->insert_id;

        // Send notification ONLY to users with role_key = 'account'
        $notif_response = sendBroadcastNotification(
            "New Order Created",
            "Order #$srno placed by $user. Tap to view details.",
            "new_order",
            ['order_id' => (string)$srno],
            "account"   // <-- only 'account' role users receive this
        );

        if ($notif_response && isset($notif_response['status']) && $notif_response['status'] === 'success') {
            echo json_encode([
                "status" => "success", 
                "message" => "Order submitted successfully", 
                "srno" => $srno,
                "notif_details" => $notif_response
            ]);
        } else {
            $error_msg = isset($notif_response['message']) ? $notif_response['message'] : "Unknown notification error";
            echo json_encode([
                "status" => "error", 
                "message" => "Order submitted but notification failed. Check error log for details.", 
                "srno" => $srno,
                "notif_error" => $error_msg
            ]);
        }

    } else {
        echo json_encode(["status" => "error", "message" => "Failed to submit order", "error" => $query->error]);
    }
    $query->close();
} else {
    echo json_encode(["status" => "error", "message" => "Failed to prepare SQL statement", "error" => $conn->error]);
}

$conn->close(); 

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
        'role_key'    => $role_key   // null = broadcast to all
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

    // --- LOGGING START ---
    if ($response === false) {
        error_log("Notification Error: CURL failed. Error: $curl_error, URL: $broadcast_api_url");
        return ["status" => "error", "message" => "CURL Connection Error ($http_code): " . $curl_error];
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

    if (isset($decoded_response['status']) && $decoded_response['status'] === 'error') {
        $msg = isset($decoded_response['message']) ? $decoded_response['message'] : 'No message';
        error_log("Notification Error: API returned error. Message: " . $msg);
    }
    // --- LOGGING END ---

    return $decoded_response;
}
?>
