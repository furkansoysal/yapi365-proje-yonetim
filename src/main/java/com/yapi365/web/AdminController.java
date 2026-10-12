package com.yapi365.web;

import com.yapi365.domain.Action;
import com.yapi365.domain.AppModule;
import com.yapi365.domain.AppRole;
import com.yapi365.repo.AppRoleRepository;
import com.yapi365.repo.AppUserRepository;
import com.yapi365.service.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AppRoleRepository roles;
    private final AppUserRepository users;
    private final AuditService audit;

    public AdminController(AppRoleRepository roles, AppUserRepository users, AuditService audit) {
        this.roles = roles;
        this.users = users;
        this.audit = audit;
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('YONETIM:VIEW')")
    @Transactional(readOnly = true)
    public String roles(Model model) {
        model.addAttribute("roles", roles.findAllByOrderByIdAsc());
        model.addAttribute("modules", AppModule.values());
        model.addAttribute("actions", Action.values());
        model.addAttribute("users", users.findAll());
        return "admin-roles";
    }

    @PostMapping("/roles/{id}/toggle")
    @PreAuthorize("hasAuthority('YONETIM:EDIT')")
    @Transactional
    public String toggle(@PathVariable Long id, @RequestParam String permission, RedirectAttributes ra) {
        boolean valid = Arrays.stream(AppModule.values()).anyMatch(m ->
                Arrays.stream(Action.values()).anyMatch(a -> Action.permission(m, a).equals(permission)));
        if (!valid) {
            throw new IllegalArgumentException("Geçersiz yetki");
        }
        AppRole role = roles.findById(id).orElseThrow();
        if (role.getCode().equals("SISTEM_YONETICISI") && permission.startsWith("YONETIM:")) {
            ra.addFlashAttribute("error", "Sistem yöneticisinin yönetim yetkisi kaldırılamaz");
            return "redirect:/admin/roles";
        }
        role.toggle(permission);
        ra.addFlashAttribute("message", role.getName() + " → " + permission + " güncellendi (kullanıcı yeniden girişte geçerli olur)");
        return "redirect:/admin/roles";
    }

    @GetMapping("/audit")
    @PreAuthorize("hasAuthority('YONETIM:VIEW')")
    public String auditLog(Model model) {
        model.addAttribute("entries", audit.recent(100));
        return "audit";
    }
}
