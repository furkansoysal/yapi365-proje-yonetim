package com.yapi365.repo;

import com.yapi365.domain.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    Optional<AppRole> findByCode(String code);
    List<AppRole> findAllByOrderByIdAsc();
}
