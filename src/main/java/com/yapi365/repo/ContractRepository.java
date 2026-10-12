package com.yapi365.repo;

import com.yapi365.domain.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByProjectIdOrderByEndDateAsc(Long projectId);
    Optional<Contract> findByIdAndProjectId(Long id, Long projectId);
    List<Contract> findByProjectIdAndEndDateBetweenOrderByEndDateAsc(Long projectId, LocalDate from, LocalDate to);
    List<Contract> findByProjectIdAndGuaranteeExpiryBetweenOrderByGuaranteeExpiryAsc(Long projectId, LocalDate from, LocalDate to);
    long countByProjectId(Long projectId);
}
