package fr.skytasul.quests.gui.creation.stages;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class LineData {

	private Map<String, Object> datas = new HashMap<String, Object>();
	private Line line;
	private StagesGUI gui;
	
	public LineData(Line line, StagesGUI gui){
		this.line = line;
		this.gui = gui;
	}

	public Line getLine(){
		return line;
	}
	
	public StagesGUI getGUI(){
		return gui;
	}
	
	public void put(String key, Object value){
		datas.put(key, value);
	}
	
	public void remove(String key){
		datas.remove(key);
	}
	
	public Object get(String key){
		return datas.get(key);
	}
	
	public void clear(){
		datas.clear();
	}
	
	public boolean containsKey(String key){
		return datas.containsKey(key);
	}
	
	public Set<Entry<String, Object>> entrySet(){
		return datas.entrySet();
	}
	
}
