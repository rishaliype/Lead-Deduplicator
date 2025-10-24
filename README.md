# Lead Deduplicator

A Java application that removes duplicate leads from a JSON file based on ID and email uniqueness.

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Building

```bash
mvn clean package
```

## Running

```bash
java -jar target/lead-deduplicator.jar leads.json [output.json] [changes.log]
```

**Arguments:**
- `leads.json` - Input file (required)
- `output.json` - Output file (optional, defaults to `deduplicated_leads.json`)
- `changes.log` - Change log file (optional, defaults to `changes.log`)

## Output Files

The application generates two files:

1. **Deduplicated leads JSON** - Contains only the winning records after deduplication
2. **Change log JSON** - A detailed audit trail of all deduplication decisions:
   - Timestamp to keep track of when change was processed
   - Reason for deduplication i.e. duplicate ID, email or both
   - Source record (the record that was removed)
   - Output record (the winning record that was kept)
   - This provides visibility into what changed between the source and output for each duplicate

## Deduplication Rules

1. **Newest date wins** - Records with more recent dates are kept
2. **Unique ID and email** - Both ID and email must be unique in the output
3. **Last in list wins** - When dates are identical, the record appearing last in the input is kept

## Testing

```bash
mvn test
```