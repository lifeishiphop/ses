package com.web.back.security;

import com.web.back.service.AccountService;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Autowired
    private AccountService accountService;

    @Autowired
    private JwtTokenFilter jwtTokenFilter;

    @Autowired
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Value("${jwt.get.token.url}")
    private String authenticationPath;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(accountService).passwordEncoder(passwordEncoder);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF = Cross-Site Request Forgery, 쿠키를 사용하지 않으면 disable
        return http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ) // 세션 관리 설정 => 비활성화
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(requests ->
                        requests
                                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs",
                                        "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                                .anyRequest().authenticated()
                )  // 요청 권한 설정 (모든 요청에 대해)
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class) // 필터 추가 (JWT 토큰 필터 + 사용자 인증 필터)
                .headers(headers ->
                        headers.frameOptions(frameOptionsConfig -> frameOptionsConfig.sameOrigin().cacheControl(Customizer.withDefaults()))
                ) // 헤더 설정
                .build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return webSecurity -> {
            final String[] accessablePostUrls = new String[]{
                    authenticationPath,"/logoutAccount","/signup", "/findPassword", "/mail", "/mailVerify", "/static/**", "/"
            };
            final String[] accessableGetUrls = new String[]{
                    "/static/**", "/", "/logo.svg", "/favicon.ico", "/manifest.json"
            };
            webSecurity.ignoring().requestMatchers(accessablePostUrls);
        };
    }
}
