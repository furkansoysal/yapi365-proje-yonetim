package com.yapi365.web;

import com.yapi365.service.ProjectAccessService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Tüm sayfalara giriş yapan kullanıcıyı, erişebildiği projeleri ve seçili projeyi ekler. */
@ControllerAdvice
public class GlobalModelAdvice {

    private final ProjectAccessService access;

    public GlobalModelAdvice(ProjectAccessService access) {
        this.access = access;
    }

    @ModelAttribute
    public void common(Authentication auth, HttpSession session, Model model) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            return;
        }
        model.addAttribute("me", access.user(auth));
        var projects = access.accessible(auth);
        model.addAttribute("projects", projects);
        if (!projects.isEmpty()) {
            model.addAttribute("currentProject", access.current(auth, session));
        }
    }
}
