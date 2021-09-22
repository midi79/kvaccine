package kvaccine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hospital")
public class HospitalController {
	
    @Autowired 
    HospitalRepository hospitalRepository;
	
	// 백신 접종 완료시
	@PostMapping("/inject")
	public ResponseEntity<VaccineInjected> injectVaccine(@RequestBody VaccineInjected inject) {
		
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = format.format(Calendar.getInstance().getTime());
		
		Hospital hospital = hospitalRepository.findByUserId(inject.getUserId());
		hospital.setReserveStatus("INJECT");
		hospital.setInjectDate(dateStr);
	
		hospitalRepository.save(hospital);
		
		return ResponseEntity.ok(inject);
	}

}