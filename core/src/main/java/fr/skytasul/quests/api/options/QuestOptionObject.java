package fr.skytasul.quests.api.options;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.objects.QuestObjectsRegistry;
import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.structure.Quest;

public abstract class QuestOptionObject<T extends QuestObject, C extends QuestObjectCreator<T>> extends QuestOption<List<T>> {
	
	@Override
	public void attach(Quest quest) {
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
		return SerializableObject.serializeList(getValue());
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		getValue().addAll(QuestObject.deserializeList(config.getMapList(key), this::deserialize));
	}
	
	@Override
	public List<T> cloneValue(List<T> value) {
		return new ArrayList<>(value);
	}
	
	protected abstract T deserialize(Map<String, Object> map);
	
	protected abstract String getSizeString(int size);
	
	protected abstract QuestObjectsRegistry<T, C> getObjectsRegistry();
	
	protected String[] getLore() {
		String count = "§7" + getSizeString(getValue().size());
		if (getItemDescription() == null) return new String[] { count };
		return new String[] { formatDescription(getItemDescription()), "", count };
	}
	
	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.item(getItemMaterial(), getItemName(), getLore());
	}
	
	@Override
	public void click(FinishGUI gui, Player p, ItemStack item, int slot, ClickType click) {
		getObjectsRegistry().createGUI(QuestObjectLocation.QUEST, objects -> {
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
