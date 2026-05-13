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
    private $projectId;

    private $dbConfig;

    public function __construct($serviceAccountPath, $dbConfig = null) {
        $this->serviceAccountPath = $serviceAccountPath;
        $json = json_decode(file_get_contents($serviceAccountPath), true);
        $this->projectId = $json['project_id'];
        $this->dbConfig = $dbConfig;
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

    public function sendNotification($token, $title, $body, $data = [], $user_id = null) {
        $url = "https://fcm.googleapis.com/v1/projects/{$this->projectId}/messages:send";
        
        // Inject title and body into data payload for Android background processing
        $data['title'] = $title;
        $data['body'] = $body;

        $message = [
            "message" => [
                "token" => $token,
                "data" => $data
            ]
        ];

        $accessToken = $this->getAccessToken();
        if (!$accessToken) {
            return json_encode(["error" => "Could not generate access token"]);
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
}

// Example usage call
// if (isset($_GET['test'])) {
//     $token = "DEVICE_FCM_TOKEN";
//     $sender = new FCMSender('service-account.json');
//     echo $sender->sendNotification($token, "Hello", "Test message");
// }
?>
