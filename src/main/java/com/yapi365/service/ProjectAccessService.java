package com.yapi365.service;

import com.yapi365.domain.AppUser;
import com.yapi365.domain.Project;
import com.yapi365.repo.AppUserRepository;
import com.yapi365.repo.ProjectRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Çoklu proje yapısı: kullanıcı yalnızca atandığı projeleri görür.
 * YONETIM:VIEW yetkisi olanlar tüm projeleri görür. Seçili proje oturumda tutulur.
 */
@Service
public class ProjectAccessService {

    public static final String SESSION_KEY = "currentProjectId";

    private final ProjectRepository projects;
    private final AppUserRepository users;

    public ProjectAccessService(ProjectRepository projects, AppUserRepository users) {
        this.projects = projects;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public AppUser user(Authentication auth) {
        return users.findByEmailIgnoreCase(auth.getName()).orElseThrow(() -> new AccessDeniedException("Kullanıcı yok"));
    }

    @Transactional(readOnly = true)
    public List<Project> accessible(Authentication auth) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("YONETIM:VIEW"));
        if (admin) {
            return projects.findByActiveTrueOrderByCodeAsc();
        }
        return user(auth).getProjects().stream().filter(Project::isActive).toList();
    }

    @Transactional(readOnly = true)
    public Project current(Authentication auth, HttpSession session) {
        List<Project> list = accessible(auth);
        if (list.isEmpty()) {
            throw new AccessDeniedException("Atanmış proje yok");
        }
        Object id = session.getAttribute(SESSION_KEY);
        return list.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(list.get(0));
    }

    public void select(Long projectId, Authentication auth, HttpSession session) {
        boolean allowed = accessible(auth).stream().anyMatch(p -> p.getId().equals(projectId));
        if (!allowed) {
            throw new AccessDeniedException("Bu projeye erişiminiz yok");
        }
        session.setAttribute(SESSION_KEY, projectId);
    }
}
