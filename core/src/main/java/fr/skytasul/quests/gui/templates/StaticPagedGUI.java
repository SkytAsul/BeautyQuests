package fr.skytasul.quests.gui.templates;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.utils.Utils;

public class StaticPagedGUI<T> extends PagedGUI<Entry<T, ItemStack>> {
	
	protected final Consumer<T> clicked;
	private boolean cancelAllowed = false;
	
	public StaticPagedGUI(String name, DyeColor color, Map<T, ItemStack> objects, Consumer<T> clicked, Function<T, String> nameMapper) {
		super(name, color, objects.entrySet(), null, nameMapper == null ? null : entry -> nameMapper.apply(entry.getKey()));
		this.clicked = clicked;
	}
	
	public StaticPagedGUI<T> allowCancel() {
		cancelAllowed = true;
		return this;
	}
	
	@Override
	public ItemStack getItemStack(Entry<T, ItemStack> object) {
		return object.getValue();
	}
	
	@Override
	public void click(Entry<T, ItemStack> existing, ItemStack item, ClickType clickType) {
		clicked.accept(existing.getKey());
	}
	
	@Override
	public CloseBehavior onClose(Player p, Inventory inv) {
		if (cancelAllowed) {
			Utils.runSync(() -> clicked.accept(null));
			return CloseBehavior.NOTHING;
		}else {
			return CloseBehavior.REOPEN;
		}
	}
	
}
