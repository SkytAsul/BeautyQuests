package fr.skytasul.quests.api.requirements;

import java.util.function.Supplier;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.api.objects.QuestObjectLocation;

public class RequirementCreator extends QuestObjectCreator<AbstractRequirement> {
	
	public RequirementCreator(@NotNull String id, @NotNull Class<? extends AbstractRequirement> clazz, @NotNull ItemStack is,
			@NotNull Supplier<@NotNull AbstractRequirement> newObjectSupplier) {
		super(id, clazz, is, newObjectSupplier);
	}
	
	public RequirementCreator(@NotNull String id, @NotNull Class<? extends AbstractRequirement> clazz, @NotNull ItemStack is,
			@NotNull Supplier<@NotNull AbstractRequirement> newObjectSupplier, boolean multiple,
			@NotNull QuestObjectLocation @NotNull... allowedLocations) {
		super(id, clazz, is, newObjectSupplier, multiple, allowedLocations);
	}
	
}
