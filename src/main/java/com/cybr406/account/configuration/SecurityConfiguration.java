package com.cybr406.account.configuration;

import org.graalvm.compiler.lir.LIRInstruction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import sun.awt.HeadlessToolkit;

import javax.sql.DataSource;

@EnableGlobalMethodSecurity (prePostEnabled = true)

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    DataSource dataSource;

    @Bean
    JdbcUserDetailsManager userDetailsManager(){
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    public User.UserBuilder userBuilder(){
        PasswordEncoder passwordEncoder=passwordEncoder();
        User.UserBuilder user= User.builder();
        user.passwordEncoder(passwordEncoder::encode);
        return user;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
auth
        .jdbcAuthentication()
        .dataSource(dataSource);
    }
    @Override
    public void configure(HttpSecurity http) throws Exception {


        http.authorizeRequests()
                .mvcMatchers(HttpMethod.GET,"/check-user").hasAnyRole("ADMIN","SERVICE")
                .mvcMatchers(HttpMethod.GET,"/","/**").permitAll()

               .mvcMatchers(HttpMethod.POST,"/signup").permitAll()

                .mvcMatchers(HttpMethod.PATCH,"/profiles/a").permitAll()

                .anyRequest().authenticated()
                .and()
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .httpBasic();

    }
}



