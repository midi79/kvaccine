package kvaccine;

public class UserReservationCancelled extends AbstractEvent {

    private Long id;
    private String userName;
    private String userRegNumber;

    public UserReservationCancelled(){
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
    public String getUserRegNumber() {
        return userRegNumber;
    }

    public void setUserRegNumber(String userRegNumber) {
        this.userRegNumber = userRegNumber;
    }
}