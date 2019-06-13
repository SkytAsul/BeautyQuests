package fr.skytasul.quests.stages;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketFillEvent;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.stages.StageManager.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class StageBucket extends AbstractStage {

	private BucketType bucket;
	private int amount;
	private Map<PlayerAccount, Integer> playerAmounts = new HashMap<>();
	
	public StageBucket(StageManager manager, BucketType bucket, int amount){
		super(manager);
		this.bucket = bucket;
		this.amount = amount;
	}
	
	public BucketType getBucketType(){
		return bucket;
	}
	
	public int getBucketAmount(){
		return amount;
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onBucketFill(PlayerBucketFillEvent e){
		PlayerAccount acc = PlayersManager.getPlayerAccount(e.getPlayer());
		if (manager.hasStageLaunched(acc, this)){
			if (BucketType.fromMaterial(XMaterial.fromMaterial(e.getItemStack().getType())) == bucket){
				int newAmount = playerAmounts.get(acc) - 1;
				if (newAmount <= 0){
					playerAmounts.remove(acc);
					finishStage(e.getPlayer());
				}else {
					playerAmounts.put(acc, newAmount);
				}
			}
		}
	}
	
	public void start(PlayerAccount account){
		super.start(account);
		playerAmounts.put(account, amount);
	}
	
	protected String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_BUCKET.format(Utils.getStringFromNameAndAmount(bucket.getName(), QuestsConfiguration.getItemAmountColor(), playerAmounts.get(acc), false));
	}

	protected Object[] descriptionFormat(PlayerAccount acc, Source source){
		return new Object[]{Utils.getStringFromNameAndAmount(bucket.getName(), QuestsConfiguration.getItemAmountColor(), playerAmounts.get(acc), false)};
	}
	
	protected void serialize(Map<String, Object> map){
		map.put("bucket", bucket.name());
		map.put("amount", amount);
		Map<String, Integer> playerSerialized = new HashMap<>();
		playerAmounts.forEach((acc, amount) -> playerSerialized.put(acc.getIndex(), amount));
		map.put("players", playerSerialized);
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, StageManager manager){
		StageBucket stage = new StageBucket(manager, BucketType.valueOf((String) map.get("bucket")), (int) map.get("amount"));
		((Map<String, Object>) map.get("players")).forEach((acc, amount) -> stage.playerAmounts.put(PlayersManager.getByIndex(acc), (int) amount));
		return stage;
	}

	public static enum BucketType{
		WATER(Lang.BucketWater, XMaterial.WATER_BUCKET), LAVA(Lang.BucketLava, XMaterial.LAVA_BUCKET), MILK(Lang.BucketMilk, XMaterial.MILK_BUCKET);

		private Lang name;
		private XMaterial type;
		
		private BucketType(Lang name, XMaterial type){
			this.name = name;
			this.type = type;
		}
		
		public String getName(){
			return name.toString();
		}
		
		public XMaterial getMaterial(){
			return type;
		}
		
		public static BucketType fromMaterial(XMaterial type){
			if (type == XMaterial.WATER_BUCKET) return WATER;
			if (type == XMaterial.LAVA_BUCKET) return LAVA;
			if (type == XMaterial.MILK_BUCKET) return MILK;
			throw new IllegalArgumentException(type.name() + " does not correspond to any bucket type");
		}
	}
	
}
