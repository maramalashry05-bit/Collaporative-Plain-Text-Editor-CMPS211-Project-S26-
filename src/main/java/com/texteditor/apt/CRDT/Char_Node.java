package com.texteditor.apt.CRDT;

public class Char_Node {
    private Char_ID id;
    private char value;
    
    // Project Requirements: Tombstones and Formatting
    private boolean isDeleted;
    private boolean isBold;
    private boolean isItalic;

    public Char_Node(Char_ID id, char value) {
        this.id = id;
        this.value = value;
        this.isDeleted = false; // Starts as false
        this.isBold = false;
        this.isItalic = false;
    }

    // Getters and Setters...
    public Char_ID getId() { return id; }
    public char getValue() { return value; }

    
    public void markAsBold() { this.isBold = true; }
    public boolean isBold() { return isBold; }
    
    public void markAsItalic() { this.isItalic = true; }
    public boolean isItalic() { return isItalic; }
    
     public void markAsDeleted() { this.isDeleted = true; }
    public boolean isDeleted() { return isDeleted; }


    //for serialization
    public int getPosition() { 
    return id.getPosition()[0].getDigit(); 
    }

    public String getUserID() { 
        return id.getPosition()[0].getSiteId(); 
    }
    }