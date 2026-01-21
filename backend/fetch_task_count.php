<?php
header('Content-Type: application/json');

/* ---------------- DB SETTINGS ---------------- */

$host = isset($_POST['db_host']) ? $_POST['db_host'] : 'localhost';
$db   = isset($_POST['db_name']) ? $_POST['db_name'] : '';
$user = isset($_POST['db_username']) ? $_POST['db_username'] : '';
$pass = isset($_POST['db_password']) ? $_POST['db_password'] : '';
$charset = 'utf8';

if ($db == '' || $user == '') {
    echo json_encode(array("status"=>"error","message"=>"Missing database credentials"));
    exit;
}

$dsn = "mysql:host=$host;dbname=$db;charset=$charset";

$options = array(
    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES => false,
);

/* ---------------- CONNECT ---------------- */

try {
    $pdo = new PDO($dsn, $user, $pass, $options);
} catch (PDOException $e) {
    echo json_encode(array(
        'status' => 'error',
        'message' => 'Database Connection Failed'
    ));
    exit;
}

/* ---------------- PARAMS ---------------- */

$email = isset($_POST['email']) ? trim($_POST['email']) : '';

if ($email == '') {
    echo json_encode(array("status"=>"error", "message"=>"Email is required"));
    exit;
}

/* ---------------- QUERY ---------------- */

// The user provided query:
// SELECT count(*) FROM nripen1.org_main WHERE (user = (SELECT taskapp_user FROM nripen1.user_mapping_intranet WHERE intranet_user = 'vishwajeet') OR srno IN (SELECT task_srno FROM nripen1.org_delegate WHERE delegate_srno IN (SELECT srno FROM nripen1.org_delegate_params WHERE delegate_user_name = (SELECT taskapp_user FROM nripen1.user_mapping_intranet WHERE intranet_user = 'vishwajeet')))) and status NOT IN ('Done', 'Archived');

// We replace 'vishwajeet' with the provided email parameter.
$sql = "SELECT count(*) as total_count 
        FROM nripen1.org_main 
        WHERE (user = (SELECT taskapp_user FROM nripen1.user_mapping_intranet WHERE intranet_user = ?) 
           OR srno IN (SELECT task_srno FROM nripen1.org_delegate WHERE delegate_srno IN (SELECT srno FROM nripen1.org_delegate_params WHERE delegate_user_name = (SELECT taskapp_user FROM nripen1.user_mapping_intranet WHERE intranet_user = ?)))) 
           AND status NOT IN ('Done', 'Archived')";

try {
    $stmt = $pdo->prepare($sql);
    // Bind the email to both placeholders
    $stmt->execute([$email, $email]);
    $result = $stmt->fetch();
    
    $count = $result ? $result['total_count'] : 0;
    
    echo json_encode(array(
        "status" => "success",
        "count" => $count
    ));
    
} catch (Exception $e) {
    echo json_encode(array(
        "status" => "error",
        "message" => "Query Failed: " . $e->getMessage()
    ));
}
?>
