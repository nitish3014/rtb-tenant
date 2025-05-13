package com.rtb.tenant.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final AuthSigningKey authSigningKey;

  public SecurityConfig(AuthSigningKey authSigningKey) {
    this.authSigningKey = authSigningKey;
  }
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
               .authorizeHttpRequests(auth -> auth
                       .requestMatchers("/api/v1/tenants/{tenantId}/metadata").permitAll()
                       .requestMatchers("/api/v1/tenants/actuator/**").permitAll()
                       .requestMatchers("/swagger-ui/**").permitAll()
                       .requestMatchers("/v3/api-docs/**").permitAll()
                       .requestMatchers("/api/v1/tenants/v3/**").permitAll()
                       .requestMatchers("/api/v1/tenants/swagger-ui/**").permitAll()
                       .requestMatchers("/api/v1/tenants/**")
                       .authenticated()
                       .requestMatchers("/api/v1/communication/**")
                       .authenticated()
                       .requestMatchers("/api/v1/quick-sight/**")
                       .authenticated()
                 .requestMatchers("**").authenticated()
                 .anyRequest().authenticated())
               .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
               .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                 jwt -> jwt.jwtAuthenticationConverter(
                         new CustomJwtAuthenticationConverter())))
               .sessionManagement(session
                 -> session.sessionCreationPolicy(
                         SessionCreationPolicy.STATELESS))
              .headers(headers -> headers
                .httpStrictTransportSecurity(hsts -> hsts
                      .includeSubDomains(true)
                      .preload(true)
                      .maxAgeInSeconds(31536000))).build();
  }

  @Bean
  JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder
            .withPublicKey(authSigningKey.publicKey()).build();
  }

}

@SuppressWarnings("NullableProblems")
class CustomJwtAuthenticationConverter implements
        Converter<Jwt, AbstractAuthenticationToken> {

  private final Converter<Jwt, Collection<GrantedAuthority>>
          jwtGrantedAuthoritiesConverter =
    getJwtGrantedAuthoritiesConverter();

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> roleAuthorities =
            this.jwtGrantedAuthoritiesConverter.convert(jwt);
    Collection<GrantedAuthority> permissionAuthorities
            = extractPermissionsFromJwt(jwt);

    Collection<GrantedAuthority> combinedAuthorities =
            new ArrayList<>(roleAuthorities);
    combinedAuthorities.addAll(permissionAuthorities);

    JwtAuthenticationToken jwtAuthenticationToken =
            new JwtAuthenticationToken(jwt, combinedAuthorities, jwt.getSubject());
    jwtAuthenticationToken.setDetails(jwt.getClaims());
    return jwtAuthenticationToken;
  }

  private Converter<Jwt, Collection<GrantedAuthority>>
  getJwtGrantedAuthoritiesConverter() {
    JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
    converter.setAuthoritiesClaimName("role");
    converter.setAuthorityPrefix("");
    return converter;
  }

  private Collection<GrantedAuthority> extractPermissionsFromJwt(Jwt jwt) {
    List<String> permissions = jwt.getClaimAsStringList("permissions");

    if (permissions != null && !permissions.isEmpty()) {
      return permissions.stream()
              .map(SimpleGrantedAuthority::new)
              .collect(Collectors.toList());
    }

    return Collections.emptyList();
  }

}
