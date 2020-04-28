package com.cybr406.account;

import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class ProfileEventHandler {
    @HandleBeforeCreate
    @HandleAfterCreate
    @HandleBeforeSave
    @PreAuthorize("hasRole('ROLE_USER') or #author.username == authentication.principal.username")
    public void beforeSave(Profile profile) {
    }



}
