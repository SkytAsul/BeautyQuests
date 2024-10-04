package fr.skytasul.quests.gui.particles;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.ColorParser;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.gui.layout.LayoutedButton;
import fr.skytasul.quests.api.gui.layout.LayoutedClickEvent;
import fr.skytasul.quests.api.gui.layout.LayoutedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.ParticleEffect.ParticleShape;
import fr.skytasul.quests.utils.compatibility.Post1_13;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ParticleEffectGUI extends LayoutedGUI.LayoutedRowsGUI {

	static final List<Particle> PARTICLES = Arrays.stream(Particle.values()).filter(particle -> {
		if (particle.getDataType() == Void.class) return true;
		if (MinecraftVersion.MAJOR >= 13) return particle.getDataType() == Post1_13.getDustOptionClass();
		return false;
	}).collect(Collectors.toList());

	private final @NotNull Consumer<ParticleEffect> end;

	private @NotNull Particle particle;
	private @NotNull ParticleShape shape;
	private @NotNull Color color;

	public ParticleEffectGUI(@NotNull Consumer<ParticleEffect> end) {
		this(end, Particle.FLAME, ParticleShape.POINT, Color.AQUA);
	}

	public ParticleEffectGUI(@NotNull Consumer<ParticleEffect> end, @NotNull ParticleEffect effect) {
		this(end, effect.getParticle(), effect.getShape(), effect.getColor());
	}

	public ParticleEffectGUI(@NotNull Consumer<ParticleEffect> end, @NotNull Particle particle, @NotNull ParticleShape shape,
			@Nullable Color color) {
		super(Lang.INVENTORY_PARTICLE_EFFECT.toString(), new HashMap<>(), new DelayCloseBehavior(() -> end.accept(null)), 1);
		this.end = end;
		this.particle = particle;
		this.shape = shape;
		this.color = color == null ? Color.AQUA : color;

		initButtons();
	}

	private void initButtons() {
		buttons.put(1, LayoutedButton.create(XMaterial.FIREWORK_STAR, Lang.particle_shape.toString(),
				() -> Arrays.asList(QuestOption.formatNullableValue(shape)), this::shapeClick));
		buttons.put(3, LayoutedButton.create(XMaterial.PAPER, Lang.particle_type.toString(),
				() -> Arrays.asList(QuestOption.formatNullableValue(particle)), this::particleClick));
		buttons.put(4, new LayoutedButton.ItemButton() {
			@Override
			public void click(@NotNull LayoutedClickEvent event) {
				colorClick(event);
			}

			@Override
			public @Nullable ItemStack getItem() {
				return ItemUtils.item(XMaterial.MAGENTA_DYE, Lang.particle_color.toString(), QuestOption
						.formatNullableValue("RGB: " + color.getRed() + " " + color.getGreen() + " " + color.getBlue()));
			}

			@Override
			public boolean isValid() {
				return ParticleEffect.canHaveColor(particle);
			}
		});
		buttons.put(7, LayoutedButton.create(QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getCancel(), this::cancelClick));
		buttons.put(8, LayoutedButton.create(QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getDone(), this::doneClick));
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
