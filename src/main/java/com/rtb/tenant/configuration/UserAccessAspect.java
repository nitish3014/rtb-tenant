package com.rtb.tenant.configuration;

import com.rtb.tenant.utls.AuthenticationDetailUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Aspect
@Component
public class UserAccessAspect {

    @Autowired
    private AuthenticationDetailUtil authenticationDetailUtil;

    @Before("@annotation(com.rtb.tenant.configuration.UserAccessGuard)")
    public void checkUserAccess(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        UserAccessGuard annotation = method.getAnnotation(UserAccessGuard.class);

        String requiredPermission = annotation.value();

        List<String> roles = (List<String>) authenticationDetailUtil
                .getAuthenticationDetails("roles");

        if (roles.contains("admin")) {
            return;
        }

        List<String> permissions = (List<String>) authenticationDetailUtil
                .getAuthenticationDetails("permissions");

        // Map method parameter names to their values
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 0; i < paramNames.length; i++) {
            paramMap.put(paramNames[i], args[i]);
        }

        // Extract tenantId and userId by name
        Long tenantId = paramMap.containsKey("tenantId")
                ? (Long) paramMap.get("tenantId") : null;
        Long userId = paramMap.containsKey("userId")
                ? (Long) paramMap.get("userId") : null;

        if (tenantId != null) {
            Long authTenantId = (Long) authenticationDetailUtil
                    .getAuthenticationDetails("tenantid");
            if (!tenantId.equals(authTenantId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You are not authorized to access this tenant's data");
            }
        }

        if (permissions.contains(requiredPermission)) {
            return;
        }

        Long authUserId = (Long) authenticationDetailUtil.getAuthenticationDetails("id");

        if (userId != null && !authUserId.toString().equals(userId.toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not authorized to access this user's data");
        }
    }

    @Before("@annotation(com.rtb.tenant.configuration.EndUserGuard) && args(userId,..)")
    public void checkEndUserAccess(JoinPoint joinPoint, Long userId) {
        Long authUserId = (Long) authenticationDetailUtil.getAuthenticationDetails("id");

        if (!authUserId.toString().equals(userId.toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not authorized to access this user's data");
        }
    }
}
