package org.example.resumeadjuster.Model.Entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;


@Entity
@Table(name="users")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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



    @Getter(AccessLevel.NONE)
    @JsonManagedReference
    @JsonIgnore
    private final Set<UserAuthProvider> authProviders = new HashSet<>();

    // 自定义的安全getter方法 - 添加JsonIgnore
    @JsonIgnore
    public Set<UserAuthProvider> getAuthProviders() {
        return authProviders != null ? new HashSet<>(authProviders) : new HashSet<>();
    }

    // 添加线程安全的修改方法

    public synchronized void addAuthProvider(UserAuthProvider provider) {
        authProviders.add(provider);
        provider.setUser(this);
    }

    public synchronized void removeAuthProvider(UserAuthProvider provider) {
        authProviders.remove(provider);
        provider.setUser(null);
    }


}
