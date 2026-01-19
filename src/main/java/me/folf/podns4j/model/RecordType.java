package me.folf.podns4j.model;

/**
 * The type of pronoun record.
 */
public enum RecordType {
    /**
     * A pronoun set (e.g., "she/her", "they/them").
     */
    PRONOUN_SET,

    /**
     * A wildcard record (*) indicating the user is open to any pronouns.
     */
    WILDCARD,

    /**
     * A none record (!) indicating the user prefers to be referred to by name.
     */
    NONE,

    /**
     * A comment-only record with no base record.
     */
    COMMENT
}
