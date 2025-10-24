package com.deduplicator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.Instant;

@JsonPropertyOrder({"_id", "email", "firstName", "lastName", "address", "entryDate"})
public class Lead {
    
    @JsonProperty("_id")
    private String _id;
    
    private String email;
    private String firstName;
    private String lastName;
    private String address;
    private String entryDate;

    public Lead() {}

    // Getters
    @JsonIgnore
    public String getId() {
        return _id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAddress() {
        return address;
    }

    public String getEntryDate() {
        return entryDate;
    }
    @JsonIgnore
    public Instant getParsedDate() {
        return Instant.parse(entryDate);
    }

    // Setters
    public void setId(String _id) {
        this._id = _id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }
}
