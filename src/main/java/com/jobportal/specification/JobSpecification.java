package com.jobportal.specification;

import com.jobportal.entity.Job;
import org.springframework.data.jpa.domain.Specification;

/**
 * Dynamic query specifications for the Job entity.
 * Used with JpaSpecificationExecutor for filtering by title/skills, location, and experience.
 */
public final class JobSpecification {

    private JobSpecification() {
        // utility class
    }

    /**
     * Searches job titles and required skills using a case-insensitive partial match.
     * This makes the public search field labelled "Job title or skills" behave as advertised.
     */
    public static Specification<Job> hasSkill(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("requiredSkills")), pattern)
            );
        };
    }

    /** Filters job titles independently from required skills. */
    public static Specification<Job> hasTitle(String title) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + title.trim().toLowerCase() + "%");
    }

    /**
     * Filters jobs whose location contains the given value (case-insensitive LIKE).
     */
    public static Specification<Job> hasLocation(String location) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("location")), "%" + location.trim().toLowerCase() + "%");
    }

    /**
     * Filters jobs where the required experience is at most the given value.
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
