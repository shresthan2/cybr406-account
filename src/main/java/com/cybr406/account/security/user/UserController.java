package com.cybr406.account.security.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.*;
import java.sql.ClientInfoStatus;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RestController
public class UserController {
 private static final GrantedAuthority userRole=new SimpleGrantedAuthority("Role_USER");

 @Autowired
    private UserDetailsManager userDetailsManager;
 @Autowired
    private PasswordEncoder passwordEncoder;

 @GetMapping("/check-user")
    private ResponseEntity<List<String>> checkUser (
           @RequestHeader(value = "x-username",required = true) String usernameA,
           @RequestHeader(value = "x-password",required = true) String password) throws Exception{
     try{
         UserDetails userDetails= userDetailsManager.loadUserByUsername(usernameA);
         if (!passwordEncoder.matches(password, userDetails.getPassword()))
             return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

         if (userDetails.getAuthorities().stream().anyMatch(ga -> !Objects.equals(ga, userRole))){
             return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
         }
         return new ResponseEntity<>(userDetails.getAuthorities().stream()
                 .map(GrantedAuthority::getAuthority)
                 .collect(Collectors.toList()),
                    HttpStatus.OK);
     }catch (Exception e){
         return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
     }
 }

    /*
     * The job of the UserController is to receive a username and a password, and if they are correct, return the
     * authorities of the user (ROLE_ADMIN, ROLE_SERVICE, ROLE_USER, etc). This will allow other applications, such as
     * the Post application, to access this information without having their own copy of the USER's table.
     *
     * Main things you will need.
     *     * A UserDetailsManager for loading a UserDetails from the Spring Security USER table.
     *     * A PasswordEncoder for checking if the submitted password matches the hashed password.
     *
     * In the video implementation of this class, I use methods such as stream(), map(), and collect(). These might be
     * new to you. If you prefer, simple for loops can accomplish the same thing with a little more typing.
     */

}
