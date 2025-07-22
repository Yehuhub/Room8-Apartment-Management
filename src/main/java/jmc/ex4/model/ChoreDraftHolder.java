package jmc.ex4.model;

import jmc.ex4.dto.ChoreDto;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;

/**
 * This class holds a draft of a chore in the user's session.
 * It allows saving, retrieving, and clearing the draft.
 */
@Component
@SessionScope
public class ChoreDraftHolder implements Serializable {
    private ChoreDto draft;

    /**
     * Saves the given chore draft.
     *
     * @param draft the chore draft to save
     */
    public void saveDraft(ChoreDto draft) {
        this.draft = draft;
    }

    /**
     * Retrieves the current chore draft.
     *
     * @return the current chore draft, or null if no draft exists
     */
    public ChoreDto getDraft() {
        return draft;
    }

    /**
     * Clears the current chore draft.
     */
    public void clearDraft() {
        this.draft = null;
    }
}
