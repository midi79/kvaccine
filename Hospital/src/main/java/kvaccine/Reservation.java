package kvaccine;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String hospitalName;
    private Long uesrId;
    private String userName;
    private String userRegNumber;
    private String reserveDate;
    private String injectDate;
    private String injectType;

    @PostPersist
    public void onPostPersist(){
        VaccineInjected vaccineInjected = new VaccineInjected();
        BeanUtils.copyProperties(this, vaccineInjected);
        vaccineInjected.publishAfterCommit();

        ReservationReceived reservationReceived = new ReservationReceived();
        BeanUtils.copyProperties(this, reservationReceived);
        reservationReceived.publishAfterCommit();

        ReservationCancelled reservationCancelled = new ReservationCancelled();
        BeanUtils.copyProperties(this, reservationCancelled);
        reservationCancelled.publishAfterCommit();

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }
    public Long getUesrId() {
        return uesrId;
    }

    public void setUesrId(Long uesrId) {
        this.uesrId = uesrId;
    }
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getUserRegNumber() {
        return userRegNumber;
    }

    public void setUserRegNumber(String userRegNumber) {
        this.userRegNumber = userRegNumber;
    }
    public String getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(String reserveDate) {
        this.reserveDate = reserveDate;
    }
    public String getInjectDate() {
        return injectDate;
    }

    public void setInjectDate(String injectDate) {
        this.injectDate = injectDate;
    }
    public String getInjectType() {
        return injectType;
    }

    public void setInjectType(String injectType) {
        this.injectType = injectType;
    }




}