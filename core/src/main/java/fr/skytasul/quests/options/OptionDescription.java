package fr.skytasul.quests.options;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOptionString;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OptionDescription extends QuestOptionString implements QuestDescriptionProvider {

	private Cache<QuestDescriptionContext, List<String>> cachedDescription =
			CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

	@Override
	public void setValue(String value) {
		super.setValue(value);

		if (cachedDescription != null) // not in constructor
			cachedDescription.invalidateAll();
	}

	@Override
	public String getIndication() {
		return Lang.QUEST_DESCRIPTION.getValue();
	}

	@Override
	public XMaterial getItemMaterial() {
		return XMaterial.OAK_SIGN;
	}

	@Override
	public String getItemName() {
		return Lang.customDescription.toString();
	}

	@Override
	public String getItemDescription() {
		return Lang.customDescriptionLore.toString();
	}

	@Override
	public boolean isMultiline() {
		return true;
	}

	@Override
	public List<String> provideDescription(QuestDescriptionContext context) {
		List<String> description = cachedDescription.getIfPresent(context);
		if (description == null) {
			description = Arrays.asList("§7" + MessageUtils.finalFormat(getValue(), null,
					PlaceholdersContext.of(context.getQuester(), true, null)));
			cachedDescription.put(context, description);
		}
		return description;
	}

	@Override
	public String getDescriptionId() {
		return "description";
	}

	@Override
	public double getDescriptionPriority() {
		return 0;
	}

}
