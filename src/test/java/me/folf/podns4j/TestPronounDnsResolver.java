package me.folf.podns4j;

import me.folf.podns4j.model.PronounRecord;
import javax.naming.NamingException;
import java.util.*;

public class TestPronounDnsResolver extends PronounDnsResolver {
    private final Map<String, List<String>> records = new HashMap<>();

    public void addRecord(String domain, String record) {
        records.computeIfAbsent(domain, k -> new ArrayList<>()).add(record);
    }

    public void addRecords(String domain, List<String> recordList) {
        records.computeIfAbsent(domain, k -> new ArrayList<>()).addAll(recordList);
    }

    @Override
    public List<PronounRecord> resolve(String domain) throws PronounParseException, NamingException {
        List<String> txtRecords = records.getOrDefault(domain, List.of());

        List<PronounRecord> parsed = new ArrayList<>();
        for (String txtRecord : txtRecords) {
            PronounRecord record = PronounRecordParser.parse(txtRecord);
            parsed.add(record);
        }

        PronounRecordParser.validateRecords(parsed);
        return parsed;
    }
}
