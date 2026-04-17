package com.texteditor.apt;

import com.texteditor.apt.CRDT.Block_CRDT;

public class ActiveDocument{
    
    String docID;
    Block_CRDT document;
    
    public ActiveDocument(String docid, Block_CRDT doc){
        this.docID=docid;
        this.document=doc;
    }

}