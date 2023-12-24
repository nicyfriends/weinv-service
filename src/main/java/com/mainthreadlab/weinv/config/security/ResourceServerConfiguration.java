package com.mainthreadlab.weinv.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    private static final String RESOURCE_ID = "WEDDING-INVITATION";

    @Autowired
    @Qualifier("customAuthenticationEntryPoint")
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    @Qualifier("customAccessDeniedHandler")
    private AccessDeniedHandler accessDeniedHandler;

    @Value("${server.servlet.context-path}")
    private String contextPath;


    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(RESOURCE_ID).stateless(true)
                // Set unauthorized requests exception handler
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler);
    }

    // TODO: use roles instead of scopes
    // TODO: allow access to /register only for admin and user roles
    @Override
    public void configure(HttpSecurity http) throws Exception {
        // Enable CORS and disable CSRF
        http = http.cors().and().csrf().disable();

        // Set session management to stateless
        http = http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and();

        // Set permissions on endpoints
        http.authorizeRequests()
                // Our public endpoints
                //.antMatchers("/auth/**").permitAll()
                // Our private endpoints
                .antMatchers("/users/register").access("hasAuthority('admin')")
                .antMatchers("/confirm").access("hasAuthority('guest')")
                .antMatchers(HttpMethod.POST, contextPath).access("hasAnyAuthority('admin','user')")
                .antMatchers(HttpMethod.PATCH, contextPath).access("hasAnyAuthority('admin','user')")
                .antMatchers(HttpMethod.PUT, contextPath).access("hasAnyAuthority('admin','user')")
                .antMatchers(HttpMethod.DELETE, contextPath).access("hasAuthority('admin')")
                .antMatchers(HttpMethod.GET, contextPath).access("hasAnyAuthority('admin','user','guest')")
                .anyRequest().authenticated()
                .and()
                .anonymous().disable();
    }

//    @Bean
//    public DataSourceInitializer dataSourceInitializer(@Qualifier("dataSource") final DataSource dataSource) {
//        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
//        resourceDatabasePopulator.addScript(new ClassPathResource("/data.sql"));
//        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
//        dataSourceInitializer.setDataSource(dataSource);
//        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
//        return dataSourceInitializer;
//    }

    /**
     * Authority: @PreAuthorize(“hasAuthority(‘EDIT_BOOK’)”)
     * Role: @PreAuthorize(“hasRole(‘BOOK_ADMIN’)”)
     * To make the difference between these two terms more explicit,
     * the Spring Security framework adds a ROLE_ prefix to the role name by default.
     * So, instead of checking for a role named BOOK_ADMIN, it will check for ROLE_BOOK_ADMIN.
     */
    @Bean
    GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(""); // Remove the ROLE_ prefix
    }

    /**
     * Used by Spring Security if CORS is enabled.
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}