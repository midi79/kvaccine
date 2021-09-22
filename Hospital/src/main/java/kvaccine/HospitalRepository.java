package kvaccine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="hospitals", path="hospitals")
public interface HospitalRepository extends JpaRepository<Hospital, Long>{

	Hospital findByUserId(Long userId);
	
}
