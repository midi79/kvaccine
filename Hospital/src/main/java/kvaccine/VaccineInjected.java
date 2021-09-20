package kvaccine;

public class VaccineInjected extends AbstractEvent {

    private Long id;

    public VaccineInjected(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}