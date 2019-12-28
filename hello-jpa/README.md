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
- JPQL은 테이블이 아닌 **객체**를 탐색
- SQL은 데이터베이스 테이블 대상으로 탐색
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
- 영속 : `EntityManager`를 통해 .persistence(`Entity`)시 `Entity`가 `영속성 컨텍스트`에 관리되는 상태
    - <pre><code>entityManager.persist(member)</code></pre>
    - **영속 되었다고해서 DB에 쿼리가 날라가지 않는다. 즉, DB에 바로 저장되지 않는다.** <br>
    - **트랜잭션 commit시 `영속성 컨텍스트`에 있는(영속 되어있는) `Entity`가 DB에 저장이 된다.**
- 준영속 : `Entity`를 `영속성 컨텍스트`에서 분리
    - <pre><code>entityManager.detach(member)</code></pre>
- 삭제 : 객체를 삭제, 영구 저장된 데이터를 DB에서 지운다.
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

### 트랜잭션을 지원하는 쓰기 지연 ( Transactionl Wrtie-Behind )
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




