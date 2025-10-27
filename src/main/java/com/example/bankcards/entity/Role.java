package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Role {
    @Id
    private Integer roleId;

    @Column(name = "name")
    private String roleName;

    @Column(name = "access_level")
    private Integer accessLevel;
}
