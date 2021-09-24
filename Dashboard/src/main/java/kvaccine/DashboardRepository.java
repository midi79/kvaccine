package kvaccine;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {

	Dashboard findByUserId(Long userId);
	
}