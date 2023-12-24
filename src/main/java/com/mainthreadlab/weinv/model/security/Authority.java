package com.mainthreadlab.weinv.model.security;


import com.mainthreadlab.weinv.model.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "authorities")
public class Authority extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", unique = true, length = 20)
    private String name;

    @ManyToMany(mappedBy = "authorities", cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    private List<UserAuth> users = new ArrayList<>();

}
