package com.deduplicator.model;

import com.deduplicator.model.Lead;

public class LeadEntry {
    final public Lead lead;
    final public int index;

    public LeadEntry(Lead lead, int index) {
        this.lead = lead;
        this.index = index;
    }
}