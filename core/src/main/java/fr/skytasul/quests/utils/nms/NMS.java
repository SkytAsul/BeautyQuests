package fr.skytasul.quests.utils.nms;

import fr.skytasul.quests.utils.ReflectUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public abstract class NMS{

	protected final ReflectUtils craftReflect;

	protected Field unhandledTags;
	protected Method equalsCommon;

	public NMS() {
		try {
			craftReflect = ReflectUtils.fromPackage(Bukkit.getServer().getClass().getPackageName());
			if (!(this instanceof NullNMS)) {
				Class<?> itemMetaClass = craftReflect.fromName("inventory.CraftMetaItem");
				unhandledTags = itemMetaClass.getDeclaredField("unhandledTags");
				equalsCommon = itemMetaClass.getDeclaredMethod("equalsCommon", itemMetaClass);
				unhandledTags.setAccessible(true);
				equalsCommon.setAccessible(true);
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> getAvailableBlockProperties(Material material){
		throw new UnsupportedOperationException();
	}

	public List<Key> getAvailableBlockTags() {
		throw new UnsupportedOperationException();
	}

	public boolean equalsWithoutNBT(ItemMeta meta1, ItemMeta meta2) throws ReflectiveOperationException {
		((Map<?, ?>) unhandledTags.get(meta1)).clear();
		((Map<?, ?>) unhandledTags.get(meta2)).clear();
		return (boolean) equalsCommon.invoke(meta1, meta2);
	}

}
