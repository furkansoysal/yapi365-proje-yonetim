package com.yapi365.web;

import com.yapi365.domain.AppModule;
import com.yapi365.domain.MailActionType;
import com.yapi365.domain.MailRule;
import com.yapi365.mail.MailMessage;
import com.yapi365.mail.MailRuleEngine;
import com.yapi365.repo.MailLogRepository;
import com.yapi365.repo.MailRuleRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.util.Optional;

@Controller
@RequestMapping("/mail")
public class MailController {

    private final MailRuleRepository rules;
    private final MailLogRepository logs;
    private final MailRuleEngine engine;

    public MailController(MailRuleRepository rules, MailLogRepository logs, MailRuleEngine engine) {
        this.rules = rules;
        this.logs = logs;
        this.engine = engine;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MAIL:VIEW')")
    public String page(Model model) {
        fill(model);
        return "mail";
    }

    /** Kural test alanı: örnek bir mailin hangi kurala düşeceğini kaydetmeden gösterir. */
    @PostMapping("/test")
    @PreAuthorize("hasAuthority('MAIL:VIEW')")
    public String test(@RequestParam String sender, @RequestParam String subject,
                       @RequestParam(defaultValue = "") String body, Model model) {
        MailMessage m = new MailMessage("test", sender, subject, body, Instant.now(), false);
        Optional<MailRule> match = engine.match(rules.findByActiveTrueOrderByPriorityAsc(), m);
        model.addAttribute("testMail", m);
        model.addAttribute("testResult", match.orElse(null));
        model.addAttribute("tested", true);
        fill(model);
        return "mail";
    }

    @PostMapping("/rules")
    @PreAuthorize("hasAuthority('MAIL:EDIT')")
    public String add(@RequestParam String name, @RequestParam int priority,
                      @RequestParam(required = false) String senderContains,
                      @RequestParam(required = false) String subjectContains,
                      @RequestParam(required = false) String keywords,
                      @RequestParam MailActionType actionType,
                      @RequestParam(required = false) AppModule targetModule,
                      @RequestParam(required = false) String targetFolder,
                      @RequestParam(required = false) String notifyEmail,
                      RedirectAttributes ra) {
        MailRule r = new MailRule();
        r.setName(name);
        r.setPriority(priority);
        r.setSenderContains(blankToNull(senderContains));
        r.setSubjectContains(blankToNull(subjectContains));
        r.setKeywords(blankToNull(keywords));
        r.setActionType(actionType);
        r.setTargetModule(targetModule);
        r.setTargetFolder(blankToNull(targetFolder));
        r.setNotifyEmail(blankToNull(notifyEmail));
        rules.save(r);
        ra.addFlashAttribute("message", "Kural eklendi: " + name);
        return "redirect:/mail";
    }

    @PostMapping("/rules/{id}/toggle")
    @PreAuthorize("hasAuthority('MAIL:EDIT')")
    @Transactional
    public String toggle(@PathVariable Long id) {
        MailRule r = rules.findById(id).orElseThrow();
        r.setActive(!r.isActive());
        return "redirect:/mail";
    }

    private void fill(Model model) {
        model.addAttribute("rules", rules.findAllByOrderByPriorityAsc());
        model.addAttribute("logs", logs.findTop50ByOrderByReceivedAtDesc());
        model.addAttribute("actionTypes", MailActionType.values());
        model.addAttribute("modules", AppModule.values());
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
