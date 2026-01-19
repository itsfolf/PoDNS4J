package me.folf.podns4j;

import me.folf.podns4j.model.*;
import javax.naming.NamingException;
import java.util.List;

/**
 * Main API for the PoDNS4J library - Pronouns over DNS for Java.
 * 
 * This library implements the Pronouns over DNS specification, allowing
 * retrieval and parsing of personal pronouns from DNS TXT records.
 * 
 * Example usage:
 *
 * <pre>
 * PoDNS4J podns = new PoDNS4J();
 * PronounResult result = podns.lookup("example.com");
 * </pre>
 */
public class PoDNS4J {
    private final PronounDnsResolver resolver;

    /**
     * Creates a new PoDNS4J instance.
     */
    public PoDNS4J() {
        this(new PronounDnsResolver());
    }

    /**
     * Creates a new PoDNS4J instance with a custom resolver.
     *
     * @param resolver the resolver to use
     */
    public PoDNS4J(PronounDnsResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Looks up pronouns for the given domain.
     *
     * @param domain the domain to look up (e.g., "example.com")
     * @return the pronoun result, or null if no records were found
     * @throws PronounParseException if the records cannot be parsed
     * @throws NamingException       if the DNS query fails
     */
    public PronounResult lookup(String domain) throws PronounParseException, NamingException {
        List<PronounRecord> records = resolver.resolve(domain);
        if (records.isEmpty()) {
            return null;
        }
        return PronounResult.fromRecords(records);
    }

    /**
     * Parses a pronoun record string.
     *
     * @param record the record string to parse
     * @return the parsed pronoun record
     * @throws PronounParseException if the record is invalid
     */
    public static PronounRecord parse(String record) throws PronounParseException {
        return PronounRecordParser.parse(record);
    }

    /**
     * Parses pronoun record strings and selects the preferred one.
     *
     * @param records the record strings to parse
     * @return the pronoun result
     * @throws PronounParseException if any record is invalid
     */
    public static PronounResult parseAndSelect(List<String> records) throws PronounParseException {
        if (records == null || records.isEmpty()) {
            return null;
        }

        List<PronounRecord> parsedRecords = new java.util.ArrayList<>();
        for (String record : records) {
            parsedRecords.add(PronounRecordParser.parse(record));
        }

        PronounRecordParser.validateRecords(parsedRecords);
        return PronounResult.fromRecords(parsedRecords);
    }
}
