package fr.skytasul.quests.gui.particles;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.checkers.ColorParser;
import fr.skytasul.quests.api.gui.Gui;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.ParticleEffect.ParticleShape;
import fr.skytasul.quests.utils.compatibility.Post1_13;

public class ParticleEffectGUI extends Gui {
	
	private static final int SLOT_SHAPE = 1;
	private static final int SLOT_PARTICLE = 3;
	private static final int SLOT_COLOR = 4;
	private static final int SLOT_CANCEL = 7;
	private static final int SLOT_FINISH = 8;
	
	static final List<Particle> PARTICLES = Arrays.stream(Particle.values()).filter(particle -> {
		if (particle.getDataType() == Void.class) return true;
		if (MinecraftVersion.MAJOR >= 13) return particle.getDataType() == Post1_13.getDustOptionClass();
		return false;
	}).collect(Collectors.toList());
	
	private final Consumer<ParticleEffect> end;
	
	private Particle particle;
	private ParticleShape shape;
	private Color color;
	
	private Inventory inv;
	
	public ParticleEffectGUI(Consumer<ParticleEffect> end) {
		this(end, Particle.FLAME, ParticleShape.POINT, Color.AQUA);
	}
	
	public ParticleEffectGUI(Consumer<ParticleEffect> end, ParticleEffect effect) {
		this(end, effect.getParticle(), effect.getShape(), effect.getColor());
	}
	
	public ParticleEffectGUI(Consumer<ParticleEffect> end, Particle particle, ParticleShape shape, Color color) {
		this.end = end;
		this.particle = particle;
		this.shape = shape;
		this.color = color;
	}
	
	@Override
	protected Inventory instanciate(@NotNull Player player) {
		return Bukkit.createInventory(null, 9, Lang.INVENTORY_PARTICLE_EFFECT.toString());
	}

	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inventory) {
		inventory.setItem(SLOT_SHAPE,
				ItemUtils.item(XMaterial.FIREWORK_STAR, Lang.particle_shape.toString(), Lang.optionValue.format(shape)));
		inventory.setItem(SLOT_PARTICLE,
				ItemUtils.item(XMaterial.PAPER, Lang.particle_type.toString(), Lang.optionValue.format(particle)));
		if (ParticleEffect.canHaveColor(particle))
			setColorItem();

		inventory.setItem(SLOT_CANCEL, ItemUtils.itemCancel);
		inventory.setItem(SLOT_FINISH, ItemUtils.itemDone);
	}
	
	@Override
	public CloseBehavior onClose(Player p) {
		return new DelayCloseBehavior(() -> end.accept(null));
	}
	
	@Override
	public void onClick(GuiClickEvent event) {
		switch (slot) {
		
		case SLOT_SHAPE:
			List<ParticleShape> shapes = Arrays.asList(ParticleShape.values());
			int index = shapes.indexOf(shape);
			shape = shapes.get(index == shapes.size() - 1 ? 0 : (index + 1));
			ItemUtils.lore(current, Lang.optionValue.format(shape));
			break;
		
		case SLOT_PARTICLE:
			new ParticleListGUI(existing -> {
				if (existing != null) {
					particle = existing;
					ItemUtils.lore(current, Lang.optionValue.format(particle));
					if (ParticleEffect.canHaveColor(existing)) {
						setColorItem();
					}else {
						inv.setItem(SLOT_COLOR, null);
					}
				}
				ParticleEffectGUI.this.open(p);
			}).allowCancel().open(p);
			break;
		
		case SLOT_COLOR:
			if (ParticleEffect.canHaveColor(particle)) {
				Runnable reopen = () -> open(p);
				Lang.COLOR_EDITOR.send(p);
				new TextEditor<>(p, reopen, newColor -> {
					color = newColor;
					ItemUtils.lore(current, getColorLore());
					reopen.run();
				}, ColorParser.PARSER).start();
			}
			break;
		
		case SLOT_CANCEL:
			end.accept(null);
			break;
		
		case SLOT_FINISH:
			end.accept(new ParticleEffect(particle, shape, color));
			break;
		}
		return true;
	}
	
	private void setColorItem() {
		if (color == null) color = Color.RED;
		inv.setItem(SLOT_COLOR, ItemUtils.item(XMaterial.MAGENTA_DYE, Lang.particle_color.toString(), getColorLore()));
	}
	
	private String[] getColorLore() {
		return new String[] { Lang.optionValue.format("RGB: " + color.getRed() + " " + color.getGreen() + " " + color.getBlue()) };
	}
	
}
