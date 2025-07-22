package jmc.ex4.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jmc.ex4.model.UserInfo;
import jmc.ex4.utils.CurrentUserProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to check if the current user has a residence assigned.
 * If no residence is assigned, redirects to the home page.
 */
@Component
@AllArgsConstructor
public class ResidenceInterceptor implements HandlerInterceptor {

    private final CurrentUserProvider currentUserProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfo user = currentUserProvider.getCurrentUser();

        // If user has no residence, redirect to home page
        if (user.getResidence() == null) {
            response.sendRedirect("/no-apartment");
            return false;
        }

        return true;
    }
}