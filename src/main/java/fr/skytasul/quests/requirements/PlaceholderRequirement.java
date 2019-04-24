package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.utils.MissingDependencyException;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderHook;

public class PlaceholderRequirement extends AbstractRequirement {

	private String rawPlaceholder;
	
	private PlaceholderHook hook;
	private String params;
	private String value;
	
	public PlaceholderRequirement(){
		super("placeholderRequired");
		if (!Dependencies.papi) throw new MissingDependencyException("PlaceholderAPI");
	}

	public boolean test(Player p){
		return hook.onRequest(p, params).equals(value);
	}

	/*public void sendReason(Player p){
		p.sendMessage(rawPlaceholder + " | " + params + " | " + hook.onRequest(p, params));
	}*/
	
	public void setPlaceholder(String placeholder){
		this.rawPlaceholder = placeholder;
		int index = placeholder.indexOf("_");
		hook = PlaceholderAPI.getPlaceholders().get(placeholder.substring(0, index).toLowerCase());
		params = placeholder.substring(index + 1);
	}
	
	public void setValue(String value){
		this.value = value;
	}
	
	public String getPlaceholder(){
		return rawPlaceholder;
	}
	
	public String getValue(){
		return value;
	}
	
	protected void save(Map<String, Object> datas){
		datas.put("placeholder", rawPlaceholder);
		datas.put("value", value);
	}

	protected void load(Map<String, Object> savedDatas){
		setPlaceholder((String) savedDatas.get("placeholder"));
		this.value = (String) savedDatas.get("value");
	}

}
