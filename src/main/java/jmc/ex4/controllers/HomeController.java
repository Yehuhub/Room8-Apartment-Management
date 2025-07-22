package jmc.ex4.controllers;

import jakarta.validation.Valid;
import jmc.ex4.dto.UsersDto;
import jmc.ex4.exceptions.NoAuthenticatedUserFoundException;
import jmc.ex4.model.enums.Role;
import jmc.ex4.model.UserInfo;
import jmc.ex4.services.UserService;
import jmc.ex4.utils.CurrentUserProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling home, login, and registration pages.
 * Handles user redirection based on roles and manages user registration.
 */
@Controller
@AllArgsConstructor
public class HomeController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;


    /**
     * Redirects users to their respective dashboards based on their roles.
     * If no authenticated user is found, it returns the home page.
     *
     * @return Redirects to tenant or owner dashboard or returns home page.
     */
    @GetMapping("/")
    public String home() {
        try{

            UserInfo user = currentUserProvider.getCurrentUser();
            Role userRole = user.getRole();
            if(userRole == Role.TENANT){
                return "redirect:/tenant/dashboard";
            } else if (userRole == Role.OWNER) {
                return "redirect:/owner/dashboard";
            }
        } catch (NoAuthenticatedUserFoundException e){
            return "home";
        }
        return "home";
    }

    /**
     * Displays the login page.
     * @param auth The authentication object to check if the user is already logged in.
     * @return "loginpage" if not authenticated, otherwise redirects to home.
     */
    @GetMapping("/login")
    public String login(Authentication auth) {
        if(auth != null && auth.isAuthenticated()){
            return "redirect:/"; // Redirect to home if already logged in
        }
        return "loginpage";
    }

    /**
     * Displays the registration page.
     * If the user is already authenticated, redirects to home.
     * @param auth The authentication object to check if the user is already logged in.
     * @param model The model to add attributes for the view.
     * @return "registration" view if not authenticated, otherwise redirects to home.
     */
    @GetMapping("/register")
    public String register(Authentication auth, Model model) {
        if(auth != null && auth.isAuthenticated()){
            return "redirect:/"; // Redirect to home if already logged in
        }
        model.addAttribute("user", new UsersDto());
        model.addAttribute("roles",List.of(Role.TENANT, Role.OWNER)); // Populate the dropdown
        return "registration";
    }

    /**
     * Handles user registration.
     * Validates the user input and adds the user if valid.
     * If there are validation errors, it redisplays the registration form with error messages.
     *
     * @param userDto The user data transfer object containing registration details.
     * @param result The binding result to capture validation errors.
     * @param model The model to add attributes for the view.
     * @return Redirects to login page on success, or redisplays the registration form on error.
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UsersDto userDto,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles",List.of(Role.TENANT, Role.OWNER)); // Re-populate the dropdown
            return "registration"; // Redisplay form with error messages
        }

        try{
            userService.addUser(userDto);
        } catch (IllegalArgumentException ie){
            result.rejectValue("apartmentReferenceCode", "error.user", ie.getMessage());
            model.addAttribute("roles",List.of(Role.TENANT, Role.OWNER)); // Re-populate the dropdown
            return "registration"; // Redisplay form with error messages
        }

        return "redirect:/login";
    }

}
