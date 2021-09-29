package kvaccine;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="hospital_table")
public class Hospital {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String hospitalName;
    private Long userId;
    private String userName;
    private String userRegNumber;
    private String reserveDate;
    private String injectDate;
    private String vaccineType;
    private String reserveStatus;

    @PostPersist
    public void onPostPersist() {
    	if (this.reserveStatus.equals("RESERVE")) {
            HospitalReservationReceived hospitalReservationReceived = new HospitalReservationReceived();
            BeanUtils.copyProperties(this, hospitalReservationReceived);
            hospitalReservationReceived.publishAfterCommit();
            
            // 병원 예약 완료 카톡 메세지 발송
            
    	}
    }
    
    @PostUpdate
    public void onPostUpdate() {
    	if (this.reserveStatus.equals("INJECT")) {
            VaccineInjected vaccineInjected = new VaccineInjected();
            BeanUtils.copyProperties(this, vaccineInjected);
            vaccineInjected.publishAfterCommit();    		
    	} else if (this.reserveStatus.equals("CANCEL")) {
            HospitalReservationCancelled hospitalReservationCancelled = new HospitalReservationCancelled();
            BeanUtils.copyProperties(this, hospitalReservationCancelled);
            hospitalReservationCancelled.publishAfterCommit();
            
            // 병원 예약 완료 카톡 메세지 발송
    	}
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

    public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
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

	public String getVaccineType() {
		return vaccineType;
	}

	public void setVaccineType(String vaccineType) {
		this.vaccineType = vaccineType;
	}

	public String getReserveStatus() {
		return reserveStatus;
	}

	public void setReserveStatus(String reserveStatus) {
		this.reserveStatus = reserveStatus;
	}




}