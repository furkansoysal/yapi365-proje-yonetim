package com.yapi365.repo;

import com.yapi365.domain.MailLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MailLogRepository extends JpaRepository<MailLog, Long> {
    List<MailLog> findTop50ByOrderByReceivedAtDesc();
    boolean existsByGraphMessageId(String graphMessageId);
}
