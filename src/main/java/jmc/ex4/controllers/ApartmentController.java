package jmc.ex4.controllers;

import jakarta.validation.Valid;
import jmc.ex4.dto.CreateApartmentDto;
import jmc.ex4.dto.JoinApartmentDto;
import jmc.ex4.model.Apartment;
import jmc.ex4.model.enums.Role;
import jmc.ex4.model.UserInfo;
import jmc.ex4.services.ApartmentService;
import jmc.ex4.services.UserService;
import jmc.ex4.utils.CurrentUserProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling apartment-related requests.
 * Provides endpoints for creating, joining, and leaving apartments.
 */
@Controller
@AllArgsConstructor
@RequestMapping
public class ApartmentController {

    private final ApartmentService apartmentService;
    private final CurrentUserProvider currentUserProvider;
    private final UserService userService;

    /**
     * Displays the no-apartment page where users can create or join an apartment.
     * If the user is already associated with an apartment, they are redirected to the home page.
     *
     * @param model the model to add attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/no-apartment")
    public String noApartmentPage (Model model) {
        UserInfo user = currentUserProvider.getCurrentUser();
        if(user.getRole() == Role.TENANT && user.getResidence() != null){
            return "redirect:/";
        }
        model.addAttribute("createApartmentDto", new CreateApartmentDto());
        model.addAttribute("joinApartmentDto", new JoinApartmentDto());
        return "no-apartment";
    }

    /**
     * Handles the creation of a new apartment.
     * Validates the input and creates the apartment based on the user's role.
     *
     * @param createApartmentDto the DTO containing apartment creation data
     * @param result             the binding result for validation errors
     * @param model              the model to add attributes for the view
     * @return redirect to home page or re-render no-apartment page with errors
     */
    @PostMapping("/apartment/registration")
    public String createApartment (@ModelAttribute @Valid CreateApartmentDto createApartmentDto,
                                   BindingResult result,
                                   Model model) {
        UserInfo user = currentUserProvider.getCurrentUser();
        if (result.hasErrors()) {
            model.addAttribute("showCreateApartmentModal", true);
            return "no-apartment";
        }

        try {
            if (user.getRole() == Role.OWNER) {
                apartmentService.createApartmentAsOwner(createApartmentDto.getAddress(), user.getId());
            } else if (user.getRole() == Role.TENANT) {
                apartmentService.createApartmentAsTenant(createApartmentDto.getAddress(), user.getId());
            }
        } catch (IllegalArgumentException e) {
            result.rejectValue("address", "error.apartmentDto", e.getMessage());
            model.addAttribute("showCreateApartmentModal", true);
            return "no-apartment";
        }
        return "redirect:/";
    }

    /**
     * Handles the request to join an existing apartment using a reference code.
     * Validates the input and adds the user to the apartment based on their role.
     *
     * @param joinApartmentDto the DTO containing the reference code for joining an apartment
     * @param result           the binding result for validation errors
     * @param model            the model to add attributes for the view
     * @return redirect to home page or re-render no-apartment page with errors
     */
    @PostMapping("/apartment/join")
    public String joinApartment (@ModelAttribute @Valid JoinApartmentDto joinApartmentDto,
                                 BindingResult result,
                                 Model model) {
        UserInfo user = currentUserProvider.getCurrentUser();
        try{
            Apartment apt = apartmentService.getApartmentByReferenceCode(joinApartmentDto.getReferenceCode());
            if(user.getRole() == Role.OWNER){
                apartmentService.addOwnerToApartment(apt.getId(), user.getId());
            }else{
                apartmentService.addTenantToApartment(apt.getId(), user.getId());
            }
        }catch (IllegalArgumentException e){
            result.rejectValue("referenceCode", "error.apartmentDto", e.getMessage());
            model.addAttribute("showJoinApartmentModal", true);
            return "no-apartment";
        }
        return "redirect:/";
    }

    /**
     * Handles the request to leave an apartment.
     * The action depends on the user's role (tenant or owner).
     *
     * @param apartmentId the ID of the apartment to leave
     * @return redirect to home page
     */
    @PostMapping("/apartment/leave")
    public String leaveApartment(@RequestParam("apartmentId") Long apartmentId){
        UserInfo user = currentUserProvider.getCurrentUser();
        if(user.getResidence() != null && user.getRole() == Role.TENANT){
            userService.leaveRentedApartment(user.getId());
        } else if (user.getRole() == Role.OWNER) {
            userService.removeOwnedApartment(user.getId(), apartmentId);
        }
        return "redirect:/";
    }
}
