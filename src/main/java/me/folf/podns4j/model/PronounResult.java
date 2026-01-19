package me.folf.podns4j.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Result of pronoun lookup, containing the preferred pronoun set and context.
 * 
 * @param preferred   the preferred pronoun set (may be null if person prefers
 *                    name)
 * @param allSets     all available pronoun sets
 * @param acceptsAny  true if the person accepts any pronouns (wildcard was
 *                    present)
 * @param prefersName true if the person prefers to be referred to by name (none
 *                    record)
 */
public record PronounResult(
        PronounSet preferred,
        List<PronounSet> allSets,
        boolean acceptsAny,
        boolean prefersName) {
    /**
     * Validates and copies parameters.
     * 
     * @param preferred   the preferred set
     * @param allSets     all sets
     * @param acceptsAny  accepts any
     * @param prefersName prefers name
     */
    public PronounResult {
        allSets = allSets != null ? List.copyOf(allSets) : List.of();
    }

    /**
     * Creates a pronoun result from a list of pronoun records.
     *
     * @param records the records to process
     * @return the pronoun result, or null if records is empty or contains only
     *         comments
     */
    public static PronounResult fromRecords(List<PronounRecord> records) {
        if (records == null || records.isEmpty()) {
            return null;
        }

        List<PronounRecord> effectiveRecords = records.stream()
                .filter(r -> !r.isCommentOnly())
                .collect(Collectors.toList());

        if (effectiveRecords.isEmpty()) {
            return null;
        }

        if (effectiveRecords.size() == 1 && effectiveRecords.get(0).isNone()) {
            return none();
        }

        boolean hasWildcard = false;
        List<PronounSet> pronounSets = new ArrayList<>();
        for (PronounRecord record : effectiveRecords) {
            if (record.isWildcard()) {
                hasWildcard = true;
            } else if (record.isPronounSet()) {
                pronounSets.add(record.pronounSet());
            }
        }

        // If only wildcard, return they/them as default per spec
        if (hasWildcard && pronounSets.isEmpty()) {
            PronounSet defaultSet = new PronounSet("they", "them", Set.of());
            return wildcard(defaultSet, List.of(defaultSet));
        }

        // Select preferred set
        PronounSet preferred = selectPreferred(pronounSets);
        if (hasWildcard) {
            return wildcard(preferred, pronounSets);
        } else {
            return standard(preferred, pronounSets);
        }
    }

    /**
     * Selects the preferred pronoun set from a list of pronoun sets.
     * 
     * <p>
     * Selection strategy per spec:
     * 1. If there's exactly one set tagged with 'preferred', use it
     * 2. If there are multiple sets tagged with 'preferred', use the first of those
     * 3. Otherwise, use the first set in the list
     * </p>
     */
    private static PronounSet selectPreferred(List<PronounSet> pronounSets) {
        if (pronounSets.isEmpty()) {
            return null;
        }

        if (pronounSets.size() == 1) {
            return pronounSets.get(0);
        }

        List<PronounSet> preferredSets = pronounSets.stream()
                .filter(PronounSet::isPreferred)
                .collect(Collectors.toList());

        return preferredSets.isEmpty() ? pronounSets.get(0) : preferredSets.get(0);
    }

    /**
     * Creates wildcard result.
     * 
     * @param preferred the preferred set
     * @param allSets   all sets
     * @return wildcard result
     */
    public static PronounResult wildcard(PronounSet preferred, List<PronounSet> allSets) {
        return new PronounResult(preferred, allSets, true, false);
    }

    /**
     * Creates none result.
     * 
     * @return none result
     */
    public static PronounResult none() {
        return new PronounResult(null, List.of(), false, true);
    }

    /**
     * Creates standard result.
     * 
     * @param preferred the preferred set
     * @param allSets   all sets
     * @return standard result
     */
    public static PronounResult standard(PronounSet preferred, List<PronounSet> allSets) {
        return new PronounResult(preferred, allSets, false, false);
    }

    @Override
    public String toString() {
        if (prefersName) {
            return "PronounResult{prefersName=true}";
        }
        StringBuilder sb = new StringBuilder("PronounResult{");
        if (preferred != null) {
            sb.append("preferred=").append(preferred);
        }
        if (acceptsAny) {
            sb.append(", acceptsAny=true");
        }
        if (!allSets.isEmpty()) {
            sb.append(", allSets=").append(allSets);
        }
        sb.append("}");
        return sb.toString();
    }
}
