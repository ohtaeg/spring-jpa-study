# Project
JPA 입문 공부를 위한 프로젝트


## 환경 세팅 (version)

[Spring boot Dependency versions 참고](https://docs.spring.io/spring-boot/docs/2.2.2.RELEASE/reference/html/appendix-dependency-versions.html#appendix-dependency-versions)
- H2 DB : 1.4.199
- Hibernate : 5.4.9 Final
- JPA 2.2

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

- JPA는 속성을 통해 특정 DB의 방언을 정의할 수 있다.
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
4. JPA의 모든 데이터 변경은 `트랜잭션` 안에서 실행 되어야 한다.

### JPQL
Java Persistence Query Language
- SQL과 비슷한 문법을 가진 `객체지향 쿼리`
- SQL을 추상화한 `객체지향 쿼리`
- JPQL은 테이블이 아닌 `객체`를 탐색
- SQL은 데이터베이스 테이블 대상으로 탐색
- JPQL을 통해 검색할때 엔티티 객체 대상으로 검색이 복잡한 쿼리 수행 가능
- JQPL을 실행하면 방언과 합쳐져 현재 DB에 맞는 SQL로 변경 (방언을 바꿔도 JPQL을 바꿀 필요가 없음.)

