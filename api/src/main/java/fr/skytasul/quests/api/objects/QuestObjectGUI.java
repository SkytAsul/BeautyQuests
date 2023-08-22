package fr.skytasul.quests.api.objects;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.gui.templates.ListGUI;
import fr.skytasul.quests.api.gui.templates.PagedGUI;

public class QuestObjectGUI<T extends QuestObject> extends ListGUI<T> {

	private String name;
	private Collection<QuestObjectCreator<T>> creators;
	private Consumer<List<T>> end;

	public QuestObjectGUI(@NotNull String name, @NotNull QuestObjectLocation objectLocation,
			@NotNull Collection<@NotNull QuestObjectCreator<T>> creators, @NotNull Consumer<@NotNull List<T>> end,
			@NotNull List<T> objects) {
		super(name, DyeColor.CYAN, (List<T>) objects.stream().map(QuestObject::clone).collect(Collectors.toList()));
		this.name = name;
		this.creators = creators.stream()
				.filter(creator -> creator.isAllowed(objectLocation))
				.filter(creator -> creator.canBeMultiple()
						|| objects.stream().noneMatch(object -> object.getCreator() == creator))
				.collect(Collectors.toList());
		this.end = end;
	}
	
	@Override
	public ItemStack getObjectItemStack(QuestObject object) {
		return object.getItemStack();
	}

	@Override
	protected ClickType getRemoveClick(@NotNull T object) {
		return object.getRemoveClick();
	}

	@Override
	protected void removed(T object) {
		if (!object.getCreator().canBeMultiple()) creators.add(object.getCreator());
	}
	
	@Override
	public void createObject(Function<T, ItemStack> callback) {
		new PagedGUI<QuestObjectCreator<T>>(name, DyeColor.CYAN, creators) {
			
			@Override
			public ItemStack getItemStack(QuestObjectCreator<T> object) {
				return object.getItem();
			}
			
			@Override
			public void click(QuestObjectCreator<T> existing, ItemStack item, ClickType clickType) {
				T object = existing.newObject();
				if (!existing.canBeMultiple()) creators.remove(existing);
				object.click(
						new QuestObjectClickEvent(player, QuestObjectGUI.this, callback.apply(object), clickType, true, object));
			}
			
			@Override
			public CloseBehavior onClose(Player p) {
				return new DelayCloseBehavior(QuestObjectGUI.super::reopen);
			}
			
		}.open(player);
	}
	
	@Override
	public void clickObject(QuestObject existing, ItemStack item, ClickType clickType) {
		existing.click(new QuestObjectClickEvent(player, this, item, clickType, false, existing));
	}
	
	@Override
	public void finish(List<T> objects) {
		end.accept(objects);
	}

}