package kvaccine.external;

import java.util.ArrayList;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name="Reservation", url="${api.url.pay}")
public interface ReservationService {
	
    @RequestMapping(method= RequestMethod.GET, path="/date")
    public ArrayList<ReserveDate> dateRequest(@RequestBody Reservation reservation);

}

