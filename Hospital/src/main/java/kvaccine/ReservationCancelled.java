package kvaccine;

public class ReservationCancelled extends AbstractEvent {

    private Long id;
    private Long userId;
    private String vaccineType;
    private String userName;
    private String userRegNumber;
    private String reserveStatus;
    private String cancelDate;

    public ReservationCancelled(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getVaccineType() {
		return vaccineType;
	}

	public void setVaccineType(String vaccineType) {
		this.vaccineType = vaccineType;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCancelDate() {
		return cancelDate;
	}

	public void setCancelDate(String cancelDate) {
		this.cancelDate = cancelDate;
	}

	public String getReserveStatus() {
		return reserveStatus;
	}

	public void setReserveStatus(String reserveStatus) {
		this.reserveStatus = reserveStatus;
	}

	public String getUserRegNumber() {
		return userRegNumber;
	}

	public void setUserRegNumber(String userRegNumber) {
		this.userRegNumber = userRegNumber;
	}
    
	
    
}