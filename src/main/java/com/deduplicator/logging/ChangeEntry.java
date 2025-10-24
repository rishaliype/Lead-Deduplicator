package com.deduplicator.logging;

import com.deduplicator.model.Lead;
import java.time.Instant;

public class ChangeEntry {
    private String timestamp;
    private String reason;
    private Lead sourceRecord;
    private Lead outputRecord; 
    
    public ChangeEntry(Lead source, Lead output, String reason) {
        this.timestamp = Instant.now().toString();
        this.reason = reason;
        this.sourceRecord = source;
        this.outputRecord = output;
    }
    
    //Getters
    public String getTimestamp() {
        return timestamp;
    }

    public String getReason() {
        return reason;
    }

    public Lead getSourceRecord() {
        return sourceRecord;
    }

    public Lead getOutputRecord() {
        return outputRecord;
    }
}