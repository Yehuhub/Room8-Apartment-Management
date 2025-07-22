package jmc.ex4.controllers;

import jakarta.validation.Valid;
import jmc.ex4.dto.ChoreDto;
import jmc.ex4.model.*;
import jmc.ex4.services.*;
import jmc.ex4.utils.CurrentUserProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller for handling tenant-related requests.
 * Provides endpoints for tenant dashboard, chores management, and handling cases where a tenant has no assigned apartment.
 */
@Controller
@RequestMapping("/tenant")
@AllArgsConstructor
public class TenantController {

    private final CurrentUserProvider currentUserProvider;
    private final ApartmentService apartmentService;
    private final ChoreService choreService;
    private final ChoreDraftHolder choreDraftHolder;

    /**
     * Redirects to the tenant dashboard if the user has an assigned apartment.
     * If not, redirects to a page indicating no apartment is assigned.
     *
     * @param model the model to add attributes for the view
     * @return the view name for the tenant dashboard or redirection to no-apartment page
     */
    @GetMapping("/dashboard")
    public String tenantDashboard(Model model) {
        UserInfo user = currentUserProvider.getCurrentUser();

        model.addAttribute("user", user);
        Apartment apt = apartmentService.getApartmentById(user.getResidence().getId());
        List<UserInfo> allResidents = apartmentService.getApartmentResidents(apt.getId());
        List<UserInfo> otherResidents = allResidents.stream()
                .filter(resident -> !resident.getId().equals(user.getId()))
                .toList();
        model.addAttribute("apt", apt);
        model.addAttribute("otherResidents", otherResidents);
        return "tenant/dashboard";
    }

    /**
     * Displays the chores page for the tenant, allowing them to view and manage chores.
     * If a chore draft exists in the session, it is used to pre-populate the form.
     *
     * @param model the model to add attributes for the view
     * @return the view name for the tenant chores page
     */
    @GetMapping("/chores-page")
    public String tenantChores(Model model) {
        UserInfo currentUser = currentUserProvider.getCurrentUser();

        //if we have a draft saved in the session
        ChoreDto choreDto = choreDraftHolder.getDraft() != null ? choreDraftHolder.getDraft() : new ChoreDto();

        model.addAttribute("choreDto", choreDto);
        model.addAttribute("users", apartmentService.getApartmentResidents(currentUser.getResidence().getId()));
        model.addAttribute("currentUser", currentUser);

        return "tenant/chores-page";
    }

    /**
     * Handles the submission of a new chore by the tenant.
     * Validates the chore data and, if valid, creates a new chore.
     * If there are validation errors, it redisplays the chores page with error messages.
     *
     * @param choreDto the data transfer object containing chore details
     * @param result the binding result containing validation errors, if any
     * @param model the model to add attributes for the view
     * @return the view name for the tenant chores page or a redirect to the chores page if successful
     */
    @PostMapping("/chores-page/add")
    public String addChore(@Valid @ModelAttribute("choreDto") ChoreDto choreDto, BindingResult result, Model model) {
        UserInfo currentUser = currentUserProvider.getCurrentUser();

        if (result.hasErrors()) {
            model.addAttribute("choreDto", choreDto);
            model.addAttribute("users", apartmentService.getApartmentResidents(currentUser.getResidence().getId()));
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("showChoreModal", true);
            return "tenant/chores-page";
        }

        choreService.createChore(choreDto);
        return "redirect:/tenant/chores-page";

    }


}
