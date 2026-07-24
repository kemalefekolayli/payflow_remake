package com.example.payflow_rewrite.Auth.Repository;


import com.example.payflow_rewrite.Auth.Entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<UserEntity, Long> {

        Optional<UserEntity> findByUsername(String username);

        Optional<UserEntity> findByEmail(String email);

        Boolean exitsByUsername(String username);

        Boolean existByEmail(String email);

}
