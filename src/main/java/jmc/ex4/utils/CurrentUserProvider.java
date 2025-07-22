package jmc.ex4.utils;

import jmc.ex4.exceptions.NoAuthenticatedUserFoundException;
import jmc.ex4.model.UserInfo;
import jmc.ex4.repositories.UsersRepository;
import jmc.ex4.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


/**
 * Utility class to provide the currently authenticated user.
 */
@Component
@AllArgsConstructor
public class CurrentUserProvider {

    private UserService userService;

    /**
     * Retrieves the currently authenticated user.
     *
     * @return the current UserInfo object
     * @throws IllegalStateException if no authenticated user is found
     */
    public UserInfo getCurrentUser(){
        Long userId = getCurrentUserId();
        return userService.getUserById(userId);
    }
    /**
     * Retrieves the ID of the currently authenticated user.
     *
     * @return the ID of the current user
     * @throws NoAuthenticatedUserFoundException if no authenticated user is found
     */
    public Long getCurrentUserId() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserInfo) {
            return ((UserInfo) auth.getPrincipal()).getId();
        }
        throw new NoAuthenticatedUserFoundException("No authenticated user found");

    }
}
