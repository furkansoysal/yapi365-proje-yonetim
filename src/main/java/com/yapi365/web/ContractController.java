package com.yapi365.web;

import com.yapi365.domain.Contract;
import com.yapi365.domain.ContractType;
import com.yapi365.domain.Project;
import com.yapi365.repo.ContractRepository;
import com.yapi365.service.AuditService;
import com.yapi365.service.ProjectAccessService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/contracts")
public class ContractController {

    private final ProjectAccessService access;
    private final ContractRepository contracts;
    private final AuditService audit;

    public ContractController(ProjectAccessService access, ContractRepository contracts, AuditService audit) {
        this.access = access;
        this.contracts = contracts;
        this.audit = audit;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SOZLESME:VIEW')")
    public String list(Authentication auth, HttpSession session, Model model) {
        Project p = access.current(auth, session);
        model.addAttribute("contracts", contracts.findByProjectIdOrderByEndDateAsc(p.getId()));
        model.addAttribute("today", LocalDate.now());
        return "contracts";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('SOZLESME:EDIT')")
    public String create(Model model) {
        model.addAttribute("contract", new Contract());
        model.addAttribute("types", ContractType.values());
        return "contract-form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('SOZLESME:EDIT')")
    public String edit(@PathVariable Long id, Authentication auth, HttpSession session, Model model) {
        Project p = access.current(auth, session);
        model.addAttribute("contract", contracts.findByIdAndProjectId(id, p.getId()).orElseThrow());
        model.addAttribute("types", ContractType.values());
        return "contract-form";
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('SOZLESME:EDIT')")
    @Transactional
    public String save(@Valid @ModelAttribute("contract") ContractForm form, BindingResult result,
                       Authentication auth, HttpSession session, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("types", ContractType.values());
            return "contract-form";
        }
        Project p = access.current(auth, session);
        Contract c = form.getId() == null
                ? new Contract()
                : contracts.findByIdAndProjectId(form.getId(), p.getId()).orElseThrow();
        c.setProject(p);
        form.applyTo(c);
        contracts.save(c);
        ra.addFlashAttribute("message", "Sözleşme kaydedildi: " + c.getContractNo());
        return "redirect:/contracts";
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('SOZLESME:VIEW')")
    public String history(@PathVariable Long id, Authentication auth, HttpSession session, Model model) {
        Project p = access.current(auth, session);
        model.addAttribute("contract", contracts.findByIdAndProjectId(id, p.getId()).orElseThrow());
        model.addAttribute("history", audit.contractHistory(id));
        return "contract-history";
    }
}
