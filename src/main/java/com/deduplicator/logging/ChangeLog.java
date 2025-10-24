package com.deduplicator.logging;

import com.deduplicator.model.Lead;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChangeLog {
    private List<ChangeEntry> changes = new ArrayList<>();
    
    public void recordChange(Lead source, Lead output, String reason) {
        changes.add(new ChangeEntry(source, output, reason));
    }
    
    public void saveToJson(String filename) throws IOException {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT);
        
        Map<String, List<ChangeEntry>> wrapper = Map.of("changes", changes);
        mapper.writeValue(new File(filename), wrapper);
    }
    
    public int getChangeCount() {
        return changes.size();
    }
}