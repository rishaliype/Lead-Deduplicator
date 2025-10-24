package com.deduplicator.service;

import com.deduplicator.logging.ChangeLog;
import com.deduplicator.model.Lead;
import com.deduplicator.model.LeadEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DuplicateDetector {

    public List<Lead> process(List<Lead> leads, ChangeLog changeLog) {

        List<LeadEntry> entries = new ArrayList<>();
        for (int i = 0; i < leads.size(); i++) {
            Lead lead = leads.get(i);
            //Validation for id, email and date
            if (lead.getId() == null || lead.getId().trim().isEmpty()) {
                throw new IllegalArgumentException("Lead at index " + i + " has null or empty ID");
            }
            if (lead.getEmail() == null || lead.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Lead at index " + i + " has null or empty email");
            }
            if (lead.getParsedDate() == null) {
                throw new IllegalArgumentException("Lead at index " + i + " has null or unparseable date");
            }
            entries.add(new LeadEntry(leads.get(i), i));
        }
        
        //Group duplicates together
        Map<String, List<LeadEntry>> groupById = new HashMap<>();
        Map<String, List<LeadEntry>> groupByEmail = new HashMap<>();
        
        for (LeadEntry entry : entries) {
            String id = entry.lead.getId();
            String email = entry.lead.getEmail();
            
            if (!groupById.containsKey(id)) {
                groupById.put(id, new ArrayList<>());
            }
            groupById.get(id).add(entry);
            
            if (!groupByEmail.containsKey(email)) {
                groupByEmail.put(email, new ArrayList<>());
            }
            groupByEmail.get(email).add(entry);
        }
        
        //Find losers
        Set<LeadEntry> losers = new HashSet<>();
        
        //Pick winner for each ID group with duplicates
        for (List<LeadEntry> group : groupById.values()) {
            if (group.size() > 1) {
                LeadEntry winner = pickWinner(group);
                for (LeadEntry loser : group) {
                    if (loser != winner) {
                        losers.add(loser);
                        // Check if they also share same email for logging
                        boolean sameEmail = loser.lead.getEmail().equals(winner.lead.getEmail());
                        String reason = sameEmail 
                            ? "Duplicate ID '" + loser.lead.getId() + "' and email '" + loser.lead.getEmail() + "'"
                            : "Duplicate ID '" + loser.lead.getId() + "'";
                        
                        changeLog.recordChange(loser.lead, winner.lead, reason);
                    }
                }
            }
        }
        
        //Pick winner for each email group with duplicates
        for (List<LeadEntry> group : groupByEmail.values()) {
            if (group.size() > 1) {
                LeadEntry winner = pickWinner(group);
                for (LeadEntry loser : group) {
                    if (loser != winner) {
                        losers.add(loser);
                        //Only log if they don't share the same ID (otherwise already logged above)
                        boolean sameId = loser.lead.getId().equals(winner.lead.getId());
                        if (!sameId) {
                            changeLog.recordChange(loser.lead, winner.lead, 
                                "Duplicate email '" + loser.lead.getEmail() + "'");
                        }
                    }
                }
            }
        }
        
        List<Lead> result = new ArrayList<>();
        for (LeadEntry entry : entries) {
            if (!losers.contains(entry)) {
                result.add(entry.lead);
            }
        }
        
        return result;
    }
    
    private LeadEntry pickWinner(List<LeadEntry> candidates) {
        LeadEntry winner = candidates.get(0);
        
        for (LeadEntry candidate : candidates) {
            if (isBetter(candidate, winner)) {
                winner = candidate;
            }
        }
        
        return winner;
    }
    
    private boolean isBetter(LeadEntry candidate, LeadEntry current) {
        int dateCompare = candidate.lead.getParsedDate().compareTo(current.lead.getParsedDate());
        
        if (dateCompare > 0) {
            return true; //Newer date wins
        }
        
        if (dateCompare == 0 && candidate.index > current.index) {
            return true; //Same date so later in list wins
        }
        
        return false;
    }
}