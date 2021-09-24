package kvaccine;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="dashboard_table")
public class Dashboard {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        
        private Long userId;
        private String userName;
        private String userRegNumber;
        private String reserveStatus;
        private String reserveDate;
        private String cancelDate;
        private String injectDate;
        private String vaccineType;

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

		public String getReserveStatus() {
			return reserveStatus;
		}

		public void setReserveStatus(String reserveStatus) {
			this.reserveStatus = reserveStatus;
		}

		public String getReserveDate() {
			return reserveDate;
		}

		public void setReserveDate(String reserveDate) {
			this.reserveDate = reserveDate;
		}

		public String getCancelDate() {
			return cancelDate;
		}

		public void setCancelDate(String cancelDate) {
			this.cancelDate = cancelDate;
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