package me.folf.podns4j;

import me.folf.podns4j.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@DisplayName("PoDNS4J Library Tests")
class PoDNS4JTest {

    @Test
    @DisplayName("Parse valid pronoun sets from spec")
    void testValidPronounSets() throws PronounParseException {
        // she/her
        PronounRecord record = PoDNS4J.parse("she/her");
        assertTrue(record.isPronounSet());
        assertEquals("she", record.pronounSet().subject());
        assertEquals("her", record.pronounSet().object());

        // he/him/his/his/himself;preferred
        record = PoDNS4J.parse("he/him/his/his/himself;preferred");
        assertEquals("he", record.pronounSet().subject());
        assertEquals("him", record.pronounSet().object());
        assertEquals("his", record.pronounSet().possessiveDeterminer());
        assertEquals("his", record.pronounSet().possessivePronoun());
        assertEquals("himself", record.pronounSet().reflexive());
        assertTrue(record.pronounSet().isPreferred());

        // they/them/their/theirs/themself
        record = PoDNS4J.parse("they/them/their/theirs/themself");
        assertEquals("they", record.pronounSet().subject());
        assertEquals("them", record.pronounSet().object());
        assertEquals("their", record.pronounSet().possessiveDeterminer());
        assertEquals("theirs", record.pronounSet().possessivePronoun());
        assertEquals("themself", record.pronounSet().reflexive());

        // they/them;preferred;plural
        record = PoDNS4J.parse("they/them;preferred;plural");
        assertEquals("they", record.pronounSet().subject());
        assertEquals("them", record.pronounSet().object());
        assertTrue(record.pronounSet().isPreferred());
        assertTrue(record.pronounSet().isPlural());

        // ze/zir/zir/zirself
        record = PoDNS4J.parse("ze/zir/zir/zirself");
        assertEquals("ze", record.pronounSet().subject());
        assertEquals("zir", record.pronounSet().object());
        assertEquals("zir", record.pronounSet().possessiveDeterminer());
        assertEquals("zirself", record.pronounSet().possessivePronoun());
        assertNull(record.pronounSet().reflexive());
    }

    @Test
    @DisplayName("Parse wildcard record")
    void testWildcard() throws PronounParseException {
        PronounRecord record = PoDNS4J.parse("*");
        assertTrue(record.isWildcard());
        assertNull(record.pronounSet());
    }

    @Test
    @DisplayName("Parse none record")
    void testNone() throws PronounParseException {
        PronounRecord record = PoDNS4J.parse("!");
        assertTrue(record.isNone());
        assertNull(record.pronounSet());
    }

    @Test
    @DisplayName("Parse comment-only record")
    void testCommentOnly() throws PronounParseException {
        PronounRecord record = PoDNS4J.parse("# This is just a comment");
        assertTrue(record.isCommentOnly());
        assertEquals("This is just a comment", record.comment());
    }

    @Test
    @DisplayName("Parse records with comments")
    void testRecordsWithComments() throws PronounParseException {
        PronounRecord record = PoDNS4J.parse("she/her # preferred pronouns");
        assertTrue(record.isPronounSet());
        assertEquals("she", record.pronounSet().subject());
        assertEquals("preferred pronouns", record.comment());

        record = PoDNS4J.parse("* # accepts any");
        assertTrue(record.isWildcard());
        assertEquals("accepts any", record.comment());
    }

    @Test
    @DisplayName("Normalize non-canonical records")
    void testNonCanonical() throws PronounParseException {
        // SHE/HER -> she/her (uppercase)
        PronounRecord record = PoDNS4J.parse("SHE/HER");
        assertEquals("she", record.pronounSet().subject());
        assertEquals("her", record.pronounSet().object());

        // SHE / HER -> she/her (whitespace)
        record = PoDNS4J.parse("SHE /    HER");
        assertEquals("SHE /    HER", record.raw());
        assertEquals("she", record.pronounSet().subject());
        assertEquals("her", record.pronounSet().object());

        // he/him;;;preferred -> he/him;preferred (duplicate semicolons)
        record = PoDNS4J.parse("he/him;;;preferred");
        assertEquals("he/him;;;preferred", record.raw());
        assertEquals("he", record.pronounSet().subject());
        assertTrue(record.pronounSet().isPreferred());
    }

    @Test
    @DisplayName("Parser conversions (it/its)")
    void testConversions() throws PronounParseException {
        PronounRecord record = PoDNS4J.parse("it/its");
        assertEquals("it", record.pronounSet().subject());
        assertEquals("it", record.pronounSet().object());
        assertEquals("its", record.pronounSet().possessiveDeterminer());
        assertEquals("its", record.pronounSet().possessivePronoun());
        assertEquals("itself", record.pronounSet().reflexive());
    }

    @Test
    @DisplayName("Reject invalid records")
    void testInvalidRecords() {
        // she/her/ (trailing slash)
        assertThrows(PronounParseException.class, () -> PoDNS4J.parse("she/her/"));

        // she (missing object)
        assertThrows(PronounParseException.class, () -> PoDNS4J.parse("she"));

        // >5 components and invalid tags are silently dropped, so no test here

        // Empty record
        assertThrows(PronounParseException.class, () -> PoDNS4J.parse(""));
    }

    @Test
    @DisplayName("Plural detection - they/them automatically plural")
    void testPluralDetection() throws PronounParseException {
        PronounRecord record = PoDNS4J.parse("they/them");
        assertTrue(record.pronounSet().isPlural());

        // With explicit tag
        record = PoDNS4J.parse("they/them;plural");
        assertTrue(record.pronounSet().isPlural());

        // Other pronouns not plural unless tagged
        record = PoDNS4J.parse("she/her");
        assertFalse(record.pronounSet().isPlural());
    }

    @Test
    @DisplayName("Selection: wildcard only")
    void testWildcardOnly() throws PronounParseException {
        PronounResult result = PoDNS4J.parseAndSelect(List.of("*"));
        assertTrue(result.acceptsAny());
        assertNotNull(result.preferred());
        assertEquals("they", result.preferred().subject());
        assertEquals("them", result.preferred().object());
    }

    @Test
    @DisplayName("Selection: wildcard with one other record")
    void testWildcardWithOne() throws PronounParseException {
        PronounResult result = PoDNS4J.parseAndSelect(List.of("*", "she/her"));
        assertTrue(result.acceptsAny());
        assertEquals("she", result.preferred().subject());
    }

    @Test
    @DisplayName("Selection: wildcard with multiple records")
    void testWildcardWithMultiple() throws PronounParseException {
        PronounResult result = PoDNS4J.parseAndSelect(List.of("*", "she/her", "he/him"));
        assertTrue(result.acceptsAny());
        // Should select first (per spec)
        assertEquals("she", result.preferred().subject());
    }

    @Test
    @DisplayName("Selection: wildcard with preferred tag")
    void testWildcardWithPreferred() throws PronounParseException {
        PronounResult result = PoDNS4J.parseAndSelect(
                List.of("*", "she/her;preferred", "he/him"));
        assertTrue(result.acceptsAny());
        assertEquals("she", result.preferred().subject());
        assertTrue(result.preferred().isPreferred());
    }

    @Test
    @DisplayName("Selection: multiple preferred tags, use first")
    void testMultiplePreferred() throws PronounParseException {
        PronounResult result = PoDNS4J.parseAndSelect(
                List.of("*", "she/her;preferred", "he/him;preferred", "they/them"));
        assertTrue(result.acceptsAny());
        assertEquals("she", result.preferred().subject());
    }

    @Test
    @DisplayName("Selection: none record only")
    void testNoneOnly() throws PronounParseException {
        PronounResult result = PoDNS4J.parseAndSelect(List.of("!"));
        assertTrue(result.prefersName());
        assertNull(result.preferred());
    }

    @Test
    @DisplayName("Selection: none record with others is invalid")
    void testNoneWithOthers() {
        assertThrows(PronounParseException.class, () -> PoDNS4J.parseAndSelect(List.of("!", "she/her")));
    }

    @Test
    @DisplayName("Selection: comment-only records are filtered")
    void testCommentOnlyFiltered() throws PronounParseException {
        PronounResult result = PoDNS4J.parseAndSelect(
                List.of("# Just a comment", "she/her", "# Another comment"));
        assertNotNull(result);
        assertEquals("she", result.preferred().subject());
    }

    @Test
    @DisplayName("Selection: multiple sets without preferred, use first")
    void testMultipleSetsUseFirst() throws PronounParseException {
        PronounResult result = PoDNS4J.parseAndSelect(
                List.of("she/her", "he/him", "they/them"));
        assertFalse(result.acceptsAny());
        assertEquals("she", result.preferred().subject());
        assertEquals(3, result.allSets().size());
    }

    @Test
    @DisplayName("Selection: one preferred among many")
    void testOnePreferredAmongMany() throws PronounParseException {
        PronounResult result = PoDNS4J.parseAndSelect(
                List.of("she/her", "he/him;preferred", "they/them"));
        assertEquals("he", result.preferred().subject());
    }

    @Test
    @DisplayName("Canonical string representation")
    void testCanonicalString() throws PronounParseException {
        PronounRecord record = PoDNS4J.parse("SHE / HER ; preferred");
        assertEquals("she/her;preferred", record.pronounSet().toCanonicalString());

        record = PoDNS4J.parse("they/them/their/theirs/themself;plural;preferred");
        String canonical = record.pronounSet().toCanonicalString();
        assertTrue(canonical.contains("they/them/their/theirs/themself"));
        assertTrue(canonical.contains("plural"));
        assertTrue(canonical.contains("preferred"));
    }

    @Test
    @DisplayName("End-to-end lookup with test resolver")
    void testLookup() throws PronounParseException, javax.naming.NamingException {
        TestPronounDnsResolver testResolver = new TestPronounDnsResolver();
        testResolver.addRecord("example.com", "they/them");
        testResolver.addRecord("example.com", "she/her;preferred");

        PoDNS4J podns4j = new PoDNS4J(testResolver);
        PronounResult result = podns4j.lookup("example.com");

        assertNotNull(result);
        assertEquals("she", result.preferred().subject());
        assertEquals("her", result.preferred().object());
        assertTrue(result.preferred().isPreferred());
        assertEquals(2, result.allSets().size());
    }
}
