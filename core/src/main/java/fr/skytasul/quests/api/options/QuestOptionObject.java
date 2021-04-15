package fr.skytasul.quests.api.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public abstract class QuestOptionObject<T extends QuestObject> extends QuestOption<List<T>> {
	
	@Override
	public void attach(Quest quest) {
		Validate.notNull(quest, "Attached quest cannot be null");
		super.attach(quest);
		attachObjects();
	}
	
	@Override
	public void detach() {
		super.detach();
		detachObjects();
	}
	
	@Override
	public void setValue(List<T> value) {
		if (getValue() != null && getAttachedQuest() != null) detachObjects();
		super.setValue(value);
		if (getValue() != null && getAttachedQuest() != null) attachObjects();
	}
	
	private void detachObjects() {
		getValue().forEach(T::detach);
	}
	
	private void attachObjects() {
		getValue().forEach(this::attachObject);
	}
	
	protected void attachObject(T object) {
		object.attach(getAttachedQuest());
	}
	
	@Override
	public Object save() {
		return Utils.serializeList(getValue(), getSerializeFunction());
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		List<Map<?, ?>> objectList = config.getMapList(key);
		for (Map<?, ?> objectMap : objectList) {
			try {
				getValue().add(deserialize((Map<String, Object>) objectMap));
			}catch (Exception e) {
				BeautyQuests.getInstance().getLogger().severe("An exception occured while deserializing a quest object (class " + objectMap.get("class") + ").");
				BeautyQuests.loadingFailure = true;
				e.printStackTrace();
				continue;
			}
		}
	}
	
	@Override
	public List<T> cloneValue(List<T> value) {
		return new ArrayList<>(value);
	}
	
	protected abstract Function<T, Map<String, Object>> getSerializeFunction();
	
	protected abstract T deserialize(Map<String, Object> map) throws ClassNotFoundException;
	
	protected abstract String getSizeString(int size);
	
	protected abstract String getInventoryName();
	
	protected abstract Collection<QuestObjectCreator<T>> getCreators();
	
	private String[] getLore() {
		String count = "§7" + getSizeString(getValue().size());
		if (getItemDescription() == null) return new String[] { count };
		return new String[] { formatDescription(getItemDescription()), "", count };
	}
	
	@Override
	public ItemStack getItemStack() {
		return ItemUtils.item(getItemMaterial(), getItemName(), getLore());
	}
	
	@Override
	public void click(FinishGUI gui, Player p, ItemStack item, int slot, ClickType click) {
		new QuestObjectGUI<>(getInventoryName(), QuestObjectLocation.QUEST, getCreators(), objects -> {
			setValue(objects);
			ItemUtils.lore(item, getLore());
			gui.reopen(p);
		}, getValue()).create(p);
	}
	
	public abstract XMaterial getItemMaterial();
	
	public abstract String getItemName();
	
	public String getItemDescription() {
		return null;
	}
	
}
