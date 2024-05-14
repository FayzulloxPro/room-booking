package dev.fayzullokh.roombooking.config;

import dev.fayzullokh.roombooking.entities.User;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class UserSession {
    public User getUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        var authUserDetails = authentication.getPrincipal();
        if (authUserDetails instanceof CustomUserDetails a)
            return a.getUser();
        return null;
    }

    public Long getId() {
        User user = getUser();
        if (user != null)
            return user.getId();
        return null;
    }
}
