package com.developez.Spring.boot.blog.API.config;

import com.developez.Spring.boot.blog.API.Security.JwtAuthenticationEntryPoint;
import com.developez.Spring.boot.blog.API.Security.JwtAuthenticationFilter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@SecuritySchemes({
        @SecurityScheme(
                name = "Bear Authentication",
                type = SecuritySchemeType.HTTP,
                bearerFormat = "JWT",
                scheme = "bearer"
        ),
        @SecurityScheme(
                name = "csrfToken",  // Nome dello schema di sicurezza per CSRF
                type = SecuritySchemeType.APIKEY,
                paramName = "X-XSRF-TOKEN",  // Nome dell'header
                in = SecuritySchemeIn.HEADER  // Posizione dell'header
        )
})
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    SecurityConfig( UserDetailsService userDetailsService,
                    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                    JwtAuthenticationFilter jwtAuthenticationFilter ) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager( AuthenticationConfiguration configuration ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain( HttpSecurity http ) throws Exception {

        // Crea un nuovo gestore di attributi di richiesta CSRF
//        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        // Imposta il nome dell'attributo della richiesta CSRF a "_csrf"
//        requestHandler.setCsrfRequestAttributeName( "_csrf" );

        http
                // Configura CORS
                .cors( ( httpSecurityCorsConfigurer ) -> httpSecurityCorsConfigurer
                        .configurationSource( ( httpServletRequest ) -> {
                            // Crea una nuova configurazione CORS
                            CorsConfiguration corsConfiguration = new CorsConfiguration();
                            // Consente le richieste da "http://localhost:4200" e per sicurezza aggiungiamo anche
                            // "http://127.0.0.1:4200" che è l'indirizzo IP di localhost Angular
                            corsConfiguration.setAllowedOrigins( List.of( "http://127.0.0.1:4200/", "http://localhost:4200/" ) );
                            // Consente tutti i metodi HTTP
                            corsConfiguration.setAllowedMethods( List.of( "GET", "POST", "PUT", "DELETE", "HEAD",
                                    "OPTIONS" ) );
                            // Consente tutti gli header
                            corsConfiguration.setAllowedHeaders( List.of( "*" ) );
                            // Consente le credenziali
                            corsConfiguration.setAllowCredentials( true );
                            corsConfiguration.setExposedHeaders( Arrays.asList( "Authorization", "X-XSRF-TOKEN" ) );
                            // Imposta l'età massima del risultato preflight (in secondi) a 3600
                            corsConfiguration.setMaxAge( 3600L );
                            return corsConfiguration;
                        } )
                )
                .csrf().disable()
                // Aggiunge il filtro CSRF personalizzato dopo il filtro di autenticazione di base
//                .addFilterAfter(
//                        new CsrfCookieFilter(), UsernamePasswordAuthenticationFilter.class
//                )
                .authorizeHttpRequests( ( authorize ) ->
                        authorize.requestMatchers( HttpMethod.GET, "/api/**" ).permitAll()
//                            .requestMatchers( HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers( "/api/auth/**" ).permitAll()
                        // Accesso pubblico a Swagger
                        .requestMatchers( "/swagger-ui/**" ).permitAll()
                        .requestMatchers( "/v3/api-docs/**" ).permitAll()
                        .anyRequest().authenticated() ).exceptionHandling( exception -> exception
                        .authenticationEntryPoint( jwtAuthenticationEntryPoint )
                ).sessionManagement( session -> session
                        .sessionCreationPolicy( SessionCreationPolicy.STATELESS )
                );

        http.addFilterBefore( jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class );

        return http.build();
    }
}
