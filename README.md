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

<img width="1551" alt="2021-09-13 10 47 23" src="https://user-images.githubusercontent.com/89987635/133095353-70d645ff-a7db-4662-abf4-47358c10375c.png">

### 폴리시의 이동과 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)

<img width="1498" alt="2021-09-13 10 49 08" src="https://user-images.githubusercontent.com/89987635/133095646-180e389d-f301-41b0-b8ba-c365944e4287.png">

### 완성된 1차 모형

<img width="1469" alt="2021-09-13 11 00 17" src="https://user-images.githubusercontent.com/89987635/133097409-073c5768-bfd9-4688-a0c7-3da74e4463a2.png">

    - View Model 추가

### 1차 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

<img width="1448" alt="2021-09-13 11 11 59" src="https://user-images.githubusercontent.com/89987635/133099363-2a033cc6-8d97-4751-b9e9-424cf316ec3f.png">

    - 점장은 상품을 주문한다 (ok)
    - Supplier는 제품을 배송한다 (ok)
    - 배송이 되면 상품 갯수가 늘어난다 (ok) 

<img width="1430" alt="2021-09-13 11 23 30" src="https://user-images.githubusercontent.com/89987635/133101159-d8ca7260-7063-4bdb-99c4-37ec8a027077.png">

    - 고객은 상품 목록을 조회한다 (ok)
    - 고객은 상품을 예약한다 (ok)
    - 고객이 예약한 상품을 결제한다 (ok)
    - 고객이 방문하여 예약한 상품을 찾아간다 (ok)
    - 찾아간 상품에 대한 예약은 Pickup으로 표시된다 (ok)

<img width="1430" alt="2021-09-13 11 16 05" src="https://user-images.githubusercontent.com/89987635/133099909-cfa99c23-c9d8-4f95-af6c-9a82bea15910.png">

    - 고객은 예약 내역을 취소한다 (ok)
    - 예약을 취소하면 결제가 취소된다 (ok)
    - 예약이 취소되면 상품 예약이 취소된다 (ok)


### 비기능 요구사항에 대한 검증

<img width="1430" alt="2021-09-13 11 33 01" src="https://user-images.githubusercontent.com/89987635/133102897-b2ef32d3-e1d9-498c-b868-42dd9d2951fc.png">

    - 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
        - 고객 예약시 결제처리:  ACID 트랜잭션 적용. 예약 완료 시 결제처리에 대해서는 Request-Response 방식 처리
        - 결제 완료시 Store 연결 :  PayHistory 에서 Store 마이크로서비스로 예약 요청이 전달되는 과정에 있어서 Store 마이크로서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함
        - 나머지 모든 inter-microservice 트랜잭션: 출고 처리, 픽업 완료 등 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함



## 헥사고날 아키텍처 다이어그램 도출
    
<img width="1481" alt="2021-09-13 11 51 17" src="https://user-images.githubusercontent.com/89987635/133106137-c3ff9789-2d0a-4054-acb4-cb0eab362c14.png">


    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8080 ~ 8085 이다)

```
cd gateway
mvn spring-boot:run

cd Store
mvn spring-boot:run 

cd Reservation
mvn spring-boot:run  

cd Pay
mvn spring-boot:run

cd Supplier
mvn spring-boot:run

cd View
mvn spring-boot:run
```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 Reservation 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어(유비쿼터스 랭귀지)를 영어로 번역하여 사용하였다. 

```

package convenience.store;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Reservation_table")
public class Reservation {

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Long id;
  private String status;
  private String date;
  private Long productId;
  private String productName;
  private Integer productPrice;
  private Long customerId;
  private String customerName;
  private String customerPhone;
  private Integer qty;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
     this.status = status;
  }

  public String getDate() {
     return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public Long getProductId() {
     return productId;
  }

  public void setProductId(Long productId) {
     this.productId = productId;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public Integer getProductPrice() {
    return productPrice;
  }

  public void setProductPrice(Integer productPrice) {
     this.productPrice = productPrice;
  }

  public Long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Long customerId) {
    this.customerId = customerId;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getCustomerPhone() {
    return customerPhone;
  }

  public void setCustomerPhone(String customerPhone) {
    this.customerPhone = customerPhone;
  }

  public Integer getQty() {
    return qty;
  }

  public void setQty(Integer qty) {
      this.qty = qty;
  }
}

```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```

package convenience.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="products", path="products")
public interface ProductRepository extends JpaRepository<Product, Long>{
	
  Product findByProductName(String productName);
	
}

```
- 적용 후 REST API 의 테스트 (PostMan 기준)
```

# Store 서비스의 입고 주문
POST http://localhost:8083/product/order
{
    "productName": "Milk",
    "productPrice": 1200,
    "productQty": 2
}

# Store 입고 상태 확인
GET http://localhost:8080/product/list


# Reservation 서비스의 예약 주문
POST http://localhost:8081/reservation/order
{
    "productId": 1,
    "productName": "Milk",
    "productPrice": 1200,
    "customerId": 2,
    "customerName": "Sam",
    "customerPhone": "010-9837-0279",
    "qty": 2
}

```

## 폴리글랏 퍼시스턴스

전체 서비스의 경우 빠른 속도와 개발 생산성을 극대화하기 위해 Spring Boot에서 기본적으로 제공하는 In-Memory DB인 H2 DB를 사용하였다.

```
# Product.java

package convenience.store;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.beans.BeanUtils;

@Entity
@Table(name="product_table")
public class Product {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO) // H2 DB의 경우 ID가 sequence 기준으로 순차적으로 채번된다
    private Long id;
    private String productName;
    private Integer productPrice;
    private Integer productQty;
    

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

View 서비스(dashboard)의 경우 다른 서비스와 다르게 HSQL DB를 사용하였다.

```

# application.yml

  datasource:
    driver-class-name: org.hsqldb.jdbc.JDBCDriver
    url: jdbc:hsqldb:mem:testdb
    username: sa
    password:

```


## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 예약(reservation)->결제(pay) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 결제 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```

# (Reservation) 

package convenience.store.external;

@FeignClient(name="pay", url="${api.url.pay}", fallback = PayHistoryServiceImpl.class)
public interface PayHistoryService {
	
  @RequestMapping(method= RequestMethod.POST, path="/request")
  public boolean request(@RequestBody PayHistory payHistory);

}

```

- 예약 직후(@PostPersist) 결제를 요청하도록 처리
```

# Reservation.java (Entity)

  @PostPersist
  public void onPostPersist() {

    convenience.store.external.PayHistory payHistory = new convenience.store.external.PayHistory();

    payHistory.setPayStatus(this.status);
    payHistory.setReserveStatus("RESERVE");
    payHistory.setReserveId(this.id);
    payHistory.setCustomerId(this.customerId);
    payHistory.setCustomerName(this.customerName);
    payHistory.setCustomerPhone(this.customerPhone);
    payHistory.setDate(this.date);
    payHistory.setReserveDate(this.date);
    payHistory.setProductId(this.productId);
    payHistory.setProductPrice(this.productPrice);
    payHistory.setReserveQty(this.qty);

    boolean result = ReservationApplication.applicationContext.getBean(convenience.store.external.PayHistoryService.class).request(payHistory);

  }

```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 결제 시스템이 장애가 나면 예약도 불가하다는 것을 확인:


```
# 결제 (Pay) 서비스를 잠시 내려놓음 (ctrl+c)

# 예약처리
POST http://localhost:8081/reservation/order   #Fail
{
    "productId": 1,
    "productName": "Milk",
    "productPrice": 1200,
    "customerId": 2,
    "customerName": "Sam",
    "customerPhone": "010-9837-0279",
    "qty": 2
}

POST http://localhost:8081/reservation/order   #Fail
{
    "productId": 2,
    "productName": "Snack",
    "productPrice": 1500,
    "customerId": 2,
    "customerName": "Sam",
    "customerPhone": "010-9837-0279",
    "qty": 5
}


#결제서비스 재기동
cd Pay
mvn spring-boot:run

# 예약처리
POST http://localhost:8081/reservation/order   #Success
{
    "productId": 1,
    "productName": "Milk",
    "productPrice": 1200,
    "customerId": 2,
    "customerName": "Sam",
    "customerPhone": "010-9837-0279",
    "qty": 2
}

POST http://localhost:8081/reservation/order   #Success
{
    "productId": 2,
    "productName": "Snack",
    "productPrice": 1500,
    "customerId": 2,
    "customerName": "Sam",
    "customerPhone": "010-9837-0279",
    "qty": 5
}

```

- 또한 과도한 예약 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)




## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


결제가 이루어진 후에 Store 서비스로 이를 알려주는 행위는 동기식이 아니라 비동기식으로 처리하여 Store 서비스의 처리를 위하여 결제가 블로킹 되지 않아도록 처리한다.
 
- 이를 위하여 결제 이력에 기록을 남긴 후에 곧바로 결제 요청이 되었다는 도메인 이벤트를 카프카로 송출한다. (Publish)
  이때 다른 저장 로직에 의해서 해당 이벤트가 발송되는 것을 방지하기 위해 Status 체크하는 로직을 추가했다.
 
```

package convenience.store;

@Entity
@Table(name="payhistory_table")
public class PayHistory {

    ...

    @PostPersist
    public void onPostPersist() {
      if(this.reserveStatus.equals("RESERVE")) {
        PayRequested payRequested = new PayRequested();
    	BeanUtils.copyProperties(this, payRequested);    		
    	payRequested.publishAfterCommit();

        payRequested.saveJasonToPvc(payRequested.toJson());

      }
    }

```
- 상점 서비스에서는 결제승인 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
package convenience.store;

...

@Service
public class PolicyHandler {

  ...
  
  @StreamListener(KafkaProcessor.INPUT)
  public void wheneverPayRequested_Reserve(@Payload PayRequested payRequested){

    if(!payRequested.validate()) return;
    System.out.println("\n\n##### listener Reserve : " + payRequested.toJson() + "\n\n");
        
    StoreReservation storeReservation = new StoreReservation();
    BeanUtils.copyProperties(payRequested, storeReservation);
    storeReservationRepository.save(storeReservation);
        
    // 예약이 되면 상품의 보유 갯수를 줄여준다  
    Product product = productRepository.findById(payRequested.getProductId()).orElseThrow(null);
    product.setProductQty(product.getProductQty() - payRequested.getReserveQty());
    productRepository.save(product);
        
  }

```
실제 구현을 하자면, 결제가 완료된 이후에 카톡 알림을 통해 예약이 완료되었다는 외부 이벤트를 보내고, 점장은 예약 상태를 Dashboard를 통해 확인할 수 있다.
  
```

# 결제 성공시 카톡 메세지 전송

  @PostPersist
  public void onPostPersist() {

  convenience.store.external.PayHistory payHistory = new convenience.store.external.PayHistory();

  boolean result = ReservationApplication.applicationContext.getBean(convenience.store.external.PayHistoryService.class).request(payHistory);

  if(result) {
    System.out.println("########## 결제가 완료되었습니다 ############");
    // 결제 성공 카톡 메세지 발송
  } else {
    System.out.println("########## 결제가 실패하였습니다 ############");
    // 결제 실패 카톡 메세지 발송
  }    	

}

# 예약 현황을 Dashboard에서 확인

GET http://localhost:8084/dashboard/list

```

Store 서비스는 예약/결제와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, Store 서비스를 유지보수로 인해 잠시 내려간 상태라도 예약을 받는데 문제가 없다:
```
# Store 서비스 를 잠시 내려놓음 (ctrl+c)

#예약처리
POST http://localhost:8081/reservation/order   #Success
{
    "productId": 1,
    "productName": "Milk",
    "productPrice": 1200,
    "customerId": 2,
    "customerName": "Sam",
    "customerPhone": "010-9837-0279",
    "qty": 2
}

POST http://localhost:8081/reservation/order   #Success
{
    "productId": 2,
    "productName": "Snack",
    "productPrice": 1500,
    "customerId": 2,
    "customerName": "Sam",
    "customerPhone": "010-9837-0279",
    "qty": 5
}

#예약상태 확인
GET http://localhost:8081/reservation/list     # 예약상태 조회 가능

#상점 서비스 기동
cd Store
mvn spring-boot:run

#주문상태 확인
GET http://localhost:8083/product/list     # 상품의 갯수가 예약한 갯수만큼 줄어듬

```

## Correlation (보상패턴) 구현

결제 승인시 상품의 갯수를 차감하고, 결제 취소시 상품의 갯수를 원복해준다.

```
@StreamListener(KafkaProcessor.INPUT)
public void wheneverPayRequested_Reserve(@Payload PayRequested payRequested){

  ...
        
    // 예약이 되면 상품의 보유 갯수를 줄여준다  
    Product product = productRepository.findById(payRequested.getProductId()).orElseThrow(null);
    product.setProductQty(product.getProductQty() - payRequested.getReserveQty());
    productRepository.save(product);
        
}
    
    
@StreamListener(KafkaProcessor.INPUT)
public void wheneverPayCancelled_ReservationCancel(@Payload PayCancelled payCancelled){

    ...
       
    // 예약이 취소되는 상품의 보유 갯수를 늘려준다 
    Product product = productRepository.findById(payCancelled.getProductId()).orElseThrow(null);
    product.setProductQty(product.getProductQty() + payCancelled.getReserveQty());
    productRepository.save(product);

}

```

## CQRS 패턴 구현

예약과 픽업, 결제 취소에 대한 현황을 Dashboard로 구현하여 조회할 수 있게 제공
현재 예약 현황과 사용자별 예약 리스트를 조회할 수 있는 End point 두개 구현

```
    // 예약 리스트 조회
    @GetMapping("/list")
    public ResponseEntity<List<Dashboard>> getDashboards() {
	List<Dashboard> dashboardList = dashboardRepository.findAll();
	return ResponseEntity.ok(dashboardList);
    }
	
    // 사용자별 예약 리스트 조회
    @GetMapping("/list/{userId}")
    public ResponseEntity<List<Dashboard>> getDashboardsByUserId(@PathVariable Long userId) {
	List<Dashboard> dashboardList = dashboardRepository.findByCustomerId(userId);
	return ResponseEntity.ok(dashboardList);
    }
```


예약, 결제 취소, 픽업에 대한 Event Listener 구현

```
DashboardViewHandler.java

    @StreamListener(KafkaProcessor.INPUT)
    public void whenProductReserved_then_create(@Payload ProductReserved productReserved) {
        try {

            if (!productReserved.validate()) return;
            System.out.println("\n\n##### listener ProductReserved : " + productReserved.toJson() + "\n\n");

            // view 객체 생성
            Dashboard dashboard = new Dashboard();            

            dashboard.setProductId(productReserved.getProductId());
            dashboard.setProductName(productReserved.getProductName());
            dashboard.setProductPrice(productReserved.getProductPrice());
            dashboard.setCustomerId(productReserved.getCustomerId());
            dashboard.setCustomerName(productReserved.getCustomerName());
            dashboard.setCustomerPhone(productReserved.getCustomerPhone());
            dashboard.setReserveId(productReserved.getId());
            dashboard.setReserveQty(productReserved.getReserveQty());
            dashboard.setReserveDate(productReserved.getReserveDate());
            dashboard.setReserveStatus(productReserved.getStatus());
            dashboard.setTotalPrice(productReserved.getReserveQty() * productReserved.getProductPrice());

            dashboardRepository.save(dashboard);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayCancelled_then_update(@Payload PayCancelled payCancelled) {
        try {
            if (!payCancelled.validate()) return;

            Dashboard dashboard = dashboardRepository.findByReserveId(payCancelled.getReserveId());
              
            dashboard.setReserveStatus(payCancelled.getReserveStatus());
            
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateStr = format.format(Calendar.getInstance().getTime());
			dashboard.setCancelDate(dateStr);
			
            dashboardRepository.save(dashboard);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    @StreamListener(KafkaProcessor.INPUT)
    public void whenProductPickedup_then_update(@Payload ProductPickedup productPickup) {
        try {
            if (!productPickup.validate()) return;

			Dashboard dashboard = dashboardRepository.findByReserveId(productPickup.getReserveId());
			dashboard.setReserveStatus(productPickup.getReserveStatus());
			
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateStr = format.format(Calendar.getInstance().getTime());
			dashboard.setPickupDate(dateStr);
			
			dashboardRepository.save(dashboard);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

```


# 운영

## CI/CD 설정
각 구현체들은 각자의 AWS의 ECR 에 구성되었고, 사용한 CI/CD 플랫폼은 AWS-CodeBuild를 사용하였으며, pipeline build script 는 각 프로젝트 폴더 이하에 buildspec-kubectl.yaml 에 포함되었다.

- 레포지터리 생성 확인

<img width="2509" alt="스크린샷 2021-09-15 오전 11 25 27" src="https://user-images.githubusercontent.com/89987635/133383981-57d87ff8-8772-4d94-ba1a-cca5cf83cbcc.png">

<br/>

- 생성 할 CodeBuild
  - team01-gateway
  - team01-reservation
  - team01-pay
  - team01-store
  - team01-stock01
  - team01-view
<br/>


- github의 각 서비스의 서브 폴더에 buildspec-kubect.yaml 위치.

![image](https://user-images.githubusercontent.com/22004206/133250463-b7c80d2c-e58b-4329-8ded-dca2b146215a.png)
![image](https://user-images.githubusercontent.com/22004206/133250705-66c3e747-e3aa-4aa5-90a0-1e9efb4210c5.png)
![image](https://user-images.githubusercontent.com/22004206/133250824-3e9689f6-2327-45dd-8322-bacad102e1d3.png)
![image](https://user-images.githubusercontent.com/22004206/133250923-f62f98bb-28bb-4dea-ab6f-6b6b9081c9c1.png)
![image](https://user-images.githubusercontent.com/22004206/133251040-94926311-83d1-422e-95d9-7a8950227966.png)


- 연결된 github에 Commit 진행시 6개의 서비스들 build 진행 여부 및 성공 확인 

<img width="1187" alt="스크린샷 2021-09-15 오후 3 47 31" src="https://user-images.githubusercontent.com/89987635/133384246-cf1da089-5f5c-4c29-bb08-049979231eba.png">


-	배포된 6개의 Service  확인
```
root@labs-1916923594:/home/project# kubectl get all
NAME                                 READY   STATUS    RESTARTS   AGE
pod/efs-provisioner-84b8576f-s5m2h   1/1     Running   0          4h28m
pod/gateway-fb444ccb5-qzwpb          1/1     Running   0          15m
pod/pay-d579c6997-rpcds              1/1     Running   0          10m
pod/reservation-55547fd689-vcvq8     1/1     Running   0          10m
pod/siege-pvc                        1/1     Running   0          65m
pod/store-78c6cbd6c4-h4b8s           1/1     Running   0          10m
pod/supplier-76fd6bbf4f-c6jbs        1/1     Running   0          15m
pod/view-5c8788f97d-swclr            1/1     Running   0          10m

```


## API Gateway 설정

- 단일 진입점을 구성하기 위해 Spring Cloud Gateway 적용

```
spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: Reservation
          uri: http://reservation:8080
          predicates:
            - Path=/reservation/** 
        - id: Pay
          uri: http://pay:8080
          predicates:
            - Path=/pay/** 
        - id: Store
          uri: http://store:8080
          predicates:
            - Path=/product/** 
        - id: View
          uri: http://view:8080
          predicates:
            - Path= /dashboard/**
        - id: Supplier
          uri: http://supplier:8080
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

<img width="1121" alt="스크린샷 2021-09-15 오후 9 59 25" src="https://user-images.githubusercontent.com/89987635/133437863-c26ca739-dd58-4b48-bb7e-f46cc195cc7d.png">


## 동기식 호출 / 서킷 브레이킹 / 장애격리
- 시나리오
  1. 예약(reservation) --> 결재(pay)시의 연결을 RESTful Request/Response 로 연동하여 구현 함. 결제 요청이 과도할 경우 CB가 발생하고 fallback으로 결재 지연 메새지를 보여줌으로 장애 격리 시킴.
  2. circuit break의 timeout은 610mm 설정. 
  3. Pay 서비스에 임의의 부하 처리.
  4. 부하테스터(seige) 를 통한 circuit break 확인. 
    - 결재 지연 메세지 확인.
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
    # 전역설정 timeout이 610ms 가 넘으면 CB 처리.
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610
```
- Pay 서비스에 임의 부하 처리 - 400 밀리에서 증감 220 밀리 정도 왔다갔다 하게 아래 코드 추가
```
# PayHisotryController.java

try {
    Thread.currentThread().sleep((long) (400 + Math.random() * 220));
} catch (InterruptedException e) {
    e.printStackTrace();
}
```
- Resevation 서비스에 FeignClient fallback 코드 추가.
```
# PayHistoryService.java

@FeignClient(name ="delivery", url="${api.url.pay}", fallback = PayHistoryServiceImpl.class)
```

```
# PayHistoryServiceImple.java

@Service
public class PayHistoryServiceImpl implements PayHistoryService {
    /**
     * Pay fallback
     */
    public boolean request(PayHistory payhistory) {
        System.out.println("@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@");
        System.out.println("@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@");
        System.out.println("@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@");
        return false;
    }
}
```

- 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
  - 동시사용자 100명, 60초 동안 실시
  - Reservation 서비스의 log 확인.

<img width="2061" alt="스크린샷 2021-09-15 오후 3 07 23" src="https://user-images.githubusercontent.com/89987635/133384541-fabf95af-a968-491e-b782-14bbb16d9062.png">

- 결재 서비스에 지연이 발생하는 경우 결재지연 메세지를 보여주고 장애에 분리되어 Avalablity가 100% 이다. 

- 예약 서비스(reservation)의 log에 아래에서 결재 지연 메세지를 확인한다.

<img width="1180" alt="스크린샷 2021-09-15 오후 3 06 12" src="https://user-images.githubusercontent.com/89987635/133384661-e8c55eac-215e-4d7b-be1c-c6f269541f5b.png">

- 시스템은 죽지 않고 지속적으로 과도한 부하시 CB 에 의하여 회로가 닫히고 결재 지연중 메세지를 보여주며 고객을 장애로 부터 격리시킴.



## 오토스케일 아웃
- 예약서비스(Reservation)에 대해  CPU Load 50%를 넘어서면 Replica를 10까지 늘려준다. 
  - buildspec-kubectl.yaml
```
          cat <<EOF | kubectl apply -f -
          apiVersion: autoscaling/v2beta2
          kind: HorizontalPodAutoscaler
          metadata:
            name: reservation-hpa
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

- 예약서비스(reservation)에 대한 CPU Resouce를 1000m으로 제한 한다.
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

- Siege (로더제너레이터)를 설치하고 해당 컨테이너로 접속한다.
```
> kubectl create deploy siege-pvc --image=ghcr.io/acmexii/siege-nginx:latest
> kubectl exec pod/siege-pvc -it -- /bin/bash
```

- 예약 서비스(reseravation)에 워크로드를 동시 사용자 100명 60초 동안 진행한다.
```
siege -v -c100 -t60S --content-type "application/json" 'http://reservation:8080/reservation/order POST {"productId": 222,"productName": "Galaxy Watch7","productPrice": 5000000,"customerId”:999,”customerName":"Sam","customerPhone":"010-9837-0279","qty":2}'
```
- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다 : 각각의 Terminal에 
  - 어느정도 시간이 흐른 후 (약 30초) 스케일 아웃이 벌어지는 것을 확인할 수 있다.
  
<img width="582" alt="스크린샷 2021-09-15 오후 3 08 36" src="https://user-images.githubusercontent.com/89987635/133384946-a6eedf1e-660e-4064-b1aa-d798c0a8a37a.png"> 


```	

root@labs-1916923594:/home/project# kubectl get hpa
NAME              REFERENCE                TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
reservation-hpa   Deployment/reservation   1%/50%    1         10        1          138m

```

	
## Self Healing
### ◆ Liveness- HTTP Probe
- 시나리오
  1. Reservation 서비스의 Liveness 설정을 확인힌다. 
  2. Reservation 서비스의 Liveness Probe는 actuator의 health 상태 확인을 설정되어 있어 actuator/health 확인.
  3. pod의 상태 모니터링
  4. Reservation 서비스의 Liveness Probe인 actuator를 down 시켜 Reservation 서비스가 termination 되고 restart 되는 self healing을 확인한다. 
  5. Reservation 서비스의 describe를 확인하여 Restart가 되는 부분을 확인한다.

<br/>

- Reservation 서비스의 Liveness probe 설정 확인
```
kubectl get deploy reservation -o yaml

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
> kubectl exec pod/[SIEGE-POD객체] -it -- /bin/bash
```

- Liveness Probe 확인 

<img width="582" alt="스크린샷 2021-09-15 오후 3 11 13" src="https://user-images.githubusercontent.com/89987635/133385107-d191cd39-9246-4698-9a6b-4eb1f35a8ecb.png">

- Liveness Probe Fail 설정 및 확인 
  - Reservation Liveness Probe를 명시적으로 Fail 상태로 전환한다.

<img width="582" alt="스크린샷 2021-09-15 오후 3 14 01" src="https://user-images.githubusercontent.com/89987635/133385278-a7c33be3-95c9-40f2-bc88-78897af82524.png">


- Probe Fail에 따른 쿠버네티스 동작확인  
  - Reservation 서비스의 Liveness Probe가 /actuator/health의 상태가 DOWN이 된 것을 보고 restart를 진행함. 
    - reservation pod의 RESTARTS가 1로 바뀐것을 확인. 
    - describe 를 통해 해당 pod가 restart 된 것을 알 수 있다.
```
Every 1.0s: kubectl get pod                                        labs-1916923594: Wed Sep 15 07:08:41 2021

NAME                             READY   STATUS    RESTARTS   AGE
efs-provisioner-84b8576f-s5m2h   1/1     Running   0          4h49m
gateway-f48b5bc7c-k47sf          1/1     Running   0          2m27s
pay-5bbf487cf7-jw6mg             1/1     Running   0          3m23s
reservation-db4c66457-z8z4q      0/1     Running   1          3m21s
siege-pvc                        1/1     Running   0          85m
store-56fbfc5d7d-hlcpm           1/1     Running   0          3m18s
supplier-58c6f85cb9-54tbc        1/1     Running   0          2m22s
view-bdc7b9ccd-w5ngd             1/1     Running   0          3m23s

```
<img width="1962" alt="스크린샷 2021-09-15 오후 4 11 13" src="https://user-images.githubusercontent.com/89987635/133387508-9b1d5641-48c8-481d-8fb7-0b6d14f967ce.png">

	
## 무정지 재배포
### ◆ Rediness- HTTP Probe
- 시나리오
  1. 현재 구동중인 Reservation 서비스에 길게(3분) 부하를 준다. 
  2. reservation pod의 상태 모니터링
  3. AWS에 CodeBuild에 연결 되어있는 github의 코드를 commit한다.
  4. Codebuild를 통해 새로운 버전의 Reservation이 배포 된다. 
  5. pod 상태 모니터링에서 기존 Reservation 서비스가 Terminating 되고 새로운 Reservation 서비스가 Running하는 것을 확인한다.
  6. Readness에 의해서 새로운 서비스가 정상 동작할때까지 이전 버전의 서비스가 동작하여 seieg의 Avality가 100%가 된다.

<br/>

- reservstion 서비스의 Readness probe  설정 확인
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

- 현재 구동중인 Reservation 서비스에 길게(2분) 부하를 준다. 
```
> siege -v -c1 -t120S --content-type "application/json" 'http://reservation:8080/reservation/order POST {"productId": 222,"productName": "Galaxy Watch7","productPrice": 5000000,"customerId”:999,”customerName":"Sam","customerPhone":"010-9837-0279","qty":2}'
```
<img width="794" alt="스크린샷 2021-09-15 오후 3 32 43" src="https://user-images.githubusercontent.com/89987635/133385792-924fefb0-562f-4697-bdc6-67baba830247.png">
<img width="710" alt="스크린샷 2021-09-15 오후 3 39 02" src="https://user-images.githubusercontent.com/89987635/133385810-3bb01bcf-f940-4f47-a035-82922ab02565.png">


- AWS에 CodeBuild에 연결 되어있는 github의 코드를 commit한다.
  Resevatio 서비스의 아무 코드나 수정하고 commit 한다. 
  배포 될때까지 잠시 기다린다. 
  Ex) buildspec-kubectl.yaml에 carrage return을  추가 commit 한다. 


- pod 상태 모니터링에서 기존 Reservation 서비스가 Terminating 되고 새로운 Reservation 서비스가 Running하는 것을 확인한다.
- pod의 상태 모니터링
<img width="586" alt="스크린샷 2021-09-15 오후 4 59 23" src="https://user-images.githubusercontent.com/89987635/133394310-befb67aa-4384-40f3-a33c-974f1ee52d79.png">


## Persistant Volume Claim
- 시나리오
  1. EFS 생성 화면 캡쳐.
  2. 등록된 provisoner / storageclass / pvc 확인.
  3. 각 서비스의 buildspec_kubectl.yaml에 pvc 생성 정보 확인.
  4. bash shell을 사용할 수 있는 pod를 동일한 PVC 사용할 수 있게 설정 후 배포하여 '/mnt/aws'에 각 서비스에서 생성한 파일을 확인. 
  

- EFS 등록 화면 추가..
```
이미지 추가.
```

- provisioner 확인
```
> kubectl get pod

NAME                              READY   STATUS    RESTARTS   AGE
efs-provisioner-5976978f5-cqbzq   1/1     Running   0          19s
```

- storageClass 등록, 조회
```
> kubectl get sc
NAME            PROVISIONER             RECLAIMPOLICY   VOLUMEBINDINGMODE      ALLOWVOLUMEEXPANSION   AGE
aws-efs         my-aws.com/aws-efs      Delete          Immediate              false                  14s
gp2 (default)   kubernetes.io/aws-ebs   Delete          WaitForFirstConsumer   false                  27h
```
- pvc 확인
```
> kubectl get pvc
> kubectl describe pvc
  Type    Reason                 Age                From                                                                                     Message
  ----    ------                 ----               ----                                                                                     -------
  Normal  ExternalProvisioning   35s (x2 over 35s)  persistentvolume-controller                                                              waiting for a volume to be created, either by external provisioner "my-aws.com/aws-efs" or manually created by system administrator
  Normal  Provisioning           35s                my-aws.com/aws-efs_efs-provisioner-5976978f5-cqbzq_5cde0b7c-906d-477e-9e02-5b4823a9ca5c  External provisioner is provisioning volume for claim "default/aws-efs"
  Normal  ProvisioningSucceeded  35s                my-aws.com/aws-efs_efs-provisioner-5976978f5-cqbzq_5cde0b7c-906d-477e-9e02-5b4823a9ca5c  Successfully provisioned volume pvc-c770d8b7-ef09-4a19-903b-cced4daa9f1d
```
<br/>

- 각 Deployment의 PVC 생성정보는 buildspec-kubeclt.yaml에 적용되어있다.
```
                    volumeMounts:
                      - mountPath: "/mnt/aws"
                        name: volume
                        :
                        :
                        :
                volumes:
                  - name: volume
                    persistentVolumeClaim:
                      claimName: aws-efs
```


- 각 서비스의 Event 발생시 JSON 정보를 파일로 저장한다. 마지막 정보만 저정하기 위해 Overwirte하여 저장한다. 
  - 아래와 같은 코드를 통하여 /mnt/aws의 경로에 파일을 저장한다. 
```
# AbstractEvent.java

// PVC Test
public void saveJasonToPvc(String strJson){
    File file;

    if (strJson.equals("CANCEL")){
    file = new File("/mnt/aws/reservationCancelled_json.txt");
    }else{
        file = new File("/mnt/aws/productReserved_json.txt");
    }

    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      writer.write(strJson);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
} 


public void saveJasonToPvc(String strJson){
    File file;

    if (strJson.equals("RESERVE")){
    file = new File("/mnt/aws/payRequested_json.txt");
    }else{
        file = new File("/mnt/aws/payCancelled_json.txt");
    }

    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      writer.write(strJson);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
}


// PVC Test
public void saveJasonToPvc(String strJson){
    
    File file = new File("/mnt/aws/productPickedupjson.txt");

    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      writer.write(strJson);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
}  
```

- 각 서비스에서 저장한 Event 정보파일을 동일한 PVC를 사용하는 Pod를 생성하여 배포 후 /mnt/aws에 저장되어 있는지 확인. 

<img width="1115" alt="스크린샷 2021-09-15 오후 3 42 05" src="https://user-images.githubusercontent.com/89987635/133385983-48f1a1d1-2c58-4a34-9c32-ba9d6a5f8b50.png">


- 서비스 Event를 저장한 파일들을 확인 할 수 있다. 




<br/>
