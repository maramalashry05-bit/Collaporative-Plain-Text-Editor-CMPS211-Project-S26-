package com.texteditor.apt.CRDT;

public class Identifier implements Comparable<Identifier> {
    private int digit;      // The positional number (e.g., 1, 5, 10)
    private String siteId;  // The User ID (e.g., "UserA")

    public Identifier(int digit, String siteId) {
        this.digit = digit;
        this.siteId = siteId;
    }

    public int getDigit() { return digit; }
    public String getSiteId() { return siteId; }

    // This is crucial for sorting characters correctly!
    @Override
    public int compareTo(Identifier other) {
        if (this.digit != other.digit) {
            return Integer.compare(this.digit, other.digit);
        }
        // If digits are the same, break the tie using the Site ID
        return this.siteId.compareTo(other.siteId);
    }
}