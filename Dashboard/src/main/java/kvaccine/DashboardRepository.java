package kvaccine;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {

	Dashboard findByUserId(Long userId);
	List<Dashboard> findAllByVaccineType(String vaccineType);

}