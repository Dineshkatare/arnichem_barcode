<?php
$db_host=$_POST['db_host'];  
$db_username=$_POST['db_username'];  
$db_password=$_POST['db_password'];  
$db_name=$_POST['db_name'];  

/*
$db_host='160.153.61.68';
$db_username='arnichem_barcode';  
$db_password='arnichem@123';  
$db_name='arnichem_barcode'; 
*/

include ("connect_app.php");

		$response=array();
		$bp_code=$_POST['bp_code'];    
		$visit_type=$_POST['visit_type']; 
		$name=$_POST['name']; 
		$detail=$_POST['detail']; 
		$email=$_POST['email'];  
        
        // START: Location Support
        $gps_lat = isset($_POST['GPS_lat']) ? $_POST['GPS_lat'] : '';
        $gps_long = isset($_POST['GPS_long']) ? $_POST['GPS_long'] : '';
        $address = isset($_POST['address']) ? $_POST['address'] : '';
        // END: Location Support

		$main_today_mysql_ymd = date("Y-m-d");
		$main_today_mysql = date("Y-m-d H:i:s"); 
	
		$result = mysqli_query($conn,"select srno from bpcontact where contactname='$name' and code='$bp_code'");
			$row = $result->fetch_assoc();
			 $srno=$row['srno'];


$qry2="INSERT INTO bp_crm(crm_entry_id,bp_code,cust_contact,date,visit_type,detail,timestamp,email,GPS_lat,GPS_long,address)
			      VALUES (null,'$bp_code','$srno','$main_today_mysql_ymd','$visit_type','$detail','$main_today_mysql','$email','$gps_lat','$gps_long','$address')";
 mysqli_set_charset($conn, 'utf8');
				if(mysqli_query($conn,$qry2))
				{
					$data['status']="success";
					$data['msg']="Login Successfully";
		
					array_push($response,$data);
					
					
					
			/*		//Code added by NK to check email on delivery from APP
					$headers  = 'MIME-Version: 1.0' . "\r\n";
					$headers .= 'Content-type: text/html; charset=ISO-8859-1' . "\r\n";
					$headers .= 'From: CRM Entry <crm@arnichem.co.in>' . "\r\n";		
					$subject = 'CRM Entry - '.$email;
					$mail_message = '<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">';
					$mail_message .= '<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />';
					$mail_message .= '<link rel="stylesheet" type="text/css" href="intranetstyle.css" />';
					$mail_message .= '<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />';
					$mail_message .= '<script src="http://code.jquery.com/jquery-1.9.1.js"></script>';
					$mail_message .= '<script src="http://code.jquery.com/ui/1.10.3/jquery-ui.js"></script>';
					$mail_message .= '<head>';
					$mail_message .= '<h1>CRM Entry '.$email.'</h1>';
					$mail_message .= '<h3></h3>';
					$mail_message .= '</head>';
					$mail_message .= '<body>';
					$mail_message .= '<b>Entry Details</b>';
					$mail_message .= '<p>Customer Code - '.$bp_code.'</p>';
					$mail_message .= '<p>Detail - '.$detail.'</p>';
					$mail_message .= '<br/>';
					$mail_message .='</body>';
					$mail_message .='</html>';

					mail ('nk@arnichem.co.in', $subject,$mail_message, $headers);
*/
				
				}
				else
				{
					 $data['status']="Failed";
					 $data['msg']="Login Failed";
					 array_push($response,$data);
				}
				
				echo json_encode($response);
				
			
		
				
				

?>
