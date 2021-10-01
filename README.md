![covid-19-vaccine-header](https://user-images.githubusercontent.com/19512435/134809990-fdfeea00-0e53-45a3-9888-263f815c692d.jpeg)

# 백신 예약 시스템 

백신 예약 및 접종, 취소 기능을 구현한 Microservice

# Table of contents

- [K-Vaccine]
  - [서비스 시나리오]
  - [체크포인트]
  - [분석/설계]
  - [구현:]
    - [DDD 의 적용]
    - [폴리글랏 퍼시스턴스]
    - [폴리글랏 프로그래밍]
    - [동기식 호출 과 Fallback 처리]
    - [비동기식 호출 과 Eventual Consistency]
    - [보상 패턴]
  - [운영](#운영)
    - [CI/CD 설정]
    - [동기식 호출 / 서킷 브레이킹 / 장애격리]
    - [오토스케일 아웃]
    - [Self-Healing]
    - [무정지 재배포]
    - [ConfigMap]


# 서비스 시나리오

백신 예약 기능 구현하기 

기능적 요구사항
1. 사용자가 이름과 주민번호를 입력하여 백신 예약 가능 날짜를 조회한다.
2. 사용자는 백신 예약 가능 날짜를 선택하여 예약한다.
3. 예약이 성공하면 병원에 예약 현황이 통보된다.
4. 병원에서는 사용자가 맞을 백신을 랜덤 선택한 후 백신 창고에 예약 정보를 전달한다.
5. 백신 창고는 해당 백신 수량을 차감한다.
6. 사용자는 해당 날짜에 병원에 가서 접종을 받는다.
7. 접종을 완료하면 사용자 정보가 접종 완료로 업데이트 된다.
8. 사용자는 백신 예약을 취소한다.
9. 취소된 예약은 병원에 통보된다.
10. 병원은 취소된 예약의 백신 정보를 백신 창고로 전달한다.
11. 백신 창고는 해당 백신의 수량을 복원한다.

비기능적 요구사항
1. 트랜잭션
    1. 백신 접종 가능 날짜는 실시간 조회가 되어야 한다 - Sync
1. 장애격리
    1. 병원의 기능이 동작하지 않아도 예약은 받아야 한다. - Event Driven
    1. 백신 접종 가능 날짜 조회가 폭주하면 잠시 후에 하도록 유도해야 한다. - Circuit Break
1. 성능
    1. 사용자의 예약 현황 및 상태를 프론트엔드에서 조회할 수 있어야 한다. - CQRS
    1. 백신 예약의 상태가 바뀔 때마다 카톡 등으로 알림을 줄 수 있어야 한다 - Event driven
    

# 체크포인트

- 분석 설계


  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?

- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
  - 무정지 운영 CI/CD
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 



# 분석/설계


## AS-IS 조직 (Horizontally-Aligned)

<img width="993" alt="2021-09-26 오후 10 50 33" src="https://user-images.githubusercontent.com/19512435/134810764-5616455b-7595-49cc-b432-b06a59be22bb.png">

## TO-BE 조직 (Vertically-Aligned)

<img width="1061" alt="2021-09-26 오후 11 13 29" src="https://user-images.githubusercontent.com/19512435/134811479-f62a4d5e-0aa2-4f93-aa98-da761cd24a19.png">


## Event Storming 결과

* MSAEz 로 모델링한 이벤트스토밍 결과: https://labs.msaez.io/#/storming/zm7538qsNkhoDMQ3F0AUMpn1wHS2/55846bc4b600d1b3e4bad09a58d4ca28


### 이벤트 도출

<img width="1302" alt="2021-09-26 오후 11 24 45" src="https://user-images.githubusercontent.com/19512435/134811932-5e425738-9f33-4f0d-8198-563c7a95644d.png">

### 부적격 이벤트 탈락

<img width="1302" alt="2021-09-26 오후 11 24 54" src="https://user-images.githubusercontent.com/19512435/134811934-32e163f3-9aa6-404b-9976-3d66783d8e38.png">

- 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
- 날짜가 조회됨 : UI 의 이벤트이지, 업무적인 의미의 이벤트가 아니라서 제외
- 예약시 > 병원이 선택됨 : 구현 범위 밖이라 제외, 병원은 단일 관리
- 예약시 > 예약이 거부됨 : 예약 거부의 경우 예약 가능 날짜에서 이미 필터링되는 걸로 가정함
- Vaccine Stock > 백신이 출고됨 : 해당 이벤트 수신 모듈이 없어서 제외함, Vaccine Stock은 백신 입출고 이력만 관리
- Vaccine Stock > 출고가 취소됨 : 해당 이벤트 수신 모듈이 없어서 제외함, Vaccine Stock은 백신 입출고 이력만 관리

### 액터, 커맨드 부착하여 읽기 좋게

<img width="1251" alt="2021-09-26 오후 11 38 03" src="https://user-images.githubusercontent.com/19512435/134812401-de3afcd3-37bb-486a-aa1d-27e687671468.png">

### 어그리게잇으로 묶기

<img width="1477" alt="2021-09-26 오후 11 47 26" src="https://user-images.githubusercontent.com/19512435/134812703-057d7cce-6b8f-4321-bace-a6354fb1f666.png">

- User의 날짜 조회, 예약과 취소, Reservation의 예약 요청, 예약 취소, Hospital의 접종, 예약, 예약 취소, VaccineStock의 백신 증가, 백신 감소 command와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 그들 끼리 묶어줌

### 바운디드 컨텍스트로 묶기

<img width="1588" alt="2021-09-26 오후 11 53 26" src="https://user-images.githubusercontent.com/19512435/134813019-85d8580b-ab8e-40da-8caa-ab2989c538ea.png">

    - 도메인 서열 분리 
      - Core Domain:  User, Reservation : 없어서는 안될 핵심 서비스이며, 연견 Up-time SLA 수준을 99.999% 목표, 배포주기는 의 경우 1주일 1회 미만
      - Supporting Domain: Hospital : 경쟁력을 내기위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 1주일 1회 이상을 기준으로 함.
      - General Domain: VaccineStock : 백신 재고 관리 시스템으로 ERP 솔루션 등 3rd Party 외부 서비스를 사용하는 것이 경쟁력이 높음

### 폴리시 부착 (괄호는 수행주체, 전체 연계가 초기에 드러남)

<img width="1838" alt="2021-09-27 오후 11 20 57" src="https://user-images.githubusercontent.com/19512435/134927268-300e7d7c-ca92-4f7f-8964-c478eb2f2261.png">

### 폴리시의 이동과 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)

<img width="1813" alt="2021-09-27 오후 11 36 29" src="https://user-images.githubusercontent.com/19512435/134930094-e396970f-4a31-4bfe-9aab-4a2f0251e385.png">

### 완성된 1차 모형

<img width="1530" alt="2021-09-29 오후 9 30 14" src="https://user-images.githubusercontent.com/19512435/135268625-6841e3d5-78c5-45a0-9957-451a05543a28.png">

    - View Model 추가

### 1차 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

<img width="1530" alt="2021-09-29 오후 9 34 08" src="https://user-images.githubusercontent.com/19512435/135269154-40a33b0a-2a41-495a-a566-46b5241c4b4b.png">

    - 사용자가 이름과 주민번호를 입력하여 백신 예약 가능 날짜를 조회한다 (ok)

<img width="1530" alt="2021-09-29 오후 9 49 10" src="https://user-images.githubusercontent.com/19512435/135271498-388cab2b-2ebe-4281-ba3b-8f149def9797.png">

    - 사용자는 백신 예약 가능 날짜를 선택하여 예약한다 (ok)
    - 예약이 성공하면 병원에 예약 현황이 통보된다 (ok)
    - 병원에서는 사용자가 맞을 백신을 랜덤 선택한 후 백신 창고에 예약 정보를 전달한다 (ok)
    - 백신 창고는 해당 백신 수량을 차감한다 (ok)

<img width="1530" alt="2021-09-29 오후 9 51 02" src="https://user-images.githubusercontent.com/19512435/135271779-58b0db72-dbf4-4ff3-81d0-090f1e8066e5.png">

    - 사용자는 해당 날짜에 병원에 가서 접종을 받는다 (ok)
    - 접종을 완료하면 사용자 정보가 접종 완료로 업데이트 된다 (ok)

<img width="1530" alt="2021-09-29 오후 9 53 04" src="https://user-images.githubusercontent.com/19512435/135272111-22772293-ab43-432a-8416-0ac6eb6726f4.png">

    - 사용자는 백신 예약을 취소한다 (ok)
    - 취소된 예약은 병원에 통보된다 (ok)
    - 병원은 취소된 예약의 백신 정보를 백신 창고로 전달한다 (ok)
    - 백신 창고는 해당 백신의 수량을 복원한다 (ok)



### 비기능 요구사항에 대한 검증

<img width="1530" alt="2021-09-29 오후 9 54 00" src="https://user-images.githubusercontent.com/19512435/135272263-4738fdb8-9bf2-4853-b0b7-9d7f962f1484.png">

    - 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
        - 접종 가능 날짜 조회 :  ACID 트랜잭션 적용. 접종 가능 날짜 조회의 경우 실시간으로 조회가 필요하여 Request-Response 방식 처리
        - 예약시 Reservation 연결 :  User 에서 Reservation 마이크로서비스로 예약 요청이 전달되는 과정에 있어서 Reservation 마이크로서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함
        - 나머지 모든 inter-microservice 트랜잭션: Hospital 예약, Stock 처리 등 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함



## 헥사고날 아키텍처 다이어그램 도출
    
<img width="1534" alt="2021-09-29 오후 9 57 56" src="https://user-images.githubusercontent.com/19512435/135272859-457274d9-2255-4759-8163-e82c37243ff8.png">


    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8080 ~ 8085 이다)

```
cd gateway
mvn spring-boot:run

cd User
mvn spring-boot:run 

cd Reservation
mvn spring-boot:run  

cd Hospital
mvn spring-boot:run

cd VaccineStock
mvn spring-boot:run

cd Dashboard
mvn spring-boot:run
```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 Reservation 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어(유비쿼터스 랭귀지)를 영어로 번역하여 사용하였다. 

```

package kvaccine;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long userId;
    private String userName;
    private String userRegNumber;
    private String reserveDate;
    private String reserveStatus;
    private String cancelDate;

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

    public String getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(String reserveDate) {
        this.reserveDate = reserveDate;
    }
    public String getReserveStatus() {
        return reserveStatus;
    }

    public void setReserveStatus(String reserveStatus) {
        this.reserveStatus = reserveStatus;
    }
    public String getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(String cancelDate) {
        this.cancelDate = cancelDate;
    }
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserRegNumber() {
	return userRegNumber;
    }

    public void setUserRegNumber(String userRegNumber) {
	this.userRegNumber = userRegNumber;
    }

}

```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```

package kvaccine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="reservations", path="reservations")
public interface ReservationRepository extends JpaRepository<Reservation, Long>{
	
	Reservation findByUserId(Long userId);

}


```
- 적용 후 REST API 의 테스트 (PostMan 기준)
```

# 예방 접종 가능 날짜 조회
POST http://localhost:8080/user/date
{
    "userName": "Taeyeon Kim",
    "userRegNumber": "890309-2766876"
}

# 백신 예약
POST http://localhost:8080/user/reserve
{
    "userName": "Taeyeon Kim",
    "userRegNumber": "890309-2766876",
    "reserveDate":"2021-10-05"
}

# 백신 접종
POST http://localhost:8080/hospital/inject
{
    "userId": 1
}

```

## 폴리글랏 퍼시스턴스

전체 서비스의 경우 빠른 속도와 개발 생산성을 극대화하기 위해 Spring Boot에서 기본적으로 제공하는 In-Memory DB인 H2 DB를 사용하였다.

```
# User.java

package kvaccine;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="user_table")
public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String userName;
    private String userRegNumber;
    private String reserveStatus;
    private String reserveDate;
    private String modifyDate;
    private String injectDate;
    private String vaccineType;
    

# application.yml

  h2:
    console:
      enabled: true
      path: /h2-console
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb
    username: sa
    password:         

    
```

## 폴리글랏 프로그래밍

Dashboard 서비스의 경우 다른 서비스와 다르게 HSQL DB를 사용하였다.

```

# application.yml

  datasource:
    driver-class-name: org.hsqldb.jdbc.JDBCDriver
    url: jdbc:hsqldb:mem:testdb
    username: sa
    password:

```


## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 예약 날짜 조회(User)-> 조회(Reservation) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 결제 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```

# ReservationService.java
package kvaccine.external;

@FeignClient(name="Reservation", url="${api.url.reservation}", fallback = ReservationServiceImpl.class)
public interface ReservationService {
	
    @RequestMapping(method= RequestMethod.GET, path="/date")
    public ArrayList<ReserveDate> dateRequest(@RequestBody Reservation reservation);

}

```

- 예약 직후(@PostPersist) 병원 예약을 요청하도록 처리
```

# Reservation.java (Entity)

    @PostPersist
    public void onPostPersist() {    	
    	if (this.reserveStatus.equals("RESERVE")) {
            ReservationRequested reservationRequested = new ReservationRequested();
            BeanUtils.copyProperties(this, reservationRequested);
            reservationRequested.publishAfterCommit();    		
    	} 
    }

```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 예약 시스템이 장애가 나면 날짜 조회도 불가하다는 것을 확인:

```
# Reservation 서비스를 잠시 내려놓음 (ctrl+c)

# 접종 가능 날짜 조회
POST http://localhost:8080/user/date          #Fail
{
    "userName": "Taeyeon Kim",
    "userRegNumber": "890309-2766876"
}

POST http://localhost:8080/user/date          #Fail
{
    "userName": "Jieun Lee",
    "userRegNumber": "930619-2352628"
}


#예약서비스 재기동
cd Reservation
mvn spring-boot:run

# 접종 가능 날짜 조회
POST http://localhost:8080/user/date          #Success
{
    "userName": "Taeyeon Kim",
    "userRegNumber": "890309-2766876"
}

POST http://localhost:8080/user/date          #Success
{
    "userName": "Jieun Lee",
    "userRegNumber": "930619-2352628"
}

```

- 또한 과도한 가능 날짜 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)




## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


예약이 이루어진 후에 Hospital 서비스로 이를 알려주는 행위는 동기식이 아니라 비동기식으로 처리하여 Hospital 서비스의 처리를 위하여 예약이 블로킹 되지 않도록 처리한다.
 
- 이를 위하여 예약 이력을 저장 후에 곧바로 예약 완료가 되었다는 도메인 이벤트를 카프카로 송출한다. (Publish)
  이때 다른 저장 로직에 의해서 해당 이벤트가 발송되는 것을 방지하기 위해 Status 체크하는 로직을 추가했다.
 
```

package kvaccine;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="reservation_table")
public class Reservation {

    @PostPersist
    public void onPostPersist() {    	
    	if (this.reserveStatus.equals("RESERVE")) {
            ReservationRequested reservationRequested = new ReservationRequested();
            BeanUtils.copyProperties(this, reservationRequested);
            reservationRequested.publishAfterCommit();    		
    	} 
    }

```
- Hospital 서비스에서는 결재 완료 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
package kvaccine;

...

@Service
public class PolicyHandler {
	
    ...
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationRequested_Reserve(@Payload ReservationRequested reservationRequested){
    	
    	if (vaccineType != null) {
    		vaccines = vaccineType.split(",");
    	} 
    	
        ArrayList<String> vaccineTypeList = new ArrayList<>(Arrays.asList(vaccines));


        if(!reservationRequested.validate()) return;
        System.out.println("\n\n##### listener Hospital ReservationRequested : " + reservationRequested.toJson() + "\n\n");
       
        Hospital hospital = new Hospital();
        hospital.setHospitalName("Korean Hospital");
        hospital.setUserId(reservationRequested.getUserId());
        hospital.setUserName(reservationRequested.getUserName());
        hospital.setUserRegNumber(reservationRequested.getUserRegNumber());
        hospital.setReserveDate(reservationRequested.getReserveDate());
        hospital.setReserveStatus(reservationRequested.getReserveStatus());
        
        String vaccineType = vaccineTypeList.get(((int)(Math.random() * 10)) % 4);
        hospital.setVaccineType(vaccineType);
        
        hospitalRepository.save(hospital);

    }

```
실제 구현을 하자면, 병원 예약이 완료된 이후에 카톡 알림을 통해 예약이 완료되었다는 외부 이벤트를 보내고, 사용자는 예약 상태를 Dashboard를 통해 확인할 수 있다.
  
```

# 병원 예약 완료시 카톡 메세지 전송

    @PostPersist
    public void onPostPersist() {
    	if (this.reserveStatus.equals("RESERVE")) {
            HospitalReservationReceived hospitalReservationReceived = new HospitalReservationReceived();
            BeanUtils.copyProperties(this, hospitalReservationReceived);
            hospitalReservationReceived.publishAfterCommit();
            
            // 병원 예약 완료 카톡 메세지 발송
            
    	}
    }

# 예약 현황을 Dashboard에서 확인

GET http://localhost:8080/dashboard/list

```

Hospital 서비스는 User, Reservation과 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, Hospital 서비스를 유지보수로 인해 잠시 내려간 상태라도 예약을 받는데 문제가 없다:
```
# Hospital 서비스 를 잠시 내려놓음 (ctrl+c)

# 백신 예약
POST http://localhost:8080/user/reserve       #Success
{
    "userName": "Taeyeon Kim",
    "userRegNumber": "890309-2766876",
    "reserveDate":"2021-10-05"
}

POST http://localhost:8080/user/reserve       #Success
{
    "userName": "Jieun Lee",
    "userRegNumber": "930619-2377821",
    "reserveDate":"2021-10-05"
}

# 예약 상태 확인
GET http://localhost:8080/dashboard/list     # 예약상태 조회 가능

# Hospital 서비스 기동
cd Hospital
mvn spring-boot:run

# 예약 상태 확인
GET http://localhost:8080/dashboard/list     # 예약상태 조회 가능 (데이터 변경 없음)

```

## Correlation (보상패턴) 구현

예약 완료시 백신의 재고 갯수를 차감하고, 예약 취소시 백신의 갯수를 원복해준다. (구현 난이도를 낮추기 위해 단순 -, + 이력 처리)

```
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationReceived_Reserve(@Payload HospitalReservationReceived reservationReceived) {

        if(!reservationReceived.validate()) return;
        System.out.println("\n\n##### listener Reserve : " + reservationReceived.toJson() + "\n\n");

        VaccineStock stock = new VaccineStock();
        stock.setVaccineType(reservationReceived.getVaccineType());
        stock.setVaccineCount(-1);
        
        vaccineStockRepository.save(stock);

    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCancelled_Cancel(@Payload HospitalReservationCancelled reservationCancelled){

        if(!reservationCancelled.validate()) return;
        System.out.println("\n\n##### listener Cancel : " + reservationCancelled.toJson() + "\n\n");

        VaccineStock stock = new VaccineStock();
        stock.setVaccineType(reservationCancelled.getVaccineType());
        stock.setVaccineCount(1);
        vaccineStockRepository.save(stock);

    }

```

## CQRS 패턴 구현

예약과 예약 취소, 백신 접종 현황을 Dashboard로 구현하여 조회할 수 있게 제공
현재 예약 현황과 백신별 예약 리스트를 조회할 수 있는 End point 두개 구현

```

	// 전체 리스트 가져오기 
	@GetMapping("/list")
	public ResponseEntity<List<Dashboard>> getDashboardList() {

		List<Dashboard> dashboardList = dashboardRepository.findAll();

		return ResponseEntity.ok(dashboardList);
	}

	// 백신 타입에 따라 리스트 가져오기 
	@GetMapping("/list/{vaccineType}")
	public ResponseEntity<List<Dashboard>> getDashboardListByUserId(@PathVariable String vaccineType) {

		List<Dashboard> dashboardList = dashboardRepository.findAllByVaccineType(vaccineType);

		return ResponseEntity.ok(dashboardList);
	}	

```


예약, 예약 취소, 백신 접종 대한 Event Listener 구현

```
PolicyHandler.java


    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationRequested_Reserve(@Payload ReservationRequested reservationRequested) {

        if(!reservationRequested.validate()) return;
        System.out.println("\n\n##### listener Dashboard ReservationRequested : " + reservationRequested.toJson() + "\n\n");
       
        Dashboard dashboard = new Dashboard();
        dashboard.setUserId(reservationRequested.getUserId());
        dashboard.setUserName(reservationRequested.getUserName());
        dashboard.setUserRegNumber(reservationRequested.getUserRegNumber());
        dashboard.setReserveDate(reservationRequested.getReserveDate());        
        dashboard.setReserveStatus(reservationRequested.getReserveStatus());        
        
        dashboardRepository.save(dashboard);

    }
    
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCancelled_Cancel(@Payload ReservationCancelled reservationCancelled) {

        if(!reservationCancelled.validate()) return;
        System.out.println("\n\n##### listener Dashboard ReservationCancelled : " + reservationCancelled.toJson() + "\n\n");

        Dashboard dashboard = dashboardRepository.findByUserId(reservationCancelled.getUserId());
        dashboard.setReserveStatus(reservationCancelled.getReserveStatus());
        dashboard.setCancelDate(reservationCancelled.getCancelDate());

        dashboardRepository.save(dashboard);

    }
    
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverVaccineInjected_UpdateDate(@Payload VaccineInjected vaccineInjected){

        if(!vaccineInjected.validate()) return;
        System.out.println("\n\n##### listener Dashboard VaccineInjected_UpdateDate : " + vaccineInjected.toJson() + "\n\n");

        Dashboard dashboard = dashboardRepository.findByUserId(vaccineInjected.getUserId());
        dashboard.setReserveStatus("INJECT");
        dashboard.setInjectDate(vaccineInjected.getInjectDate());
        dashboard.setVaccineType(vaccineInjected.getVaccineType());

        dashboardRepository.save(dashboard);
    }


```


# 운영

## CI/CD 설정
각 구현체들은 각자의 AWS의 ECR 에 구성되었고, 사용한 CI/CD 플랫폼은 AWS-CodeBuild를 사용하였으며, pipeline build script 는 각 프로젝트 폴더 이하에 buildspec-kubectl.yaml 에 포함되었다.

- 레포지터리 생성 확인

![2021-09-30 오전 10 45 33](https://user-images.githubusercontent.com/19512435/135386077-97149d4d-e42e-4b46-a5c1-c4ca84cd855e.png)

<br/>

- 생성 할 CodeBuild
  - user15-gateway
  - user15-user
  - user15-reservation
  - user15-hospital
  - user15-stock
  - user15-dashboar
<br/>


- github의 각 서비스의 서브 폴더에 buildspec-kubect.yaml 위치.

<img width="1350" alt="2021-09-30 오후 1 19 20" src="https://user-images.githubusercontent.com/19512435/135386959-f4ab2f21-4c58-490c-bc65-9d5780a62f2c.png">
<img width="1350" alt="2021-09-30 오후 1 19 51" src="https://user-images.githubusercontent.com/19512435/135386963-5a5670f3-e6a2-4b6c-b678-68f2ae717233.png">
<img width="1350" alt="2021-09-30 오후 1 22 15" src="https://user-images.githubusercontent.com/19512435/135386966-91c9e472-4fe5-4b93-90ed-417f828a3ccb.png">
<img width="1350" alt="2021-09-30 오후 1 21 30" src="https://user-images.githubusercontent.com/19512435/135386968-4dc4a068-e8d5-46f4-adc3-d6e633c73bb3.png">
<img width="1350" alt="2021-09-30 오후 1 21 48" src="https://user-images.githubusercontent.com/19512435/135386969-7a3f4bd9-1d69-43eb-93aa-f976ed899c2a.png">
<img width="1350" alt="2021-09-30 오후 1 22 43" src="https://user-images.githubusercontent.com/19512435/135386973-2c04e781-f162-4920-ac5c-684368d49438.png">


- 연결된 github에 Commit 진행시 6개의 서비스들 build 진행 여부 및 성공 확인 

<img width="1364" alt="2021-09-30 오후 12 10 32" src="https://user-images.githubusercontent.com/19512435/135387075-6ef51071-f39b-4e5d-beaa-ab732e0d40ef.png">


- 배포된 6개의 Service  확인
```
midi79@Cheolkyuui-iMac ~ % kubectl get all
NAME                               READY   STATUS    RESTARTS   AGE
pod/dashboard-5dcc5bc45b-bjmmp     1/1     Running   0          97m
pod/gateway-6dcf665bd6-s6ktt       1/1     Running   0          120m
pod/hospital-787b869f7f-d22qc      1/1     Running   0          93m
pod/reservation-77495748fd-qmcvc   1/1     Running   0          107m
pod/stock-dc848464b-fz4kw          1/1     Running   0          104m
pod/user-78fdf576f9-xvft9          1/1     Running   0          111m

```


## API Gateway 설정

- 단일 진입점을 구성하기 위해 Spring Cloud Gateway 적용

```
spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: User
          uri: http://user:8080
          predicates:
            - Path=/user/** 
        - id: Dashboard
          uri: http://dashboard:8080
          predicates:
            - Path= /dashboard/**
        - id: Reservation
          uri: http://reservation:8080
          predicates:
            - Path=/reservation/** 
        - id: Hospital
          uri: http://hospital:8080
          predicates:
            - Path=/hospital/** 
        - id: VaccineStock
          uri: http://stock:8080
          predicates:
            - Path=/stock/** 
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true
```

- Gateway는 AWS 상의 Load Balancer와 연결하여 외부로 노출함

```
  cat <<EOF | kubectl apply -f -
  apiVersion: v1
  kind: Service
   metadata:
   name: $_POD_NAME
    labels:
      app: $_POD_NAME
  spec:
   ports:
   - port: 8080
      targetPort: 8080
     selector:
      app: $_POD_NAME
     type:
      LoadBalancer      

```

<img width="1350" alt="2021-09-30 오후 1 28 55" src="https://user-images.githubusercontent.com/19512435/135387444-d1bc9bcb-5268-4290-a99e-2bfb410dfd5d.png">


## 동기식 호출 / 서킷 브레이킹 / 장애격리
- 시나리오
  1. User -> Reservation으로 예약 가능 날짜 요청은 RESTful Request/Response 로 연동하여 구현 함. 요청이 과도할 경우 CB가 발생하고 fallback으로 결재 지연 메새지를 보여줌으로 장애 격리 시킴.
  2. circuit break의 timeout은 500mm 설정. 
  3. Reservation 서비스에 임의의 부하 처리.
  4. 부하테스터(seige) 를 통한 circuit break 확인. 
    - 예약 가능 날짜 조회 지연 메세지 확인.
    - seige의 Availability 100% 확인.

<br/>
    
- 서킷 브레이킹 프레임워크의 선택
  - Spring FeignClient + Hystrix 옵션을 사용하여 구현함

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히고 결재 로직 대신 fallback으로 결재 지연 메세지 보여줌으로 장애 격리.
```
# application.yml

feign:
  hystrix:
    enabled: true

hystrix:
  command:
    # 전역설정 timeout이 810ms 가 넘으면 CB 처리.
    default:
      execution.isolation.thread.timeoutInMilliseconds: 810
```
- Reservation 서비스에 임의 부하 처리 - 600 밀리에서 증감 220 밀리 정도 왔다갔다 하게 아래 코드 추가
```
# ReservationController.java

try {
    Thread.currentThread().sleep((long) (600 + Math.random() * 220));
} catch (InterruptedException e) {
    e.printStackTrace();
}
```
- User 서비스에 FeignClient fallback 코드 추가.
```
# ReservationService.java

@FeignClient(name="Reservation", url="${api.url.reservation}", fallback = ReservationServiceImpl.class)
```

```
# ReservationServiceImpl.java

@Service
public class ReservationServiceImpl implements ReservationService {

	public ArrayList<ReserveDate> dateRequest(Reservation reservation) {
		
		System.out.println("################## 조회 폭주로 서비스 지연 중입니다.#######################");
		System.out.println("################## 조회 폭주로 서비스 지연 중입니다.#######################");
		System.out.println("################## 조회 폭주로 서비스 지연 중입니다.#######################");
	
		return null;
	}
}

```

- 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
  - 동시사용자 10명, 30초 동안 실시
  - User 서비스의 log 확인.

<img width="706" alt="2021-09-30 오후 2 14 51" src="https://user-images.githubusercontent.com/19512435/135391609-db8b8637-a6f1-4081-af0c-db10f3f26779.png">

- 예약 서비스에 지연이 발생하는 경우 지연 메세지를 보여주고 장애에 분리되어 Avalablity가 100% 이다. 

- User 서비스의 log에 아래에서 조회 지연 메세지를 확인한다.

<img width="706" alt="2021-09-30 오후 2 15 04" src="https://user-images.githubusercontent.com/19512435/135391601-b84920ec-5beb-42af-a3c8-f121e3caa37c.png">


- 시스템은 죽지 않고 지속적으로 과도한 부하시 CB 에 의하여 회로가 닫히고 결재 지연중 메세지를 보여주며 고객을 장애로 부터 격리시킴.



## 오토스케일 아웃
- User 서비스에 대해  CPU Load 50%를 넘어서면 Replica를 5까지 늘려준다. 
  - buildspec-kubectl.yaml
```
          cat <<EOF | kubectl apply -f -
          apiVersion: autoscaling/v2beta2
          kind: HorizontalPodAutoscaler
          metadata:
            name: user-hpa
          spec:
            scaleTargetRef:
              apiVersion: apps/v1
              kind: Deployment
              name: $_POD_NAME
            minReplicas: 1
            maxReplicas: 10
            metrics:
            - type: Resource
              resource:
                name: cpu
                target:
                  type: Utilization
                  averageUtilization: 50
          EOF
```

- User서비스에 대한 CPU Resouce를 1000m으로 제한 한다.
  - buildspec-kubectl.yaml
```
                    resources:
                      limits:
                        cpu: 1000m
                        memory: 500Mi
                      requests:
                        cpu: 500m
                        memory: 300Mi
```

- Siege를 설치하고 해당 컨테이너로 접속한다.
```
> kubectl create deploy siege-pvc --image=ghcr.io/acmexii/siege-nginx:latest
> kubectl exec -it pod/siege-75d5587bf6-qblsz -- /bin/bash
```

- User 서비스에 워크로드를 동시 사용자 100명 60초 동안 진행한다.
```
siege -v c100 -t60S --content-type "application/json" 'http://user:8080/user/date POST {"userName":"Lee","userRegNumber":"930619-2345678"}'

```
- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다 : 각각의 Terminal에 
- 어느정도 시간이 흐른 후 (약 30초) 스케일 아웃이 벌어지는 것을 확인할 수 있다.
  
<img width="706" alt="2021-09-30 오후 2 34 50" src="https://user-images.githubusercontent.com/19512435/135393511-90b2e890-3b01-4fd9-b450-ee61d89b09a6.png">


```	

midi79@Cheolkyuui-iMac ~ % kubectl get hpa
NAME       REFERENCE         TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
user-hpa   Deployment/user   2%/50%    1         5         5          3h4m

```

	
## Self Healing
### ◆ Liveness- HTTP Probe
- 시나리오
  1. User 서비스의 Liveness 설정을 확인힌다. 
  2. User 서비스의 Liveness Probe는 actuator의 health 상태 확인을 설정되어 있어 actuator/health 확인.
  3. pod의 상태 모니터링
  4. User 서비스의 Liveness Probe인 actuator를 down 시켜 User 서비스가 termination 되고 restart 되는 self healing을 확인한다. 
  5. User 서비스의 describe를 확인하여 Restart가 되는 부분을 확인한다.

<br/>

- User 서비스의 Liveness probe 설정 확인
```
kubectl get deploy user -o yaml

                  :
        livenessProbe:
          failureThreshold: 5
          httpGet:
            path: /actuator/health
            port: 8080
            scheme: HTTP
          initialDelaySeconds: 120
          periodSeconds: 5
          successThreshold: 1
          timeoutSeconds: 2
                  :
```

- Httpie를 사용하기 위해 Siege를 설치하고 해당 컨테이너로 접속한다.
```
> kubectl create deploy siege --image=ghcr.io/acmexii/siege-nginx:latest
> kubectl exec -it pod/siege-75d5587bf6-qblsz -- /bin/bash
```

- Liveness Probe 확인 

<img width="735" alt="2021-09-30 오후 2 43 27" src="https://user-images.githubusercontent.com/19512435/135394188-9091f11d-d633-453f-986a-6218e31ef802.png">

- Liveness Probe Fail 설정 및 확인 
  - User Liveness Probe를 명시적으로 Fail 상태로 전환한다.

<img width="1398" alt="2021-09-30 오후 3 02 39" src="https://user-images.githubusercontent.com/19512435/135396258-5d6d1829-27e6-4c53-9172-711820816429.png">


- Probe Fail에 따른 쿠버네티스 동작확인  
  - User 서비스의 Liveness Probe가 /actuator/health의 상태가 DOWN이 된 것을 보고 restart를 진행함. 
    - user pod의 RESTARTS가 1로 바뀐것을 확인. 
    - describe 를 통해 해당 pod가 restart 된 것을 알 수 있다.

<img width="735" alt="2021-09-30 오후 3 00 10" src="https://user-images.githubusercontent.com/19512435/135396186-89e7d767-56a1-4d83-90fb-43b387aade34.png">

<img width="1894" alt="2021-09-30 오후 3 02 00" src="https://user-images.githubusercontent.com/19512435/135396386-e7683bd8-4d36-45fa-b28e-eddf6ff6f699.png">

	
## 무정지 재배포
### ◆ Rediness- HTTP Probe
- 시나리오
  1. 현재 구동중인 User 서비스에 길게(3분) 부하를 준다. 
  2. user pod의 상태 모니터링
  3. AWS에 CodeBuild에 연결 되어있는 github의 코드를 commit한다.
  4. Codebuild를 통해 새로운 버전의 User이 배포 된다. 
  5. pod 상태 모니터링에서 기존 User 서비스가 Terminating 되고 새로운 User 서비스가 Running하는 것을 확인한다.
  6. Readness에 의해서 새로운 서비스가 정상 동작할때까지 이전 버전의 서비스가 동작하여 seige의 Availability가 100%가 된다.

<br/>

- User 서비스의 Readness probe  설정 확인
  - buildspec_kubectl.yaml
```
                    readinessProbe:
                      httpGet:
                        path: /actuator/health
                        port: 8080
                      initialDelaySeconds: 10
                      timeoutSeconds: 2
                      periodSeconds: 5
                      failureThreshold: 10
```

- 현재 구동중인 User 서비스에 길게(3분) 부하를 준다. (HPA 설정 제거 및 delete 필요)
```
siege -v c1 -t180S --content-type "application/json" 'http://user:8080/users'

```
<img width="644" alt="2021-09-30 오후 3 43 06" src="https://user-images.githubusercontent.com/19512435/135400935-767f73d9-ef26-4d18-b8bf-a0a746476d0a.png">

- AWS에 CodeBuild에 연결 되어있는 github의 코드를 commit한다.
  User 서비스의 아무 코드나 수정하고 commit 한다. 
  배포 될때까지 잠시 기다린다. 
  Ex) buildspec-kubectl.yaml에 carrage return을  추가 commit 한다. 


- pod 상태 모니터링에서 기존 User 서비스가 Terminating 되고 새로운 User 서비스가 Running하는 것을 확인한다.
- pod의 상태 모니터링

<img width="807" alt="2021-09-30 오후 4 05 12" src="https://user-images.githubusercontent.com/19512435/135403847-795e4dba-a99f-4fa0-8243-429c08b97ce4.png">



## ConfigMap
- 시나리오
  1. ConfigMap 등록
  2. ConfigMap 설정 소스
  3. ConfigMap 활용 결과
  

- ConfigMap 등록
```
kubectl create configmap vaccine-type --from-literal=type=Pfizer,Moderna,Janssen,AstraZeneca

```
<img width="811" alt="2021-09-30 오후 3 48 21" src="https://user-images.githubusercontent.com/19512435/135401618-c5b5dca8-be9a-4578-aa7a-6824a1ced6d9.png">


- ConfigMap 설정 소스
  - Hospital 서비스 : 백신 종류를 String 타입으로 받아서 파싱하여 배열로 활용
```
# buildspec-kubectl.yaml

    env:
    - name: VACCINE_TYPE
      valueFrom:
	configMapKeyRef:
	  name: vaccine-type
	  key: type
	  
	  
	  
# application.yml 

vaccine:
  type: ${VACCINE_TYPE}
  
  
  
# PolicyHandler.java

	@Value("${vaccine.type}")
	String vaccineType;

```

- ConfigMap 활용 결과
  - 백신 예약시 랜덤으로 백신 종류가 선택되고, Dashboard에서 사용자별로 접종된 백신 종류 조회 가능
  
<img width="807" alt="2021-09-30 오후 3 54 17" src="https://user-images.githubusercontent.com/19512435/135403153-c7832afb-db7c-46d3-8c48-d142ae8185da.png">



<br/>
