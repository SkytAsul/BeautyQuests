package fr.skytasul.quests.stages;

import java.util.Map;
import java.util.function.Supplier;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.BucketTypeGUI;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class StageBucket extends AbstractStage {

	private BucketType bucket;
	private int amount;

	public StageBucket(QuestBranch branch, BucketType bucket, int amount) {
		super(branch);
		this.bucket = bucket;
		this.amount = amount;
	}

	public BucketType getBucketType() {
		return bucket;
	}

	public int getBucketAmount() {
		return amount;
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onBucketFill(PlayerBucketFillEvent e) {
		Player p = e.getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (branch.hasStageLaunched(acc, this) && canUpdate(p)) {
			if (BucketType.fromMaterial(XMaterial.matchXMaterial(e.getItemStack())) == bucket) {
				int amount = getPlayerAmount(acc);
				if (amount <= 1) {
					finishStage(p);
				}else {
					updateObjective(acc, p, "amount", --amount);
				}
			}
		}
	}

	private int getPlayerAmount(PlayerAccount acc) {
		return getData(acc, "amount");
	}

	@Override
	protected void initPlayerDatas(PlayerAccount acc, Map<String, Object> datas) {
		datas.put("amount", amount);
	}

	@Override
	protected String descriptionLine(PlayerAccount acc, Source source) {
		return Lang.SCOREBOARD_BUCKET.format(Utils.getStringFromNameAndAmount(bucket.getName(), QuestsConfiguration.getItemAmountColor(), getPlayerAmount(acc), amount, false));
	}

	@Override
	protected Supplier<Object>[] descriptionFormat(PlayerAccount acc, Source source) {
		return new Supplier[] { () -> Utils.getStringFromNameAndAmount(bucket.getName(), QuestsConfiguration.getItemAmountColor(), getPlayerAmount(acc), amount, false) };
	}

	@Override
	protected void serialize(Map<String, Object> map) {
		map.put("bucket", bucket.name());
		map.put("amount", amount);
	}

	public static StageBucket deserialize(Map<String, Object> map, QuestBranch branch) {
		return new StageBucket(branch, BucketType.valueOf((String) map.get("bucket")), (int) map.get("amount"));
	}

	public enum BucketType {
		WATER(Lang.BucketWater, XMaterial.WATER_BUCKET),
		LAVA(Lang.BucketLava, XMaterial.LAVA_BUCKET),
		MILK(Lang.BucketMilk, XMaterial.MILK_BUCKET),
		SNOW(Lang.BucketSnow, XMaterial.POWDER_SNOW_BUCKET)
		;

		private Lang name;
		private XMaterial type;

		private BucketType(Lang name, XMaterial type) {
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return name.toString();
		}

		public XMaterial getMaterial() {
			return type;
		}

		public static BucketType fromMaterial(XMaterial type) {
			for (BucketType bucket : values()) {
				if (bucket.type == type) return bucket;
			}
			throw new IllegalArgumentException(type.name() + " does not correspond to any bucket type");
		}
	}

	public static class Creator extends StageCreation<StageBucket> {
		
		private BucketType bucket;
		private int amount;
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(6, ItemUtils.item(XMaterial.REDSTONE, Lang.editBucketAmount.toString()), (p, item) -> {
				Lang.BUCKET_AMOUNT.send(p);
				new TextEditor<>(p, () -> reopenGUI(p, true), obj -> {
					setAmount(obj);
					reopenGUI(p, true);
				}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
			});
			line.setItem(7, ItemUtils.item(XMaterial.BUCKET, Lang.editBucketType.toString()), (p, item) -> {
				new BucketTypeGUI(() -> reopenGUI(p, true), bucket -> {
					setBucket(bucket);
					reopenGUI(p, true);
				}).create(p);
			});
		}
		
		public void setBucket(BucketType bucket) {
			this.bucket = bucket;
			ItemStack newItem = ItemUtils.lore(line.getItem(7), Lang.optionValue.format(bucket.getName()));
			newItem.setType(bucket.type.parseMaterial());
			line.editItem(7, newItem);
		}
		
		public void setAmount(int amount) {
			this.amount = amount;
			line.editItem(6, ItemUtils.lore(line.getItem(6), Lang.Amount.format(amount)));
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			Runnable cancel = removeAndReopen(p, true);
			new BucketTypeGUI(cancel, bucket -> {
				setBucket(bucket);
				Lang.BUCKET_AMOUNT.send(p);
				new TextEditor<>(p, cancel, obj -> {
					setAmount(obj);
					reopenGUI(p, true);
				}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
			}).create(p);
		}

		@Override
		public void edit(StageBucket stage) {
			super.edit(stage);
			setBucket(stage.getBucketType());
			setAmount(stage.getBucketAmount());
		}
		
		@Override
		public StageBucket finishStage(QuestBranch branch) {
			return new StageBucket(branch, bucket, amount);
		}
	}

}
