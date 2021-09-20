package kvaccine.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="Reservation", url="http://Reservation:8080")
public interface ReservationService {
    @RequestMapping(method= RequestMethod.GET, path="/reservations")
    public void dateRequest(@RequestBody Reservation reservation);

}

