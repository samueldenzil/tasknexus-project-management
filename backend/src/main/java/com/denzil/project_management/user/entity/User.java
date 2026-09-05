package com.denzil.project_management.user.entity;

import com.denzil.project_management.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;
}
