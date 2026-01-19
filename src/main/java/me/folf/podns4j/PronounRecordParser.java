package me.folf.podns4j;

import me.folf.podns4j.model.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Parser for pronoun records according to the Pronouns over DNS specification.
 */
public class PronounRecordParser {
    private static final Pattern VALUE_PATTERN = Pattern.compile("^[a-z]+$");

    // Conversion table for common pronoun sets
    private static final Map<String, String> CONVERSIONS = new HashMap<>();
    static {
        CONVERSIONS.put("it/its", "it/it/its/its/itself");
    }

    /**
     * Parses a pronoun record from a DNS TXT record string.
     *
     * @param record the record string to parse
     * @return the parsed pronoun record
     * @throws PronounParseException if the record is invalid
     */
    public static PronounRecord parse(String record) throws PronounParseException {
        if (record == null) {
            throw new PronounParseException("Record cannot be null");
        }

        String comment = null;
        String baseRecord = record;
        int commentIndex = record.indexOf('#');
        if (commentIndex != -1) {
            comment = record.substring(commentIndex + 1).trim();
            baseRecord = record.substring(0, commentIndex);
        }
        baseRecord = baseRecord.trim().toLowerCase();

        // Comment record
        if (baseRecord.isEmpty()) {
            if (comment != null && !comment.isEmpty()) {
                return new PronounRecord(RecordType.COMMENT, comment);
            }
            throw new PronounParseException("Record cannot be empty");
        }

        // Check for wildcard
        if (baseRecord.equals("*")) {
            return new PronounRecord(RecordType.WILDCARD, comment);
        }

        // Check for none
        if (baseRecord.equals("!")) {
            return new PronounRecord(RecordType.NONE, comment);
        }

        // Apply conversions
        for (Map.Entry<String, String> conversion : CONVERSIONS.entrySet()) {
            if (baseRecord.startsWith(conversion.getKey())) {
                String remainder = baseRecord.substring(conversion.getKey().length());
                baseRecord = conversion.getValue() + remainder;
                break;
            }
        }

        // Parse pronoun set
        PronounSet pronounSet = parsePronounSet(baseRecord);
        return new PronounRecord(RecordType.PRONOUN_SET, pronounSet, comment);
    }

    private static PronounSet parsePronounSet(String record) throws PronounParseException {
        // Split by semicolon to separate pronouns from tags
        String[] parts = record.split(";", -1);
        String pronounPart = parts[0].trim();

        if (pronounPart.isEmpty()) {
            throw new PronounParseException("Pronoun set cannot be empty");
        }

        // Parse tags
        Set<Tag> tags = new HashSet<>();
        for (int i = 1; i < parts.length; i++) {
            String tagStr = parts[i].trim();
            if (!tagStr.isEmpty()) {
                Tag tag = parseTag(tagStr);
                if (tag != null) {
                    tags.add(tag);
                }
            }
        }

        // Parse pronouns
        String[] pronouns = pronounPart.split("/", -1);

        // Validate: must have at least 2 components (subject and object)
        if (pronouns.length < 2) {
            throw new PronounParseException("Pronoun set must have at least subject and object: " + pronounPart);
        }

        // Validate: must have at most 5 components
        if (pronouns.length > 5) {
            throw new PronounParseException("Pronoun set has too many components (max 5): " + pronounPart);
        }

        // Validate: no empty components except possibly trailing
        for (int i = 0; i < pronouns.length; i++) {
            pronouns[i] = pronouns[i].trim();
            if (pronouns[i].isEmpty()) {
                throw new PronounParseException("Pronoun component cannot be empty: " + pronounPart);
            }
            if (!VALUE_PATTERN.matcher(pronouns[i]).matches()) {
                throw new PronounParseException(
                        "Invalid pronoun value (must be lowercase letters only): " + pronouns[i]);
            }
        }

        String subject = pronouns[0];
        String object = pronouns[1];
        String possessiveDeterminer = pronouns.length > 2 ? pronouns[2] : null;
        String possessivePronoun = pronouns.length > 3 ? pronouns[3] : null;
        String reflexive = pronouns.length > 4 ? pronouns[4] : null;

        return new PronounSet(subject, object, possessiveDeterminer, possessivePronoun, reflexive, tags);
    }

    private static Tag parseTag(String tagStr) throws PronounParseException {
        switch (tagStr) {
            case "preferred":
                return Tag.PREFERRED;
            case "plural":
                return Tag.PLURAL;
            default:
                throw new PronounParseException("Unknown tag: " + tagStr);
        }
    }

    /**
     * Validates that a list of records is valid according to the specification.
     * This checks that if a NONE record is present, it must be the only record.
     *
     * @param records the records to validate
     * @throws PronounParseException if the records are invalid
     */
    public static void validateRecords(List<PronounRecord> records) throws PronounParseException {
        if (records == null || records.isEmpty()) {
            return;
        }

        boolean hasNone = false;
        for (PronounRecord record : records) {
            if (record.isNone()) {
                hasNone = true;
                break;
            }
        }

        if (hasNone && records.size() > 1) {
            throw new PronounParseException("A none record (!) must be the only record if present");
        }
    }
}
