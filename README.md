# JPA

- Java Persistence Api
- ORM 기술 표준
- 자바 어플리케이션과 JDBC API 사이에서 동작 <br>
(JPA가 JDBC 사용하는 것, 대신 쿼리를 개발자가 아닌 JPA가 만들어 준다.)
- JPA는 인터페이스의 모음   

## ORM
---
Obejct Relational Mapping (객체 관계 매핑)
- 객체는 객체대로 설계
- 관게형 DB는 관계형 DB에 맞게 설계
- ORM 프레임워크가 중간에서 매핑 

기존에는 
#### 1. SQL에 의존적인 개발
#### 2. 객체와 관계형 DB의 "패러다임 불일치"
객체지향 : 추상화, 정보은닉, 상속 등을 통해 시스템의 복잡성을 제어 <br>
관계형 DB : 데이터를 잘 정규화하여 보관 <br>

- 관계형 DB는 객체지향적 특징(상속, 다형성, 레퍼런스, 오브젝트 등)과 같은 유사한 개념이 <br> 존재하지 않는다.

- 연관 관계 (mapping) : 객체는 단반향으로 연관 관계가 맺어지는데, 관계형 DB는 PK, FK를 통해 양뱡향 연관 관계를 맺는다.

- 식별성 (identity) : 관계형 데이터는 PK라는 기본키를 통해 데이터를 식별이 가능하지만
자바에서는 객체의 식별성과 동일성을 모두 정의하여 일치함을 정의한다.

- 실행하는 SQL에 따라 객체 탐색 범위가 결정되기 때문에 내부를 보지않는 이상 엔티티를 신뢰하기 힘들다.
<pre>
    <code>SELECT M.*, T.*</code>
    <code>  FROM MEMBER M</code>
    <code> INNER JOIN TEAM T</code>
    <code>    ON ...</code>
</pre>
<pre>
    <code>Member member = memberDao.find(memberId);</code>
    <code>member.getTeam() // ?</code>
    <code>member.getOrder() // ?</code>
    <code>member.getDelivery() // ?</code>
</pre>

등의 문제로 인해 개발자가 객체답게 모델링 할수록 매핑작업만 늘어나는 단점이 있었다. <br>
위에 문제들을 해결하여 객체를 자바 컬렉션에 저장하고 조회하듯 DB에 저장할 수 있도록 <br>
중간에서 ORM 프레임워크를 통해 개발자는 객체지향대로, 관계형 DB는 관계형 DB답게 설계가 가능하다.

<br>
<br>

## JPA 성능 최적화 지원
---
1. 1차 캐시와 동일성(identity) 보장
    - 동일한 트랜잭션에서 조회한 엔티티는 같음을 보장.
    - `DB Isolaion level`이 Read Commit이어도, 어플리케이션에서 Repeatable Read 보장.
2. 트랙잭션을 지원하는 쓰기 지연 (Transactional write-behind)
    - 버퍼링과 비슷함.
    - 트랜잭션을 커밋할때까지 INSERT SQL문을 모은 후, `JDBC BATCH SQL`기능을 통해
    한번에 SQL기능을 전송
3. 지연 로딩 (Lazy Loading)과 즉시 로딩
    - 지연 로딩 : 객체가 실제 `사용`될 때 로딩
    <pre>
        <code>Member member = memberDao.find(memberId); // SELECT * FROM MEMBER</code>
        <code>Team team = member.getTeam();</code>
        <code>String teamName = team.getName(); // SELECT * FROM TEAM</code>
    </pre>
    - 즉시 로딩 : JOIN SQL로 한번에 연관된 객체까지 `미리` 조회
    <pre>
        <code>Member member = memberDao.find(memberId); // SELECT * FROM MEMBER JOIN TEAM..</code>
        <code>Team team = member.getTeam();</code>
        <code>String teamName = team.getName();</code>
    </pre>
    - 옵션을 통해 지연 로딩, 즉시 로딩을 설정할 수 있다.

    


