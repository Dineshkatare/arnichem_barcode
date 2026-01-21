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

/* ---------------- SEARCH PARAMS ---------------- */

$s_cust  = isset($_POST['cust_name']) ? trim($_POST['cust_name']) : '';
$s_cont  = isset($_POST['contact_name']) ? trim($_POST['contact_name']) : '';
$s_phone = isset($_POST['phone']) ? trim($_POST['phone']) : '';
$s_email = isset($_POST['email']) ? trim($_POST['email']) : '';

/* ---------------- VALIDATION ---------------- */

if ($s_cust == '' && $s_cont == '' && $s_phone == '' && $s_email == '') {
    echo json_encode(array("status"=>"error","message"=>"No search criteria provided"));
    exit;
}

/* ---------------- QUERY ---------------- */

// We select the flat fields first, then structure them in PHP
$sql = "SELECT 
            bp.code, bp.name, bp.city, bp.phone1, bp.phone2, bp.email AS company_email,
            bpc.contactname, bpc.contactdesignation, bpc.contactmobile, 
            bpc.contactphone, bpc.contactemail
        FROM businesspartners bp
        LEFT JOIN bpcontact bpc ON bp.code = bpc.code
        WHERE 1=1 ";

$params = array();

if ($s_cust != '') {
    $sql .= " AND bp.name LIKE ?";
    $params[] = "%".$s_cust."%";
}

if ($s_cont != '') {
    $sql .= " AND bpc.contactname LIKE ?";
    $params[] = "%".$s_cont."%";
}

if ($s_phone != '') {
    // Check both company phones and contact person phones
    $sql .= " AND (bp.phone1 LIKE ? OR bp.phone2 LIKE ? 
              OR bpc.contactmobile LIKE ? OR bpc.contactphone LIKE ?)";
    $params[] = "%".$s_phone."%";
    $params[] = "%".$s_phone."%";
    $params[] = "%".$s_phone."%";
    $params[] = "%".$s_phone."%";
}

if ($s_email != '') {
    // Check both company email and contact email
    $sql .= " AND (bp.email LIKE ? OR bpc.contactemail LIKE ?)";
    $params[] = "%".$s_email."%";
    $params[] = "%".$s_email."%";
}

$sql .= " LIMIT 50";

/* ---------------- EXECUTE ---------------- */

try {
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $results = $stmt->fetchAll();
    
    $outputData = array();
    
    foreach ($results as $row) {
        // Structure the Company Data
        $item = array(
            "code" => $row['code'],
            "name" => $row['name'],
            "city" => $row['city'],
            "phone1" => $row['phone1'],
            "phone2" => $row['phone2'],
            "company_email" => $row['company_email'],
            "contact_person" => null // Default to null
        );
        
        // If there is contact person data, structure it into a nested object
        // We check if 'contactname' is not empty to verify a contact exists
        if (!empty($row['contactname'])) {
            $item["contact_person"] = array(
                "name" => $row['contactname'],
                "designation" => $row['contactdesignation'],
                "mobile" => $row['contactmobile'],
                "phone" => $row['contactphone'],
                "email" => $row['contactemail']
            );
        }
        
        $outputData[] = $item;
    }
    
    echo json_encode(array(
        "status" => "success",
        "data" => $outputData
    ));
    
} catch (Exception $e) {
    echo json_encode(array(
        "status" => "error",
        "message" => "Query Failed: " . $e->getMessage()
    ));
}
?>
