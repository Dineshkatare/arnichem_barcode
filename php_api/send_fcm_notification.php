<?php
/**
 * Utility to send FCM notifications using HTTP v1 API.
 * Requires: Google Service Account JSON file.
 * 
 * Usage:
 * $sender = new FCMSender('path/to/service-account.json');
 * $sender->sendNotification($token, "Title", "Body", ["key" => "value"]);
 */

class FCMSender {
    private $serviceAccountPath;
    public $projectId;

    private $dbConfig;

    public function __construct($serviceAccountPath, $dbConfig = null) {
        $this->serviceAccountPath = $serviceAccountPath;
        $this->dbConfig = $dbConfig;
        
        if (file_exists($serviceAccountPath)) {
            $content = file_get_contents($serviceAccountPath);
            $json = json_decode($content, true);
            $this->projectId = isset($json['project_id']) ? $json['project_id'] : null;
        } else {
            $this->projectId = null;
        }
    }

    private function getAccessToken() {
        if (!file_exists($this->serviceAccountPath)) {
            error_log("FCM Error: Service account file not found at " . $this->serviceAccountPath);
            return null;
        }

        $json = json_decode(file_get_contents($this->serviceAccountPath), true);
        if (!$json) {
            error_log("FCM Error: Failed to decode service account JSON.");
            return null;
        }

        $client_email = $json['client_email'];
        $private_key = $json['private_key'];

        $header = json_encode(['alg' => 'RS256', 'typ' => 'JWT']);
        $now = time();
        $payload = json_encode([
            'iss' => $client_email,
            'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
            'aud' => 'https://oauth2.googleapis.com/token',
            'exp' => $now + 3600,
            'iat' => $now
        ]);

        $base64UrlHeader = $this->base64UrlEncode($header);
        $base64UrlPayload = $this->base64UrlEncode($payload);

        $signature = '';
        if (!openssl_sign($base64UrlHeader . "." . $base64UrlPayload, $signature, $private_key, 'SHA256')) {
            error_log("FCM Error: Failed to sign JWT. Check if OpenSSL is enabled and private key is valid.");
            return null;
        }
        $base64UrlSignature = $this->base64UrlEncode($signature);

        $jwt = $base64UrlHeader . "." . $base64UrlPayload . "." . $base64UrlSignature;

        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, 'https://oauth2.googleapis.com/token');
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query([
            'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            'assertion' => $jwt
        ]));

        $result = curl_exec($ch);
        $curl_error = curl_error($ch);
        curl_close($ch);

        if ($result === false) {
            error_log("FCM Error: Token request failed. CURL Error: " . $curl_error);
            return null;
        }

        $data = json_decode($result, true);
        if (isset($data['access_token'])) {
            return $data['access_token'];
        } else {
            error_log("FCM Error: Token API response did not contain access_token. Response: " . $result);
            return null;
        }
    }

    private function base64UrlEncode($data) {
        return str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($data));
    }

    public function sendNotification($deviceToken, $title, $body, $data = [], $user_id = 0) {
        if (!$this->projectId) {
            return json_encode(["error" => "Service account file missing or invalid project_id"]);
        }
        $url = "https://fcm.googleapis.com/v1/projects/{$this->projectId}/messages:send";
        
        // --- AUTO LOGGING TO HISTORY BEFORE SENDING ---
        // This is crucial to get the generated DB notification_id and inject it into the FCM data payload.
        if ($user_id && $this->dbConfig) {
            $notification_id = $this->logNotification($user_id, $title, $body, $data, 'sent');
            if ($notification_id) {
                $data['notification_id'] = (string)$notification_id;
            }
        }

        // Inject title and body into data payload for Android background processing
        $data['title'] = $title;
        $data['body'] = $body;

        $message = [
            "message" => [
                'token' => $deviceToken,
                "notification" => [
                    "title" => $title,
                    "body" => $body
                ],
                "data" => $data
            ]
        ];

        $accessToken = $this->getAccessToken();
        if (!$accessToken) {
            $error_json = json_encode(["error" => "Could not generate access token"]);
            return $error_json;
        }

        $headers = [
            'Authorization: Bearer ' . $accessToken,
            'Content-Type: application/json'
        ];

        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($message));

        $result = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        
        if ($result === FALSE) {
            error_log('FCM Curl failed: ' . curl_error($ch));
        } elseif ($httpCode !== 200) {
            error_log("FCM API error (HTTP $httpCode): " . $result);
        }
        curl_close($ch);

        return $result;
    }

    /**
     * Logs notification to the database history table.
     */
    public function logNotification($user_id, $title, $body, $data, $status = 'sent') {
        if (!$this->dbConfig) return false;

        $conn = new mysqli($this->dbConfig['host'], $this->dbConfig['user'], $this->dbConfig['pass'], $this->dbConfig['name']);
        if ($conn->connect_error) {
            error_log("FCM Logging Error: Connection failed: " . $conn->connect_error);
            return false;
        }

        // Match user's requested schema exactly and auto-migrate
        $conn->query("CREATE TABLE IF NOT EXISTS notification_history (
            id INT(11) AUTO_INCREMENT PRIMARY KEY,
            user_id INT(11),
            title VARCHAR(255),
            body TEXT,
            data TEXT,
            sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            status ENUM('sent','delivered','opened') DEFAULT 'sent'
        )");

        // Auto-migration: Check if 'username' column exists and rename/change it to 'user_id'
        $check_uname = $conn->query("SHOW COLUMNS FROM notification_history LIKE 'username'");
        if ($check_uname && $check_uname->num_rows > 0) {
            $conn->query("ALTER TABLE notification_history CHANGE COLUMN username user_id INT(11)");
        }
        
        // Ensure status is ENUM
        $check_status = $conn->query("SHOW COLUMNS FROM notification_history LIKE 'status'");
        $status_row = $check_status->fetch_assoc();
        if ($status_row && strpos($status_row['Type'], 'enum') === false) {
             $conn->query("ALTER TABLE notification_history MODIFY COLUMN status ENUM('sent','delivered','opened') DEFAULT 'sent'");
        }

        $data_json = is_string($data) ? $data : json_encode($data);
        $stmt = $conn->prepare("INSERT INTO notification_history (user_id, title, body, data, status) VALUES (?, ?, ?, ?, ?)");
        
        $inserted_id = 0;
        if ($stmt) {
            $user_id_int = intval($user_id);
            $stmt->bind_param("issss", $user_id_int, $title, $body, $data_json, $status);
            if ($stmt->execute()) {
                $inserted_id = $conn->insert_id;
            }
            $stmt->close();
        } else {
            error_log("FCM Logging Error: Prepare failed: " . $conn->error);
        }
        $conn->close();

        return $inserted_id;
    }

    /**
     * Helper to broadcast a notification to all users of a specific role.
     */
    public static function broadcastToRole($serviceAccountPath, $dbConfig, $title, $body, $role_key = null, $event_type = 'broadcast', $extra_data = []) {
        $conn = new mysqli($dbConfig['host'], $dbConfig['user'], $dbConfig['pass'], $dbConfig['name']);
        if ($conn->connect_error) return ["status" => "error", "message" => "DB Connection failed"];

        if ($role_key) {
            // Deduplicate: pick the latest token per username to avoid double notifications
            $stmt = $conn->prepare(
                "SELECT t.id, t.username, t.fcm_token
                 FROM user_fcm_tokens t
                 INNER JOIN (
                     SELECT username, MAX(id) AS max_id
                     FROM user_fcm_tokens
                     WHERE fcm_token != ''
                     AND (FIND_IN_SET(?, role_key) > 0 OR role_key LIKE CONCAT('%', ?, '%'))
                     GROUP BY username
                 ) latest ON t.id = latest.max_id"
            );
            $rkey = $role_key;
            $stmt->bind_param("ss", $rkey, $rkey);
            $stmt->execute();
            $result = $stmt->get_result();
        } else {
            // Deduplicate: latest token per username across all users
            $result = $conn->query(
                "SELECT t.id, t.username, t.fcm_token
                 FROM user_fcm_tokens t
                 INNER JOIN (
                     SELECT username, MAX(id) AS max_id
                     FROM user_fcm_tokens
                     WHERE fcm_token != ''
                     GROUP BY username
                 ) latest ON t.id = latest.max_id"
            );
        }

        $success_count = 0;
        $fail_count = 0;
        
        if ($result && $result->num_rows > 0) {
            $sender = new self($serviceAccountPath, $dbConfig);
            while ($row = $result->fetch_assoc()) {
                $token = $row['fcm_token'];
                $uid = $row['id'];
                $data = array_merge(["event_type" => $event_type], $extra_data);
                $res = $sender->sendNotification($token, $title, $body, $data, $uid);
                
                $res_data = json_decode($res, true);
                if (isset($res_data['name']) || (isset($res_data['status']) && $res_data['status'] === 'success')) {
                    $success_count++;
                } else {
                    $fail_count++;
                    $last_error = $res;
                }
            }
        }

        $conn->close();
        return [
            "status" => "success", 
            "message" => "Broadcast complete", 
            "total_sent" => $success_count, 
            "total_failed" => $fail_count,
            "role" => $role_key ? $role_key : 'all',
            "last_error" => isset($last_error) ? $last_error : null
        ];
    }
}

/**
 * Global helper for logging notifications without explicit FCMSender instantiation.
 */
function logNotification($dbConfig, $user_id, $title, $body, $data, $status = 'sent') {
    $conn = new mysqli($dbConfig['host'], $dbConfig['user'], $dbConfig['pass'], $dbConfig['name']);
    if ($conn->connect_error) return 0;
    
    // Ensure table exists and migrate
    $conn->query("CREATE TABLE IF NOT EXISTS notification_history (
        id INT(11) AUTO_INCREMENT PRIMARY KEY,
        user_id INT(11),
        title VARCHAR(255),
        body TEXT,
        data TEXT,
        sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        status ENUM('sent','delivered','opened') DEFAULT 'sent'
    )");

    $check_uname = $conn->query("SHOW COLUMNS FROM notification_history LIKE 'username'");
    if ($check_uname && $check_uname->num_rows > 0) {
        $conn->query("ALTER TABLE notification_history CHANGE COLUMN username user_id INT(11)");
    }

    $data_json = is_string($data) ? $data : json_encode($data);
    $stmt = $conn->prepare("INSERT INTO notification_history (user_id, title, body, data, status) VALUES (?, ?, ?, ?, ?)");
    
    $inserted_id = 0;
    if ($stmt) {
        $user_id_int = intval($user_id);
        $stmt->bind_param("issss", $user_id_int, $title, $body, $data_json, $status);
        if ($stmt->execute()) {
            $inserted_id = $conn->insert_id;
        }
        $stmt->close();
    } else {
        error_log("FCM Global Logging Error: Prepare failed: " . $conn->error);
    }
    $conn->close();
    return $inserted_id;
}

// Example usage call
// if (isset($_GET['test'])) {
//     $token = "DEVICE_FCM_TOKEN";
//     $sender = new FCMSender('service-account.json');
//     echo $sender->sendNotification($token, "Hello", "Test message");
// }
?>
