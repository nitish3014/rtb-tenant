package com.rtb.tenant.utls;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthenticationDetailUtil {
    public Object getAuthenticationDetails(String claim) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getDetails() instanceof Map<?, ?> details) {
            return details.get(claim);
        }
        return null;
    }
}
