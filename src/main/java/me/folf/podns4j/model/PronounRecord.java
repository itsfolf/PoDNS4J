package me.folf.podns4j.model;

import java.util.Objects;

/**
 * Represents a parsed pronoun record from DNS.
 * 
 * @param type       the type of record
 * @param pronounSet the pronoun set (null for non-pronoun-set types)
 * @param comment    optional comment
 */
public record PronounRecord(
        RecordType type,
        PronounSet pronounSet,
        String comment,
        String raw) {
    /**
     * Validates record parameters.
     * 
     * @param type       the record type
     * @param pronounSet the pronoun set
     * @param comment    the comment
     * @param raw        the raw record string
     */
    public PronounRecord {
        Objects.requireNonNull(type, "type cannot be null");

        if (type == RecordType.PRONOUN_SET && pronounSet == null) {
            throw new IllegalArgumentException("pronounSet cannot be null for PRONOUN_SET type");
        }
        if ((type == RecordType.WILDCARD || type == RecordType.NONE || type == RecordType.COMMENT)
                && pronounSet != null) {
            throw new IllegalArgumentException("pronounSet must be null for " + type + " type");
        }
    }

    /**
     * Creates pronoun set record.
     * 
     * @param pronounSet the pronoun set
     * @param raw        the raw record string
     */
    public PronounRecord(PronounSet pronounSet, String raw) {
        this(RecordType.PRONOUN_SET, pronounSet, null, raw);
    }

    /**
     * Creates wildcard, none, or comment record.
     * 
     * @param type the record type
     * @param raw  the raw record string
     */
    public PronounRecord(RecordType type, String raw) {
        this(type, null, null, raw);
    }

    /**
     * Creates wildcard, none, or comment record with comment.
     * 
     * @param type    the record type
     * @param comment the comment
     * @param raw     the raw record string
     */
    public PronounRecord(RecordType type, String comment, String raw) {
        this(type, null, comment, raw);
    }

    /**
     * Returns true if wildcard.
     * 
     * @return true if wildcard
     */
    public boolean isWildcard() {
        return type == RecordType.WILDCARD;
    }

    /**
     * Returns true if none record.
     * 
     * @return true if none
     */
    public boolean isNone() {
        return type == RecordType.NONE;
    }

    /**
     * Returns true if pronoun set.
     * 
     * @return true if pronoun set
     */
    public boolean isPronounSet() {
        return type == RecordType.PRONOUN_SET;
    }

    /**
     * Returns true if comment only.
     * 
     * @return true if comment only
     */
    public boolean isCommentOnly() {
        return type == RecordType.COMMENT;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        switch (type) {
            case WILDCARD -> sb.append("*");
            case NONE -> sb.append("!");
            case PRONOUN_SET -> sb.append(pronounSet.toCanonicalString());
            case COMMENT -> {
                // No base record
            }
        }

        if (comment != null && !comment.isEmpty()) {
            if (type != RecordType.COMMENT) {
                sb.append(" ");
            }
            sb.append("# ").append(comment);
        }

        return sb.toString();
    }
}
