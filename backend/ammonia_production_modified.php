<?php
$db_host=$_POST['db_host'];  
$db_username=$_POST['db_username'];  
$db_password=$_POST['db_password'];  
$db_name=$_POST['db_name'];  
include("connect_app.php");

// --- LOGGING START ---
$log_file = 'ammonia_production_debug_log.txt';
$current_time = date("Y-m-d H:i:s");

// Use isset() for older PHP compatibility
$ver = isset($_POST['appversion']) ? $_POST['appversion'] : 'Unknown';

$log_entry = "-------------------------------------------\n";
$log_entry .= "Timestamp: " . $current_time . "\n";
$log_entry .= "App Version: " . $ver . "\n";
$log_entry .= "Raw POST Data:\n" . print_r($_POST, true) . "\n";
$log_entry .= "-------------------------------------------\n\n";

// Write to file (appends to the end of the file)
$fp = fopen($log_file, 'a');
fwrite($fp, $log_entry);
fclose($fp);
// --- LOGGING END ---


		$appversion=$_POST['appversion'];
		$response=array();
		$aicode=$_POST['dura_code'];    
		$owner_code=$_POST['owner_code'];
		$start_time=$_POST['starttime'];    
		$end_time=$_POST['endtime'];
	    $full_wt=$_POST['full_wt'];
		$actual_wt=$_POST['actual_wt'];		//Line added by NK
		$emt_wt=$_POST['emt_wt'];
		$net_wt=$_POST['net_wt'];
		$totcubic=$_POST['totcubic'];
	    $manifold_no=$_POST['manifold_no'];
	    $cyl_quan=$_POST['cyl_quan'];
		$AI_qty=$_POST['AI_qty'];
		$dist_qty=$_POST['dist_qty'];
		$after_tank_pressure=$_POST['after_tank_pressure'];
		$after_tank_liquid_liter=$_POST['after_tank_liquid_liter'];
		$before_tank_pressure=$_POST['before_tank_pressure'];
		$before_tank_liquid_liter=$_POST['before_tank_liquid_liter'];
		$supervisor=$_POST['supervisor'];
		$email=$_POST['email'];
		$remarks=$_POST['remarks'];
		$batch_prefix=$_POST['batch_prefix'];
        
        // --- ADDED: Extract is_scan from POST ---
        $is_scan = isset($_POST['is_scan']) ? $_POST['is_scan'] : '';

		$main_today_mysql_ymd = date("Y-m-d");
	    $main_today_mysql = date("Y-m-d H:i:s"); 
	    $contravelstart_time = date("h:i", strtotime($start_time));
	    $contravelend_time = date("h:i", strtotime($end_time));
		$batch_id;
		$coun=1;
	   $d=date("d");
	   $m=date("m");
	    $y=date("y");

		

	   
	  

					$result = mysqli_query($conn,"select COUNT(*) AS cityCount from production_main_ammonia where date='$main_today_mysql_ymd'");
			$row = $result->fetch_assoc();
			$counter=$row['cityCount'];
			if($counter==0)
			{
				$batch_id=$batch_prefix."-".$d.$m.$y."-".$coun;
			}
			else{
				$counter++;
				$batch_id=$batch_prefix."-".$d.$m.$y."-".$counter;
			}

	
//Actual Weight > Tare Weight
//Empty Weight > Empty Weight
			
	$str_arr = explode (",", $aicode); 
		$full_wt_arr = explode (",", $full_wt);
		$emt_wt_arr = explode (",", $emt_wt);
		$net_wt_arr = explode (",", $net_wt);
		$act_wt_arr = explode (",", $actual_wt);
		$owner_code_arr = explode (",", $owner_code);
		$manifold_no_arr = explode (",", $manifold_no);
        $is_scan_arr = explode (",", $is_scan); // --- ADDED ---
		
			$str_arrrep=str_replace( array('[',']') ,'', $str_arr );
				$new_str_arrrep = str_replace(' ', '', $str_arrrep);
						$full_wt_arrep=str_replace( array('[',']') ,'', $full_wt_arr );
				$new_full_wt_arrep = str_replace(' ', '', $full_wt_arrep);
					$owner_code_rrep=str_replace( array('[',']') ,'', $owner_code_arr );
				$new_owner_code = str_replace(' ', '', $owner_code_rrep);
						$emt_wt_arr_rrep=str_replace( array('[',']') ,'', $emt_wt_arr );
				$new_emt_wt_arr_rrep = str_replace(' ', '', $emt_wt_arr_rrep);
						$act_wt_arr_rrep=str_replace( array('[',']') ,'', $act_wt_arr );
				$new_act_wt_arr_rrep = str_replace(' ', '', $act_wt_arr_rrep);
				
						$net_wt_arr_rrep=str_replace( array('[',']') ,'', $net_wt_arr );
				$new_net_wt_arr_rrep = str_replace(' ', '', $net_wt_arr_rrep);
					$manifold_no_rep=str_replace( array('[',']') ,'', $manifold_no_arr );
				$new_manifold_no = str_replace(' ', '', $manifold_no_rep);

                $is_scan_rrep = str_replace( array('[',']') ,'', $is_scan_arr ); // --- ADDED ---
                $new_is_scan = str_replace(' ', '', $is_scan_rrep); // --- ADDED ---
	
for($i=0;$i<count($str_arr);$i++) { 
			
/*NK 31-08-2025 > Changed this code to take the tare weight. This had stopped happening since June due to some changes / regression. The code is confusing. Based on the values 
received from android, I have changed the mapping
	manifold_no > $new_manifold_no[$i]
	cyl_code > $new_str_arrrep[$i]
	full_wt > $new_full_wt_arrep[$i]
	empty_wt > $new_act_wt_arr_rrep[$i]		<<-This column needs to record the first weight of the cylinder during the fill. Hence mapped to ACTUAL weight coming from Anrdoid.
	tare_wt > $new_emt_wt_arr_rrep[$i]		<<-This was the column for which the change was needed. This needs to record the weight of the cylinder from inventory_cylinders. Hence mapped to EMPTY weight coming from Android.
	net_wt > $new_net_wt_arr_rrep[$i]		Net Weight = Full Weight - Tare Weight
*/
                // --- MODIFIED: Added is_scanned to insert query ---
				$qry2="INSERT INTO production_ammonia_transactions(srno,batch_id,date,owner_code,manifold_no,cyl_code,empty_wt, full_wt,tare_wt,net_wt,rate_cubic,dc,invoiced,is_scanned)
			      VALUES ('$batch_id','$batch_id','$main_today_mysql_ymd','$new_owner_code[$i]','$new_manifold_no[$i]','$new_str_arrrep[$i]','$new_act_wt_arr_rrep[$i]','$new_full_wt_arrep[$i]','$new_emt_wt_arr_rrep[$i]','$new_net_wt_arr_rrep[$i]','0','N','N','$new_is_scan[$i]')";

			mysqli_query($conn,$qry2);
		
				
			
		}
			
	
				
				
				
			$qry3="INSERT INTO production_main_ammonia(batch_no,batch_id,date,start_time,end_time,timestamp,before_tank_pressure,before_tank_liquid_liter,WAPL_qty,dist_qty,tot_cyl_qty,weight,after_tank_pressure,after_tank_liquid_liter,remarks,supervisor,email)
			                             VALUES ('$batch_id','$batch_id','$main_today_mysql_ymd','$contravelstart_time','$contravelend_time','$main_today_mysql','$before_tank_pressure','$before_tank_liquid_liter','$AI_qty','$dist_qty','$cyl_quan','$totcubic','$after_tank_pressure','$after_tank_liquid_liter','$remarks','$supervisor','$email')";

			if(mysqli_query($conn,$qry3))
			{
					$data['status']="success";
					$data['msg']="Login Successfully";
					$data['batch_id']=$batch_id;	
			
					array_push($response,$data);
				}
				else
				{
					 $data['status']="Failed";
					 $data['msg']="Login Failed";
					 array_push($response,$data);
				}


					$qry_tt = "INSERT INTO tank_transactions (srno, tank_type, transaction_type, transaction_ref_no, timestamp, volume_filled, cubic_filled, start_tank_level, end_tank_level) VALUES (null, 'AHDAMM','AMMPROD','$batch_id','$main_today_mysql','$totcubic','$totcubic','$before_tank_liquid_liter','$after_tank_liquid_liter')";
					mysqli_query($conn, $qry_tt);
				
				echo json_encode($response);
				
				
				

?>
