package kvaccine;

public class HospitalReservationReceived extends AbstractEvent {

    private Long id;
    private String vaccineType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public String getVaccineType() {
		return vaccineType;
	}

	public void setVaccineType(String vaccineType) {
		this.vaccineType = vaccineType;
	}
}