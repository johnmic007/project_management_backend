package com.example.project_management.repository;

import com.example.project_management.model.Role;
import com.example.project_management.model.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(RoleEnum name);
}
