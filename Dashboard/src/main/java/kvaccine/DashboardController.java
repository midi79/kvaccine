package kvaccine;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
	
	@Autowired
	DashboardRepository dashboardRepository;
	
	// 전체 리스트 가져오기 
	@GetMapping("/list")
	public ResponseEntity<List<Dashboard>> getDashboardList() {
		
		List<Dashboard> dashboardList = dashboardRepository.findAll();
		
		return ResponseEntity.ok(dashboardList);
	}
	
	// 백신 타입에 따라 리스트 가져오기 
	@GetMapping("/list/{vaccineType}")
	public ResponseEntity<List<Dashboard>> getDashboardListByUserId(@PathVariable String vaccineType) {
		
		List<Dashboard> dashboardList = dashboardRepository.findAllByVaccineType(vaccineType);
		
		return ResponseEntity.ok(dashboardList);
	}	
	
}