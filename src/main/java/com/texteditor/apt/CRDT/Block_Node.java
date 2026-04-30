    package com.texteditor.apt.CRDT;

    public class Block_Node {
        private String blockID;
        private String parentBlockID;
        private boolean isDeleted;
     private boolean isBold;
     private boolean isItalic;
    
        private Character_Tree content;


        public Block_Node(String blockID,String parentBlockID){
            this.blockID=blockID;
            this.parentBlockID=parentBlockID;
            this.isDeleted=false;
            this.content=new Character_Tree();
        }

        public String getBlockID(){
            return blockID;
        }
        public String getParentBlockID(){
            return parentBlockID;
        }
           public void markAsBold() { this.isBold = true; }
            public boolean isBold() { return isBold; }
    
    public void markAsItalic() { this.isItalic = true; }
    public boolean isItalic() { return isItalic; }

        public void markAsDeleted() { 
            this.isDeleted = true; 
        }
        public boolean isDeleted() { 
            return isDeleted; 
        }
        public Character_Tree getContent() {
        return content;
    }
    }
