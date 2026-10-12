package com.yapi365.web;

import com.yapi365.repo.AppUserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Profile("!entra")
public class LoginController {

    private final AppUserRepository users;

    public LoginController(AppUserRepository users) {
        this.users = users;
    }

    @GetMapping("/login")
    @Transactional(readOnly = true)
    public String login(Model model) {
        model.addAttribute("demoUsers", users.findByActiveTrueOrderByIdAsc());
        return "login";
    }
}
