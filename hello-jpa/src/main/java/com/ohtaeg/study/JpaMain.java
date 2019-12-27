package com.ohtaeg.study;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

public class JpaMain {
    public static void main(String[] args) {
        final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("hello");
        final EntityManager entityManager = entityManagerFactory.createEntityManager();

        final EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        try {
            // insert
            /*
            Member member = new Member();
            member.setId(1L);
            member.setName("test");
            entityManager.persist(member);
             */

            // select
            /*
            Member findMember = entityManager.find(Member.class, 1L);
            System.out.println("id = " + findMember.getId());
            System.out.println("name = " + findMember.getName());
             */

            // delete
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
//                                                .setFirstResult(1)
//                                                .setMaxResults(10) paging - 1번부터 10개 갖고
                                                .getResultList();

            members.stream()
                    .forEach(member -> System.out.println(member.getName()));

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        } finally {
            entityManager.close();
            entityManagerFactory.close();
        }
    }
}
