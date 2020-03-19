package com.cybr406.account.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.stereotype.Component;

/**
 * The H2 console is rendered in a frame. Some of the protective security measures automatically added by Spring
 * Security prevent this frame from working. This adapter will allow you to use h2 console if its enabled.
 *
 * An example configuration using this class:
 *
 * @Configuration
 * public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
 *
 *     @Autowired
 *     H2SecurityConfigurer h2SecurityConfigurer;
 *
 *     @Override
 *     protected void configure(HttpSecurity http) throws Exception {
 *         h2SecurityConfigurer.configure(http);
 *         // The rest of your http configuration should come after.
 *     }
 *
 * }
 */
@Component
public class H2SecurityConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    @Value("${spring.h2.console.enabled}")
    private boolean h2ConsoleEnabled;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // Configure security for the h2 console. This should only ever happen in dev environments.
        if (h2ConsoleEnabled) {
            http
                    .authorizeRequests()
                    .antMatchers("/h2-console", "/h2-console/**").permitAll();

            // By default, frame options is set to DENY. The h2 console is rendered in a frame, however. Changing to
            // SAMEORIGIN allows the content to appear since it is originating from the same server. DENY is a better
            // option for prod, where the h2 console should be disabled anyhow.
            // See https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Frame-Options
            http.headers().frameOptions().sameOrigin();
        }
    }

}
