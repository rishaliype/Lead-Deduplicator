package com.deduplicator;

import com.deduplicator.logging.ChangeLog;
import com.deduplicator.model.Lead;
import com.deduplicator.service.DuplicateDetector;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;


public class DuplicateDetectorTest {

    @Test
    public void testNoDuplicates() {
        List<Lead> leads = new ArrayList<>();
        leads.add(createLead("id1", "email1@test.com", "2014-05-07T17:30:20+00:00"));
        leads.add(createLead("id2", "email2@test.com", "2014-05-07T17:31:20+00:00"));

        DuplicateDetector detector = new DuplicateDetector();
        ChangeLog changeLog = new ChangeLog();
        List<Lead> uniqueLeads = detector.process(leads, changeLog);

        assertEquals(2, uniqueLeads.size(), "Should keep both unique leads");
        assertEquals(0, changeLog.getChangeCount(), "Should have no changes logged");
    }

    @Test
    public void testDuplicateId_NewerWins() {
        List<Lead> leads = new ArrayList<>();
        leads.add(createLead("id1", "email1@test.com", "2014-05-07T17:30:20+00:00"));
        leads.add(createLead("id1", "email2@test.com", "2014-05-07T17:31:20+00:00"));

        DuplicateDetector detector = new DuplicateDetector();
        ChangeLog changeLog = new ChangeLog();
        List<Lead> uniqueLeads = detector.process(leads, changeLog);

        assertEquals(1, uniqueLeads.size(), "Should have 1 unique lead");
        assertEquals(1, changeLog.getChangeCount(), "Should log 1 change");

        Lead kept = findLeadById(uniqueLeads, "id1");
        assertNotNull(kept);
        assertEquals("email2@test.com", kept.getEmail(), "Newer lead should be kept");
    }

    @Test
    public void testDuplicateId_OlderRejected() {
        List<Lead> leads = new ArrayList<>();
        leads.add(createLead("id1", "email1@test.com", "2014-05-07T17:31:20+00:00"));
        leads.add(createLead("id1", "email2@test.com", "2014-05-07T17:30:20+00:00"));

        DuplicateDetector detector = new DuplicateDetector();
        ChangeLog changeLog = new ChangeLog();
        List<Lead> uniqueLeads = detector.process(leads, changeLog);

        assertEquals(1, uniqueLeads.size(), "Should have 1 unique lead");
        assertEquals(1, changeLog.getChangeCount(), "Should log 1 change");

        Lead kept = findLeadById(uniqueLeads, "id1");
        assertNotNull(kept);
        assertEquals("email1@test.com", kept.getEmail(), "Newer lead should be kept");
    }

    @Test
    public void testDuplicateEmail_NewerWins() {
        List<Lead> leads = new ArrayList<>();
        leads.add(createLead("id1", "email@test.com", "2014-05-07T17:30:20+00:00"));
        leads.add(createLead("id2", "email@test.com", "2014-05-07T17:31:20+00:00"));

        DuplicateDetector detector = new DuplicateDetector();
        ChangeLog changeLog = new ChangeLog();
        List<Lead> uniqueLeads = detector.process(leads, changeLog);

        assertEquals(1, uniqueLeads.size(), "Should have 1 unique lead");
        assertEquals(1, changeLog.getChangeCount(), "Should log 1 change");

        Lead kept = findLeadById(uniqueLeads, "id2");
        assertNotNull(kept, "Newer lead with id2 should be kept");
    }

    @Test
    public void testSameIdAndEmail_NewerWins() {
        List<Lead> leads = new ArrayList<>();
        Lead lead1 = createLead("id1", "email@test.com", "2014-05-07T17:30:20+00:00");
        lead1.setFirstName("John");
        leads.add(lead1);

        Lead lead2 = createLead("id1", "email@test.com", "2014-05-07T17:31:20+00:00");
        lead2.setFirstName("Jane");
        leads.add(lead2);

        DuplicateDetector detector = new DuplicateDetector();
        ChangeLog changeLog = new ChangeLog();
        List<Lead> uniqueLeads = detector.process(leads, changeLog);

        assertEquals(1, uniqueLeads.size(), "Should have 1 unique lead");
        assertEquals(1, changeLog.getChangeCount(), "Should log 1 change (both ID and email match)");

        Lead kept = uniqueLeads.get(0);
        assertEquals("Jane", kept.getFirstName(), "Newer lead should be kept");
    }

    @Test
    public void testSameDate_LastInListWins() {
        List<Lead> leads = new ArrayList<>();
        Lead lead1 = createLead("id1", "email@test.com", "2014-05-07T17:30:20+00:00");
        lead1.setFirstName("First");
        leads.add(lead1);

        Lead lead2 = createLead("id2", "email@test.com", "2014-05-07T17:30:20+00:00");
        lead2.setFirstName("Second");
        leads.add(lead2);

        DuplicateDetector detector = new DuplicateDetector();
        ChangeLog changeLog = new ChangeLog();
        List<Lead> uniqueLeads = detector.process(leads, changeLog);

        assertEquals(1, uniqueLeads.size(), "Should have 1 unique lead");
        assertEquals(1, changeLog.getChangeCount(), "Should log 1 change");

        Lead kept = uniqueLeads.get(0);
        assertEquals("Second", kept.getFirstName(), "Last lead in list should win when dates are equal");
        assertEquals("id2", kept.getId());
    }

    @Test
    public void testMultipleDuplicates() {
        List<Lead> leads = new ArrayList<>();
        leads.add(createLead("id1", "email1@test.com", "2014-05-07T17:30:20+00:00"));
        leads.add(createLead("id1", "email1@test.com", "2014-05-07T17:31:20+00:00"));
        leads.add(createLead("id2", "email2@test.com", "2014-05-07T17:30:20+00:00"));
        leads.add(createLead("id2", "email2@test.com", "2014-05-07T17:32:20+00:00"));

        DuplicateDetector detector = new DuplicateDetector();
        ChangeLog changeLog = new ChangeLog();
        List<Lead> uniqueLeads = detector.process(leads, changeLog);

        assertEquals(2, uniqueLeads.size(), "Should have 2 unique leads");
        assertEquals(2, changeLog.getChangeCount(), "Should log 2 changes");
    }

    @Test
    public void testComplexCase_IdFromOneEmailFromAnother() {
        List<Lead> leads = new ArrayList<>();
        leads.add(createLead("id1", "email1@test.com", "2014-05-07T17:30:20+00:00"));
        leads.add(createLead("id2", "email2@test.com", "2014-05-07T17:30:20+00:00"));
        
        leads.add(createLead("id1", "email2@test.com", "2014-05-07T17:31:20+00:00"));

        DuplicateDetector detector = new DuplicateDetector();
        ChangeLog changeLog = new ChangeLog();
        List<Lead> uniqueLeads = detector.process(leads, changeLog);

        assertEquals(1, uniqueLeads.size(), "Should have 1 unique lead (newest replaces both)");
        assertEquals(2, changeLog.getChangeCount(), "Should log 2 changes (ID conflict + email conflict)");

        Lead kept = uniqueLeads.get(0);
        assertEquals("id1", kept.getId());
        assertEquals("email2@test.com", kept.getEmail());
    }

    @Test
    public void testThreeDuplicatesOfSameId() {
        List<Lead> leads = new ArrayList<>();
        leads.add(createLead("id1", "email1@test.com", "2014-05-07T17:30:20+00:00"));
        leads.add(createLead("id1", "email2@test.com", "2014-05-07T17:31:20+00:00"));
        leads.add(createLead("id1", "email3@test.com", "2014-05-07T17:32:20+00:00"));

        DuplicateDetector detector = new DuplicateDetector();
        ChangeLog changeLog = new ChangeLog();
        List<Lead> uniqueLeads = detector.process(leads, changeLog);

        assertEquals(1, uniqueLeads.size(), "Should have 1 unique lead");
        assertEquals(2, changeLog.getChangeCount(), "Should log 2 changes");

        Lead kept = uniqueLeads.get(0);
        assertEquals("email3@test.com", kept.getEmail(), "Newest should be kept");
    }

    @Test
    public void testRealWorldScenario_FromLeadsJson() {
        List<Lead> leads = new ArrayList<>();
        
        leads.add(createLead("jkj238238jdsnfsj23", "foo@bar.com", "2014-05-07T17:30:20+00:00"));
        leads.add(createLead("jkj238238jdsnfsj23", "coo@bar.com", "2014-05-07T17:32:20+00:00"));
        leads.add(createLead("jkj238238jdsnfsj23", "bill@bar.com", "2014-05-07T17:33:20+00:00"));

        DuplicateDetector detector = new DuplicateDetector();
        ChangeLog changeLog = new ChangeLog();
        List<Lead> uniqueLeads = detector.process(leads, changeLog);

        assertEquals(1, uniqueLeads.size(), "Should deduplicate to 1 lead");
        assertEquals(2, changeLog.getChangeCount(), "Should log 2 changes");

        Lead kept = uniqueLeads.get(0);
        assertEquals("jkj238238jdsnfsj23", kept.getId());
        assertEquals("bill@bar.com", kept.getEmail(), "Newest should win");
    }

    private Lead createLead(String id, String email, String date) {
        Lead lead = new Lead();
        lead.setId(id);
        lead.setEmail(email);
        lead.setFirstName("John");
        lead.setLastName("Doe");
        lead.setAddress("123 Main St");
        lead.setEntryDate(date);
        return lead;
    }

    private Lead findLeadById(List<Lead> leads, String id) {
        return leads.stream()
                .filter(lead -> lead.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}