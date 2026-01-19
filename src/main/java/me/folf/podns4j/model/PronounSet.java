package me.folf.podns4j.model;

import java.util.Objects;
import java.util.Set;

/**
 * Represents a set of personal pronouns with optional tags.
 * 
 * @param subject              the subject pronoun (e.g., "she", "he", "they")
 * @param object               the object pronoun (e.g., "her", "him", "them")
 * @param possessiveDeterminer the possessive determiner (e.g., "her", "his",
 *                             "their"), may be null
 * @param possessivePronoun    the possessive pronoun (e.g., "hers", "his",
 *                             "theirs"), may be null
 * @param reflexive            the reflexive pronoun (e.g., "herself",
 *                             "himself", "themself"), may be null
 * @param tags                 the tags applied to this pronoun set (immutable)
 */
public record PronounSet(
        String subject,
        String object,
        String possessiveDeterminer,
        String possessivePronoun,
        String reflexive,
        Set<Tag> tags) {
    private static final Set<String> IMPLICIT_PLURAL_SUBJECTS = Set.of("they");

    /**
     * Validates and copies parameters.
     * 
     * @param subject              the subject
     * @param object               the object
     * @param possessiveDeterminer the possessive determiner
     * @param possessivePronoun    the possessive pronoun
     * @param reflexive            the reflexive
     * @param tags                 the tags
     */
    public PronounSet {
        Objects.requireNonNull(subject, "subject cannot be null");
        Objects.requireNonNull(object, "object cannot be null");
        tags = tags != null ? Set.copyOf(tags) : Set.of();
    }

    /**
     * Creates pronoun set with subject and object.
     * 
     * @param subject the subject
     * @param object  the object
     * @param tags    the tags
     */
    public PronounSet(String subject, String object, Set<Tag> tags) {
        this(subject, object, null, null, null, tags);
    }

    /**
     * Creates pronoun set with possessive determiner.
     * 
     * @param subject              the subject
     * @param object               the object
     * @param possessiveDeterminer the possessive determiner
     * @param tags                 the tags
     */
    public PronounSet(String subject, String object, String possessiveDeterminer, Set<Tag> tags) {
        this(subject, object, possessiveDeterminer, null, null, tags);
    }

    /**
     * Creates pronoun set with possessive pronoun.
     * 
     * @param subject              the subject
     * @param object               the object
     * @param possessiveDeterminer the possessive determiner
     * @param possessivePronoun    the possessive pronoun
     * @param tags                 the tags
     */
    public PronounSet(String subject, String object, String possessiveDeterminer,
            String possessivePronoun, Set<Tag> tags) {
        this(subject, object, possessiveDeterminer, possessivePronoun, null, tags);
    }

    /**
     * Returns true if has tag.
     * 
     * @param tag the tag to check
     * @return true if has tag
     */
    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    /**
     * Returns true if plural verb agreement.
     * 
     * @return true if plural
     */
    public boolean isPlural() {
        return hasTag(Tag.PLURAL) || IMPLICIT_PLURAL_SUBJECTS.contains(subject);
    }

    /**
     * Returns true if preferred.
     * 
     * @return true if preferred
     */
    public boolean isPreferred() {
        return hasTag(Tag.PREFERRED);
    }

    /**
     * Returns canonical string representation.
     * 
     * @return canonical string
     */
    public String toCanonicalString() {
        StringBuilder sb = new StringBuilder();
        sb.append(subject).append("/").append(object);

        if (possessiveDeterminer != null) {
            sb.append("/").append(possessiveDeterminer);
        }
        if (possessivePronoun != null) {
            sb.append("/").append(possessivePronoun);
        }
        if (reflexive != null) {
            sb.append("/").append(reflexive);
        }

        if (!tags.isEmpty()) {
            sb.append(";");
            boolean first = true;
            for (Tag tag : tags) {
                if (!first) {
                    sb.append(";");
                }
                sb.append(tag.name().toLowerCase());
                first = false;
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return toCanonicalString();
    }
}
