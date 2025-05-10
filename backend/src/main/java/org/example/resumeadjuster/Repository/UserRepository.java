package org.example.resumeadjuster.Repository;

import org.example.resumeadjuster.Model.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

@Repository

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // PostgreSQL 序列重置方法
    @Modifying
    @Transactional
    @Query(value = "ALTER SEQUENCE users_user_id_seq RESTART WITH 1", nativeQuery = true)
    void resetPostgresSequence();

    // MySQL 自增重置方法
    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE users AUTO_INCREMENT = 1", nativeQuery = true)
    void resetMySQLAutoIncrement();

    // H2 数据库序列重置方法
    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1", nativeQuery = true)
    void resetH2Sequence();

    // 删除所有认证提供商的方法
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_auth_providers", nativeQuery = true)
    void deleteAllAuthProviders();

    // 删除所有用户的方法
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users", nativeQuery = true)
    void deleteAllUsers();
}
