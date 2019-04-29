package fr.skytasul.quests.scoreboards;

import java.util.Map;

import org.apache.commons.lang.Validate;

public class ScoreboardLine {

	String value;
	int refresh = 0;
	int length = 0;
	
	public ScoreboardLine(String value){
		Validate.notNull(value);
		this.value = value.replaceAll("&", "§");
	}
	
	public String getValue(){
		return value;
	}
	
	public int getRefreshTime(){
		return refresh;
	}
	
	public int getMaxLength(){
		return length;
	}
	
	public static ScoreboardLine deserialize(Map<String, Object> map){
		ScoreboardLine line = new ScoreboardLine((String) map.get("value"));
		
		if (map.containsKey("refresh")) line.refresh = (int) map.get("refresh");
		if (map.containsKey("length")) line.length = (int) map.get("length");
		
		return line;
	}
	
}
