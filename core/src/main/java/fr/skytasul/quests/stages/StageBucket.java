package fr.skytasul.quests.stages;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketFillEvent;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreationRunnables;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.BucketTypeGUI;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.creation.stages.LineData;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.players.PlayersManagerYAML;
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

	protected void initPlayerDatas(PlayerAccount acc, Map<String, Object> datas) {
		datas.put("amount", amount);
	}

	protected String descriptionLine(PlayerAccount acc, Source source) {
		return Lang.SCOREBOARD_BUCKET.format(Utils.getStringFromNameAndAmount(bucket.getName(), QuestsConfiguration.getItemAmountColor(), getPlayerAmount(acc), false));
	}

	protected Object[] descriptionFormat(PlayerAccount acc, Source source) {
		return new Object[] { Utils.getStringFromNameAndAmount(bucket.getName(), QuestsConfiguration.getItemAmountColor(), getPlayerAmount(acc), false) };
	}

	protected void serialize(Map<String, Object> map) {
		map.put("bucket", bucket.name());
		map.put("amount", amount);
	}

	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch) {
		StageBucket stage = new StageBucket(branch, BucketType.valueOf((String) map.get("bucket")), (int) map.get("amount"));

		if (map.containsKey("players")) {
			PlayersManagerYAML migration = PlayersManagerYAML.getMigrationYAML();
			((Map<String, Object>) map.get("players")).forEach((acc, amount) -> stage.setData(migration.getByIndex(acc), "amount", (int) amount));
		}

		return stage;
	}

	public static enum BucketType {
		WATER(Lang.BucketWater, XMaterial.WATER_BUCKET), LAVA(Lang.BucketLava, XMaterial.LAVA_BUCKET), MILK(Lang.BucketMilk, XMaterial.MILK_BUCKET);

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
			if (type == XMaterial.WATER_BUCKET) return WATER;
			if (type == XMaterial.LAVA_BUCKET) return LAVA;
			if (type == XMaterial.MILK_BUCKET) return MILK;
			throw new IllegalArgumentException(type.name() + " does not correspond to any bucket type");
		}
	}

	public static class Creator implements StageCreationRunnables<StageBucket> {
		public void start(Player p, LineData datas) {
			new BucketTypeGUI((bucket) -> {
				datas.put("bucket", bucket);
				Lang.BUCKET_AMOUNT.send(p);
				new TextEditor(p, (obj) -> {
					datas.put("amount", obj);
					datas.getGUI().reopen(p, true);
					setItems(datas.getLine());
				}, new NumberParser(Integer.class, true, true)).enterOrLeave(p);
			}).create(p);
		}

		public StageBucket finish(LineData datas, QuestBranch branch) {
			StageBucket stage = new StageBucket(branch, (BucketType) datas.get("bucket"), (int) datas.get("amount"));
			return stage;
		}

		public void edit(LineData datas, StageBucket stage) {
			datas.put("bucket", stage.getBucketType());
			datas.put("amount", stage.getBucketAmount());
			setItems(datas.getLine());
		}

		public static void setItems(Line line) {
			line.setItem(7, ItemUtils.item(XMaterial.REDSTONE, Lang.editBucketAmount.toString(), Lang.Amount.format(line.data.get("amount"))), (p, datas, item) -> {
				Lang.BUCKET_AMOUNT.send(p);
				new TextEditor(p, (obj) -> {
					datas.put("amount", obj);
					datas.getGUI().reopen(p, true);
					ItemUtils.lore(item, Lang.Amount.format(obj));
				}, new NumberParser(Integer.class, true, true)).enterOrLeave(p);
			});
			BucketType type = (BucketType) line.data.get("bucket");
			line.setItem(6, ItemUtils.item(type.getMaterial(), Lang.editBucketType.toString(), type.getName()), (p, datas, item) -> {
				new BucketTypeGUI((bucket) -> {
					datas.put("bucket", bucket);
					datas.getGUI().reopen(p, true);
					item.setType(bucket.getMaterial().parseMaterial());
					ItemUtils.lore(item, bucket.getName());
				}).create(p);
			});
		}
	}

}
