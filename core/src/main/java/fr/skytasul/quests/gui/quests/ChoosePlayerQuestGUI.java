package fr.skytasul.quests.gui.quests;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.PlayerListCategory;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.players.PlayerAccountImplementation;

public class ChoosePlayerQuestGUI extends PagedGUI<Quest> {

	private final @NotNull Player targetPlayer;
	private final @NotNull PlayerAccount acc;

	public ChoosePlayerQuestGUI(@NotNull Collection<Quest> quests, @NotNull Player player) {
		super(Lang.INVENTORY_CHOOSE.toString(), DyeColor.MAGENTA, quests);

		this.targetPlayer = player;
		this.acc = PlayersManager.getPlayerAccount(player);

		Collections.sort(super.objects);

		setValidate(__ -> {
			new PlayerListGUI((PlayerAccountImplementation) acc).open(player);
		}, ItemUtils.item(XMaterial.BOOKSHELF, Lang.questMenu.toString(),
				QuestOption.formatDescription(Lang.questMenuLore.toString())));
	}

	@Override
	public @NotNull ItemStack getItemStack(@NotNull Quest quest) {
		List<String> lore = new QuestDescriptionContext(QuestsConfiguration.getConfig().getQuestDescriptionConfig(), quest,
				acc, PlayerListCategory.NOT_STARTED, DescriptionSource.MENU).formatDescription();
		return ItemUtils.nameAndLore(quest.getQuestItem(), Lang.formatNormal.format(quest), lore);
	}

	@Override
	public void click(@NotNull Quest existing, @NotNull ItemStack item, @NotNull ClickType clickType) {
		close();
		existing.doNpcClick(targetPlayer);
	}

	@Override
	public @NotNull CloseBehavior onClose(@NotNull Player player) {
		return StandardCloseBehavior.REMOVE;
	}

}
