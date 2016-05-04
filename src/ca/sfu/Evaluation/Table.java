package ca.sfu.Evaluation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Table {
    private String name;
    public enum KeyType{PRIMARY,FOREIGH,NONE}
    private ArrayList<String> column_array;
    private HashMap<String, KeyType> column_hash;


    public Table(String name) {
        column_array = new ArrayList<String>();
        column_hash = new HashMap<String, KeyType>();
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addKey(String name, KeyType type){
        column_array.add(name);
        column_hash.put(name, type);
    }

   
    public ArrayList<String> getPrimaryKeys(){
        ArrayList<String> primaryKeys = new ArrayList<String>();
        for(Iterator<String> i=column_array.iterator();i.hasNext();){
            String keyName=i.next();
            KeyType nextType=column_hash.get(keyName);
            if(nextType==KeyType.PRIMARY)
                primaryKeys.add(keyName);
        }
        return primaryKeys;
    }
   
    public ArrayList<String> getForeignKeys(){
        ArrayList<String> foreignKeys = new ArrayList<String>();
        for(Iterator<String> i=column_array.iterator();i.hasNext();){
            String keyName=i.next();
            KeyType nextType=column_hash.get(keyName);
            if(nextType==KeyType.FOREIGH)
                foreignKeys.add(keyName);
        }
        return foreignKeys;
    }
   
    public ArrayList<String> getOtherKeys(){
        ArrayList<String> otherKeys = new ArrayList<String>();
        for(Iterator<String> i=column_array.iterator();i.hasNext();){
            String keyName=i.next();
            KeyType nextType=column_hash.get(keyName);
            if(nextType==KeyType.NONE)
                otherKeys.add(keyName);
        }
        return otherKeys;
    }
   
}