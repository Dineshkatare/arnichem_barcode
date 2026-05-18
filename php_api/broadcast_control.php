<?php
/**
 * Broadcast Control Panel
 * Allows sending push notifications to all registered users or specific roles.
 */

// Basic Security (Optional: Add your own authentication here)
// password_hash logic could be added here for a simple login

$status_message = "";
$status_type = "";

// Database credentials - usually passed from app, but for this UI we might want to store them or ask for them
$db_defaults = [
    'host' => '',
    'user' => '',
    'pass' => '',
    'name' => ''
];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $db_host = $_POST['db_host'] ?? '';
    $db_user = $_POST['db_username'] ?? '';
    $db_pass = $_POST['db_password'] ?? '';
    $db_name = $_POST['db_name'] ?? '';
    
    $title = $_POST['title'] ?? '';
    $body = $_POST['body'] ?? '';
    $role_key = $_POST['role_key'] ?? ''; // empty means all
    
    if (empty($db_host) || empty($db_user) || empty($db_name)) {
        $status_message = "Error: Missing database credentials.";
        $status_type = "error";
    } elseif (empty($title) || empty($body)) {
        $status_message = "Error: Title and Message are required.";
        $status_type = "error";
    } else {
        // Proceed with sending
        $conn = new mysqli($db_host, $db_user, $db_pass, $db_name);
        if ($conn->connect_error) {
            $status_message = "Database Connection Failed: " . $conn->connect_error;
            $status_type = "error";
        } else {

            // Fetch tokens
            if ($role_key) {
                $stmt = $conn->prepare("SELECT username, fcm_token FROM user_fcm_tokens WHERE fcm_token != '' AND FIND_IN_SET(?, role_key) > 0");
                $stmt->bind_param("s", $role_key);
                $stmt->execute();
                $result = $stmt->get_result();
            } else {
                $result = $conn->query("SELECT username, fcm_token FROM user_fcm_tokens WHERE fcm_token != ''");
            }
            
            if ($result && $result->num_rows > 0) {
                $fcm_file = __DIR__ . '/send_fcm_notification.php';
                if (!file_exists($fcm_file)) {
                    $status_message = "Required file missing: send_fcm_notification.php";
                    $status_type = "error";
                } else {
                    require_once $fcm_file;
                    $serviceAccountPath = __DIR__ . '/service-account.json';
                    
                    if (!file_exists($serviceAccountPath)) {
                        $status_message = "Error: service-account.json not found in " . __DIR__ . ". Please upload your Firebase Service Account key.";
                        $status_type = "error";
                    } else {
                        $dbConfig = ['host' => $db_host, 'user' => $db_user, 'pass' => $db_pass, 'name' => $db_name];
                        $response = FCMSender::broadcastToRole($serviceAccountPath, $dbConfig, $title, $body, $role_key);
                        
                        if ($response['status'] === 'success') {
                            $status_message = "Broadcast Complete! Success: " . $response['total_sent'] . ", Failed: " . $response['total_failed'];
                            $status_type = "success";
                        } else {
                            $status_message = "Broadcast Failed: " . $response['message'];
                            $status_type = "error";
                        }
                    }
                }
            } else {
                $status_message = "No users found for the selected criteria.";
                $status_type = "warning";
            }
            $conn->close();
        }
    }
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Arnichem Broadcast Control</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #6366f1;
            --primary-hover: #4f46e5;
            --bg: #0f172a;
            --card-bg: rgba(30, 41, 59, 0.7);
            --text: #f8fafc;
            --text-muted: #94a3b8;
            --success: #10b981;
            --error: #ef4444;
            --warning: #f59e0b;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Outfit', sans-serif;
        }

        body {
            background-color: var(--bg);
            background-image: 
                radial-gradient(at 0% 0%, rgba(99, 102, 241, 0.15) 0px, transparent 50%),
                radial-gradient(at 100% 100%, rgba(236, 72, 153, 0.1) 0px, transparent 50%);
            color: var(--text);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }

        .container {
            width: 100%;
            max-width: 600px;
            background: var(--card-bg);
            backdrop-filter: blur(12px);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 24px;
            padding: 40px;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
        }

        h1 {
            font-size: 28px;
            font-weight: 600;
            margin-bottom: 8px;
            background: linear-gradient(to right, #818cf8, #e879f9);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        p.subtitle {
            color: var(--text-muted);
            margin-bottom: 32px;
            font-size: 15px;
        }

        .status-box {
            padding: 16px;
            border-radius: 12px;
            margin-bottom: 24px;
            font-size: 14px;
            display: <?php echo $status_message ? 'block' : 'none'; ?>;
        }

        .status-success { background: rgba(16, 185, 129, 0.1); color: var(--success); border: 1px solid var(--success); }
        .status-error { background: rgba(239, 68, 68, 0.1); color: var(--error); border: 1px solid var(--error); }
        .status-warning { background: rgba(245, 158, 11, 0.1); color: var(--warning); border: 1px solid var(--warning); }

        .form-group {
            margin-bottom: 20px;
        }

        label {
            display: block;
            margin-bottom: 8px;
            font-size: 14px;
            color: var(--text-muted);
            font-weight: 400;
        }

        input, textarea, select {
            width: 100%;
            padding: 12px 16px;
            background: rgba(15, 23, 42, 0.6);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 12px;
            color: var(--text);
            font-size: 15px;
            transition: all 0.2s;
        }

        input:focus, textarea:focus, select:focus {
            outline: none;
            border-color: var(--primary);
            box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
        }

        .db-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 16px;
            margin-bottom: 20px;
            padding: 20px;
            background: rgba(255, 255, 255, 0.03);
            border-radius: 16px;
        }

        button {
            width: 100%;
            padding: 14px;
            background: var(--primary);
            color: white;
            border: none;
            border-radius: 12px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
            margin-top: 10px;
        }

        button:hover {
            background: var(--primary-hover);
            transform: translateY(-2px);
            box-shadow: 0 10px 15px -3px rgba(99, 102, 241, 0.4);
        }

        button:active {
            transform: translateY(0);
        }

        .footer {
            margin-top: 32px;
            text-align: center;
            font-size: 12px;
            color: var(--text-muted);
        }

        .toggle-db {
            font-size: 13px;
            color: var(--primary);
            text-decoration: none;
            display: inline-block;
            margin-bottom: 15px;
            cursor: pointer;
        }
    </style>
</head>
<body>

<div class="container">
    <h1>Broadcast Notification</h1>
    <p class="subtitle">Send a push notification to all Arnichem users instantly.</p>

    <?php if ($status_message): ?>
    <div class="status-box status-<?php echo $status_type; ?>">
        <?php echo $status_message; ?>
    </div>
    <?php endif; ?>

    <form method="POST">
        <div id="db-section">
            <label>Database Configuration</label>
            <div class="db-grid">
                <div class="form-group">
                    <input type="text" name="db_host" placeholder="DB Host (e.g. localhost)" required>
                </div>
                <div class="form-group">
                    <input type="text" name="db_name" placeholder="DB Name" required>
                </div>
                <div class="form-group">
                    <input type="text" name="db_username" placeholder="DB User" required>
                </div>
                <div class="form-group">
                    <input type="password" name="db_password" placeholder="DB Password">
                </div>
            </div>
        </div>

        <div class="form-group">
            <label for="role_key">Target Audience</label>
            <select name="role_key" id="role_key">
                <option value="">All Registered Users</option>
                <option value="account">Account Team</option>
                <option value="driver">Drivers</option>
                <option value="manager">Managers</option>
            </select>
        </div>

        <div class="form-group">
            <label for="title">Notification Title</label>
            <input type="text" name="title" id="title" placeholder="e.g. Important Update" required>
        </div>

        <div class="form-group">
            <label for="body">Message Content</label>
            <textarea name="body" id="body" rows="4" placeholder="Enter your broadcast message here..." required></textarea>
        </div>

        <button type="submit">🚀 Send Broadcast Now</button>
    </form>

    <div class="footer">
        Powered by Arnichem FCM Engine v2.0
    </div>
</div>

</body>
</html>
