package kvaccine;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="user_table")
public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String userName;
    private String userRegNumber;
    private String reserveStatus;
    private String reserveDate;
    private String modifyDate;
    private String injectDate;
    private String vaccineType;

    @PostPersist
    public void onPostPersist() {
    	if("RESERVE".equals(this.reserveStatus)) {
	        UserReservationRequested reservationRequested = new UserReservationRequested();
	        BeanUtils.copyProperties(this, reservationRequested);
	        reservationRequested.publishAfterCommit();
    	}     	
    }
        
    @PostUpdate
    public void onUpdatePersist() {
    	if ("CANCEL".equals(this.reserveStatus)) {
    		UserReservationCancelled reservationCancelled = new UserReservationCancelled();
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
    public String getUserRegNumber() {
        return userRegNumber;
    }

    public void setUserRegNumber(String userRegNumber) {
        this.userRegNumber = userRegNumber;
    }
    public String getReserveStatus() {
        return reserveStatus;
    }

    public void setReserveStatus(String reserveStatus) {
        this.reserveStatus = reserveStatus;
    }
    public String getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(String reserveDate) {
        this.reserveDate = reserveDate;
    }
    public String getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(String modifyDate) {
        this.modifyDate = modifyDate;
    }

	public String getInjectDate() {
		return injectDate;
	}

	public void setInjectDate(String injectDate) {
		this.injectDate = injectDate;
	}

	public String getVaccineType() {
		return vaccineType;
	}

	public void setVaccineType(String vaccineType) {
		this.vaccineType = vaccineType;
	}

}