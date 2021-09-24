package kvaccine;

public class ReservationRequested extends AbstractEvent {

    private Long id;
    private Long userId;
    private String userName;
    private String userRegNumber;
    private String reserveDate;
    private String reserveStatus;

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

	public String getReserveStatus() {
		return reserveStatus;
	}

	public void setReserveStatus(String reserveStatus) {
		this.reserveStatus = reserveStatus;
	}

	public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(String reserveDate) {
        this.reserveDate = reserveDate;
    }

	public String getUserRegNumber() {
		return userRegNumber;
	}

	public void setUserRegNumber(String userRegNumber) {
		this.userRegNumber = userRegNumber;
	}

}