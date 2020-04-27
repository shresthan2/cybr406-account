package com.cybr406.account;

import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class ProfileEventHandler {
    @HandleBeforeSave
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void beforeSave(Profile profile) {
    }

}
