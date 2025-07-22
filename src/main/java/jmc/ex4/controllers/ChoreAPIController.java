package jmc.ex4.controllers;


import jmc.ex4.dto.ChoreDisplayDto;
import jmc.ex4.dto.ChoreDto;
import jmc.ex4.dto.UserDataDto;
import jmc.ex4.model.Chore;
import jmc.ex4.model.ChoreDraftHolder;
import jmc.ex4.model.enums.Periods;
import jmc.ex4.model.UserInfo;
import jmc.ex4.services.ApartmentService;
import jmc.ex4.services.ChoreService;
import jmc.ex4.services.UserService;
import jmc.ex4.utils.CurrentUserProvider;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing chores in the application.
 * Provides endpoints to get, delete, complete, update chores and manage chore drafts.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chores")
public class ChoreAPIController {

    private final ChoreService choreService;
    private final CurrentUserProvider currentUserProvider;
    private final ApartmentService apartmentService;
    private final ChoreDraftHolder choreDraftHolder;

    /**
     * Endpoint to get the current user's chore draft.
     * @return ResponseEntity containing the chore draft or a message if no draft exists.
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDataDto>> getUsers(){
        UserInfo user = currentUserProvider.getCurrentUser();
        List<UserInfo> users = apartmentService.getApartmentResidents(user.getResidence().getId());

        List<UserDataDto> usersData = users.stream().map(UserDataDto::new).toList();
        return ResponseEntity.ok(usersData);
    }

    /**
     * Endpoint to get all chores or filtered chores based on user ID and period.
     * @param userId Optional user ID to filter chores.
     * @param period Optional period to filter chores.
     * @return ResponseEntity containing a list of chore display DTOs.
     */
    @GetMapping
    public ResponseEntity<List<ChoreDisplayDto>> getChores(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "WEEK") Periods period){
        UserInfo user = currentUserProvider.getCurrentUser();
        List<Chore> chores;

        if (userId != null || period != null) {
            chores = choreService.getFilteredChores(user.getResidence(), userId, period);
        } else {
            chores = choreService.getAllChores(user.getResidence());
        }

        List<ChoreDisplayDto> allChores = chores.stream().map(ChoreDisplayDto::new).toList();
        return ResponseEntity.ok(allChores);
    }

    /**
     * Endpoint to get a chore by its ID.
     * @param choreId The ID of the chore to retrieve.
     * @return ResponseEntity containing the chore display DTO or a not found response.
     */
    @DeleteMapping( "/{choreId}" )
    public ResponseEntity<String> deleteChore(@PathVariable Long choreId){
        choreService.deleteChore(choreId);
        return ResponseEntity.ok("Chore deleted successfully");
    }

    /**
     * Endpoint to mark a chore as completed.
     * @param choreId The ID of the chore to mark as completed.
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping("/{choreId}/complete")
    public ResponseEntity<String> completeChore(@PathVariable Long choreId){
        choreService.markChoreAsCompleted(choreId);
        return ResponseEntity.ok("Chore marked as completed successfully");
    }

    /**
     * Endpoint to update a chore.
     * @param choreId The ID of the chore to update.
     * @param choreData Map containing the updated chore data.
     * @return ResponseEntity indicating success or failure.
     */
    @PutMapping("/{choreId}")
    public ResponseEntity<String> updateChore(
            @PathVariable Long choreId,
            @RequestBody Map<String, Object> choreData) {

        String name = (String) choreData.get("name");
        String description = (String) choreData.get("description");
        LocalDate scheduledDate = LocalDate.parse((String) choreData.get("scheduledDate"));
        LocalDate today = LocalDate.now();

        // Validate that the scheduled date is not in the past
        if (scheduledDate.isBefore(today)) {
            return ResponseEntity.badRequest().body("Cannot schedule a chore for a past date");
        }

        UserInfo currentUser = currentUserProvider.getCurrentUser();

        try {
            choreService.updateChore(choreId, name, description, scheduledDate, currentUser);
            return ResponseEntity.ok("Chore updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint to save a chore draft.
     * @param choreDto The chore data transfer object containing the draft details.
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping("/draft")
    public ResponseEntity<String> saveChoreDraft(@RequestBody ChoreDto choreDto){
        choreDraftHolder.saveDraft(choreDto);
        return ResponseEntity.ok("Chore draft saved successfully");
    }

}
