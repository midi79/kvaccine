package kvaccine.external;

public class Reservation {

    private Long id;
    private String userName;
    private String userRegNumber;
    private String reserveDate;
    private String reserveStatus;
    private String cancelDate;
    private Long userId;

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
	public String getUserRegNumber() {
		return userRegNumber;
	}
	public void setUserRegNumber(String userRegNumber) {
		this.userRegNumber = userRegNumber;
	}

}
