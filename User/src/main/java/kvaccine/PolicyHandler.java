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
    UserRepository userRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverVaccineInjected_UpdateDate(@Payload VaccineInjected vaccineInjected){

        if(!vaccineInjected.validate()) return;
        System.out.println("\n\n##### listener UpdateDate : " + vaccineInjected.toJson() + "\n\n");

        User user = userRepository.findById(vaccineInjected.getUserId()).orElse(null);
        user.setReserveStatus("INJECT");
        user.setInjectDate(vaccineInjected.getInjectDate());
        user.setVaccineType(vaccineInjected.getVaccineType());

        userRepository.save(user);

    }

}