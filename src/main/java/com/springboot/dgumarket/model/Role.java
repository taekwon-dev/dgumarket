package com.springboot.dgumarket.model;

import com.springboot.dgumarket.model.member.Member;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by TK YOUN (2020-10-20 오전 8:16)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */



@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 50)
    private String name;

    @ManyToMany(mappedBy = "roles")
    private Set<Member> members = new HashSet<>();

}
