package es.vargontoc.educational.framework.tracking.validation;

import es.vargontoc.educational.framework.tracking.model.ActivityProposalLog;
import es.vargontoc.educational.framework.tracking.model.ActivityProposalOutcome;
import es.vargontoc.educational.framework.shared.exception.ValidationException;

public class ActivityProposalLogValidator {

    public void validateForRegistration(
            Long childProfileId,
            Long childSessionId,
            Long activityId) {

        if (childProfileId == null) {
            throw new ValidationException("childProfileId is required");
        }
        if (childSessionId == null) {
            throw new ValidationException("childSessionId is required");
        }
        if (activityId == null) {
            throw new ValidationException("activityId is required");
        }
    }

    public void validateForResolution(
            ActivityProposalLog existingProposal,
            ActivityProposalOutcome outcome) {

        if (outcome == null) {
            throw new ValidationException("outcome is required");
        }
        if (existingProposal.getOutcome() != null) {
            throw new ValidationException("proposal already resolved");
        }
    }
}
