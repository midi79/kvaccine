package kvaccine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="reservations", path="reservations")
public interface ReservationRepository extends JpaRepository<Reservation, Long>{
	
	Reservation findByUserId(Long userId);

}
