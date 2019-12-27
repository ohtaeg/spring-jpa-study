package com.ohtaeg.study;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

public class JpaMain {

    final static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("hello");

    public static void main(String[] args) {
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction transaction = entityManager.getTransaction();
        final Long id = 1L;

        transaction.begin();

        try {
            // 비영속
            Member member = new Member();
            member.setId(id);
            member.setName("test");

            // 영속
            entityManager.persist(member);

            // select
            Member findMember = entityManager.find(Member.class, id);
            System.out.println("id = " + findMember.getId());
            System.out.println("name = " + findMember.getName());

            // 준영속
            //entiryManager.detach(member);

            // 삭제
            //entityManager.remove(findMember);

            /**
             * update
             * jpa를 통해 엔티티를 갖고오게 되면 jpa가 관리를 해주기 때문에
             * 트랜잭션 커밋시점에 데이터 변경시 update 쿼리를 만들어서 날린다.
             * persist()를 사용안해도, 자바 컬렉션처럼 다룰 수 있음.
             */
            //findMember.setName("hello jpa");

            /**
             * JPQL
             * 테이블이 아닌 객채 대상으로 탐색하기 때문에 일반 sql과는 형태가 다름.
             * 방언에 맞게 쿼리가 변경된다.
             * SELECT M
             *   FROM Member M -> Merber 객체를 대상으로 한다.
             *   alias를 통해 해당 객체의 필드들을 갖고올 수 있다.
             *
             *   쿼리 결과
             *   select member0_.id as id1_0_,
             *          member0_.name as name2_0_
             *     from Member member0_
             */
            List<Member> members = entityManager.createQuery("SELECT M. FROM Member M", Member.class)
                                                .setFirstResult(1)
                                                .setMaxResults(10) //paging - 1번부터 10개 갖고와라
                                                .getResultList();

            members.stream()
                    .forEach(m -> System.out.println(m.getName()));

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        } finally {
            entityManager.close();
            entityManagerFactory.close();
        }
    }
}
