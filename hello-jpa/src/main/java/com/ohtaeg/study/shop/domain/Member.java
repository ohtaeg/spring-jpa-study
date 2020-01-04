package com.ohtaeg.study.shop.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@SequenceGenerator(
        name = "MEMBER_SEQ_GENERATOR"
        ,sequenceName = "MEMBER_SEQ" // 매핑할 DB 시퀀스 이름
        ,initialValue = 1)
@Table(uniqueConstraints = {
        @UniqueConstraint(name ="unq_user_email", columnNames = {"email"})
})
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE
            ,generator = "MEMBER_SEQ_GENERATOR")
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(length = 10, nullable = false)
    private String name;

    private String email;
    private String city;
    private String street;
    private String zipCode;

    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

    public Member() { }

    public Member(final String name, final String email, final String city, final String street, final String zipCode) {
        this.name = name;
        this.email = email;
        this.city = city;
        this.street = street;
        this.zipCode = zipCode;
    }

    public void changeOrders(List<Order> orders) {
        this.orders = orders;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public String getZipCode() {
        return zipCode;
    }

    public List<Order> getOrders() {
        return orders;
    }
}
