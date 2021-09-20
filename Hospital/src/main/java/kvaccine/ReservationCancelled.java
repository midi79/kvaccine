package kvaccine;

public class ReservationCancelled extends AbstractEvent {

    private Long id;

    public ReservationCancelled(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}