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
## Example Test Cases

The algorithm was designed to handle cases like:

**Case 1: ID conflict with different emails**
```
Input:  [id=1, email=a@test.com, date=Jan 1]
        [id=1, email=b@test.com, date=Jan 2]
Output: [id=1, email=b@test.com, date=Jan 2]
```

**Case 2: Email conflict with different IDs**
```
Input:  [id=1, email=a@test.com, date=Jan 1]
        [id=2, email=a@test.com, date=Jan 2]
Output: [id=2, email=a@test.com, date=Jan 2]
```

**Case 3: Complex conflict (intersection required)**
```
Input:  [id=1, email=a@test.com, date=Jan 1]
        [id=1, email=b@test.com, date=Jan 2]  
        [id=2, email=a@test.com, date=Jan 3]
Output: [id=1, email=b@test.com, date=Jan 2]
        [id=2, email=a@test.com, date=Jan 3]
```
*Lead 1 is excluded: email conflicts with lead 3, ID conflicts with lead 2*

## Algorithm Explanation

### Current Implementation: Grouping Approach

**Data Structures:**
- `Map<String, List<LeadEntry>>` for groupById - maps each unique ID to list of leads with that ID
- `Map<String, List<LeadEntry>>` for groupByEmail - maps each unique email to list of leads with that email
- `Set<LeadEntry>` for tracking excluded leads

**Steps:**
1. Wrap each lead with its index to track position in input
2. Iterate through leads to build groupById and groupByEmail maps
3. For each ID that has multiple leads (duplicates):
   - Iterate through the group to find the lead to keep (newest date, or highest index if dates equal)
   - Mark all others as excluded and log changes
4. For each email that has multiple leads (duplicates):
   - Iterate through the group to find the lead to keep (newest date, or highest index if dates equal)
   - Mark all others as excluded and log changes
5. Build result by iterating through original lead order, excluding marked leads

**Complexity:** 
- Time: O(n)
- Space: O(n) - while there are u unique keys, all n leads are stored across the lists as values

**Characteristics:**
- ✓ Maintains input order in output
- ✓ Explicit grouping makes duplicate handling clear
- − Stores all leads in memory

---

### Optimized Implementation: Intersection Approach

Available in the [`optimized-intersection-approach`](https://github.com/rishaliype/Lead-Deduplicator/tree/optimized-intersection-approach) branch.

**Data Structures:**
- `Map<String, Lead>` for ID retention - maps each unique ID to its current retained lead
- `Map<String, Lead>` for email retention - maps each unique email to its current retained lead

**Steps:**
1. Iterate through leads in order:
   - For each lead's ID: if no retained lead exists, set this lead; if retained lead exists, compare dates (later index breaks ties) and keep newer
   - For each lead's email: if no retained lead exists, set this lead; if retained lead exists, compare dates (later index breaks ties) and keep newer
   - Log changes when retained leads are replaced
2. Find intersection: iterate through smaller retention map and include only leads that are retained for both their ID and email

**Complexity:**
- Time: O(n)
- Space: O(u) where u = number of unique IDs + unique emails - only u leads stored (one per unique key)

**Characteristics:**
- ✓ Lower memory usage - stores only currently retained leads (one lead per unique ID/email)
- ✓ Single pass processing
- − Does not maintain input order in output


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
