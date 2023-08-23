package fr.skytasul.quests.stages;

import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.api.utils.progress.ProgressPlaceholders;
import fr.skytasul.quests.api.utils.progress.itemdescription.HasItemsDescriptionConfiguration.HasSingleObject;
import fr.skytasul.quests.api.utils.progress.itemdescription.ItemsDescriptionConfiguration;
import fr.skytasul.quests.gui.misc.BucketTypeGUI;

public class StageBucket extends AbstractStage implements HasSingleObject {

	private BucketType bucket;
	private int amount;

	public StageBucket(StageController controller, BucketType bucket, int amount) {
		super(controller);
		this.bucket = bucket;
		this.amount = amount;
	}

	public BucketType getBucketType() {
		return bucket;
	}

	@Override
	public @NotNull String getObjectName() {
		return bucket.getName();
	}

	public int getBucketAmount() {
		return amount;
	}

	@Override
	public int getObjectAmount() {
		return amount;
	}

	@Override
	public @NotNull ItemsDescriptionConfiguration getItemsDescriptionConfiguration() {
		return QuestsConfiguration.getConfig().getStageDescriptionConfig();
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onBucketFill(PlayerBucketFillEvent e) {
		Player p = e.getPlayer();
		if (hasStarted(p) && canUpdate(p)) {
			if (BucketType.fromMaterial(XMaterial.matchXMaterial(e.getItemStack())) == bucket) {
				int amount = getPlayerAmount(PlayersManager.getPlayerAccount(p));
				if (amount <= 1) {
					finishStage(p);
				}else {
					updateObjective(p, "amount", --amount);
				}
			}
		}
	}

	@Override
	public int getPlayerAmount(PlayerAccount acc) {
		return getData(acc, "amount");
	}

	@Override
	public void initPlayerDatas(PlayerAccount acc, Map<String, Object> datas) {
		datas.put("amount", amount);
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.register("bucket_type", bucket.getName());
		ProgressPlaceholders.registerObject(placeholders, "buckets", this);
	}

	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_BUCKET.toString();
	}

	@Override
	protected void serialize(ConfigurationSection section) {
		section.set("bucket", bucket.name());
		section.set("amount", amount);
	}

	public static StageBucket deserialize(ConfigurationSection section, StageController controller) {
		return new StageBucket(controller, BucketType.valueOf(section.getString("bucket")), section.getInt("amount"));
	}

	public enum BucketType {
		WATER(Lang.BucketWater, XMaterial.WATER_BUCKET),
		LAVA(Lang.BucketLava, XMaterial.LAVA_BUCKET),
		MILK(Lang.BucketMilk, XMaterial.MILK_BUCKET),
		SNOW(Lang.BucketSnow, XMaterial.POWDER_SNOW_BUCKET)
		;

		private static BucketType[] AVAILABLE;

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

		public static BucketType[] getAvailable() {
			if (AVAILABLE == null) {
				AVAILABLE = MinecraftVersion.MAJOR >= 17 ? values() : new BucketType[] {WATER, LAVA, MILK};
				// inefficient? yes. But it's christmas and I don't want to work on this anymore, plus there will
				// probably not be more bucket types in the future
			}
			return AVAILABLE;
		}
	}

	public static class Creator extends StageCreation<StageBucket> {
		
		private BucketType bucket;
		private int amount;
		
		public Creator(@NotNull StageCreationContext<StageBucket> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);
			
			line.setItem(6, ItemUtils.item(XMaterial.REDSTONE, Lang.editBucketAmount.toString()), event -> {
				Lang.BUCKET_AMOUNT.send(event.getPlayer());
				new TextEditor<>(event.getPlayer(), event::reopen, obj -> {
					setAmount(obj);
					event.reopen();
				}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).start();
			});
			line.setItem(7, ItemUtils.item(XMaterial.BUCKET, Lang.editBucketType.toString()), event -> {
				new BucketTypeGUI(event::reopen, bucket -> {
					setBucket(bucket);
					event.reopen();
				}).open(event.getPlayer());
			});
		}
		
		public void setBucket(BucketType bucket) {
			this.bucket = bucket;
			ItemStack newItem = ItemUtils.lore(getLine().getItem(7), QuestOption.formatNullableValue(bucket.getName()));
			newItem.setType(bucket.type.parseMaterial());
			getLine().refreshItem(7, newItem);
		}
		
		public void setAmount(int amount) {
			this.amount = amount;
			getLine().refreshItemLore(6, Lang.Amount.quickFormat("amount", amount));
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			new BucketTypeGUI(context::removeAndReopenGui, bucket -> {
				setBucket(bucket);
				Lang.BUCKET_AMOUNT.send(p);
				new TextEditor<>(p, context::removeAndReopenGui, obj -> {
					setAmount(obj);
					context.reopenGui();
				}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).start();
			}).open(p);
		}

		@Override
		public void edit(StageBucket stage) {
			super.edit(stage);
			setBucket(stage.getBucketType());
			setAmount(stage.getBucketAmount());
		}
		
		@Override
		public StageBucket finishStage(StageController controller) {
			return new StageBucket(controller, bucket, amount);
		}
	}

}
