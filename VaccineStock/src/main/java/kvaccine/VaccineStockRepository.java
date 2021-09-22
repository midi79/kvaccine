package kvaccine;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="stocks", path="stocks")
public interface VaccineStockRepository extends PagingAndSortingRepository<VaccineStock, Long>{


}
