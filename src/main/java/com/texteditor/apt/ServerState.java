package com.texteditor.apt;

import java.util.LinkedList;

import com.texteditor.apt.CRDT.Block_CRDT;
import com.texteditor.apt.CRDT.Block_Node;
import com.texteditor.apt.CRDT.Char_ID;
import com.texteditor.apt.CRDT.Char_Node;
import com.texteditor.apt.CRDT.Identifier;
public class ServerState {
    LinkedList<ActiveDocument> alldocs= new LinkedList<>();

    public Block_CRDT FindaDoc(String docid){
        for(int i=0;i<alldocs.size();i++){
            ActiveDocument document=alldocs.get(i);
            if(document.docID. equals(docid))
                return document.document;
        }
        return null;
    }

    public String getblockText(String blockID, String docID){
        Block_CRDT doc= FindaDoc(docID);
        
        if(doc ==null)
            return " ";
        String text= doc.getBlockText(blockID);
        return text;
    }

    public void ApplyOperation(String docID, NetworkMessage msg){
        Block_CRDT doc= FindaDoc(docID);
        if(doc == null){
            ActiveDocument docnew= new ActiveDocument(docID, new Block_CRDT());
            alldocs.add(docnew);
            doc= FindaDoc(docID);
        }


        if(msg.opType. equals("BLOCK_INSERT"))
            doc.insertBlock(msg.blockID, msg.parentBlockID);
        else if(msg.opType. equals("BLOCK_DELETE"))
            doc.deleteBlock(msg.blockID);
        else if(msg.opType. equals("CHAR_INSERT")){
            Identifier[] ids = { new Identifier(msg.position, msg.userID) };
            Char_ID charid = new Char_ID(ids);
            Char_Node node= new Char_Node(charid, msg.character);
            doc.insertCharacter(msg.blockID,node);
        }
        else if(msg.opType. equals("CHAR_DELETE")){
             Identifier[] ids = { new Identifier(msg.position, msg.userID) };
            Char_ID charid = new Char_ID(ids);
            Block_Node block= doc.getSpecificBlock(msg.blockID);
            if(block !=null)
                block.getContent().delete(charid);
        }
        
    }
}

 