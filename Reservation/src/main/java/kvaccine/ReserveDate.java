package kvaccine;

public class ReserveDate {
	
	private String date;
	
	private Integer count;	
	
	public ReserveDate(String date, Integer count) {
		this.date = date;
		this.count = count;
	}
	
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public Integer getCount() {
		return count;
	}
	
	public void setCount(Integer count) {
		this.count = count;
	}
	
}
