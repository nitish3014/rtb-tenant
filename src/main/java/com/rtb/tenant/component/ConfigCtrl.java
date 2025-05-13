package com.rtb.tenant.component;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ConfigCtrl implements Filter {
  private static final List<String> ALLOWED_ORIGINS = List.of(
          "https://portal.mvpin90days.com",
          "https://dev.portal.mvpin90days.com",
          "http://localhost:5000"
  );

  @Override
  public void doFilter(ServletRequest req, ServletResponse res,
                       FilterChain chain) throws IOException, ServletException {

    HttpServletResponse response = (HttpServletResponse) res;
    HttpServletRequest request = (HttpServletRequest) req;

    String origin = request.getHeader("Origin");

    if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
      response.setHeader("Access-Control-Allow-Origin", origin);
      response.setHeader("Access-Control-Allow-Credentials", "true");
    }

    response.setHeader("Access-Control-Allow-Methods",
            "POST, PUT, GET, PATCH, OPTIONS, DELETE");
    response.setHeader("Access-Control-Allow-Headers",
            "Authorization, Content-Type, X-Insights-Data, Set-Cookie");
    response.setHeader("Access-Control-Max-Age", "3600");
    response.setHeader("X-Frame-Options", "DENY");
    response.setHeader("Content-Security-Policy", "frame-ancestors 'none'");
    response.setHeader("Content-Security-Policy", 
      "default-src 'self'; script-src 'self' https://trusted-cdn.com;"
      + " style-src 'self' 'unsafe-inline'; img-src 'self' data:;" 
      + " font-src 'self' https://fonts.googleapis.com;");
    
    if ("OPTIONS".equalsIgnoreCase(((HttpServletRequest) req).getMethod())) {
      response.setStatus(HttpServletResponse.SC_OK);
    } else {
      chain.doFilter(req, res);
    }
  }
  @Override
  public void destroy() {
  }
  @Override
  public void init(FilterConfig config) throws ServletException {
  }

}

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class AuthTokenFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
                       FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String authToken = null;
    if (httpRequest.getCookies() != null) {
      for (Cookie cookie : httpRequest.getCookies()) {
        if ("authToken".equals(cookie.getName())) {
          authToken = cookie.getValue();
          break;
        }
      }
    }

    if (authToken != null) {
      String finalAuthToken = authToken;
      HttpServletRequestWrapper modifiedRequest = new HttpServletRequestWrapper(httpRequest) {
        @Override
        public String getHeader(String name) {
          if ("Authorization".equalsIgnoreCase(name)) {
            return "Bearer " + finalAuthToken;
          }
          return super.getHeader(name);
        }
      };

      chain.doFilter(modifiedRequest, response);
      return;
    }

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }
}
