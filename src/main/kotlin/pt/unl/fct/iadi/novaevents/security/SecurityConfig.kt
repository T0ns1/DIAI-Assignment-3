package pt.unl.fct.iadi.novaevents.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler
import org.springframework.security.web.savedrequest.CookieRequestCache
import org.springframework.security.web.util.matcher.RegexRequestMatcher

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtCookieAuthFilter: JwtCookieAuthFilter,
    private val jwtAuthSuccessHandler: JwtAuthSuccessHandler
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .securityContext {
                it.securityContextRepository(RequestAttributeSecurityContextRepository())
            }
            .csrf { csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                csrf.csrfTokenRequestHandler(CsrfTokenRequestAttributeHandler())
            }
            .requestCache {
                it.requestCache(CookieRequestCache())
            }
            .addFilterBefore(jwtCookieAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/login").permitAll()
                    .requestMatchers(HttpMethod.GET, "/clubs", "/events").permitAll()
                    .requestMatchers(RegexRequestMatcher("^/clubs/\\d+$", "GET")).permitAll()
                    .requestMatchers(RegexRequestMatcher("^/clubs/\\d+/events$", "GET")).permitAll()
                    .requestMatchers(RegexRequestMatcher("^/clubs/\\d+/events/\\d+$", "GET")).permitAll()
                    .requestMatchers(RegexRequestMatcher("^/clubs/\\d+/events/new$", "GET")).hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(RegexRequestMatcher("^/clubs/\\d+/events/\\d+/edit$", "GET")).hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(RegexRequestMatcher("^/clubs/\\d+/events/\\d+/delete$", "GET")).hasRole("ADMIN")
                    .requestMatchers(RegexRequestMatcher("^/clubs/\\d+/events$", "POST")).hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(RegexRequestMatcher("^/clubs/\\d+/events/\\d+$", "POST")).hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(RegexRequestMatcher("^/clubs/\\d+/events/\\d+$", "PUT")).hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(RegexRequestMatcher("^/clubs/\\d+/events/\\d+$", "DELETE")).hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .formLogin { form ->
                form.loginPage("/login").permitAll()
                form.successHandler(jwtAuthSuccessHandler)
            }
            .logout { logout ->
                logout.logoutUrl("/logout")
                logout.deleteCookies("jwt", "XSRF-TOKEN")
                logout.logoutSuccessUrl("/clubs")
            }

        return http.build()
    }
}
