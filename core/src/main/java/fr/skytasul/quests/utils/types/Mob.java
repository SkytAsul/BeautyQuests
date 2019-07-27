package fr.skytasul.quests.utils.types;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.EntityType;

import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.compatibility.mobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class Mob implements Cloneable{

	private MythicMob mmob;
	private String eboss;
	private EntityType bmob;
	private NPC npc;
	public int amount = 1;
	
	public Mob(){}
	
	public Mob(MythicMob mythicMob, int amount){
		this.mmob = mythicMob;
		this.amount = amount;
	}
	
	public Mob(EntityType bukkitMob, int amount){
		this.bmob = bukkitMob;
		this.amount = amount;
	}
	
	public Mob(NPC npc, int amount){
		this.npc = npc;
		this.amount = amount;
	}
	
	public Mob(String epicBoss, int amount){
		this.eboss = epicBoss;
		this.amount = amount;
	}
	
	public Mob(Object obj, int amount){
		this.amount = (amount < 1) ? 1 : amount;
		if (obj instanceof EntityType){
			bmob = (EntityType) obj;
		}else if (obj instanceof NPC){
			npc = (NPC) obj;
		}else if (obj.getClass().getSimpleName().equals("MythicMob")){
			mmob = (MythicMob) obj;
		}else if (obj instanceof String){
			eboss = (String) obj;
		}else {
			throw new IllegalArgumentException("The object specified isn't a bukkit entity, a MythicMob or a NPC. Maybe MythicMobs is disabled ?");
			/*String str = (String) obj;
			try{
				bmob = EntityType.valueOf(str);
			}catch (IllegalArgumentException ex){
				if (Dependencies.mm) mmob = MythicMobs.getMythicMob(str);
				if (mmob == null)
					throw new IllegalArgumentException("The object specified isn't a bukkit entity or a MythicMob (" + obj.getClass().getSimpleName() + "). Maybe MythicMobs is disabled ?");
			}*/
		}
	}

	public MythicMob getMythicMob(){
		return mmob;
	}
	
	public EntityType getBukkitMob(){
		return bmob;
	}
	
	public NPC getNPC(){
		return npc;
	}
	
	public String getBossName(){
		return eboss;
	}
	
	public boolean hasMythicMob(){
		return mmob != null;
	}
	
	public boolean hasBukkitMob(){
		return bmob != null;
	}
	
	public boolean hasNPC(){
		return npc != null;
	}
	
	public boolean hasEpicBoss(){
		return eboss != null;
	}
	
	public boolean isEmpty(){
		return npc == null && bmob == null && mmob == null && eboss == null;
	}
	
	public boolean equalsMob(Object mob){
		if (mob == null) return false;
		if (mob instanceof EntityType) return mob.equals(bmob);
		if (mob instanceof NPC) return mob.equals(npc);
		if (mob instanceof MythicMob) return mob.equals(mmob);
		if (mob instanceof String) return mob.equals(eboss);
		return false;
	}
	
	public String getName(){
		if (hasBukkitMob()){
			return /*CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, bmob.getName());*/MinecraftNames.getEntityName(bmob);
		}else if (hasMythicMob()){
			return MythicMobs.getDisplayName(mmob);
		}else if (hasNPC()){
			return npc.getName();
		}else if (hasEpicBoss()){
			return eboss;
		}
		return "§cerror";
	}
	
	public Mob clone(){
		if (hasBukkitMob()){
			return new Mob(bmob, amount);
		}else if (hasMythicMob()){
			return new Mob(mmob, amount);
		}else if (hasNPC()){
			return new Mob(npc, amount);
		}else if (hasEpicBoss()){
			return new Mob(eboss, amount);
		}
		throw new IllegalArgumentException("The object specified isn't a bukkit entity, a MythicMob or a NPC.");
	}
	
	public boolean equals(Object obj){
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof Mob)) return false;
		Mob mob = (Mob) obj;
		if (hasBukkitMob()){
			return bmob.equals(mob.bmob);
		}else if (hasMythicMob()){
			return mmob == mob.mmob;
		}else if (hasNPC()){
			return npc == mob.npc;
		}else if (hasEpicBoss()){
			return eboss.equals(mob.eboss);
		}
		return false;
	}
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("amount", amount);
		if (hasBukkitMob()){
			map.put("bmob", bmob.name());
		}else if (hasMythicMob()){
			map.put("mmob", MythicMobs.getInternalName(mmob));
		}else if (hasNPC()){
			map.put("npc", npc.getId());
		}else if (hasEpicBoss()){
			map.put("eboss", eboss);
		}else return null;
		
		return map;
	}
	
	public static Mob deserialize(Map<String, Object> map){
		/*Object obj = (map.get("bmob") != null) ? map.get("bmob") : map.get("mmob");
		if (obj == null) return null;
		return new Mob(obj, (int) map.get("amount"));*/
		int amount = (int) map.get("amount");
		if (map.containsKey("bmob")){
			return new Mob(EntityType.valueOf((String) map.get("bmob")), amount);
		}else if (map.containsKey("mmob") && Dependencies.mm){
			return new Mob(MythicMobs.getMythicMob((String) map.get("mmob")), amount);
		}else if (map.containsKey("npc")){
			return new Mob(CitizensAPI.getNPCRegistry().getById((int) map.get("npc")), amount);
		}else if (map.containsKey("eboss") && Dependencies.eboss){
			return new Mob(map.get("eboss"), amount);
		}
		throw new IllegalArgumentException("The mob data cannot be loaded. Maybe MythicMobs or EpicBosses is disabled ?");
	}
	
}
