package kvaccine;

public class VaccineInjected extends AbstractEvent {

    private Long id;
    private Long userId;
    private String injectDate;
    private String vaccineType;

    public VaccineInjected(){
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

	public String getInjectDate() {
		return injectDate;
	}

	public void setInjectDate(String injectDate) {
		this.injectDate = injectDate;
	}

	public String getVaccineType() {
		return vaccineType;
	}

	public void setVaccineType(String vaccineType) {
		this.vaccineType = vaccineType;
	}
    
    
    
}