package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Entity
@Table(name = "roles")
@Data
@Builder
public class Role {
    @Id
    private Integer roleId;

    @Column(name = "name")
    private String roleName;

    @Column(name = "access_level")
    private Integer accessLevel;
}
