package com.deduplicator.service;

import com.deduplicator.logging.ChangeLog;
import com.deduplicator.model.Lead;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DuplicateDetector {

    public List<Lead> process(List<Lead> leads, ChangeLog changeLog) {

        //Determine winner for each ID
        Map<String, Lead> idWinners = new HashMap<>();
        //Determine winner for each email
        Map<String, Lead> emailWinners = new HashMap<>();

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

            String id = lead.getId();
            String email = lead.getEmail();

            if (!idWinners.containsKey(id)) {
                idWinners.put(id, lead);
            } else {
                Lead current = idWinners.get(id);
                if (isBetter(lead, current)) {
                    //New lead wins. Log the loser and replace
                    boolean sameEmail = lead.getEmail().equals(current.getEmail());
                    String reason = sameEmail 
                        ? "Duplicate ID '" + id + "' and email '" + lead.getEmail() + "'"
                        : "Duplicate ID '" + id + "'";
                    changeLog.recordChange(current, lead, reason);
                    idWinners.put(id, lead);
                } else {
                    //Current winner stays. Log the new entry as loser
                    boolean sameEmail = lead.getEmail().equals(current.getEmail());
                    String reason = sameEmail 
                        ? "Duplicate ID '" + id + "' and email '" + lead.getEmail() + "'"
                        : "Duplicate ID '" + id + "'";
                    changeLog.recordChange(lead, current, reason);
                }
            }

            if (!emailWinners.containsKey(email)) {
                emailWinners.put(email, lead);
            } else {
                Lead current = emailWinners.get(email);
                if (isBetter(lead, current)) {
                    //New entry wins. Only log if they don't share the same ID (ID winner processing already logged that. Avoid duplicate logging)
                    boolean sameId = lead.getId().equals(current.getId());
                    if (!sameId) {
                        changeLog.recordChange(current, lead, "Duplicate email '" + email + "'");
                    }
                    emailWinners.put(email, lead);
                } else {
                    //Current winner stays
                    boolean sameId = lead.getId().equals(current.getId());
                    if (!sameId) {
                        changeLog.recordChange(lead, current, "Duplicate email '" + email + "'");
                    }
                }
            }
        }

        List<Lead> result = new ArrayList<>();

        if (idWinners.size() <= emailWinners.size()) {
            for (Lead lead : idWinners.values()) {
                //Check intersection
                Lead emailWinner = emailWinners.get(lead.getEmail());
                if (emailWinner == lead) {
                    result.add(lead);
                }
            }
        } else {
            for (Lead lead : emailWinners.values()) {
                //Check intersection
                Lead idWinner = idWinners.get(lead.getId());
                if (idWinner == lead) {
                    result.add(lead);
                }
            }
        }

        return result;
    }
    
    private boolean isBetter(Lead candidate, Lead current) {
        int dateCompare = candidate.getParsedDate().compareTo(current.getParsedDate());
        
        if (dateCompare >= 0) {//Newer or same date
            return true; //Candidate wins since it is a newer date or later in the list in case of tie
        }
               
        return false;
    }
}