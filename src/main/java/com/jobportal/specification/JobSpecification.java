package com.jobportal.specification;

import com.jobportal.entity.Job;
import org.springframework.data.jpa.domain.Specification;

/**
 * Dynamic query specifications for the Job entity.
 * Used with JpaSpecificationExecutor for filtering by skill, location, and experience level.
 */
public final class JobSpecification {

    private JobSpecification() {
        // utility class
    }

    /**
     * Filters jobs whose requiredSkills column contains the given skill (case-insensitive LIKE).
     */
    public static Specification<Job> hasSkill(String skill) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("requiredSkills")), "%" + skill.toLowerCase() + "%");
    }

    /**
     * Filters jobs whose location contains the given value (case-insensitive LIKE).
     */
    public static Specification<Job> hasLocation(String location) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }

    /**
     * Filters jobs where the required experience is at most the given value.
     * This finds jobs the candidate qualifies for based on their years of experience.
     */
    public static Specification<Job> hasMaxExperience(Integer experience) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("experienceRequired"), experience);
    }

    /**
     * Filters only active (non-deleted) jobs.
     */
    public static Specification<Job> isActive() {
        return (root, query, cb) ->
                cb.isTrue(root.get("isActive"));
    }
}
