package jmc.ex4.exceptions;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown when no authenticated user is found in the security context.
 */
public class NoAuthenticatedUserFoundException extends AuthenticationException {

    public NoAuthenticatedUserFoundException(String msg){
        super(msg);
    }
}
