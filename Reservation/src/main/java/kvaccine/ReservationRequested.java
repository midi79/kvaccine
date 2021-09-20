package kvaccine;

public class ReservationRequested extends AbstractEvent {

    private Long id;
    private String userName;
    private String userRedNumber;
    private String reserveDate;
    private String reserveStataus;

    public ReservationRequested(){
        super();
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
    public String getReserveStataus() {
        return reserveStataus;
    }

    public void setReserveStataus(String reserveStataus) {
        this.reserveStataus = reserveStataus;
    }
}