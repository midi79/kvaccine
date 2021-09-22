package kvaccine;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long userId;
    private String userName;
    private String userRedNumber;
    private String reserveDate;
    private String reserveStatus;
    private String cancelDate;


    @PostPersist
    public void onPostPersist(){
    	
    	if (this.reserveStatus.equals("RESERVE")) {
            ReservationRequested reservationRequested = new ReservationRequested();
            BeanUtils.copyProperties(this, reservationRequested);
            reservationRequested.publishAfterCommit();    		
    	} else if (this.reserveStatus.equals("CANCEL")) {
            ReservationCancelled reservationCancelled = new ReservationCancelled();
            BeanUtils.copyProperties(this, reservationCancelled);
            reservationCancelled.publishAfterCommit();    		
    	}
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getUserRedNumber() {
        return userRedNumber;
    }

    public void setUserRedNumber(String userRedNumber) {
        this.userRedNumber = userRedNumber;
    }
    public String getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(String reserveDate) {
        this.reserveDate = reserveDate;
    }
    public String getReserveStatus() {
        return reserveStatus;
    }

    public void setReserveStatus(String reserveStatus) {
        this.reserveStatus = reserveStatus;
    }
    public String getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(String cancelDate) {
        this.cancelDate = cancelDate;
    }
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }




}