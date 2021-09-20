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
    @Autowired StockRepository stockRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationReceived_Reserve(@Payload ReservationReceived reservationReceived){

        if(!reservationReceived.validate()) return;

        System.out.println("\n\n##### listener Reserve : " + reservationReceived.toJson() + "\n\n");



        // Sample Logic //
        // Stock stock = new Stock();
        // stockRepository.save(stock);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCancelled_Cancel(@Payload ReservationCancelled reservationCancelled){

        if(!reservationCancelled.validate()) return;

        System.out.println("\n\n##### listener Cancel : " + reservationCancelled.toJson() + "\n\n");



        // Sample Logic //
        // Stock stock = new Stock();
        // stockRepository.save(stock);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}