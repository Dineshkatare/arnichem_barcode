<?php
$db_host = $_POST['db_host'];
$db_username = $_POST['db_username'];
$db_password = $_POST['db_password'];
$db_name = $_POST['db_name'];

include("connect_app.php");

$customer_code = $_POST['cust_code'];

$response = array();

if (!$customer_code) {
    echo json_encode(array("status" => "error", "msg" => "Customer code missing"));
    exit;
}

try {
    // 1. Get Customer Name
    $customer_name = 'N/A';
    $stmt = $conn->prepare("SELECT name FROM businesspartners WHERE code = ?");
    $stmt->bind_param("s", $customer_code);
    $stmt->execute();
    $result = $stmt->get_result();
    if ($row = $result->fetch_assoc()) {
        $customer_name = $row['name'];
    }

    // 2. Get Next Entry Number
    $next_entry_no = 1;
    $result = mysqli_query($conn, "SELECT COALESCE(MAX(no), 0) + 1 AS max_entry_no FROM inventory_holding_crm_main");
    if ($row = $result->fetch_assoc()) {
        $next_entry_no = $row['max_entry_no'];
    }

    // 3. Get Cylinders
    $cylinders = array();
    // Using a more compatible subquery approach instead of ROW_NUMBER() if MySQL < 8.0
    $sql = "
        SELECT 
            ic.item_code, 
            ic.item_description, 
            ic.filled_with,
            ct.trans_ref_no,
            ct.date,
            DATEDIFF(NOW(), ct.date) as pending_days,
            ct.is_scanned
        FROM 
            inventory_cylinders ic
        LEFT JOIN (
            SELECT t1.*
            FROM cylinder_transactions t1
            INNER JOIN (
                SELECT cyl_code, MAX(date) as max_date
                FROM cylinder_transactions
                WHERE to_warehouse = ? AND trans_ref_type IN ('DEL', 'ISS')
                GROUP BY cyl_code
            ) t2 ON t1.cyl_code = t2.cyl_code AND t1.date = t2.max_date
        ) ct ON ic.item_code = ct.cyl_code
        WHERE 
            ic.location = ?
    ";
    
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ss", $customer_code, $customer_code);
    $stmt->execute();
    $result = $stmt->get_result();
    
    while ($row = $result->fetch_assoc()) {
        $cylinders[] = array(
            "item_code" => $row['item_code'],
            "item_description" => $row['item_description'],
            "filled_with" => $row['filled_with'],
            "trans_ref_no" => isset($row['trans_ref_no']) ? $row['trans_ref_no'] : 'N/A',
            "date" => isset($row['date']) ? $row['date'] : 'N/A',
            "pending_days" => isset($row['pending_days']) ? (int)$row['pending_days'] : -1,
            "is_scanned" => $row['is_scanned']
        );
    }

    echo json_encode(array(
        "status" => "success",
        "customer_name" => $customer_name,
        "next_entry_no" => $next_entry_no,
        "cylinders" => $cylinders
    ));

} catch (Exception $e) {
    echo json_encode(array("status" => "error", "msg" => $e->getMessage()));
}
?>
