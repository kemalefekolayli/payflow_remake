package com.example.payflow.auth.repository;


import com.example.payflow.auth.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<UserEntity, Long> {

        Optional<UserEntity> findByUsername(String username);

        Optional<UserEntity> findByEmail(String email);

        boolean existsByUsername(String username);

        boolean existsByEmail(String email);

}
