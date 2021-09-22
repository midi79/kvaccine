package kvaccine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservation")
public class ReservationController {
	
	@PostMapping("/date")
	public ArrayList<ReserveDate> getDateList(@RequestBody Reservation reservation) {
		ArrayList<ReserveDate> dateList = new ArrayList<>();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar today = Calendar.getInstance();
		
		for (int i=0; i < 10; i++) {
			today.add(Calendar.DAY_OF_MONTH, 1);
			String dateFormat = format.format(today.getTime());
			Integer random = Integer.valueOf((int)(Math.random() * 1000));
			ReserveDate reserveDate = new ReserveDate(dateFormat, random);
			dateList.add(reserveDate);
		}
		
		return dateList;
	}
	
	


}