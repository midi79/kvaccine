package kvaccine;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import kvaccine.config.kafka.KafkaProcessor;

@Service
public class PolicyHandler {
	
    @Autowired 
    HospitalRepository hospitalRepository;
   
    // configMap으로 변경 
    String[] vaccines = {"Pfizer", "Moderna", "Janssen", "AstraZeneca"};
    ArrayList<String> vaccineTypeList = new ArrayList<>(Arrays.asList(vaccines));

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationRequested_Reserve(@Payload ReservationRequested reservationRequested){

        if(!reservationRequested.validate()) return;
        System.out.println("\n\n##### listener Hospital ReservationRequested : " + reservationRequested.toJson() + "\n\n");
       
        Hospital hospital = new Hospital();
        hospital.setHospitalName("Korean Hospital");
        hospital.setUserId(reservationRequested.getUserId());
        hospital.setUserName(reservationRequested.getUserName());
        hospital.setUserRegNumber(reservationRequested.getUserRedNumber());
        hospital.setReserveDate(reservationRequested.getReserveDate());
        hospital.setReserveStatus(reservationRequested.getReserveStatus());
        
        String vaccineType = vaccineTypeList.get(((int)(Math.random() * 10)) % 4);
        hospital.setVaccineType(vaccineType);
        
        hospitalRepository.save(hospital);

    }
    
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCancelled_Cancel(@Payload ReservationCancelled reservationCancelled) {

        if(!reservationCancelled.validate()) return;
        System.out.println("\n\n##### listener Hospital ReservationCancelled : " + reservationCancelled.toJson() + "\n\n");

        Hospital hospital = hospitalRepository.findByUserId(reservationCancelled.getUserId());
        hospital.setReserveStatus(reservationCancelled.getReserveStataus());

        hospitalRepository.save(hospital);

    }


}