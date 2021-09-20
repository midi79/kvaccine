package kvaccine;

public class ReservationReceived extends AbstractEvent {

    private Long id;

    public ReservationReceived(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}