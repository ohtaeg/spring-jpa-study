# Project
JPA 입문 공부를 위한 프로젝트

### 환경 세팅 (version)
[Spring boot Dependency versions 참고](https://docs.spring.io/spring-boot/docs/2.2.2.RELEASE/reference/html/appendix-dependency-versions.html#appendix-dependency-versions)
- H2 DB : 1.4.199
- Hibernate : 5.4.9 Final
- JPA 2.2

### 요구사항
- 회원은 상품을 주문할 수 있다.
    - 회원은 여러 번 주문할 수 있다.
- 주문 시 여러 종류의 상품을 선택할 수 있다.
    - 같은 상품도 여러번 주문될 수 있다.
- 주문시 배송을 할 수 있다.
- 하나의 상품은 여러 카테고리에 속할 수 있다.
 
<br>

## I learn

### Dialect
- JPA는 특정 DB에 종속되지 않는다.
- 각 DB마다 SQL문법과 함수는 조금씩 다르다.
- SQL 표준을 지키지 않는 특정 DB만의 고유기능을 `방언`이라고 일컫는다.
    - 문자열을 자르는 함수
        - Mysql : substring()
        - Oracle : substr()
    - 페이징
        - Mysql : limit()
        - Oracle : ROWNUM

- JPA는 속성을 통해 특정 DB의 `방언`을 정의할 수 있다.
- hibernate.dialect 속성에 지정
    - H2 : org.hibernate.dialect.H2Dialect
    - Oracle 10g : org.hibernate.dialect.Oracle10gDialect
    - MySQL : org.hibernate.dialect.MySQL5InnoDBDialect

<pre>
    <code>
        &lt;property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/&gt;
    </code>
</pre>

<br>

### JPA 구동방식
1. `Persistence 클래스`가 설정 정보를 조회. (/META-INF/persistence.xml)
2. `EntityManangerFactory 클래스` 생성. <br>
`EntityManangerFactory`는 어플리케이션 전체에서 공유되기 때문에 어플리케이션 로딩 시점에 하나만 만들어야 한다.
3. 필요시마다(트랜잭션 단위) Factory에서 `EntityManager`를 생성하되 쓰레드간 공유는 하면 안된다.
4. JPA의 모든 데이터 변경은 **트랜잭션** 안에서 실행 되어야 한다.

<br>

### JPQL
Java Persistence Query Language
- SQL과 비슷한 문법을 가진 **객체지향 쿼리**
- SQL을 추상화한 **객체지향 쿼리**
- JPQL은 테이블이 아닌 **객체**를 탐색, SQL은 데이터베이스 테이블 대상으로 탐색
- JPQL을 통해 검색할때 `Entity` 객체 대상으로 검색이 복잡한 쿼리 수행 가능
- JPQL을 실행하면 `방언`과 합쳐져 현재 DB에 맞는 SQL로 변경 (`방언`을 바꿔도 JPQL을 바꿀 필요가 없음.)

<br>

### 엔티티 생명 주기
- 비영속 (new) : `영속성 컨텍스트`와 관계없는 객체를 생성한 상태
    - <pre>
        <code>Member member = new Member()</code>
        <code>member.setId(id);</code>
        <code>member.setName("test");</code>
    </pre>
- 영속 : `EntityManager`를 통해 .persist(`Entity`)시 `Entity`가 `영속성 컨텍스트`에 관리되는 상태
    - <pre><code>entityManager.persist(member)</code></pre>
    - **영속 되었다고해서 DB에 쿼리가 날라가지 않는다. 즉, DB에 바로 저장되지 않는다.** <br>
    - **트랜잭션 commit시 `영속성 컨텍스트`에 있는(영속 되어있는) `Entity`가 DB에 저장이 된다.**
- 준영속 : `Entity`를 `영속성 컨텍스트`에서 분리
    - <pre><code>entityManager.detach(member)</code></pre>
- 삭제 : 객체를 삭제, 영구 저장된 데이터를 DELETE 쿼리를 통해 DB에서 지운다.
    - <pre><code>entityManager.remove(member)</code></pre>

<br>

### 영속성 컨텍스트
- 어플리케이션과 DB 사이에 존재하는 논리적인 개념으로써, `Entity`를 영구 저장하는 환경
- `EntityManager`를 통해서 `영속성 컨텍스트`에 접근이 가능하다. 
- <pre><code>entityManager.persist(member)</code></pre>
- 즉, `Entity`를 `영속성 컨텍스트`에 저장.
- 내부에 `1차 캐시`와 `쓰기 지연 SQL 저장소`가 존재 한다.

<br>
 
### 영속성 컨텍스트 이점
- 1차 캐시
- 동일성(identity) 보장
- 트랜잭션을 지원하는 쓰기 지연
- 변경 감지
- 지연 로딩

이러한 이점들을 상황에따라 잘 이용한다면, JPA 성능을 높일 수 있다.

<br>

### 1차 캐시
- `영속성 컨텍스트` 내부에 `Entity`를 저장하는 `1차 캐시`가 존재 한다.
- `1차 캐시`는 일종의 Map 형태인 key - (value + `Snapshot`) 구조로 되어 있다.
    - key : @Id로 지정한 식별자
    - value : `Entity` 인스턴스
    - `Snapshot` : `Entity`가 `1차 캐시`에 최초로 들어올때 (값을 넣거나, DB에서 읽거나) 해당 `Entity` 상태를 `Snapshot`으로 떠둔다.
- 조회시 `1차 캐시`에 값이 있는 경우
    - DB에 접근하는 SELECT 쿼리를 수행하지 않고 `1차 캐시`를 탐색하여 반환
- 조회시 `1차 캐시`에 없고 DB에 존재하는 경우
    - DB에 접근하여 엔티티를 가져오고 나서 `1차 캐시`에 저장 후 반환
- 즉, 캐싱을 통해 DB를 직접 접근하지 않아도 되는 성능상의 이점은 있다.
    - 그러나 `EntityManager`는 트랜잭선 단위로 만들고 트랜잭션이 종료되면 `EntityManager` 또한 close() 시키기 때문에
    `EntityManager`를 종료한다는건 `영속성 컨텍스트`를 지운다는 뜻이므로
    트랜잭션이 일어나는 찰나의 순간에만 이득이 있어 큰 이점이라고 보기는 힘들다.

<br>

### 동일성 (identity) 보장
<pre>
    <code>Member member = EntityManager.find(Member.class, 1);</code>
    <code>Member otherMember = EntityManager.find(Member.class, 1);</code>
    <code>System.out.print(member == otherMember); // true </code>
</pre>
> 반복가능한 읽기 등급 (Repetable Read)의 트랜잭션 격리 수준을 <br>
> DB가 아닌 Application 차원인 `1차 캐시`로 제공하기 때문에 동일성 보장

<br>

### 트랜잭션을 지원하는 쓰기 지연 ( Transactional Write-Behind )
<pre>
    <code>EntityManager.persist(member);</code>
    <code>EntityManager.persist(otherMember);</code>
    <code>transaction.commit();</code>
</pre>
- 위에서 언급한 `영속성 컨텍스트` 내부에는 `1차 캐시`와 `쓰기 지연 SQL 저장소`가 존재한다.
- persist(entity)시 내부적으로는 
    1. `1차 캐시`에 `Entity` 저장
    2. Insert 쿼리 생성 후 `쓰기 지연 SQL 저장소`에 저장.
- 트랜잭션 커밋시, DB에 저장해둔 insert 쿼리를 수행한다.
- 쓰기 지연을 하는 이유는 버퍼링과 같이 모아두었다가 한번에 실행하기 위함. (JDBC BATCH)
- Hibernate 같은 경우 JDBC BATCH 옵션을 설정할 수 있다.
<pre>
    &lt;property name="hibernate.jdbc.batch_size" value="10"/&gt;
    <code>// 10개까지 쌓고 전송</code>
</pre>

<br>

### 변경 감지 (Dirty Check)
<pre>
    <code>Member member = EntityManager.find(Member.class, 1L); //1L, hello</code>
    <code>member.setName("aaa")</code>
</pre>

- 트랜잭션 커밋 시점에 JPA는 내부적으로 `flush()`를 호출한 후 `Entity` 와 `Snapshot`을 비교한다.
- 상태를 비교하여 변경이 되었다면 UPDATE SQL을 생성하여 `쓰기 지연 SQL 저장소`에 저장 후 DB에 접근하여 커밋한다.
- 이렇게 내부적인 처리로 인해 따로 UPDATE 메서드가 존재하지 않고 자바 컬렉션 처럼 다룰 수 있도록 지원해준다.

<br>

### 플러시 (Flush)
- `영속성 컨텍스트`의 변경 내용을 DB에 반영하여 `영속성 컨텍스트`와 DB를 동기화 하는 작업.
- 트랜잭션 커밋이 일어나기전에 `플러시`가 동작함.
- `플러시`를 하고 나서도 `영속성 컨텍스트`는 그대로 유지 된다.
- `플러시` 동작
    1. 변경 감지를 수행
    2. 수정된 엔티티가 존재하면 `쓰기 지연 SQL 저장소`에 등록
    3. `쓰기 지연 SQL 저장소`의 쿼리를 DB에 전송 (insert, update, delete)
- `영속성 컨텍스트`를 `플러시`하는 방법
<pre>
    <code>1. entityManager.flush(); // 직접 호출 (직접 호출할 일은 없으나 테스트시 필요할 수 있음)</code>
    <code>2. transaction.commit(); // 자동 호출</code>
    <code>3. JPQL 쿼리 실행         // 자동 호출</code>
</pre>
- `JPQL` 실행시 `플러시`가 자동으로 호출되는 이유 <br>
ex) <br>
<pre>
    <code>entityManager.persist(member);</code>
    <code>entityManager.persist(otherMember);</code>
    <code>// persist() 이후 커밋 되기전 중간 JPQL 실행</code>
    <code>List&lt;Member&gt; members = entityManager.createQuery("SELECT M FROM Member M", Member.class)
                                              .getResultList()</code>
    <code>transaction.commit();</code>
</pre>
이런 경우에 트랜잭션 커밋 전이라 아직 DB에 접근하지 않았으므로, <br>
persist()를 수행한 `Entity`들을 가져오려고 할 경우 문제가 발생한다.
그렇기에 기본 모드 `JPQL` 실행시 자동으로 `플러시`를 호출 한다.

- `플러시` 모드 옵션
    - FlushModeType.AUTO : 커밋이나 쿼리를 실행할 `플러시` (기본값)
    - FlushModeType.COMMIT : 커밋할때만 `플러시`
    - `플러시` 모드 옵션을 건드릴 일이 없다.
<pre>
    <code>entityManager.setFlushMode(FlushModeType.AUTO)</code>
</pre>

<br>

### 준영속
- 영속 상태의 `Entity`가 `영속성 컨텍스트`에서 분리 (detached)
- `영속성 컨텍스트`가 제공하는 기능을 사용 못함
<pre>
    <code>entityManager.detach(entity); // 특정 엔티티만 준영속 상태로 전환</code>
    <code>entityManager.clear();        // 영속성 컨텍스트 초기화</code>
    <code>entityManager.close();        // 영속성 컨텍스트 종료</code>
</pre>

<br>

----

<br>
<br>

### DB 스키마 생성
- JPA는 어플리케이션 로딩 시점에 @Entity 어노테이션이 적용된 클래스들을 쭉 훑고나서 DB `방언`을 통해 적절한 DDL을 자동 생성 해주고 DB 테이블을 생성하는 기능을 제공 한다.
- 자동 생성된 DDL은 주로 로컬 환경에서만 사용하는 것을 권장하고, 운영에서는 다듬고 사용해야 한다.
- 자동 생성 옵션은 persistence.xml에서 다음과 같은 옵션을 설정하면 된다.
<pre>
    <code>&lt;property name="hibernate.hbm2ddl.auto" value="create" /&gt;</code>
</pre>
- 자동 옵션
    - create : 기존 테이블 삭제후 다시 생성 (DROP + CREATE)
    - create-drop : create와 같으나 어플리케이션 종료하는 시점에 테이블 DROP
    - update : 변경 부분만 반영
    - validate : 기존에 존재하던 엔티티와 테이블이 정상 매핑되었는지 확인
    - none : 사용하지 않음 == 주석처리  
    - 운영에서는 절대 create, create-drop, update 사용 하지 말것.
    - 운영은 validate or none을 권장.
    
<br>

#### DDL 생성 기능
- DDL 자동 생성할때만 사용되고, JPA 실행 로직에는 영향을 주지 않는다.
- 제약조건 추가 
    - <pre>
        <code>@Column(unique = true, length = 10)</code>
    </pre>
- 유니크 제약 조건 추가
    - <pre>
        <code>@Table(uniqueConstraints = {</code>
        <code>      @UniqueConstraint(name ="NAME_UNIQUE", columnNames = {"name"})</code>
        <code>})</code>
    </pre>

<br>

### 객체와 테이블 매핑
- **@Entity** : 해당 어노테이션이 붙은 객체는 JPA가 관리, `Entity`라고 한다.
    - <u>기본 생성자 필수 (public or protected)</u>
    - final 클래스, interface, inner 클래스는 `Entity`로 관리를 못한다.
    - 저장할 필드에 final 키워드를 사용하면 안된다.

- **@Table**

    | 속성              |      기능      |  기본값 |
    |------------------|:--------------|:-------|
    |name              | 매핑할 테이블 이름 | 엔티티 이름 |
    |catalog           | DB Catalog    |    |
    |schema            | DB Schema     |    |
    |uniqueConstraints | 유니크 제약 조건  |    |

- **@Column** : 컬럼 매핑

    | 속성                 |      기능      |  기본값  |
    |---------------------|:--------------|:-------|
    |name                 | 필드와 매핑할 테이블 컬럼의 이름 | 객체 필드 이름 |
    |insertable, updatable| 등록, 변경 가능 여부          |True   |
    |nullable             | DDL 기능, null값의 허용 여부 설정, false일 경우 not null 제약조건이 붙음|True    |
    |length               | DDL 기능, 문자 길이 제약 조건 | 255 |
    |columnDefinition     | DDL 기능, 문자열로 직접 컬럼정보 설정 가능.(필드 크기, 디폴트값 등등)| |
    |scale                | DDL 기능, 소수의 자릿수, double, float 타입에는 적용이 안된다. 주로 BigDecimal에 사용| 2 | 
    |precision            | 소숫점을 포함한 전체 자릿 수, 주로 BigDecimal이나 BigInteger에 사용| 19 |
  
- **@JoinColumn**

    | 속성                |      기능                |  기본값 |
    |--------------------|:------------------------|:-------|
    |name                |매핑할 외래키 이름            | 필드명 + '_' + 참조하는 테이블의 기본키 컬럼명 |
    |referencedColumnName|외래키가 참조하는 테이블의 컬럼명 | 참조하는 테이블의 기본키 컬럼명 |
    |foreignKey (DDL)    |외래키 제약 조건              |    |
   
    
- **@Enumerated** : Enum 타입 매핑
    - EnumType.ORDINAL : enum의 순서를 DB에 저장, 사용하지 말것.
    - EnumType.STRING  : enum 이름을 DB에 저장 
    
- **@Temporal** : 날짜 타입 필드 매핑
    - java8 이전은 @Temporal 어노테이션 사용
        - DATE : java.sql.Date (2019-12-30)
        - TIME : java.sql.Time (16:58:00)
        - TIMESTAMP : java.sql.Timestamp (2019-12-30 16:58:00) 
    - java8 이후는 @Temporal 어노테이션 생략 가능
        - 타입에 맞게 알아서 설정해준다.
        - LocalDate : (2019-12-30)
        - LocalDateTime : (2019-12-30 16:58:00)
        
- **@Lob** : VARCHAR 타입보다 큰 CONTENT를 넣고 싶을때 BLOB, CLOB 매핑
    - 문자열일 경우 CLOB로 매핑
    - 나머지는 BLOB로 매핑
    
- **@Transient** : DB랑 관련없는 특정 필드를 매핑하고 싶지 않을때

<br>

### 기본 키 매핑
- @Id : 직접 할당
- @GeneratedValue : 자동 생성

#### @GeneratedValue Strategy
- AUTO : 기본 값, DB `방언`에 맞게 아래 3개 전략중 하나를 선택하여 자동 생성
- IDENTITY : 기본 키 생성을 DB에게 위임, (Auto Increment)
    - 주로 MySQL, PostgreSQL, SQL Server에서 사용
    - IDENTITY 전략은 ID 값을 null로 DB에 던져 DB가 null로 INSERT 쿼리가 날라오면 그때 ID 값을 설정한다. <br>
      그런데 JPA 특징은 트랜잭션 커밋시 DB에 접근하여 쿼리를 수행하는 것인데, <br>
      이렇게 되면 커밋전까지는 ID 값을 null을 가지고 있게되어 `영속성 컨텍스트`에서 관리 할 수 없는 모순이 생긴다.
      
    - 즉, `영속성 컨텍스트`에서 관리 되려면 ID 값이 있어야 하는데 ID 값을 알 수 있는 시점이 DB에 들어가봐야 알 수 있어서 
      트랜잭션 커밋 전까지 알 수 없는 문제점이 생겼다.
    - **그렇기 때문에 JPA에서는 IDENTITY 전략만 예외로 .persist(`Entity`) 실행시 DB에 접근하여 바로 INSERT 쿼리를 날리고
      내부적으로 JPA가 ID 값을 조회해서 `영속성 컨텍스트`에 저장하여 관리할 수 있도록 되어 있다.**
    
- SEQUENCE : DB에 SEQUENCE_OBJECT를 만들어 유일한 값을 순서대로 생성하여 사용
    - 주로 Oracle, PostgreSQL에서 사용
    - 자동으로 생성된 SEQUENCE_OBJECT의 이름은 데이테베이스 명을 따라간다. (ex. Hibernate_Sequence)
    - 테이블 마다 SEQUENCE_OBJECT를 관리하려면 다음과 같이 설정한다.
        <pre>
            <code>@Entity</code>
            <code>@SequenceGenerator(</code>
            <code>      name = "MEMBER_SEQ_GENERATOR"</code>
            <code>      ,sequenceName = "MEMBER_SEQ" // 매핑할 DB 시퀀스 이름</code>
            <code>      ,initialValue = 1, allocationSize = 1 )</code>
            <code>public class Member {</code>
            <code>    @Id</code>
            <code>    @GeneratedValue(strategy = GenerationType.SEQUENCE</code>
            <code>                   ,generator = "MEMBER_SEQ_GENERATOR")</code>
            <code>    private Long id;</code>
            <code>}</code>
        </pre>  
    
    - @SequenceGenerator
    
        | 속성              |      기능           |  기본값 |
        |------------------|:-------------------|:-------|
        |name              | 시퀀스 생성기 이름      |  |
        |sequenceName      | DB에 등록된 시퀀스 이름  | hibername_sequence |
        |initialValue      | DDL 생성시에만 사용됨, 초기값 | 1   |
        |allocationSize    | 시퀀스 호출 시 증가하는       | 50  |
    
    - SEQUENCE 전략 또한 IDENTITY 전략과 비슷한 문제가 존재한다. <br>
    ID 값을 DB에서 시퀀스로 생성되도록 위임한 전략이기 때문에 JPA가 ID 값을 알 수 있는 방법이 없기에
    .persist(`Entity`) 수행시 시퀀스를 미리 조회 해서 `영속성 컨텍스트`에서 관리 한다.
        <pre><code>call next value for MEMBER_SEQ</code></pre>
    
    <br>
    
    - 이렇게 시퀀스를 자주 조화할 경우 네트워크 접속이 불가피하여 allocationSize를 기본값 (50)으로 할 경우
     내부적으로 .persist(`Entity`) 수행시 DB의 시퀀스를 50개를 증가시키고 갖고와 어플리케이션에서
     50개를 내부적으로 갖고있어 순차적으로 증가시키는 방법을 사용한다. 동시성 이슈 없이 사용이 가능 하다.
    
        | DB | MEMORY |
        |----|:-------|
        |1   | 1      |
        |51  | 1      |
        |51  | 2      | 
        |51  | ....   |
        |51  | 51     |
        |101 | 51     |
        |101 | 52     |
        |101 | ....   |
        
    - 시퀀스를 증가 시킨후 ROLLBACK시 시퀀스는 ROLLBACK없이 숫자사이 구멍이 생긴채로 사용 된다.
    
- TABLE : 키 생성 전용 테이블을 하나 만들어서 시퀀스 흉내내는 전략
    - 장점 : 어떤 DB는 Auto Increment, 어떤 DB는 시퀀스를 사용하기에 둘중 하나를 선택해야 하는데,
            TABLE 전략은 위와 상관없이 모든 DB에 사용 가능
    - 단점 : Lock 등의 성능 이슈
    <pre>
        <code>@Entity</code>
        <code>@TableGenerator(</code>
        <code>      name = "MEMBER_SEQ_GENERATOR"</code>
        <code>      ,table = "MY_SEQENCES" // 시퀀스를 관리할 테이블 명</code>
        <code>      ,pkColumnValue = "MEMBER_SEQ" // 시퀀스 컬럼</code>
        <code>      ,allocationSize = 1 )</code>
        <code>public class Member {</code>
        <code>    @Id</code>
        <code>    @GeneratedValue(strategy = GenerationType.TABLE</code>
        <code>                   ,generator = "MEMBER_SEQ_GENERATOR")</code>
        <code>    private Long id;</code>
        <code>}</code>
    </pre> 


<br>
<br>
<br>

## 연관관계 매핑
- 단방향 관계 : 두 `Entity`가 관계를 맺을 때 한 쪽의 `Entity`만 참조하는 것
- 양방향 관계 : 두 `Entity`가 서로 참조 하는 것
- 데이터 모델링은 관계를 맺으면 양방향 관계로 자동 매핑이 되지만 <br>
  객체지향 모델링은 어떤 `Entity`가 중심이냐에 따라 단방향인지, 양방향인지 선택 해야 한다.
  
- `Entity`들은 대부분 다음중 하나의 관계를 맺고 있기에 서로 어떤 연관 관계를 맺는지 파악하는것은 매우 중요하다.
    - 다대일 (N : 1) : @ManyToOne
    - 일대다 (1 : N) : @OneToMany
    - 일대일 (1 : 1) : @OneToOne
    - ~~다대다 (N : M) : @ManyToMany~~
- 기존 테이블의 연관 관계 매핑은 `외래 키 식별자`를 통해 서로간의 연관 관계를 관리 하였다.
- 객체 지향 설계의 목표는 자율적인 객체들의 협력 공동체를 만드는 것.
- 객체를 테이블에 맞춰 데이터 중심(`외래 키 식별자`)으로 모델링을 하게 된다면 협력 관계를 만들 수 없다.
    - ex) 회원은 하나의 팀에만 소속되고, 회원과 팀은 다대일 관계일때
    
    <pre>
        <code>Team team = new Team(); </code>
        <code>team.setName("TeamA"); </code>
        <code>em.persist(team); </code>
        <code></code>
        <code>Member member = new Member(); </code>
        <code>member.setName("member1"); </code>
        <code>member.setTeamId(team.getId()); </code>
        <code>em.persist(member); </code>
        <code></code>
        <code>Member findMember = em.find(Member.class, member.getId();</code>
        <code>Long findTeamId = findMember.getId();</code>
        <code>Team findTeam = em.find(Team.class, findTeamId);</code>
    </pre>
    
    - 위와 같이 테이블은 `외래 키 식별자`로 조인을 사용해서 연관된 테이블을 찾는다. <br>
    
    - 그러나 객체 지향 방식은 **참조**를 사용해서 연관된 객체를 찾기때문에 객체 지향적인 방법이 아니다.
    - JPA에서는 연관 관계에 있는 테이블의 PK를 멤버변수로 갖지 않고, **`Entity` 객체 자체를 통째로 참조한다.**
    
    <pre>
        <code>Team team = new Team(); </code>
        <code>team.setName("TeamA"); </code>
        <code>em.persist(team); </code>
        <code></code>
        <code>Member member = new Member(); </code>
        <code>member.setName("member1"); </code>
        <code>member.setTeam(team);  // 객체 참조</code>
        <code>em.persist(member); </code>
        <code></code>
        <code>Member findMember = em.find(Member.class, member.getId();</code>
        <code>Team findTeam = findMember.getTeam();</code>
    </pre>
    
    - JPA에서는 **`Entity` 참조**를 통해 연관 관계를 맺을 수 있다.
  
<br>

### 객체와 테이블의 관계 차이
- 테이블 연관 관계
    - 회원 <-> 팀 양방향 1
    - 테이블은 `외래 키 식별자` 하나로 두 테이블의 양방향 연관 관계를 가진다.
    - 즉, 양쪽으로 조인이 가능하다.
- 객체 연관 관계
    - 회원 -> 팀 단방향 1개
    - 팀 -> 회원 단방향 1개 
    - 참조용 필드가 있는 쪽으로만 참조 가능
    - **객체의 양방향 관계는 사실 서로 다른 단방향 관계가 2개가 존재하는 것이다.**
- 만약 회원이 새로운 팀으로 바꿔야 할 경우, 
    - 회원의 팀 값을 새로운 팀 값으로 변경되어야 하는지?
    - 아니면 팀에 있는 회원들(members)의 값을 바꿔야 할지?

- `Entity`를 양방향 연관 관계로 설정하면 객체의 참조는 둘인데 어떤 객체를 기준으로 `외래 키`를 설정해야 할지 딜레마가 발생한다.
    - 회원에 있는 팀으로 `외래 키`를 관리 할지, 팀에 있는 회원들로 `외래 키`로 관리 할지
- 즉, 두 객체 연관 관계 중 하나를 정해서 테이블의 `외래 키`를 관리해야 하는데 이것을 `주인`이라고 한다.

<br>

### 연관 관계의 주인 (Owner)
- 양방향 매핑 규칙으로써, 객체의 두 관계중 하나를 연관 관계의 `주인`으로 지정
- 비지니스 로직 기준이 아닌 **외래키 위치 기준**으로 `주인`을 지정해야 한다.
- **`주인`만이 `외래 키`를 관리 (등록, 수정) 할 수 있다.**
- `주인`은 `mappedBy` 속성 사용을 할 수 없다.

- **`주인`이 아닌 쪽은 `외래 키`를 읽기만 가능 하다.**
- `주인`이 아닌 쪽은 `mappedBy` 속성으로 `주인`을 지정 해야한다.

- Who is Owner ?
    - **`외래 키`가 있는 곳을 `주인`으로 정한다.**
    - 통상 다대일 관계일때, 다 쪽에 연관 관계 `주인`으로 정하면 쉽다.
    - 위에 예제로는 회원이 `주인`이 된다.

<br>

### 단방향 연관 관계
- 한쪽의 `Entity`가 상대 `Entity`를 참조하는 것을 말한다.
- `다대일 관계 (N : 1)`
    - ex) 여러 회원은 하나의 팀에만 소속되고, 회원과 팀은 `다대일 관계 (N : 1)`일때
    - `@ManyToOne`
        
        | 속성              |      기능           |  기본값 |
        |------------------|:-------------------|:-------|
        |optional          | false로 설정하면 연관된 엔티티는 항상 존재해야함 | true |
        |fetch             | 글로벌 페치 전략 설장한다.  | 즉시 로딩 |
        |cascade           | 영속성 전이 기능을 사용한다. |   |
                
    - **회원 입장(`Entity` 자신을 기준으로)** 에서는 다수고 Team으로는 One이기 때문 `@ManyToOne` 어노테이션을 통해 매핑
    - `외래 키 식별자`를 매핑하기 위해 `@JoinColumn` 어노테이션을 사용한다.
    - name 속성에는 매핑할 외래 키 컬럼명을 지정 한다.
    <pre>
        <code>public class Member {</code>
        <code>    ...</code>
        <code>    @ManyToOne</code>
        <code>    @JoinColumn(name = "TEAM_ID)"</code>
        <code>    private Team team</code>
        <code>}</code>
    </pre>
    - 단방향 관계를 맺었으므로 외래키가 생겼기 때문에 현재 회원은 팀의 정보를 갖고 올 수 있게 되었다.
    - 팀에서 회원 정보를 갖고 오려면 ?  ->  양방향 연관 관계를 맺어야 한다.  
<br>

### 양방향 연관 관계
- 양쪽 `Entity`들을 서로 참조해서 관계를 맺을 수 있다.
- 단방향 매핑만으로도 이미 양방향 연관 관계 매핑은 완료. **JPA 설계시 단방향 연관 관계 매핑으로 우선시 한다.**
- JPQL에서 역방향으로 탐색할 일이 많음. 그럴때마다 양방향을 추가하면 된다.
- `다대일 관계 (N : 1)`
    - ex) 여러 회원은 하나의 팀에만 소속되고 하나의 팀은 여러 회원을 가질때,
    - `@OneToMany`
        
        | 속성              |      기능           |  기본값 |
        |------------------|:-------------------|:-------|
        |mappedBy          | 연관 관계의 주인 필드를 선택한다. |  |
        |fetch             | 글로벌 페치 전략 설장한다.  | 즉시 로딩 |
        |cascade           | 영속성 전이 기능을 사용한다. |   |
    
    - **팀 입장(`Entity` 자신을 기준으로)** 에서는 One이고 회원들은 다수이기 때문 `@OneToMany` 어노테이션을 통해 매핑
    - `mappedBy` 속성을 통해 회원 테이블(`주인`) 쪽의 어떤 필드와 관계가 있는지 매핑해줘야 한다.
    - 여러 회원을 가질 수 있도록 회원 컬렉션을 필드로 가진다. + 동시에 초기화 해주도록 한다.
    <pre>
        <code>public class Team {</code>
        <code>    ...</code>
        <code>    @OneToMany(mappedBy = "team")</code>
        <code>    List&lt;Member&gt; members = new ArrayList&lt;&gt;();</code>
        <code>}</code>
    </pre>
    
    <pre>
        <code>public class Member {</code>
        <code>    ...</code>
        <code>    @ManyToOne</code>
        <code>    @JoinColumn(name = "TEAM_ID)"</code>
        <code>    private Team team</code>
        <code>}</code>
    </pre>

 
- 양방향 매핑시 가장 많이 하는 실수
    - 연관 관계 `주인`에다가 값을 입력하지 않고 `주인`이 아닌곳에 값을 넣는다.
        <pre>
            <code></code>
            <code>Member member = new Member(); </code>
            <code>member.setName("member1"); </code>
            <code>em.persist(member); </code>
            <code></code>
            <code>Team team = new Team(); </code>
            <code>team.setName("TeamA"); </code>
            <code>team.getMembers().add(member); // 역방향에만 연관 관계 설정하는 실수 조심</code>
            <code>em.persist(team); </code>
        </pre>
    
    - `mappedBy`속성은 읽기만 가능하고 JPA에서는 INSERT나 변경시 쳐다도 보지 않기 때문에 값을 넣을 필요가 없다.
    - `주인`에 값을 넣고 역방향에도 넣어준다.
        <pre>
            <code>Team team = new Team(); </code>
            <code>team.setName("TeamA"); </code>
            <code>em.persist(team); </code>
            <code></code>
            <code>Member member = new Member(); </code>
            <code>member.setName("member1"); </code>
            <code>member.setTeam(team); // 주인에 값 설정 OK!</code>
            <code>em.persist(member); </code>
            <code>team.getMembers().add(member); // 역방향 OK!</code>
        </pre>
    
    - 역방향에도 넣어줘야 하는 이유는 아래 코드와 같이 해버리면 team `Entity`가 members에 값이 없는 상태로 <br>
      `1차 캐시`에 등록되어 커밋 전이라 .find(DB 조회)를 해도 members를 갖고올 수 없음. <br>
      <pre>
          <code>Team team = new Team(); </code>
          <code>team.setName("TeamA"); </code>
          <code>em.persist(team); </code>
          <code></code>
          <code>Member member = new Member(); </code>
          <code>member.setName("member1"); </code>
          <code>member.setTeam(team); // 주인에 값 설정 OK!</code>
          <code>em.persist(member); </code>
          <code>Team findTeam = em.find(Team.class, team.getId();</code>
          <code>List&lt;Member&gt; members = findTeam.getMembers(); // size 0 !</code>
      </pre>
      
      그렇기에 .find(DB 조회)전에 `flush` + clear를 하여 `1차 캐시`를 비워 재조회 하도록 하거나 **역방향에 넣어준다.** <br>
      **항상 양쪽에 값을 설정하는 습관을 들이자.**
        
    - 또는 `주인`쪽 or 역방향에다가 연관 관계 편의 메서드를 생성하자.
        <pre>
            <code>public class Member {</code>
            <code>    ...</code>
            <code>    @ManyToOne</code>
            <code>    @JoinColumn(name = "TEAM_ID)"</code>
            <code>    private Team team</code>
            <code>    public void changeTeam(Team team) {</code>
            <code>        this.team = team;</code>
            <code>        this.team.getMembers().add(this);</code>
            <code>    }</code>
            <code>}</code>
        </pre>
        
    - 양방향 매핑시 무한루프를 조심하자.
        - toString()이 양쪽으로 오버라이드 되어있으면 무한호출 된다. (양쪽 객체를 필드로 가지고 있기 때문)
        - JSON 생성 라이브러리를 조심하자. **컨트롤러에서 절대 `Entity`를 바로 반환하지 말자.**
            - `Entity`를 바로 반환해버리면 JSON 생성 라이브러리로 인해 toString()이 호출된다.
            - `Entity` 대신 DTO로 반환하자.

<br>
<br>

### 1:N 관계
- `일대다 단방향`은 일(1)이 연관 관계의 `주인`이다.
- 문제는 테이블 일대다 관계는 항상 다(N)쪽에 `외래 키`가 있다. 
- 테이블과 객체 패러다임 차이로 인해 `주인` 반대편에서 `외래키`를 관리하는 특이성이 생겼다.
<pre>
    <code>public class Team {</code>
    <code>    ...</code>
    <code>    @OneToMany</code>
    <code>    @JoinColumn(name = "TEAM_ID")</code>
    <code>    List&lt;Member&gt; members = new ArrayList&lt;&gt;();</code>
    <code>}</code>
</pre>

- 일(1) `Entity`에 @JoinColumn을 꼭 사용해야 한다. 그렇지 않으면 조인테이블(중간 테이블)이 생긴다.
<pre>
    <code>create table Team_Member ( ... )</code>
</pre>

- 팀의 members값이 변경되면 회원의 teamId (`외래키`)를 변경해주어야 한다.
- 즉, 연관 관계 관리를 위해 추가로 옆 테이블의 update sql을 실행한다.
- 실무에서 지양하는 연관 관계 (테이블이 수십개가 엮어서 진행할 경우 외래키가 있는 테이블이 update 되므로 조심해야함)
- 일대다 단방향 매핑보다 다대일 양방향 연관 관계를 맺는 방식을 더 선호.
<pre>
    <code>Member member = new Member(); </code>
    <code>member.setName("member1"); </code>
    <code>em.persist(member); </code>
    <code></code>
    <code>Team team = new Team(); </code>
    <code>team.setName("TeamA"); </code>
    <code>team.getMembers().add(member); // MEMBER 테이블에 있는 TEAM_ID 외래키 update가 된다.</code>
    <code>em.persist(team); </code>
</pre>
    
<br>

### 1:1 관계
- @OneToOne
- `주인`으로 주 테이블이나 대상 테이블 중에 선택
    - 즉, 둘중 한군데만 넣으면 된다.
    - 주 테이블에 `외래 키`
        - 다대일 단방향(@ManyToOne)과 유사하다.
        - `주인`은 @JoinColumn은 필수로 붙여준다. `주인` 반대는 mappedBy를 붙여준다.
        - 장점은 주 테이블만 조회해도 대상 테이블에 데이터가 있는지 확인 가능
        - 단점은 : 값이 없으면 외래키에 null을 허용해야 한다.
    - 대상 테이블에 `외래 키`
        - 주 테이블에 `외래 키` 방법과 비슷하다.
        - 장점 : 1:1 -> 1:N으로 변경될 경우 테이블 구조를 유지할 수 있다.
        - 단점 : `프록시`의 한계로 인해 지연 로딩으로 설정해도 항상 즉시 로딩이 된다.
- `외래 키`에 DB UNIQUE 제약 조건을 추가해야 1:1 관계가 성립된다.
- 명확한 1:1 관계라면 주 테이블에 `외래 키`를 권장 한다.
    - 주 테이블은 주로 실무에서 사용하고 있으므로 미리 조회되어 있는 상태라 조금이나마 성능 최적가 있다.

<br>

### N:M 관계
- RDB는 다대다 관계를 표현할 수 없다.
- 다만 객체는 컬렉션을 사용해서 객체 2개로 다대다 관계가 가능하다.
- 중간테이블(조인테이블)을 추가해서 다대일 or 일대다 관계로 풀어내야 한다.
- 객체는 되고 테이블은 안되므로 JPA에서 @ManyToMany를 지원해주긴 하는데 지양하도록 하자.
- 회원은 여러 상품을 가질 수 있고, 여러명의 회원에 상품이 포함될 수 있을때,
    <pre>
        <code>public class Member {</code>
        <code>    ...</code>
        <code>    @ManyToMany</code>
        <code>    @JoinTable(name = "MEMBER_PRODUCT")</code>
        <code>    List&lt;Product&gt; products = new ArrayList&lt;&gt;();</code>
        <code>}</code>
    </pre>
    - 각 테이블의 PK들이 중간테이블에는 FK 되는 구조
    - 현재는 단방향인데 양방향으로 하려면
     <pre>
         <code>public class Product {</code>
         <code>    ...</code>
         <code>    @ManyToMany(mappedBy = "products")</code>
         <code>    List&lt;Member&gt; members = new ArrayList&lt;&gt;();</code>
         <code>}</code>
     </pre>   
- 실무에서 사용 X, 중간 테이블에 컬럼 추가가 불가능.   

<br>

#### 다대다 대체
- 연결 테이블용 `Entity` 추가 (연결 테이블을 `Entity`로 승격)
- N:M 관계를 중간테이블을 이용해서 1:N, N:1
- @ManyToMany -> @OneToMany, @ManyToOne
    <pre>
        <code>public class MemberProduct {</code>
        <code>    @Id</code> 
        <code>    Long id; // 각 FK를 PK로 가지는것보다 별도로 PK를 관리하는것을 선호하자.</code> 
        <code>    @ManyToOne</code>
        <code>    @JoinColumn(name = "MEMBER_ID")</code>
        <code>    Member member;</code>
        <code>    @ManyToOne</code>
        <code>    @JoinColumn(name = "PRODUCT_ID")</code>
        <code>    Product product;</code>
        <code>    ...</code>
        <code>}</code>
        <code></code>
        <code>public class Member {</code>
        <code>    @OneToMany(mappedBy = "member")</code>
        <code>    List&lt;MemberProduct&gt; memberProducts = new ArrayList&lt;&gt;();</code>
        <code>    ...</code>
        <code>}</code>
    </pre>

<br>

##### @ManyToOne에 mappedBy 속성이 없는 이유
- mappedBy 스펙을 양쪽 연관 관계에다가 풀게되면 JPA는 개발자에게 잘못된 관계를 맺도록 <br>
  선택의 여지를 열어주게 되어 혼란만 가중시키기 때문에
  
<br>
<br>

### 상속 관계
- 객체의 상속구조와 RDB의 Super타입 - Sub타입 관계를 매핑하는 방법.
- 객체는 상속 관계가 있지만 RDB는 상속 관계 대신 Super타입 - Sub타입 관계라는 논리적인 모델링 기법이 있다.
- JPA는 RDB의 Super타입 - Sub타입 논리적인 기법을 물리적인 모델인 상속 관계로 해결하기위해 3가지의 방식을 지원한다.
    - 조인 전략 : 부모의 PK를 자식들이 FK를 갖는 방법
    - 단일 테이블 전략 : 부모 - 자식 관계를 하나의 테이블로 합치는 방법 (Default 전략)
    - ~~서브타입 테이블 변환 전략 : 부모의 속성들을 자식들이 속성으로 갖는 방법~~

- ex) 물품은 앨범, 영화, 책으로 관계가 이루어져 있을때, 
<pre>
    <code>@Entity</code>
    <code>public abstract class Item {</code>
    <code>    @Id</code>
    <code>    @GenerateValue</code>
    <code>    private Long id;</code>
    <code>    private String name;</code>
    <code>    private price;</code>
    <code>}</code>
    <code></code>
    <code>@Entity</code>
    <code>public class Album extends Item {</code>
    <code>    private String artist;</code>
    <code>}</code>
    <code></code>
    <code>@Entity</code>
    <code>public class Movie extends Item {</code>
    <code>    private String director;</code>
    <code>    private String actor;</code>
    <code>}</code>
    <code></code>
    <code>@Entity</code>
    <code>public class Book extends Item {</code>
    <code>    private String author;</code>
    <code>    private String isbn;</code>
    <code>}</code>
</pre>

- 기본으로 단일 테이블 전략으로 설정됨.
<pre>
    <code>create table Item (</code>
    <code>    ITEM_ID bigint not null,</code>
    <code>    name varchar(255),</code>
    <code>    price integer not null,</code>
    <code>    author varchar(255),</code>
    <code>    artist varchar(255),</code>
    <code>    isbn varchar(255),</code>
    <code>    ...</code>
    <code>    primary key (ITEM_ID)</code>
</pre>

#### 조인 전략
- JPA와 가장 유사한 모델
- @Inheritance : 어떤 상속 전략을 사용할지 ( 기본값 단일 테이블 )
- @DiscriminatorColumn : 부모 `Entity`에 사용하며, 부모가 자식을 구분할때 자식의 테이블 명이 부모 테이블에 값으로 저장됨.
- @DiscriminatorColumn을 항상 정의 하는것이 좋다.
    - ex) Item table
        
        | ITEM_ID | DTYPE | NAME  | PRICE |
        |---------|:------|:------|-------|
        | 1       | Movie | 겨울왕국| 10000 |
        | 2       | Book  | 클린코드| 10000 |
        
    - name : 컬럼 명 변경 가능 (기본 "DTYPE")
    
- @DiscriminatorValue : 자식 `Entity`에 사용하며, 부모 테이블의 DTYPE에 자식 테이블 명이 아닌 설정한 값으로 변경이 가능하다.
    - ex) Item table
        
        | ITEM_ID | DTYPE | NAME  | PRICE |
        |---------|:------|:------|-------|
        | 1       | A     | 겨울왕국| 10000 |
        | 2       | B     | 클린코드| 10000 |
<pre>
    <code>@Entity</code>
    <code>@Inheritance(strategy = InheritanceType.JOINED) // 조인 전략 설정</code>
    <code>@DiscriminatorColumn</code>
    <code>public abstract class Item {</code>
    <code>    @Id</code>
    <code>    @GenerateValue</code>
    <code>    private Long id;</code>
    <code>    private String name;</code>
    <code>    private price;</code>
    <code>}</code>
</pre>
<pre>
    <code>create table Item (</code>
    <code>    ITEM_ID bigint not null,</code>
    <code>    DTYPE varchar(31) not null, // @DiscriminatorColumn</code>
    <code>    name varchar(255),</code>
    <code>    price integer not null,</code>
    <code>    primary key (ITEM_ID)</code>
    <code>)</code>
    <code></code>
    <code>create table Album (</code>
    <code>    artist varchar(255),</code>
    <code>    id bigint not null,</code>
    <code>    primary key (id)</code>
    <code>)</code>
    <code>...</code>
</pre>

- Item의 id가 Sub 테이블들의 PK + FK로 가지게 된다.
- INSERT 쿼리를 실행하면 먼저 Item에 INSERT후 해당 Sub클래스를 INSERT 한다.
- SELECT시에는 조인해서 갖고 오게 된다.
- 장점 
    1. 정규화가 되어 만들어지기 때문에 데이터 중복 제거
    2. 외래 키 참조 무결성 제약조건 활용 가능
    3. 저장공간 효율화
- 단점
    1. 조회시 조인을 사용되어 성능 저하
    2. 조회 쿼리 복잡함  
    
#### 단일 테이블 전략
- @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
- 하나의 테이블이 부모 - 자식 속성들을 다 관리한다.
- 자식 테이블을 구별하는 방법은 @DiscriminatorColumn을 통해 구별한다.
<pre>
    <code>create table Item (</code>
    <code>    ITEM_ID bigint not null,</code>
    <code>    name varchar(255),</code>
    <code>    price integer not null,</code>
    <code>    author varchar(255),</code>
    <code>    artist varchar(255),</code>
    <code>    isbn varchar(255),</code>
    <code>    ...</code>
    <code>    DTYPE varchar(31) not null, // @DiscriminatorColumn</code>
    <code>    primary key (ITEM_ID)</code>
</pre>
- 장점 
    1. 조인이 필요 없어서 성능이 빠름
    2. 조회 쿼리 단순함
- 단점
    1. 자식 테이블의 컬럼들의 null 허용
    2. 테이블이 커지면 문제임.

#### 각 구현 클래스 전략
- @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
- 부모 테이블은 만들어지지않고 부모 클래스의 속성들을 가진 자식 테이블들만 만들어 진다.
    - @DiscriminatorColumn가 필요 없다.
- 이 전략은 사용하지 말것.
- 단점
    1. join이 아닌 union을 사용하므로 복잡하고 비효율적으로 동작함.
    2. 자식 테이블을 통합해서 쿼리하기 어려움.
    