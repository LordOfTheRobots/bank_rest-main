package com.example.bankcards.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    private Integer roleId;

    @Column(name = "name")
    private String roleName;

    @Column(name = "access_level")
    private Integer accessLevel;
}
