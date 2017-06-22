package ca.sfu.jbn.SQLtoXML;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class TableXML {
	private String name;
	public enum KeyType{PRIMARY,FOREIGH,NONE}
	private HashMap<String, KeyType> column;


	public TableXML(String name) {
		column = new HashMap<String, KeyType>();
		this.name=name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addKey(String name, KeyType type){
		column.put(name, type);
	}

	
	public ArrayList<String> getPrimaryKeys(){
		ArrayList<String> primaryKeys = new ArrayList<String>();
		for(Iterator<String> i=column.keySet().iterator();i.hasNext();){
			String keyName=i.next();
			KeyType nextType=column.get(keyName);
			if(nextType==KeyType.PRIMARY)
				primaryKeys.add(keyName);
		}
		return primaryKeys;
	}
	
	public ArrayList<String> getForeignKeys(){
		ArrayList<String> foreignKeys = new ArrayList<String>();
		for(Iterator<String> i=column.keySet().iterator();i.hasNext();){
			String keyName=i.next();
			KeyType nextType=column.get(keyName);
			if(nextType==KeyType.FOREIGH)
				foreignKeys.add(keyName);
		}
		return foreignKeys;
	}
	
	public ArrayList<String> getOtherKeys(){
		ArrayList<String> otherKeys = new ArrayList<String>();
		for(Iterator<String> i=column.keySet().iterator();i.hasNext();){
			String keyName=i.next();
			KeyType nextType=column.get(keyName);
			if(nextType==KeyType.NONE)
				otherKeys.add(keyName);
		}
		return otherKeys;
	}
	
}
