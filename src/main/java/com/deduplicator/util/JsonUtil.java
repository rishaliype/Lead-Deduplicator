package com.deduplicator.util;

import com.deduplicator.model.Lead;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static List<Lead> readLeadsFromFile(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            throw new IOException("File not found: " + filename);
        }

        LeadsWrapper wrapper = mapper.readValue(file, LeadsWrapper.class);
        
        if (wrapper.getLeads() == null) {
            throw new IOException("'leads' array not found in JSON file: " + filename);
        }
        
        return wrapper.getLeads();
    }

    public static void writeLeadsToFile(List<Lead> leads, String filename) throws IOException {
        LeadsWrapper wrapper = new LeadsWrapper(leads);
        mapper.writeValue(new File(filename), wrapper);
    }

    private static class LeadsWrapper {
        private List<Lead> leads;

        public LeadsWrapper() {}

        public LeadsWrapper(List<Lead> leads) {
            this.leads = leads;
        }

        public List<Lead> getLeads() {
            return leads;
        }

        public void setLeads(List<Lead> leads) {
            this.leads = leads;
        }
    }
}