package fr.skytasul.quests.api.requirements;

import java.util.function.Supplier;

import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.api.objects.QuestObjectLocation;

public class RequirementCreator extends QuestObjectCreator<AbstractRequirement> {
	
	public RequirementCreator(String id, Class<? extends AbstractRequirement> clazz, ItemStack is, Supplier<AbstractRequirement> newObjectSupplier) {
		super(id, clazz, is, newObjectSupplier);
	}
	
	public RequirementCreator(String id, Class<? extends AbstractRequirement> clazz, ItemStack is, Supplier<AbstractRequirement> newObjectSupplier, boolean multiple, QuestObjectLocation... allowedLocations) {
		super(id, clazz, is, newObjectSupplier, multiple, allowedLocations);
	}
	
}
