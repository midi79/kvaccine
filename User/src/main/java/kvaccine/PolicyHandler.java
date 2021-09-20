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
    @Autowired UserRepository userRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDateRequested_UpdateDate(@Payload DateRequested dateRequested){

        if(!dateRequested.validate()) return;

        System.out.println("\n\n##### listener UpdateDate : " + dateRequested.toJson() + "\n\n");



        // Sample Logic //
        // User user = new User();
        // userRepository.save(user);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverVaccineInjected_UpdateDate(@Payload VaccineInjected vaccineInjected){

        if(!vaccineInjected.validate()) return;

        System.out.println("\n\n##### listener UpdateDate : " + vaccineInjected.toJson() + "\n\n");



        // Sample Logic //
        // User user = new User();
        // userRepository.save(user);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}