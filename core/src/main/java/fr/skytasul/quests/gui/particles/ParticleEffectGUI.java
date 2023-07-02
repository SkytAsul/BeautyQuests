package fr.skytasul.quests.gui.particles;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.ColorParser;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.gui.layout.LayoutedButton;
import fr.skytasul.quests.api.gui.layout.LayoutedClickEvent;
import fr.skytasul.quests.api.gui.layout.LayoutedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.ParticleEffect.ParticleShape;
import fr.skytasul.quests.utils.compatibility.Post1_13;

public class ParticleEffectGUI extends LayoutedGUI.LayoutedRowsGUI {
	
	static final List<Particle> PARTICLES = Arrays.stream(Particle.values()).filter(particle -> {
		if (particle.getDataType() == Void.class) return true;
		if (MinecraftVersion.MAJOR >= 13) return particle.getDataType() == Post1_13.getDustOptionClass();
		return false;
	}).collect(Collectors.toList());
	
	private final Consumer<ParticleEffect> end;
	
	private Particle particle;
	private ParticleShape shape;
	private Color color;
	
	public ParticleEffectGUI(Consumer<ParticleEffect> end) {
		this(end, Particle.FLAME, ParticleShape.POINT, Color.AQUA);
	}
	
	public ParticleEffectGUI(Consumer<ParticleEffect> end, ParticleEffect effect) {
		this(end, effect.getParticle(), effect.getShape(), effect.getColor());
	}
	
	public ParticleEffectGUI(Consumer<ParticleEffect> end, Particle particle, ParticleShape shape, Color color) {
		super(Lang.INVENTORY_PARTICLE_EFFECT.toString(), new HashMap<>(), new DelayCloseBehavior(() -> end.accept(null)), 1);
		this.end = end;
		this.particle = particle;
		this.shape = shape;
		this.color = color;

		buttons.put(1, LayoutedButton.create(XMaterial.FIREWORK_STAR, Lang.particle_shape.toString(),
				() -> Arrays.asList(Lang.optionValue.format(shape)), this::shapeClick));
		buttons.put(3, LayoutedButton.create(XMaterial.PAPER, Lang.particle_type.toString(),
				() -> Arrays.asList(Lang.optionValue.format(particle)), this::particleClick));
		buttons.put(4, new LayoutedButton.ItemButton() {
			@Override
			public void click(@NotNull LayoutedClickEvent event) {
				colorClick(event);
			}

			@Override
			public @Nullable ItemStack getItem() {
				return ItemUtils.item(XMaterial.MAGENTA_DYE, Lang.particle_color.toString(),
						Lang.optionValue.format("RGB: " + color.getRed() + " " + color.getGreen() + " " + color.getBlue()));
			}

			@Override
			public boolean isValid() {
				return ParticleEffect.canHaveColor(particle);
			}
		});
		buttons.put(7, LayoutedButton.create(ItemUtils.itemCancel, this::cancelClick));
		buttons.put(8, LayoutedButton.create(ItemUtils.itemDone, this::doneClick));
	}

	private void shapeClick(LayoutedClickEvent event) {
		List<ParticleShape> shapes = Arrays.asList(ParticleShape.values());
		int index = shapes.indexOf(shape);
		shape = shapes.get(index == shapes.size() - 1 ? 0 : (index + 1));
		event.refreshItem();
	}

	private void particleClick(LayoutedClickEvent event) {
		new ParticleListGUI(existing -> {
			if (existing != null)
				particle = existing;
			event.refreshGuiReopen(); // refresh gui to refresh color button
		}).allowCancel().open(event.getPlayer());
	}

	private void colorClick(LayoutedClickEvent event) {
		Lang.COLOR_EDITOR.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), event::reopen, newColor -> {
			color = newColor;
			event.refreshItemReopen();
		}, ColorParser.PARSER).start();
	}

	private void cancelClick(LayoutedClickEvent event) {
		end.accept(null);
	}
	
	private void doneClick(LayoutedClickEvent event) {
		end.accept(new ParticleEffect(particle, shape, color));
	}
	
}
