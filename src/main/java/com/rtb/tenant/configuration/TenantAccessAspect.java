package com.rtb.tenant.configuration;

import com.rtb.tenant.utls.AuthenticationDetailUtil;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Aspect
@Component
public class TenantAccessAspect {

    @Autowired
    private AuthenticationDetailUtil authenticationDetailUtil;

    @Before("@annotation(TenantAccessGuard) && args(tenantId,..)")
    public void checkTenantAccess(Long tenantId) {
        List<String> roles = (List<String>) authenticationDetailUtil
                .getAuthenticationDetails("roles");

        if (roles.contains("admin")) {
            return;
        }

        Long authenticatedTenantId = (Long) authenticationDetailUtil
                .getAuthenticationDetails("tenantid");

        if (!tenantId.equals(authenticatedTenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not authorized for this tenant");
        }
    }
}

