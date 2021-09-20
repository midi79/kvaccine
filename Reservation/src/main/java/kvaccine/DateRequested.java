package kvaccine;

public class DateRequested extends AbstractEvent {

    private Long id;
    private String possibleDate;
    private Integer capacity;

    public DateRequested(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getPossibleDate() {
        return possibleDate;
    }

    public void setPossibleDate(String possibleDate) {
        this.possibleDate = possibleDate;
    }
    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}