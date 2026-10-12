package com.yapi365.web;

import com.yapi365.domain.Project;
import com.yapi365.domain.SyncStatus;
import com.yapi365.repo.ContractRepository;
import com.yapi365.repo.DocumentRepository;
import com.yapi365.repo.DocumentVersionRepository;
import com.yapi365.repo.MailLogRepository;
import com.yapi365.service.AuditService;
import com.yapi365.service.ProjectAccessService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    private final ProjectAccessService access;
    private final ContractRepository contracts;
    private final DocumentRepository documents;
    private final DocumentVersionRepository versions;
    private final MailLogRepository mailLogs;
    private final AuditService audit;

    public DashboardController(ProjectAccessService access, ContractRepository contracts, DocumentRepository documents,
                               DocumentVersionRepository versions, MailLogRepository mailLogs, AuditService audit) {
        this.access = access;
        this.contracts = contracts;
        this.documents = documents;
        this.versions = versions;
        this.mailLogs = mailLogs;
        this.audit = audit;
    }

    @GetMapping("/")
    public String dashboard(Authentication auth, HttpSession session, Model model) {
        Project p = access.current(auth, session);
        LocalDate today = LocalDate.now();
        model.addAttribute("contractCount", contracts.countByProjectId(p.getId()));
        model.addAttribute("documentCount", documents.countByProjectId(p.getId()));
        model.addAttribute("pendingSync", versions.countByDocumentProjectIdAndSyncStatusIn(p.getId(),
                List.of(SyncStatus.PENDING, SyncStatus.FAILED)));
        model.addAttribute("endingSoon", contracts.findByProjectIdAndEndDateBetweenOrderByEndDateAsc(
                p.getId(), today, today.plusDays(60)));
        model.addAttribute("guaranteesSoon", contracts.findByProjectIdAndGuaranteeExpiryBetweenOrderByGuaranteeExpiryAsc(
                p.getId(), today, today.plusDays(60)));
        model.addAttribute("mailLogs", mailLogs.findTop50ByOrderByReceivedAtDesc().stream().limit(5).toList());
        model.addAttribute("recentAudit", audit.recent(6));
        model.addAttribute("today", today);
        return "dashboard";
    }

    @PostMapping("/project/select")
    public String select(@RequestParam Long projectId, Authentication auth, HttpSession session) {
        access.select(projectId, auth, session);
        return "redirect:/";
    }
}
