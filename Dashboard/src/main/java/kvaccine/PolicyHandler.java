package kvaccine;

import kvaccine.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{

	@Autowired
	DashboardRepository dashboardRepository;

	@StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationRequested_Reserve(@Payload ReservationRequested reservationRequested) {

        if(!reservationRequested.validate()) return;
        System.out.println("\n\n##### listener Dashboard ReservationRequested : " + reservationRequested.toJson() + "\n\n");
       
        Dashboard dashboard = new Dashboard();
        dashboard.setUserId(reservationRequested.getUserId());
        dashboard.setUserName(reservationRequested.getUserName());
        dashboard.setUserRegNumber(reservationRequested.getUserRegNumber());
        dashboard.setReserveDate(reservationRequested.getReserveDate());        
        dashboard.setReserveStatus(reservationRequested.getReserveStatus());        
        
        dashboardRepository.save(dashboard);

    }
    
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCancelled_Cancel(@Payload ReservationCancelled reservationCancelled) {

        if(!reservationCancelled.validate()) return;
        System.out.println("\n\n##### listener Dashboard ReservationCancelled : " + reservationCancelled.toJson() + "\n\n");

        Dashboard dashboard = dashboardRepository.findByUserId(reservationCancelled.getUserId());
        dashboard.setReserveStatus(reservationCancelled.getReserveStatus());
        dashboard.setCancelDate(reservationCancelled.getCancelDate());

        dashboardRepository.save(dashboard);

    }
    
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverVaccineInjected_UpdateDate(@Payload VaccineInjected vaccineInjected){

        if(!vaccineInjected.validate()) return;
        System.out.println("\n\n##### listener Dashboard VaccineInjected_UpdateDate : " + vaccineInjected.toJson() + "\n\n");

        Dashboard dashboard = dashboardRepository.findByUserId(vaccineInjected.getUserId());
        dashboard.setReserveStatus("INJECT");
        dashboard.setInjectDate(vaccineInjected.getInjectDate());
        dashboard.setVaccineType(vaccineInjected.getVaccineType());

        dashboardRepository.save(dashboard);
    }
}