package com.Teryaq.config;

import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.user.entity.User;
import com.Teryaq.user.repository.EmployeeRepository;
import com.Teryaq.utils.auditing.ApplicationAuditingAware;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.logging.Logger;

@Configuration
@RequiredArgsConstructor
@EnableCaching
public class ApplicationConfig {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Bean
    public UserDetailsService userDetailsService(){
        return email -> {
          var user =  userRepository.findByEmail(email).orElse(null);
            if(user == null) {
               user = employeeRepository.findByEmail(email).orElseThrow(
                        () -> new UsernameNotFoundException("User or Employee not found"));
            }
            Logger logger = Logger.getLogger(ApplicationConfig.class.getName());
            logger.info("User Email is: " + user.getEmail());
            return user;
        };
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean(name = "auditorAware1")
    public AuditorAware<Long> auditorAware(){
        return new ApplicationAuditingAware();
    }

    @Bean(name = "auditorAware2")
    public AuditorAware<Long> auditorAware2() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() &&
             auth.getPrincipal() instanceof User user) {
                return Optional.of(user.getId());
            }
            return Optional.of(1L);
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider =  new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

//    @Bean
//    public CacheManager cacheManager() {
//        return new ConcurrentMapCacheManager("books", "patrons" );
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
