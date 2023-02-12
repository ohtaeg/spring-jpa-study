package org.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Member {
    @Id
    private String id;
    private String userName;
    private Integer age;

    public void setId(String id) {
        this.id = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public Integer getAge() {
        return age;
    }
}
