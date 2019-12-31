# Project
JPA 입문 공부를 위한 프로젝트

<br>

## 환경 세팅 (version)
[Spring boot Dependency versions 참고](https://docs.spring.io/spring-boot/docs/2.2.2.RELEASE/reference/html/appendix-dependency-versions.html#appendix-dependency-versions)
- H2 DB : 1.4.199
- Hibernate : 5.4.9 Final
- JPA 2.2

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

### 트랜잭션을 지원하는 쓰기 지연 ( Transactionl Write-Behind )
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

## 연관관계 매핑

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
    |schema             | DB Schema     |    |
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
    
- **@Enumerated** : Enum타입 매핑
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
            <code>      ,initialValue = 1, allocateSize = 1 )</code>
            <code>public class Member {</code>
            <code>    @Id</code>
            <code>    @GeneratedValue(strategy = GenerationType.SEQUENCE</code>
            <code>                   ,generator = "MEMBER_SEQ_GENERATOR"</code>
            <code>    private Long id;</code>
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
     50개를 내부적으로 갖고있어 순차적으로 증가시키는 방법을 사용한다. <br>
     동시성 이슈 없이 사용이 가능 하다.
    
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
        <code>      ,allocateSize = 1 )</code>
        <code>public class Member {</code>
        <code>    @Id</code>
        <code>    @GeneratedValue(strategy = GenerationType.TABLE</code>
        <code>                   ,generator = "MEMBER_SEQ_GENERATOR"</code>
        <code>    private Long id;</code>
    </pre> 










