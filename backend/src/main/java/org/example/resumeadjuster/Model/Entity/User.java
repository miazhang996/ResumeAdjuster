package org.example.resumeadjuster.Model.Entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;




@Entity
@Table(name="users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="user_id")
    private long userId;

    @Column(name="first_name",nullable=false,length=100)
    private String firstName;

    @Column(name="last_name",nullable=false,length=100)
    private String lastName;

    @Column(name="email",nullable=false,unique=true,length=255)
    private String email;


    @Column(name="email_verified")
    private Boolean emailVerified=false;

    @Column(name="password_hash")
    private String passwordHash;

    @CreationTimestamp
    @Column(name="created_at",nullable=false,updatable=false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at",nullable=false)
    private OffsetDateTime updatedAt;

    @Column(name="last_login_at")
    private OffsetDateTime lastLoginAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserAuthProvider> authProviders = new HashSet<>();




}
