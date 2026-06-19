package com.jobportal.entity;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Enumeration representing the lifecycle status of a job application.
 * Includes a built-in state machine that enforces valid transitions.
 */
public enum ApplicationStatus {
    APPLIED,
    SHORTLISTED,
    INTERVIEW,
    REJECTED,
    HIRED;

    private static final Map<ApplicationStatus, Set<ApplicationStatus>> VALID_TRANSITIONS;

    static {
        VALID_TRANSITIONS = new EnumMap<>(ApplicationStatus.class);
        VALID_TRANSITIONS.put(APPLIED, Set.of(SHORTLISTED, REJECTED));
        VALID_TRANSITIONS.put(SHORTLISTED, Set.of(INTERVIEW, REJECTED));
        VALID_TRANSITIONS.put(INTERVIEW, Set.of(HIRED, REJECTED));
        VALID_TRANSITIONS.put(REJECTED, Set.of());   // terminal
        VALID_TRANSITIONS.put(HIRED, Set.of());      // terminal
    }

    /**
     * Checks whether transitioning from this status to the given target is allowed.
     *
     * @param target the desired next status
     * @return true if the transition is valid
     */
    public boolean canTransitionTo(ApplicationStatus target) {
        return VALID_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
