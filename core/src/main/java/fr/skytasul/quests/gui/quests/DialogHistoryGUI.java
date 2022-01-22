package fr.skytasul.quests.gui.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang.Validate;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.skytasul.quests.api.stages.Dialogable;
import fr.skytasul.quests.gui.quests.DialogHistoryGUI.WrappedDialogable;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.options.OptionStartDialog;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerQuestDatas;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.utils.ChatUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.Message;

public class DialogHistoryGUI extends PagedGUI<WrappedDialogable> {
	
	private final Runnable end;
	private final Player player;
	
	public DialogHistoryGUI(PlayerAccount acc, Quest quest, Runnable end) {
		super(quest.getName(), DyeColor.LIGHT_BLUE, Collections.emptyList(), x -> end.run(), null);
		this.end = end;
		
		Validate.isTrue(acc.hasQuestDatas(quest), "Player must have started the quest");
		Validate.isTrue(acc.isCurrent(), "Player must be online");
		
		player = acc.getPlayer();
		
		if (quest.hasOption(OptionStartDialog.class))
			objects.add(new WrappedDialogable(quest.getOption(OptionStartDialog.class)));
		
		getDialogableStream(acc.getQuestDatas(quest), quest)
			.map(WrappedDialogable::new)
			.forEach(objects::add);
	}
	
	@Override
	public ItemStack getItemStack(WrappedDialogable object) {
		return object.setMeta(XMaterial.WRITTEN_BOOK.parseItem());
	}
	
	@Override
	public void click(WrappedDialogable existing, ItemStack item, ClickType clickType) {
		boolean changed = false;
		if (clickType.isLeftClick()) {
			if (existing.page > 0) {
				existing.page--;
				changed = true;
				Utils.playPluginSound(p, "ENTITY_BAT_TAKEOFF", 0.4f, 1.5f);
			}
		}else if (clickType.isRightClick()) {
			if (existing.page + 1 < existing.pages.size()) {
				existing.page++;
				changed = true;
				Utils.playPluginSound(p, "ENTITY_BAT_TAKEOFF", 0.4f, 1.7f);
			}
		}
		
		if (changed) existing.setMeta(item);
	}
	
	@Override
	public CloseBehavior onClose(Player p, Inventory inv) {
		Utils.runSync(end);
		return CloseBehavior.NOTHING;
	}

	public static Stream<Dialogable> getDialogableStream(PlayerQuestDatas datas, Quest quest) {
		return Arrays.stream(datas.getQuestFlow().split(";"))
			.filter(x -> !x.isEmpty())
			.map(arg -> {
				String[] args = arg.split(":");
				int branchID = Integer.parseInt(args[0]);
				QuestBranch branch = quest.getBranchesManager().getBranch(branchID);
				if (branch == null) return null;
				if (args[1].startsWith("E")) {
					return branch.getEndingStage(Integer.parseInt(args[1].substring(1)));
				}else {
					return branch.getRegularStage(Integer.parseInt(args[1]));
				}
			})
			.filter(Dialogable.class::isInstance)
			.map(Dialogable.class::cast)
			.filter(Dialogable::hasDialog);
	}
	
	class WrappedDialogable {
		static final int MAX_LINES = 9;
		
		final Dialogable dialogable;
		final List<Page> pages;
		
		int page = 0;
		
		WrappedDialogable(Dialogable dialogable) {
			this.dialogable = dialogable;
			
			List<Message> messages = dialogable.getDialog().messages;
			List<List<String>> lines = new ArrayList<>(messages.size());
			for (int i = 0; i < messages.size(); i++) {
				Message msg = messages.get(i);
				String formatted = msg.formatMessage(player, dialogable.getDialog().getNPCName(dialogable.getNPC()), i, messages.size());
				lines.add(ChatUtils.wordWrap(formatted, 40, 100));
			}
			
			pages = new ArrayList<>();
			Page page = new Page();
			int messagesAdded = 0;
			for (int i = 0; i < lines.size(); i++) {
				List<String> msg = lines.get(i);
				boolean last = i + 1 == lines.size();
				if (last || page.lines.size() + msg.size() > MAX_LINES) {
					boolean added = false;
					if (last || page.lines.isEmpty()) {
						page.lines.addAll(msg);
						messagesAdded++;
						added = true;
					}
					page.header = "§7§l" + messagesAdded + "§8 / §7§l" + Lang.dialogLines.format(messages.size());
					page.lines.addLast("  " + (pages.isEmpty() ? "§8" : "§7") + "◀ " + Lang.ClickLeft + " §8/ " + (last ? "§8" : "§7") + Lang.ClickRight + " ▶");
					pages.add(page);
					page = new Page();
					if (added) continue;
				}
				messagesAdded++;
				page.lines.addAll(msg);
			}
		}
		
		public Page getCurrentPage() {
			return pages.get(page);
		}
		
		public ItemStack setMeta(ItemStack item) {
			ItemMeta meta = item.getItemMeta();
			meta.setLore(getCurrentPage().lines);
			meta.setDisplayName("§8" + objects.indexOf(this) + " (" + dialogable.getNPC().getName() + "§8) - " + getCurrentPage().header);
			meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			item.setItemMeta(meta);
			return item;
		}
	}
	
	class Page {
		LinkedList<String> lines = new LinkedList<>();
		String header;
	}
	
}
