package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Role {
    @Id
    private Integer roleId;

    @Column(name = "name")
    private String roleName;

    @Column(name = "description")
    private String description;

    @Column(name = "access_level")
    private Integer accessLevel;
}
