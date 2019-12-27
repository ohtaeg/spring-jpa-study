package com.ohtaeg.study;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
//@Table(name = "USER")
public class Member {

    @Id
    private Long id;

    //@Column(name ="USER_NAME")
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
