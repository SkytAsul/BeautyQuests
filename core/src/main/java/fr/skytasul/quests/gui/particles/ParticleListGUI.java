package fr.skytasul.quests.gui.particles;

import java.util.function.Consumer;

import org.bukkit.DyeColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class ParticleListGUI extends PagedGUI<Particle> {
	private Consumer<Particle> end;
	
	public ParticleListGUI(Consumer<Particle> end) {
		super(Lang.INVENTORY_PARTICLE_LIST.toString(), DyeColor.MAGENTA, ParticleEffectGUI.PARTICLES);
		this.end = end;
	}
	
	@Override
	public ItemStack getItemStack(Particle object) {
		String[] lore = ParticleEffect.canHaveColor(object) ? new String[] { QuestOption.formatDescription(Lang.particle_colored.toString()) } : new String[0];
		return ItemUtils.item(XMaterial.PAPER, "§e" + object.name(), lore);
	}
	
	@Override
	public void click(Particle existing, ItemStack item, ClickType clickType) {
		end.accept(existing);
	}
	
	@Override
	public CloseBehavior onClose(Player p, Inventory inv) {
		Utils.runSync(() -> end.accept(null));
		return CloseBehavior.REMOVE;
	}
}