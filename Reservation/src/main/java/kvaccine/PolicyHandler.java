package kvaccine;

import kvaccine.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler {
	
    @Autowired 
    ReservationRepository reservationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationRequested_Reserve(@Payload UserReservationRequested userReservationRequested) {

        if(!userReservationRequested.validate()) return;
        System.out.println("\n\n##### listener UserReservationRequested : " + userReservationRequested.toJson() + "\n\n");

        Reservation reservation = new Reservation();
        reservation.setUserId(userReservationRequested.getId());
        reservation.setReserveStatus(userReservationRequested.getReserveStatus());
        reservation.setUserName(userReservationRequested.getUserName());
        reservation.setUserRegNumber(userReservationRequested.getUserRegNumber());
        reservation.setReserveDate(userReservationRequested.getReserveDate());
        
        reservationRepository.save(reservation);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCancelled_Cancel(@Payload UserReservationCancelled userReservationCancelled){

        if(!userReservationCancelled.validate()) return;
        System.out.println("\n\n##### listener UserReservationCancelled : " + userReservationCancelled.toJson() + "\n\n");

        Reservation reservation = reservationRepository.findByUserId(userReservationCancelled.getId());
        
        if (reservation != null) {
    		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		String dateStr = format.format(Calendar.getInstance().getTime());
        	
        	reservation.setReserveStatus("CANCEL");
            reservation.setCancelDate(dateStr);         
            reservationRepository.save(reservation);	
        }
    }



}