package jmc.ex4.controllers;

import jmc.ex4.dto.CreateApartmentDto;
import jmc.ex4.dto.JoinApartmentDto;
import org.springframework.ui.Model;
import jmc.ex4.model.Apartment;
import jmc.ex4.model.UserInfo;
import jmc.ex4.services.UserService;
import jmc.ex4.utils.CurrentUserProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/owner")
@AllArgsConstructor
public class OwnerController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    /**
     * Displays the owner dashboard with a list of owned apartments.
     * If the user has no apartments, redirects to a "no apartment" page.
     *
     * @param model the model to add attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/dashboard")
    public String ownerDashboard(Model model) {
        UserInfo user = currentUserProvider.getCurrentUser();
        List<Apartment> ownedApartments = userService.getUserOwnedApartments(user.getId());
        if(ownedApartments.isEmpty()){
            return "redirect:/no-apartment";
        }

        model.addAttribute("user", user);
        model.addAttribute("ownedApartments", ownedApartments);
        model.addAttribute("createApartmentDto", new CreateApartmentDto());
        model.addAttribute("joinApartmentDto", new JoinApartmentDto());
        return "owner/dashboard";
    }
}
