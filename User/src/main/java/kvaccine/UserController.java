package kvaccine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kvaccine.external.Reservation;
import kvaccine.external.ReserveDate;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	UserRepository userRepository;
	
	// 접종 가능한 날짜 가져오기
	@PostMapping("/date")
	public ResponseEntity<ArrayList<ReserveDate>> getDateList(@RequestBody Reservation reservation) {
        kvaccine.external.Reservation externalReservation = new kvaccine.external.Reservation();
        externalReservation.setUserName(reservation.getUserName());
        externalReservation.setUserRegNumber(reservation.getUserRegNumber());
        ArrayList<ReserveDate> dateList = UserApplication.applicationContext.getBean(kvaccine.external.ReservationService.class).dateRequest(externalReservation);
		return ResponseEntity.ok(dateList);
	}
	
	// 백신 접종 날짜 예약하기 
	@PostMapping("/reserve")
	public ResponseEntity<User> reserveDate(@RequestBody User user) {
		
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = format.format(Calendar.getInstance().getTime());
		user.setModifyDate(dateStr);
		user.setReserveStatus("RESERVE");
		
		userRepository.save(user);
		
		return ResponseEntity.ok(user);
	}
	

	// 백신 예약 날짜 취소하기
	@PostMapping("/cancel")
	public ResponseEntity<User> cancelDate(@RequestBody User user) {
		User foundUser = userRepository.findByUserRegNumber(user.getUserRegNumber());
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = format.format(Calendar.getInstance().getTime());
		foundUser.setModifyDate(dateStr);
		foundUser.setReserveStatus("CANCEL");
		
		userRepository.save(foundUser);
		
		return ResponseEntity.ok(foundUser);
	}
	
	
	// CPU 부하 코드
	@GetMapping("/hpa")
	public String testHPA(){
		double x = 0.0001;
		String hostname = "";
		for (int i = 0; i <= 1000000; i++){
			x += java.lang.Math.sqrt(x);
		}
		try{
			hostname = java.net.InetAddress.getLocalHost().getHostName();
		} catch(java.net.UnknownHostException e){
			e.printStackTrace();
		}

		return "====== HPA Test(" + hostname + ") ====== \n";
	}	
	

}