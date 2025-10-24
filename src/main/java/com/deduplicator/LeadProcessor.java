package com.deduplicator;

import com.deduplicator.logging.ChangeLog;
import com.deduplicator.model.Lead;
import com.deduplicator.service.DuplicateDetector;
import com.deduplicator.util.JsonUtil;
import java.io.IOException;
import java.util.List;


public class LeadProcessor {
    
    public static void main(String[] args) {
        try {
            String inputFile = args[0];
            String outputFile = args.length > 1 ? args[1] : "deduplicated_leads.json";
            String logFile = args.length > 2 ? args[2] : "changes.log";

            System.out.println();

            //Read leads from JSON file
            System.out.println("Reading leads from: " + inputFile);
            List<Lead> leads = JsonUtil.readLeadsFromFile(inputFile);
            System.out.println("Total leads read: " + leads.size());
            System.out.println();

            DuplicateDetector detector = new DuplicateDetector();
            ChangeLog changeLog = new ChangeLog();

            //Process leads and deduplicate
            System.out.println("Deduplicating leads...");
            List<Lead> uniqueLeads = detector.process(leads, changeLog);
            System.out.println();

            //Save outputs
            System.out.println("Writing deduplicated leads to: " + outputFile);
            JsonUtil.writeLeadsToFile(uniqueLeads, outputFile);

            System.out.println("Writing change log to: " + logFile);
            changeLog.saveToJson(logFile);

            System.out.println();

            System.out.println("SUMMARY");
            System.out.println("Total leads processed: " + leads.size());
            System.out.println("Unique leads: " + uniqueLeads.size());
            System.out.println("Duplicates removed: " + (leads.size() - uniqueLeads.size()));
            System.out.println();
            System.out.println("Deduplication complete!");
            System.out.println();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
