package com.texteditor.apt;

public class Char_ID implements Comparable<Char_ID> {
    private final Identifier[] position;

    public Char_ID(Identifier[] position) {
        this.position = position;
    }

    public Identifier[] getPosition() { 
        return position; 
    }

    // Compares two arrays of identifiers to figure out which character comes first
    @Override
    public int compareTo(Char_ID other) {
        // Find the length of the shorter array to avoid ArrayIndexOutOfBoundsException
        int minLength = Math.min(this.position.length, other.position.length);
        
        for (int i = 0; i < minLength; i++) {
            int comp = this.position[i].compareTo(other.position[i]);
            if (comp != 0) {
                return comp; // We found a difference at this depth level
            }
        }
        
        // If all matching levels are equal, the shorter array comes first
        // Example: [1, 5] comes before [1, 5, 2]
        return Integer.compare(this.position.length, other.position.length);
    }
}