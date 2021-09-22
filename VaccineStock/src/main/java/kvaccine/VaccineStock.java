package kvaccine;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="stock_table")
public class VaccineStock {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private String vaccineType;
    
    private int vaccineCount;

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

	public int getVaccineCount() {
		return vaccineCount;
	}

	public void setVaccineCount(int vaccineCount) {
		this.vaccineCount = vaccineCount;
	}

}