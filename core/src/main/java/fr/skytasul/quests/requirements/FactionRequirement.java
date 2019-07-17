package fr.skytasul.quests.requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import com.massivecraft.factions.FactionsIndex;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.utils.MissingDependencyException;
import fr.skytasul.quests.utils.compatibility.Dependencies;

public class FactionRequirement extends AbstractRequirement {

	public List<Faction> factions = new ArrayList<>();
	
	public FactionRequirement() {
		super("factionRequired");
		if (!Dependencies.fac) throw new MissingDependencyException("Factions");
	}

	public List<String> getFactionsName(){
		List<String> ls = new ArrayList<>();
		for (Faction f : factions) ls.add(f.getName());
		return ls;
	}
	
	public void addFaction(Object faction){
		factions.add((Faction) faction);
	}
	
	public boolean test(Player p) {
		if (factions.isEmpty()) return true;
		for (Faction fac : factions){
			if (FactionsIndex.get().getFaction(MPlayer.get(p)) == fac) return true;
		}
		return false;
	}

	
	protected void save(Map<String, Object> datas) {
		List<String> ls = new ArrayList<>();
		for (Faction fac : factions){
			ls.add(fac.getId());
		}
		datas.put("factions", factions);
	}

	protected void load(Map<String, Object> savedDatas) {
		for (String s : (List<String>) savedDatas.get("factions")){
			if (!FactionColl.get().containsId(s)){
				BeautyQuests.getInstance().getLogger().warning("Faction with ID " + s + " no longer exists. Quest \"" + quest.getName() + "\", ID " + quest.getID());
				continue;
			}
			factions.add(FactionColl.get().get(s));
		}
	}

}
