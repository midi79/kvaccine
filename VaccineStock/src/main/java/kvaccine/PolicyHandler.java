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
    VaccineStockRepository vaccineStockRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationReceived_Reserve(@Payload HospitalReservationReceived reservationReceived) {

        if(!reservationReceived.validate()) return;
        System.out.println("\n\n##### listener Reserve : " + reservationReceived.toJson() + "\n\n");

        VaccineStock stock = new VaccineStock();
        stock.setVaccineType(reservationReceived.getVaccineType());
        stock.setVaccineCount(-1);
        
        vaccineStockRepository.save(stock);

    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCancelled_Cancel(@Payload HospitalReservationCancelled reservationCancelled){

        if(!reservationCancelled.validate()) return;
        System.out.println("\n\n##### listener Cancel : " + reservationCancelled.toJson() + "\n\n");

        VaccineStock stock = new VaccineStock();
        stock.setVaccineType(reservationCancelled.getVaccineType());
        stock.setVaccineCount(1);
        vaccineStockRepository.save(stock);

    }

}