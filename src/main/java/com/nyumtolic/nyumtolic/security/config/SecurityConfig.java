package com.nyumtolic.nyumtolic.security.config;


import com.nyumtolic.nyumtolic.security.filter.IpBlackListFilter;
import com.nyumtolic.nyumtolic.security.service.IpService;
import com.nyumtolic.nyumtolic.security.service.PrincipalOauth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.security.web.authentication.session.*;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final PrincipalOauth2UserService principalOauth2UserService;
    private final IpService ipService;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http    .addFilterBefore(new IpBlackListFilter(ipService), RequestHeaderAuthenticationFilter.class)

                .sessionManagement(sessionManager -> sessionManager
                        .sessionAuthenticationStrategy(concurrentSession()) // 세션관리 전략
                        .maximumSessions(-1) // 최대 동시 세션 수
                        .expiredSessionStrategy(sessionInformationExpiredStrategy())) // 세션 만료 전략

                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        .requestMatchers(new AntPathRequestMatcher("/**")).permitAll() // 모든 요청을 허용
                        .requestMatchers(HttpMethod.POST, "/**").permitAll() // POST 요청을 허용
                        .anyRequest().authenticated()) // 나머지 요청에 대해서는 인증 필요

                .csrf((csrf) -> csrf
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")))

                .headers((headers) -> headers
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(
                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))

                .formLogin((formLogin) -> formLogin
                        .loginPage("/user/login")
                        .defaultSuccessUrl("/"))

                .logout((logout) -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true))

                .oauth2Login((oauthLogin) -> oauthLogin
                        .loginPage("/user/login")
                        .defaultSuccessUrl("/")
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                .userService(principalOauth2UserService)))
        ;
        return http.build();

    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 세션 전략 세팅
    @Bean
    public CompositeSessionAuthenticationStrategy concurrentSession() {

        ConcurrentSessionControlAuthenticationStrategy concurrentAuthenticationStrategy = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry());
        List<SessionAuthenticationStrategy> delegateStrategies = new ArrayList<>();
        delegateStrategies.add(concurrentAuthenticationStrategy); // 동시 세션 제어 전략
        delegateStrategies.add(new SessionFixationProtectionStrategy()); // 세션 보호 전략
        delegateStrategies.add(new RegisterSessionAuthenticationStrategy(sessionRegistry())); // 세션 등록 전략

        return new CompositeSessionAuthenticationStrategy(delegateStrategies);
    }

    // 세션 만료 전략
    @Bean
    SessionInformationExpiredStrategy sessionInformationExpiredStrategy() {
        return new CustomSessionInformationExpiredStrategy();
    }

    // 세션 관리
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

}