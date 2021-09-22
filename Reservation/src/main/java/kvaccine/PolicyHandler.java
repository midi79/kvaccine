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
    public void wheneverReservationRequested_Reserve(@Payload ReservationRequested reservationRequested){

        if(!reservationRequested.validate()) return;
        System.out.println("\n\n##### listener ReservationRequested : " + reservationRequested.toJson() + "\n\n");

        Reservation reservation = new Reservation();
        reservation.setUserId(reservationRequested.getId());
        reservation.setReserveStatus(reservationRequested.getReserveStataus());
        reservation.setUserName(reservationRequested.getUserName());
        reservation.setUserRedNumber(reservationRequested.getUserRedNumber());
        reservation.setReserveDate(reservationRequested.getReserveDate());
        
        reservationRepository.save(reservation);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCancelled_Cancel(@Payload ReservationCancelled reservationCancelled){

        if(!reservationCancelled.validate()) return;
        System.out.println("\n\n##### listener ReservationCancelled : " + reservationCancelled.toJson() + "\n\n");

        Reservation reservation = reservationRepository.findByUserId(reservationCancelled.getId());
        
        if (reservation != null) {
    		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		String dateStr = format.format(Calendar.getInstance().getTime());
        	
        	reservation.setReserveStatus("CANCEL");
            reservation.setCancelDate(dateStr);         
            reservationRepository.save(reservation);	
        }
    }



}