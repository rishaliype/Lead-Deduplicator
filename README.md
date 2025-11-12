# Lead Deduplicator - Optimized Intersection Approach

**Note:** This is an optimized implementation developed during panel interview preparation. The [original submission](https://github.com/rishaliype/Lead-Deduplicator/tree/main) uses a grouping-based approach. 

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

## Deduplication Rules

1. **Newest date wins** - Records with more recent dates are kept
2. **Unique ID and email** - Both ID and email must be unique in the output
3. **Last in list wins** - When dates are identical, the record appearing last in the input is kept

## Algorithm: Intersection Approach

### Key Optimization

This implementation uses an **intersection-based approach** for improved space efficiency compared to the grouping approach.

### Data Structures

- `Map<String, Lead>` for `idWinners` - maps each unique ID to its current retained lead
- `Map<String, Lead>` for `emailWinners` - maps each unique email to its current retained lead

### Steps

1. Iterate through leads in order:
   - For each lead's ID: if no entry in `idWinners`, add this lead; if entry exists, compare dates (later index breaks ties) and keep newer
   - For each lead's email: if no entry in `emailWinners`, add this lead; if entry exists, compare dates (later index breaks ties) and keep newer
   - Log changes when retained leads are replaced
2. Find intersection: iterate through smaller of the two and include only leads that appear in both `idWinners` and `emailWinners`

### Example Walkthrough

Given:
```
Lead A: id=1, email=a@test.com, date=Jan 1
Lead B: id=1, email=b@test.com, date=Jan 2  
Lead C: id=2, email=a@test.com, date=Jan 3
```

**Processing:**
1. Process Lead A:
   - `idWinners[1] = A`
   - `emailWinners[a@test.com] = A`

2. Process Lead B:
   - ID conflict: B newer than A → `idWinners[1] = B`, log change
   - `emailWinners[b@test.com] = B`

3. Process Lead C:
   - `idWinners[2] = C`
   - Email conflict: C newer than A → `emailWinners[a@test.com] = C`, log change

4. Find intersection:
   - Lead A: in neither map (was replaced)
   - Lead B: in both `idWinners[1]` and `emailWinners[b@test.com]` ✓
   - Lead C: in both `idWinners[2]` and `emailWinners[a@test.com]` ✓

**Result:** [B, C]

### Complexity

- **Time:** O(n) where n = number of leads
- **Space:** O(u) where u = number of unique IDs + unique emails - only u leads stored (one per unique key)

### Advantages

- ✓ **Lower memory usage** - stores only currently retained leads (one lead per unique ID/email)
- ✓ **Single pass processing** - builds both maps in one iteration

### Trade-off

- − **Does not maintain input order** - output order depends on map iteration (typically insertion order but not guaranteed to match input)

## Testing
```bash
mvn test
```

All tests from the main branch pass with identical results.

## Output Files

The application generates two files:

1. **Deduplicated leads JSON** - Contains the retained records (order not preserved from input)
2. **Change log JSON** - A detailed audit trail with timestamp, reason, source record, and output record for each deduplication decision