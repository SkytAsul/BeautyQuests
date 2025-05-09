package fr.skytasul.quests.api.options;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.objects.QuestObjectsRegistry;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import fr.skytasul.quests.api.serializable.SerializableObject;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class QuestOptionObject<T extends QuestObject, C extends QuestObjectCreator<T>, L extends List<T>>
		extends QuestOption<L> {

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
	public void setValue(L value) {
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
		setValue(instanciate(SerializableObject.deserializeList(config.getMapList(key), this::deserialize)));
	}

	protected abstract T deserialize(Map<String, Object> map);

	protected abstract String getSizeString();

	protected abstract QuestObjectsRegistry<T, C> getObjectsRegistry();

	protected abstract L instanciate(Collection<T> objects);

	@Override
	public @Nullable L cloneValue(@Nullable L value) {
		return instanciate(value);
	}

	protected String[] getLore() {
		String count = "§7" + getSizeString();
		if (getItemDescription() == null) return new String[] { count };
		return new String[] { formatDescription(getItemDescription()), "", count };
	}

	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.item(getItemMaterial(), getItemName(), getLore());
	}

	@Override
	public void click(QuestCreationGuiClickEvent event) {
		getObjectsRegistry().createGUI(QuestObjectLocation.QUEST, objects -> {
			setValue(instanciate(objects));
			ItemUtils.lore(event.getClicked(), getLore());
			event.reopen();
		}, getValue()).open(event.getPlayer());
	}

	public abstract XMaterial getItemMaterial();

	public abstract String getItemName();

	public String getItemDescription() {
		return null;
	}

}
