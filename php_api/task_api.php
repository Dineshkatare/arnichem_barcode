<?php
// task_api.php
// Main API for TaskApp management

header('Content-Type: application/json');

// -- 1. Database credentials --------------------------------------------------
if (isset($_POST['db_host']) && isset($_POST['db_username']) && isset($_POST['db_password']) && isset($_POST['db_name'])) {
    $servername = $_POST['db_host'];
    $db_user    = $_POST['db_username'];
    $db_pass    = $_POST['db_password'];
    $dbname     = $_POST['db_name'];
} else {
    echo json_encode(array("status" => "error", "message" => "Missing database credentials"));
    exit;
}

// -- 2. Input params ----------------------------------------------------------
$action = isset($_POST['action']) ? $_POST['action'] : '';
$user   = isset($_POST['user'])   ? trim($_POST['user']) : '';

if (empty($action)) {
    echo json_encode(array("status" => "error", "message" => "Missing action"));
    exit;
}

// -- 3. Connect ---------------------------------------------------------------
$conn = new mysqli($servername, $db_user, $db_pass, $dbname);
if ($conn->connect_error) {
    echo json_encode(array("status" => "error", "message" => "Connection failed: " . $conn->connect_error));
    exit;
}

// -- 4. Table Initialization --------------------------------------------------
function init_tables($conn) {
    $queries = [
        "CREATE TABLE IF NOT EXISTS org_main (
            srno INT AUTO_INCREMENT PRIMARY KEY,
            user VARCHAR(255) NOT NULL,
            date_added DATETIME DEFAULT CURRENT_TIMESTAMP,
            description TEXT,
            effort VARCHAR(50),
            priority VARCHAR(50),
            status VARCHAR(50) DEFAULT 'Pending',
            category VARCHAR(100),
            due DATE,
            remarks TEXT,
            date_completed DATETIME NULL
        )",
        "CREATE TABLE IF NOT EXISTS org_taskdet (
            srno INT AUTO_INCREMENT PRIMARY KEY,
            task_srno INT NOT NULL,
            date DATETIME DEFAULT CURRENT_TIMESTAMP,
            detail TEXT,
            user VARCHAR(255)
        )",
        "CREATE TABLE IF NOT EXISTS org_delegate (
            srno INT AUTO_INCREMENT PRIMARY KEY,
            task_srno INT NOT NULL,
            delegate_srno INT NOT NULL,
            delegate_name VARCHAR(255)
        )",
        "CREATE TABLE IF NOT EXISTS org_delegate_params (
            srno INT AUTO_INCREMENT PRIMARY KEY,
            user VARCHAR(255) NOT NULL,
            delegate VARCHAR(255),
            delegate_user_name VARCHAR(255),
            status VARCHAR(50) DEFAULT 'Active'
        )",
        "CREATE TABLE IF NOT EXISTS org_parent_child (
            id INT AUTO_INCREMENT PRIMARY KEY,
            parent_task_no INT NOT NULL,
            child_task_no INT NOT NULL,
            user VARCHAR(255)
        )",
        "CREATE TABLE IF NOT EXISTS org_task_checklist (
            id INT AUTO_INCREMENT PRIMARY KEY,
            task_srno INT NOT NULL,
            item_text VARCHAR(255),
            detail TEXT,
            is_completed TINYINT(1) DEFAULT 0,
            user VARCHAR(255)
        )",
        "CREATE TABLE IF NOT EXISTS org_catg (
            srno INT AUTO_INCREMENT PRIMARY KEY,
            user VARCHAR(255) NOT NULL,
            catg VARCHAR(100)
        )",
        "CREATE TABLE IF NOT EXISTS events (
            id INT AUTO_INCREMENT PRIMARY KEY,
            task_srno INT NOT NULL,
            title VARCHAR(255),
            description TEXT,
            date DATE,
            time_from TIME,
            time_to TIME,
            google_calendar_event_id VARCHAR(255)
        )"
    ];
    foreach ($queries as $q) {
        if (!$conn->query($q)) {
            return false;
        }
    }
    return true;
}

if (!init_tables($conn)) {
    echo json_encode(array("status" => "error", "message" => "Table initialization failed: " . $conn->error));
    exit;
}

// -- 5. Helper Functions ------------------------------------------------------
function send_task_notification($conn, $db_creds, $user_to, $title, $body, $task_srno) {
    $broadcast_url = "http://" . $_SERVER['HTTP_HOST'] . str_replace('task_api.php', 'send_broadcast.php', $_SERVER['PHP_SELF']);
    
    $postData = array_merge($db_creds, [
        'title'      => $title,
        'body'       => $body,
        'role_key'   => $user_to, // Assuming username is treated as a role or we modify send_broadcast to support specific user
        'event_type' => 'task_update',
        'extra_data' => json_encode(['task_srno' => $task_srno])
    ]);

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $broadcast_url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($postData));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_exec($ch);
    curl_close($ch);
}

// -- 6. Action Handlers -------------------------------------------------------

$db_creds = [
    'db_host'     => $servername,
    'db_username' => $db_user,
    'db_password' => $db_pass,
    'db_name'     => $dbname
];

switch ($action) {
    case 'listTasks':
        listTasks($conn, $user);
        break;
    case 'getTask':
        getTask($conn);
        break;
    case 'addTask':
        addTask($conn, $user);
        break;
    case 'updateTaskHeader':
        updateTaskHeader($conn);
        break;
    case 'updateStatus':
        updateStatus($conn);
        break;
    case 'markDone':
        markDone($conn);
        break;
    case 'deleteTask':
        deleteTask($conn);
        break;
    case 'taskSearch':
        taskSearch($conn, $user);
        break;
    case 'getDetails':
        getDetails($conn);
        break;
    case 'addDetail':
        addDetail($conn, $user);
        break;
    case 'editDetail':
        editDetail($conn);
        break;
    case 'deleteDetail':
        deleteDetail($conn);
        break;
    case 'getChecklist':
        getChecklist($conn);
        break;
    case 'addChecklistItem':
        addChecklistItem($conn, $user);
        break;
    case 'updateChecklistItem':
        updateChecklistItem($conn);
        break;
    case 'deleteChecklistItem':
        deleteChecklistItem($conn);
        break;
    case 'createTaskRelationship':
        createTaskRelationship($conn, $user);
        break;
    case 'copyTaskText':
        copyTaskText($conn);
        break;
    case 'getAllDelegates':
        getAllDelegates($conn, $user);
        break;
    case 'getDelegateById':
        getDelegateById($conn);
        break;
    case 'getDelegateParams':
        getDelegateParams($conn, $user);
        break;
    case 'getCategories':
        getCategories($conn, $user);
        break;
    case 'addCategory':
        addCategory($conn, $user);
        break;
    case 'deleteCategory':
        deleteCategory($conn, $user);
        break;
    case 'getDashboardSummary':
        getDashboardSummary($conn, $user);
        break;
    case 'addDelegateProfile':
        addDelegateProfile($conn, $user);
        break;
    case 'deleteDelegateProfile':
        deleteDelegateProfile($conn);
        break;
    case 'getSyncHistory':
        getSyncHistory($conn);
        break;
    default:
        echo json_encode(array("status" => "error", "message" => "Unknown action: $action"));
        break;
}

// -- 6. Implementation Functions ----------------------------------------------

function listTasks($conn, $user) {
    $status_filter    = isset($_POST['status']) ? $_POST['status'] : '';
    $category_filter  = isset($_POST['category']) ? $_POST['category'] : '';
    $priority_filter  = isset($_POST['priority']) ? $_POST['priority'] : '';
    $effort_filter    = isset($_POST['effort']) ? $_POST['effort'] : '';
    $due_filter       = isset($_POST['due_filter']) ? $_POST['due_filter'] : '';
    $delegate_srno    = isset($_POST['delegate_srno']) ? $_POST['delegate_srno'] : '';
    $event_filter     = isset($_POST['event_filter']) ? $_POST['event_filter'] : '';
    $delegate_user_srno = isset($_POST['delegate_user_srno']) ? $_POST['delegate_user_srno'] : $user; // for union

    $dynamic_conditions = "1=1";
    $params = [];
    $types = "";

    // Build dynamic WHERE conditions
    if ($status_filter === 'Archived') {
        $dynamic_conditions .= " AND a.status = 'Archived'";
    } elseif (!empty($status_filter)) {
        $dynamic_conditions .= " AND a.status = ?";
        $params[] = $status_filter;
        $types .= "s";
    } else {
        $dynamic_conditions .= " AND a.status NOT IN ('Done', 'Archived')";
    }

    if (!empty($category_filter)) {
        $dynamic_conditions .= " AND a.category = ?";
        $params[] = $category_filter;
        $types .= "s";
    }
    if (!empty($priority_filter)) {
        $dynamic_conditions .= " AND a.priority = ?";
        $params[] = $priority_filter;
        $types .= "s";
    }
    if (!empty($effort_filter)) {
        $dynamic_conditions .= " AND a.effort = ?";
        $params[] = $effort_filter;
        $types .= "s";
    }

    $today = date('Y-m-d');
    if ($due_filter === 'overdue') {
        $dynamic_conditions .= " AND a.due < ?";
        $params[] = $today;
        $types .= "s";
    } elseif ($due_filter === 'today') {
        $dynamic_conditions .= " AND a.due = ?";
        $params[] = $today;
        $types .= "s";
    } elseif ($due_filter === '7days') {
        $seven_days = date('Y-m-d', strtotime('+7 days'));
        $dynamic_conditions .= " AND a.due BETWEEN ? AND ?";
        $params[] = $today;
        $params[] = $seven_days;
        $types .= "ss";
    } elseif ($due_filter === 'future') {
        $seven_days = date('Y-m-d', strtotime('+7 days'));
        $dynamic_conditions .= " AND a.due > ?";
        $params[] = $seven_days;
        $types .= "s";
    }

    if ($delegate_srno === 'none') {
        $dynamic_conditions .= " AND a.srno NOT IN (SELECT task_srno FROM org_delegate)";
    } elseif (!empty($delegate_srno)) {
        $dynamic_conditions .= " AND a.srno IN (SELECT task_srno FROM org_delegate WHERE delegate_srno = ?)";
        $params[] = $delegate_srno;
        $types .= "i";
    }

    $yesterday = date('Y-m-d', strtotime('-1 day'));
    $last7 = date('Y-m-d', strtotime('-7 days'));
    $next7 = date('Y-m-d', strtotime('+7 days'));

    if ($event_filter === 'today') {
        $dynamic_conditions .= " AND a.srno IN (SELECT task_srno FROM events WHERE date = '$today')";
    } elseif ($event_filter === 'yesterday') {
        $dynamic_conditions .= " AND a.srno IN (SELECT task_srno FROM events WHERE date = '$yesterday')";
    } elseif ($event_filter === 'last7') {
        $dynamic_conditions .= " AND a.srno IN (SELECT task_srno FROM events WHERE date BETWEEN '$last7' AND '$today')";
    } elseif ($event_filter === 'next7') {
        $dynamic_conditions .= " AND a.srno IN (SELECT task_srno FROM events WHERE date BETWEEN '$today' AND '$next7')";
    } elseif ($event_filter === 'past') {
        $dynamic_conditions .= " AND a.srno IN (SELECT task_srno FROM events WHERE date < '$today')";
    } elseif ($event_filter === 'future') {
        $dynamic_conditions .= " AND a.srno IN (SELECT task_srno FROM events WHERE date > '$today')";
    }

    $sql = "SELECT * FROM (
        SELECT a.*,
               (SELECT GROUP_CONCAT(delegate_name SEPARATOR ', ') FROM org_delegate WHERE task_srno=a.srno) AS delegate,
               (SELECT COUNT(*) FROM org_parent_child WHERE parent_task_no = a.srno) as has_children,
               e.date as next_event_date,
               e.time_from as next_event_time
        FROM org_main a
        LEFT JOIN (
            SELECT task_srno, date, time_from
            FROM events
            WHERE (date > CURDATE()) OR (date = CURDATE() AND time_from >= CURTIME())
            ORDER BY date ASC, time_from ASC
        ) e ON a.srno = e.task_srno
        WHERE a.user = ? AND $dynamic_conditions
        GROUP BY a.srno

        UNION

        SELECT a.*,
               (SELECT GROUP_CONCAT(delegate_name SEPARATOR ', ') FROM org_delegate WHERE task_srno=a.srno) AS delegate,
               (SELECT COUNT(*) FROM org_parent_child WHERE parent_task_no = a.srno) as has_children,
               e.date as next_event_date,
               e.time_from as next_event_time
        FROM org_main a
        LEFT JOIN (
            SELECT task_srno, date, time_from
            FROM events
            WHERE (date > CURDATE()) OR (date = CURDATE() AND time_from >= CURTIME())
            ORDER BY date ASC, time_from ASC
        ) e ON a.srno = e.task_srno
        WHERE a.srno IN (SELECT DISTINCT task_srno FROM org_delegate WHERE delegate_srno = (SELECT srno FROM org_delegate_params WHERE delegate_user_name = ? LIMIT 1)) AND $dynamic_conditions
        GROUP BY a.srno
    ) AS combined_results
    ORDER BY date_added DESC, srno DESC";

    // Double the params for UNION
    $full_params = array_merge([$user], $params, [$user], $params);
    $full_types = "s" . $types . "s" . $types;

    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        echo json_encode(array("status" => "error", "message" => "Prepare failed: " . $conn->error));
        return;
    }

    $stmt->bind_param($full_types, ...$full_params);
    $stmt->execute();
    $result = $stmt->get_result();
    $tasks = $result->fetch_all(MYSQLI_ASSOC);
    echo json_encode(array("status" => "success", "data" => $tasks));
}

function getTask($conn) {
    $srno = isset($_POST['srno']) ? $_POST['srno'] : '';
    if (empty($srno)) {
        echo json_encode(array("status" => "error", "message" => "Missing srno"));
        return;
    }

    $res = $conn->prepare("SELECT a.*, (SELECT COUNT(*) FROM org_taskdet WHERE task_srno=a.srno) AS count FROM org_main a WHERE a.srno = ?");
    $res->bind_param("i", $srno);
    $res->execute();
    $task = $res->get_result()->fetch_assoc();

    if (!$task) {
        echo json_encode(array("status" => "error", "message" => "Task not found"));
        return;
    }

    // Delegates
    $res_del = $conn->prepare("SELECT delegate_srno, delegate_name FROM org_delegate WHERE task_srno = ?");
    $res_del->bind_param("i", $srno);
    $res_del->execute();
    $task['delegates'] = $res_del->get_result()->fetch_all(MYSQLI_ASSOC);

    // Parent
    $res_par = $conn->prepare("SELECT parent_task_no FROM org_parent_child WHERE child_task_no = ?");
    $res_par->bind_param("i", $srno);
    $res_par->execute();
    $task['parent_task_no'] = $res_par->get_result()->fetch_all(MYSQLI_ASSOC);

    // Children
    $res_chi = $conn->prepare("SELECT child_task_no FROM org_parent_child WHERE parent_task_no = ?");
    $res_chi->bind_param("i", $srno);
    $res_chi->execute();
    $task['child_task_no'] = $res_chi->get_result()->fetch_all(MYSQLI_ASSOC);

    echo json_encode(array("status" => "success", "data" => $task));
}

function addTask($conn, $user) {
    $description = isset($_POST['description']) ? $_POST['description'] : '';
    $effort      = isset($_POST['effort']) ? $_POST['effort'] : '';
    $priority    = isset($_POST['priority']) ? $_POST['priority'] : '';
    $category    = isset($_POST['category']) ? $_POST['category'] : '';
    $due         = isset($_POST['due']) ? $_POST['due'] : null;
    $delegates   = isset($_POST['delegates']) ? json_decode($_POST['delegates'], true) : [];
    $first_update = isset($_POST['first_update']) ? $_POST['first_update'] : '';

    $conn->begin_transaction();
    try {
        $stmt = $conn->prepare("INSERT INTO org_main (user, date_added, description, effort, priority, status, category, due) VALUES (?, NOW(), ?, ?, ?, 'Pending', ?, ?)");
        $stmt->bind_param("sssssss", $user, $description, $effort, $priority, $category, $due);
        $stmt->execute();
        $task_srno = $stmt->insert_id;

        if (!empty($delegates)) {
            $ids = implode(',', array_map('intval', $delegates));
            $res = $conn->query("SELECT srno, delegate FROM org_delegate_params WHERE srno IN ($ids) AND status = 'Active'");
            while ($row = $res->fetch_assoc()) {
                $stmt_del = $conn->prepare("INSERT INTO org_delegate (task_srno, delegate_srno, delegate_name) VALUES (?, ?, ?)");
                $stmt_del->bind_param("iis", $task_srno, $row['srno'], $row['delegate']);
                $stmt_del->execute();
            }
        }

        if (!empty($first_update)) {
            $stmt_upd = $conn->prepare("INSERT INTO org_taskdet (task_srno, date, detail, user) VALUES (?, NOW(), ?, ?)");
            $stmt_upd->bind_param("iss", $task_srno, $first_update, $user);
            $stmt_upd->execute();
        }

        $conn->commit();
        echo json_encode(array("status" => "success", "task_srno" => $task_srno));
    } catch (Exception $e) {
        $conn->rollback();
        echo json_encode(array("status" => "error", "message" => $e->getMessage()));
    }
}

function updateTaskHeader($conn) {
    $srno = isset($_POST['srno']) ? $_POST['srno'] : '';
    if (empty($srno)) { echo json_encode(array("status" => "error", "message" => "Missing srno")); return; }

    $allowed_fields = ['description', 'effort', 'priority', 'category', 'remarks', 'status', 'due'];
    $updates = [];
    $params = [];
    $types = "";

    foreach ($allowed_fields as $f) {
        if (isset($_POST[$f])) {
            $updates[] = "$f = ?";
            $params[] = $_POST[$f];
            $types .= "s";
        }
    }

    $conn->begin_transaction();
    try {
        if (!empty($updates)) {
            $sql = "UPDATE org_main SET " . implode(', ', $updates) . " WHERE srno = ?";
            $params[] = $srno;
            $types .= "i";
            $stmt = $conn->prepare($sql);
            $stmt->bind_param($types, ...$params);
            $stmt->execute();
        }

        if (isset($_POST['delegates'])) {
            $conn->query("DELETE FROM org_delegate WHERE task_srno = $srno");
            $delegates = json_decode($_POST['delegates'], true);
            if (!empty($delegates)) {
                $ids = implode(',', array_map('intval', $delegates));
                $res = $conn->query("SELECT srno, delegate FROM org_delegate_params WHERE srno IN ($ids) AND status = 'Active'");
                while ($row = $res->fetch_assoc()) {
                    $stmt_del = $conn->prepare("INSERT INTO org_delegate (task_srno, delegate_srno, delegate_name) VALUES (?, ?, ?)");
                    $stmt_del->bind_param("iis", $srno, $row['srno'], $row['delegate']);
                    $stmt_del->execute();
                }
            }
        }

        $conn->commit();
        echo json_encode(array("status" => "success"));
    } catch (Exception $e) {
        $conn->rollback();
        echo json_encode(array("status" => "error", "message" => $e->getMessage()));
    }
}

function updateStatus($conn) {
    $srno = isset($_POST['srno']) ? $_POST['srno'] : '';
    $status = isset($_POST['status']) ? $_POST['status'] : '';
    if (empty($srno) || empty($status)) { echo json_encode(array("status" => "error", "message" => "Missing srno or status")); return; }

    $date_completed = ($status === 'Done') ? date('Y-m-d H:i:s') : null;
    $stmt = $conn->prepare("UPDATE org_main SET status = ?, date_completed = ? WHERE srno = ?");
    $stmt->bind_param("ssi", $status, $date_completed, $srno);
    
    if ($stmt->execute()) {
        echo json_encode(array("status" => "success"));
    } else {
        echo json_encode(array("status" => "error", "message" => $stmt->error));
    }
}

function markDone($conn) {
    $srno = isset($_POST['srno']) ? $_POST['srno'] : '';
    if (empty($srno)) { echo json_encode(array("status" => "error", "message" => "Missing srno")); return; }

    $date_completed = date('Y-m-d H:i:s');
    $stmt = $conn->prepare("UPDATE org_main SET status = 'Done', date_completed = ? WHERE srno = ?");
    $stmt->bind_param("si", $date_completed, $srno);
    if ($stmt->execute()) { echo json_encode(array("status" => "success")); }
    else { echo json_encode(array("status" => "error", "message" => $stmt->error)); }
}

function deleteTask($conn) {
    $srno = isset($_POST['srno']) ? $_POST['srno'] : '';
    if (empty($srno)) { echo json_encode(array("status" => "error", "message" => "Missing srno")); return; }

    $conn->begin_transaction();
    try {
        $conn->query("DELETE FROM org_delegate WHERE task_srno = $srno");
        $conn->query("DELETE FROM org_taskdet WHERE task_srno = $srno");
        $conn->query("DELETE FROM org_task_checklist WHERE task_srno = $srno");
        $conn->query("DELETE FROM org_main WHERE srno = $srno");
        $conn->commit();
        echo json_encode(array("status" => "success"));
    } catch (Exception $e) {
        $conn->rollback();
        echo json_encode(array("status" => "error", "message" => $e->getMessage()));
    }
}

function taskSearch($conn, $user) {
    $query = isset($_POST['query']) ? $_POST['query'] : '';
    $status_filter = isset($_POST['status_filter']) ? $_POST['status_filter'] : '';
    
    $search_param = "%$query%";
    $sql = "SELECT m.srno, m.description, m.status, m.date_added, m.due, td.detail AS match_context
            FROM org_main m
            LEFT JOIN org_taskdet td ON m.srno = td.task_srno AND td.detail LIKE ?
            WHERE (m.user = ? OR m.srno IN (SELECT task_srno FROM org_delegate WHERE delegate_srno = (SELECT srno FROM org_delegate_params WHERE delegate_user_name = ? LIMIT 1)))
            AND (m.description LIKE ? OR td.detail LIKE ?)";
    
    if ($status_filter === 'pending') {
        $sql .= " AND m.status = 'Pending'";
    }
    
    $sql .= " GROUP BY m.srno ORDER BY m.date_added DESC";
    
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("sssss", $search_param, $user, $user, $search_param, $search_param);
    $stmt->execute();
    $result = $stmt->get_result();
    echo json_encode(array("status" => "success", "data" => $result->fetch_all(MYSQLI_ASSOC)));
}

function getDetails($conn) {
    $task_srno = isset($_POST['task_srno']) ? $_POST['task_srno'] : '';
    $stmt = $conn->prepare("SELECT * FROM org_taskdet WHERE task_srno = ? ORDER BY date DESC");
    $stmt->bind_param("i", $task_srno);
    $stmt->execute();
    echo json_encode(array("status" => "success", "data" => $stmt->get_result()->fetch_all(MYSQLI_ASSOC)));
}

function addDetail($conn, $user) {
    $task_srno = isset($_POST['task_srno']) ? $_POST['task_srno'] : '';
    $detail    = isset($_POST['detail']) ? $_POST['detail'] : '';
    $stmt = $conn->prepare("INSERT INTO org_taskdet (task_srno, date, detail, user) VALUES (?, NOW(), ?, ?)");
    $stmt->bind_param("iss", $task_srno, $detail, $user);
    if ($stmt->execute()) { echo json_encode(array("status" => "success")); }
    else { echo json_encode(array("status" => "error", "message" => $stmt->error)); }
}

function editDetail($conn) {
    $srno   = isset($_POST['srno']) ? $_POST['srno'] : '';
    $detail = isset($_POST['detail']) ? $_POST['detail'] : '';
    $stmt = $conn->prepare("UPDATE org_taskdet SET detail = ? WHERE srno = ?");
    $stmt->bind_param("si", $detail, $srno);
    if ($stmt->execute()) { echo json_encode(array("status" => "success")); }
    else { echo json_encode(array("status" => "error", "message" => $stmt->error)); }
}

function deleteDetail($conn) {
    $srno = isset($_POST['srno']) ? $_POST['srno'] : '';
    $stmt = $conn->prepare("DELETE FROM org_taskdet WHERE srno = ?");
    $stmt->bind_param("i", $srno);
    if ($stmt->execute()) { echo json_encode(array("status" => "success")); }
    else { echo json_encode(array("status" => "error", "message" => $stmt->error)); }
}

function getChecklist($conn) {
    $task_srno = isset($_POST['task_srno']) ? $_POST['task_srno'] : '';
    $stmt = $conn->prepare("SELECT * FROM org_task_checklist WHERE task_srno = ? ORDER BY id ASC");
    $stmt->bind_param("i", $task_srno);
    $stmt->execute();
    echo json_encode(array("status" => "success", "data" => $stmt->get_result()->fetch_all(MYSQLI_ASSOC)));
}

function addChecklistItem($conn, $user) {
    $task_srno = isset($_POST['task_srno']) ? $_POST['task_srno'] : '';
    $item_text = isset($_POST['item_text']) ? $_POST['item_text'] : '';
    $detail    = isset($_POST['detail']) ? $_POST['detail'] : '';
    $stmt = $conn->prepare("INSERT INTO org_task_checklist (task_srno, item_text, detail, user) VALUES (?, ?, ?, ?)");
    $stmt->bind_param("isss", $task_srno, $item_text, $detail, $user);
    if ($stmt->execute()) { echo json_encode(array("status" => "success")); }
    else { echo json_encode(array("status" => "error", "message" => $stmt->error)); }
}

function updateChecklistItem($conn) {
    $id = isset($_POST['id']) ? $_POST['id'] : '';
    $allowed = ['is_completed', 'item_text', 'detail'];
    $updates = []; $params = []; $types = "";
    foreach ($allowed as $f) {
        if (isset($_POST[$f])) { $updates[] = "$f = ?"; $params[] = $_POST[$f]; $types .= "s"; }
    }
    if (empty($updates)) { echo json_encode(array("status" => "error", "message" => "Nothing to update")); return; }
    $params[] = $id; $types .= "i";
    $stmt = $conn->prepare("UPDATE org_task_checklist SET " . implode(', ', $updates) . " WHERE id = ?");
    $stmt->bind_param($types, ...$params);
    if ($stmt->execute()) { echo json_encode(array("status" => "success")); }
    else { echo json_encode(array("status" => "error", "message" => $stmt->error)); }
}

function deleteChecklistItem($conn) {
    $id = isset($_POST['id']) ? $_POST['id'] : '';
    $stmt = $conn->prepare("DELETE FROM org_task_checklist WHERE id = ?");
    $stmt->bind_param("i", $id);
    if ($stmt->execute()) { echo json_encode(array("status" => "success")); }
    else { echo json_encode(array("status" => "error", "message" => $stmt->error)); }
}

function createTaskRelationship($conn, $user) {
    $parent = isset($_POST['parent_task_no']) ? $_POST['parent_task_no'] : '';
    $child  = isset($_POST['child_task_no']) ? $_POST['child_task_no'] : '';
    $type   = isset($_POST['relationship_type']) ? $_POST['relationship_type'] : '';

    $conn->begin_transaction();
    try {
        $stmt = $conn->prepare("INSERT INTO org_parent_child (parent_task_no, child_task_no, user) VALUES (?, ?, ?)");
        $stmt->bind_param("iis", $parent, $child, $user);
        $stmt->execute();

        if ($type === 'child') {
            $stmt_arch = $conn->prepare("UPDATE org_main SET status = 'Archived' WHERE srno = ?");
            $stmt_arch->bind_param("i", $child);
            $stmt_arch->execute();
        }
        $conn->commit();
        echo json_encode(array("status" => "success"));
    } catch (Exception $e) {
        $conn->rollback();
        echo json_encode(array("status" => "error", "message" => $e->getMessage()));
    }
}

function copyTaskText($conn) {
    $srno = isset($_POST['srno']) ? $_POST['srno'] : '';
    $stmt = $conn->prepare("SELECT m.*, GROUP_CONCAT(d.delegate_name SEPARATOR ', ') as delegates FROM org_main m LEFT JOIN org_delegate d ON m.srno = d.task_srno WHERE m.srno = ? GROUP BY m.srno");
    $stmt->bind_param("i", $srno);
    $stmt->execute();
    $task = $stmt->get_result()->fetch_assoc();

    $stmt_det = $conn->prepare("SELECT detail FROM org_taskdet WHERE task_srno = ? ORDER BY date ASC");
    $stmt_det->bind_param("i", $srno);
    $stmt_det->execute();
    $task['history'] = $stmt_det->get_result()->fetch_all(MYSQLI_ASSOC);

    echo json_encode(array("status" => "success", "data" => $task));
}

function getAllDelegates($conn, $user) {
    $stmt = $conn->prepare("SELECT srno, delegate FROM org_delegate_params WHERE user = ? AND status = 'Active' ORDER BY delegate");
    $stmt->bind_param("s", $user);
    $stmt->execute();
    echo json_encode(array("status" => "success", "data" => $stmt->get_result()->fetch_all(MYSQLI_ASSOC)));
}

function getDelegateById($conn) {
    $srno = isset($_POST['srno']) ? $_POST['srno'] : '';
    $stmt = $conn->prepare("SELECT srno, delegate, delegate_user_name FROM org_delegate_params WHERE srno = ? AND status = 'Active'");
    $stmt->bind_param("i", $srno);
    $stmt->execute();
    echo json_encode(array("status" => "success", "data" => $stmt->get_result()->fetch_assoc()));
}

function getDelegateParams($conn, $user) {
    $q = isset($_POST['query']) ? $_POST['query'] : '';
    $search = "%$q%";
    $stmt = $conn->prepare("SELECT srno, delegate, delegate_user_name FROM org_delegate_params WHERE (delegate LIKE ? OR delegate_user_name LIKE ?) AND user = ? AND status = 'Active' ORDER BY delegate LIMIT 20");
    $stmt->bind_param("sss", $search, $search, $user);
    $stmt->execute();
    echo json_encode(array("status" => "success", "data" => $stmt->get_result()->fetch_all(MYSQLI_ASSOC)));
}

function getCategories($conn, $user) {
    $stmt = $conn->prepare("SELECT srno, catg FROM org_catg WHERE user = ? ORDER BY catg");
    $stmt->bind_param("s", $user);
    $stmt->execute();
    echo json_encode(array("status" => "success", "data" => $stmt->get_result()->fetch_all(MYSQLI_ASSOC)));
}

function addCategory($conn, $user) {
    $catg = isset($_POST['catg']) ? trim($_POST['catg']) : '';
    if (empty($catg)) { echo json_encode(array("status" => "error", "message" => "Missing category name")); return; }
    $stmt = $conn->prepare("INSERT INTO org_catg (user, catg) VALUES (?, ?)");
    $stmt->bind_param("ss", $user, $catg);
    if ($stmt->execute()) { echo json_encode(array("status" => "success", "srno" => $stmt->insert_id)); }
    else { echo json_encode(array("status" => "error", "message" => $stmt->error)); }
}

function deleteCategory($conn, $user) {
    $srno = isset($_POST['srno']) ? $_POST['srno'] : '';
    $stmt = $conn->prepare("DELETE FROM org_catg WHERE srno = ? AND user = ?");
    $stmt->bind_param("is", $srno, $user);
    if ($stmt->execute()) { echo json_encode(array("status" => "success")); }
    else { echo json_encode(array("status" => "error", "message" => $stmt->error)); }
}

function getDashboardSummary($conn, $user) {
    $summary = [];
    
    // Total Pending
    $res = $conn->query("SELECT COUNT(*) as count FROM org_main WHERE user = '$user' AND status NOT IN ('Done', 'Archived')");
    $summary['pending_tasks'] = $res->fetch_assoc()['count'];
    
    // Overdue
    $today = date('Y-m-d');
    $res = $conn->query("SELECT COUNT(*) as count FROM org_main WHERE user = '$user' AND status NOT IN ('Done', 'Archived') AND due < '$today' AND due IS NOT NULL");
    $summary['overdue_tasks'] = $res->fetch_assoc()['count'];
    
    // Today's Events
    $res = $conn->query("SELECT COUNT(*) as count FROM events WHERE date = '$today' AND task_srno IN (SELECT srno FROM org_main WHERE user = '$user')");
    $summary['today_events'] = $res->fetch_assoc()['count'];

    // Delegated to me
    $res = $conn->prepare("SELECT COUNT(*) as count FROM org_delegate WHERE delegate_srno = (SELECT srno FROM org_delegate_params WHERE delegate_user_name = ? LIMIT 1)");
    $res->bind_param("s", $user);
    $res->execute();
    $summary['delegated_to_me'] = $res->get_result()->fetch_assoc()['count'];

    echo json_encode(array("status" => "success", "data" => $summary));
}

function addDelegateProfile($conn, $user) {
    $delegate = isset($_POST['delegate']) ? $_POST['delegate'] : '';
    $delegate_user_name = isset($_POST['delegate_user_name']) ? $_POST['delegate_user_name'] : '';
    if (empty($delegate)) { echo json_encode(array("status" => "error", "message" => "Missing delegate name")); return; }
    
    $stmt = $conn->prepare("INSERT INTO org_delegate_params (user, delegate, delegate_user_name, status) VALUES (?, ?, ?, 'Active')");
    $stmt->bind_param("sss", $user, $delegate, $delegate_user_name);
    if ($stmt->execute()) { echo json_encode(array("status" => "success", "srno" => $stmt->insert_id)); }
    else { echo json_encode(array("status" => "error", "message" => $stmt->error)); }
}

function deleteDelegateProfile($conn) {
    $srno = isset($_POST['srno']) ? $_POST['srno'] : '';
    $stmt = $conn->prepare("UPDATE org_delegate_params SET status = 'Inactive' WHERE srno = ?");
    $stmt->bind_param("i", $srno);
    if ($stmt->execute()) { echo json_encode(array("status" => "success")); }
    else { echo json_encode(array("status" => "error", "message" => $stmt->error)); }
}

function getSyncHistory($conn) {
    $task_srno = isset($_POST['task_srno']) ? $_POST['task_srno'] : '';
    $stmt = $conn->prepare("SELECT id, title, description, date, time_from, time_to, google_calendar_event_id FROM events WHERE task_srno = ? ORDER BY date DESC");
    $stmt->bind_param("i", $task_srno);
    $stmt->execute();
    echo json_encode(array("status" => "success", "data" => $stmt->get_result()->fetch_all(MYSQLI_ASSOC)));
}

$conn->close();
?>
