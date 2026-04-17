package com.texteditor.apt.CRDT;

public class CRDT_ID_Generator {

    // A predefined gap size to use when we run out of numbers.
    // Think of this like base-10 decimals.
    private final int DEFAULT_MAX = 10; //must be 100 omnia 

    // Helper method to safely get a digit from the array, or a default if we are out of bounds
    private int getDigit(Char_ID id, int depth, int defaultDigit) {
        if (id != null && depth < id.getPosition().length) {
            return id.getPosition()[depth].getDigit();
        }
        return defaultDigit;
    }

    public Char_ID generateIdBetween(Char_ID prev, Char_ID next, String siteId) {
        // Since we cannot use ArrayList, we create a temporary array large enough 
        // to hold any reasonable depth. 100 levels deep is more than enough for a document.
        Identifier[] tempPath = new Identifier[100];
        int currentDepth = 0;

        while (true) {
            // Get the digits at the current depth. 
            // If prev runs out, we treat it as 0. If next runs out, we treat it as DEFAULT_MAX (10).
            int prevDigit = getDigit(prev, currentDepth, 0);
            int nextDigit = getDigit(next, currentDepth, DEFAULT_MAX);

            // Calculate how much space is between the two digits
            int difference = nextDigit - prevDigit;

            if (difference > 1) {
                // There is room! We can pick a number right in the middle (or just add 1).
                int newDigit = prevDigit + 1;
                tempPath[currentDepth] = new Identifier(newDigit, siteId);
                currentDepth++;
                
                // We found a unique ID, so we break out of the loop.
                break; 
            } else {
                // There is NO room at this level (e.g., prev is 5, next is 6).
                // We must copy the prevDigit and go one level deeper.
                tempPath[currentDepth] = new Identifier(prevDigit, siteId);
                currentDepth++;
            }
        }

        // Now we copy our generated path from the temporary array into an 
        // exactly-sized array so we don't have empty null slots.
        Identifier[] finalPath = new Identifier[currentDepth];
        for (int i = 0; i < currentDepth; i++) {
            finalPath[i] = tempPath[i];
        }

        return new Char_ID(finalPath);
    }
}