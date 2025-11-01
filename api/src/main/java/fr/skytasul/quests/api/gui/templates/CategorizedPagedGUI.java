package fr.skytasul.quests.api.gui.templates;

import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.gui.ItemUtils;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

public abstract class CategorizedPagedGUI<T> extends DelegatingGui {

	private @NotNull Collection<Category<T>> categories;
	private final InnerPagedGui innerGui;

	private final @NotNull Set<String> openedCategories = new HashSet<>();

	public CategorizedPagedGUI(@NotNull String name, @Nullable DyeColor color, @NotNull Collection<Category<T>> categories) {
		this.categories = categories;

		innerGui = new InnerPagedGui(name, color, Collections.emptyList());
	}

	@Override
	protected final @NotNull PagedGUI<?> getDelegate() {
		return innerGui;
	}

	protected @NotNull Player getViewer() {
		return innerGui.getViewer();
	}

	public boolean isCategoryOpened(@NotNull Category<T> category) {
		return openedCategories.contains(category.id());
	}

	public void setCategoryOpened(@NotNull Category<T> category, boolean opened) {
		if (opened) {
			openedCategories.add(category.id());
		} else {
			openedCategories.remove(category.id());
		}
	}

	protected abstract @NotNull ItemStack getItemStack(@NotNull T object);

	protected abstract void click(@NotNull T object, @NotNull ItemStack item, @NotNull ClickType clickType);

	public void setCategories(@NotNull Collection<Category<T>> categories) {
		this.categories = categories;

		for (var iterator = openedCategories.iterator(); iterator.hasNext();) {
			var openedCat = iterator.next();
			if (!categories.stream().anyMatch(cat -> cat.id().equals(openedCat)))
				iterator.remove();
		}

		innerGui.refreshObjects();
	}

	public record Category<T>(String id, List<T> objects, ItemStack item) {
	}

	private class InnerPagedGui extends PagedGUI<Object> {

		protected InnerPagedGui(@NotNull String name, @Nullable DyeColor color, @NotNull Collection<Object> objects) {
			super(name, color, objects);
		}

		@Override
		public @NotNull ItemStack getItemStack(@NotNull Object object) {
			if (object instanceof Category cat) {
				boolean opened = isCategoryOpened(cat);
				var item = cat.item.clone();
				ItemUtils.setGlittering(item, opened);
				return item;
			}
			return CategorizedPagedGUI.this.getItemStack((T) object);
		}

		@Override
		public void click(@NotNull Object existing, @NotNull ItemStack item, @NotNull ClickType clickType) {
			if (existing instanceof Category cat) {
				setCategoryOpened(cat, !isCategoryOpened(cat));
				refreshObjects();
			} else {
				CategorizedPagedGUI.this.click((T) existing, item, clickType);
			}
		}

		private void refreshObjects() {
			if (categories.size() == 1 && QuestsConfiguration.getConfig().getGuiConfig().hideFolderIfSingleCategory()) {
				this.setObjects((Collection<Object>) categories.iterator().next().objects);
				return;
			}

			List<Object> objects = new ArrayList<>();
			for (var category : categories) {
				boolean opened = isCategoryOpened(category);
				objects.add(category);
				if (opened)
					objects.addAll(category.objects);
			}
			this.setObjects(objects);
		}

	}

}
