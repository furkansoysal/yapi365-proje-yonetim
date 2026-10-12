package com.yapi365.security;

import com.yapi365.domain.AuditRevision;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Envers her revizyon oluşturduğunda işlemi yapan kullanıcıyı yazar. */
public class AuditRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = (auth == null || !auth.isAuthenticated()) ? "sistem" : auth.getName();
        ((AuditRevision) revisionEntity).setUsername(user);
    }
}
