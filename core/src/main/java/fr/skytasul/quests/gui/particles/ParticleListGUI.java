package fr.skytasul.quests.gui.particles;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.DyeColor;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.StaticPagedGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.ParticleEffect;

public class ParticleListGUI extends StaticPagedGUI<Particle> {
	
	private static final Map<Particle, ItemStack> PARTICLES = ParticleEffectGUI.PARTICLES
			.stream().collect(Collectors.toMap(Function.identity(), particle -> {
				boolean colorable = ParticleEffect.canHaveColor(particle);
				String[] lore = colorable ? new String[] { QuestOption.formatDescription(Lang.particle_colored.toString()) } : new String[0];
				return ItemUtils.item(colorable ? XMaterial.MAP : XMaterial.PAPER, "§e" + particle.name(), lore);
			}));
	
	public ParticleListGUI(Consumer<Particle> end) {
		super(Lang.INVENTORY_PARTICLE_LIST.toString(), DyeColor.MAGENTA, PARTICLES, end, Particle::name);
		sortValuesByName();
	}
	
}