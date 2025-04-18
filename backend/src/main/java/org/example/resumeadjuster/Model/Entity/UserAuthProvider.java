package org.example.resumeadjuster.Model.Entity;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name="user_auth_providers")
@Data
public class UserAuthProvider {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private User user;

    @Column(name="provider",nullable = false,length = 50)
    private String provider;

    @Column (name="provider_id",nullable=false)
    private String providerId;

    @Column(name="access_token", length=2048)
    private String accessToken;

    @Column(name="refresh_token", length=2048)
    private String refreshToken;



}
