package kvaccine;

public class ReservationCancelled extends AbstractEvent {

    private Long id;
    private Long userId;
    private String userName;
    private String userRedNumber;
    private String reserveStataus;
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
    public String getReserveStataus() {
        return reserveStataus;
    }

    public void setReserveStataus(String reserveStataus) {
        this.reserveStataus = reserveStataus;
    }
    public String getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(String cancelDate) {
        this.cancelDate = cancelDate;
    }
}