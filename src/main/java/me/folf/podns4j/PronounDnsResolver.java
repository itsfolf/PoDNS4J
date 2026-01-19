package me.folf.podns4j;

import me.folf.podns4j.model.PronounRecord;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.*;

/**
 * Resolver for fetching pronoun records from DNS.
 */
public class PronounDnsResolver {
    /**
     * Queries DNS for pronoun records for the given domain.
     *
     * @param domain the domain to query (e.g., "example.com")
     * @return a list of parsed pronoun records
     * @throws PronounParseException if any record cannot be parsed
     * @throws NamingException       if the DNS query fails
     */
    public List<PronounRecord> resolve(String domain) throws PronounParseException, NamingException {
        if (domain == null || domain.isEmpty()) {
            throw new IllegalArgumentException("Domain cannot be null or empty");
        }

        String pronounsDomain = domain.startsWith("pronouns.") ? domain : "pronouns." + domain;
        List<String> txtRecords = queryTxtRecords(pronounsDomain);

        List<PronounRecord> records = new ArrayList<>();
        for (String txtRecord : txtRecords) {
            PronounRecord record = PronounRecordParser.parse(txtRecord);
            records.add(record);
        }

        // Validate the records
        PronounRecordParser.validateRecords(records);

        return records;
    }

    /**
     * Queries DNS TXT records for the given hostname.
     *
     * @param hostname the hostname to query
     * @return a list of TXT record values
     * @throws NamingException if the DNS query fails
     */
    private List<String> queryTxtRecords(String hostname) throws NamingException {
        List<String> results = new ArrayList<>();

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.dns.DnsContextFactory");

        DirContext context = new InitialDirContext(env);

        try {
            Attributes attributes = context.getAttributes(hostname, new String[] { "TXT" });
            Attribute txtAttr = attributes.get("TXT");

            if (txtAttr != null) {
                for (int i = 0; i < txtAttr.size(); i++) {
                    String value = (String) txtAttr.get(i);
                    // Remove surrounding quotes if present
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    results.add(value);
                }
            }
        } finally {
            context.close();
        }

        return results;
    }
}
