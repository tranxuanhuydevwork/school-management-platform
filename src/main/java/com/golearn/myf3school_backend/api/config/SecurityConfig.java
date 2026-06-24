package com.golearn.myf3school_backend.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/static/**",          // CSS, JS, images, fonts
                "/favicon.ico",
                "/error",
                "/admin/**"            // server-rendered HTML pages (not API)
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(sm ->
//                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//
//                        // ── Public API ──────────────────────────────────────
//                        .requestMatchers("/api/auth/**").permitAll()
//                        .requestMatchers("/actuator/health").permitAll()
//
//                        // ── Admin-only API ──────────────────────────────────
//                        .requestMatchers("/api/users/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.POST,   "/classes/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.PUT,    "/classes/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/classes/**").hasRole("ADMIN")
//                        // ── Teacher + Admin ─────────────────────────────────
//                        .requestMatchers(HttpMethod.POST, "/api/attendance/**").permitAll()
//                        .requestMatchers(HttpMethod.PUT,  "/api/attendance/**").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/api/grades/**").permitAll()     //hasAnyRole("TEACHER","ADMIN")
//                        .requestMatchers(HttpMethod.PUT,  "/api/grades/**").permitAll()
//                        .requestMatchers(HttpMethod.DELETE,"/api/grades/**").permitAll()
//                        .requestMatchers("/api/requests/**").permitAll() //hasRole để phân quyền nha
//                        .requestMatchers(HttpMethod.GET, "/api/grades/**").permitAll()
//                        .requestMatchers("/api/attendance/**").permitAll()
//                        .requestMatchers(HttpMethod.PUT,  "/api/attendance/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/grade-corrections/**").permitAll()
//                        .requestMatchers("/api/grade-corrections/**").permitAll()
//
//                        // ── Club management ─────────────────────────────────
//                        .requestMatchers(HttpMethod.POST, "/api/clubs/**").hasAnyRole("CLUB_ADVISOR","ADMIN")
//                        .requestMatchers(HttpMethod.PUT,  "/api/clubs/**").hasAnyRole("CLUB_ADVISOR","ADMIN")
//                        .requestMatchers(HttpMethod.DELETE,"/api/clubs/**").hasRole("ADMIN")
//
//                        // ── Conduct ─────────────────────────────────────────
//                        .requestMatchers(HttpMethod.POST, "/api/conduct/**").hasAnyRole("TEACHER","ADMIN")
//                        .requestMatchers(HttpMethod.PUT,  "/api/conduct/**").hasAnyRole("TEACHER","ADMIN")
//
//                        // ── Facilities ──────────────────────────────────────
//                        .requestMatchers(HttpMethod.GET,  "/api/facilities/**").authenticated()
//                        .requestMatchers(HttpMethod.POST, "/api/facilities/bookings").authenticated()
//                        .requestMatchers(HttpMethod.PUT,  "/api/facilities/bookings/**").hasAnyRole("ADMIN","STAFF")
//
//                        // ── Everything else needs auth ───────────────────────
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}