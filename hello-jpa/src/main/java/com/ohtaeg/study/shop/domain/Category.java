package com.ohtaeg.study.shop.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Category {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    // 상위 카테고리, 셀프 조인
    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private Category parent;

    // 자식 카테고리, 셀프 조인 양방향
    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>();

    @ManyToMany
    @JoinTable(name ="CATEGORY_ITEM"
            ,joinColumns = @JoinColumn(name = "CATEGORY_ID") // 중간테이블에서 내가 조인해야할 키는 joinColumns
            ,inverseJoinColumns = @JoinColumn(name = "ITEM_ID") // 중간테이블에서 반대편에서 조인해야할 키는 inverseJoinColumns
    )
    private List<Item> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Category getParent() {
        return parent;
    }

    public List<Category> getChild() {
        return child;
    }

    public List<Item> getItems() {
        return items;
    }
}
