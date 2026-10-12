package com.yapi365.repo;

import com.yapi365.domain.MailRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MailRuleRepository extends JpaRepository<MailRule, Long> {
    List<MailRule> findByActiveTrueOrderByPriorityAsc();
    List<MailRule> findAllByOrderByPriorityAsc();
}
