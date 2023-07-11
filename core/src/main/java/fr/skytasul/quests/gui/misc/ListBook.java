package fr.skytasul.quests.gui.misc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.options.OptionRequirements;
import fr.skytasul.quests.options.OptionStarterNPC;

public class ListBook{

	public static void openQuestBook(Player p){
		ItemStack is = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta im = (BookMeta) is.getItemMeta();
		
		im.setTitle("Quests list");
		im.setAuthor("BeautyQuests");
		
		QuestsAPI.getAPI().getQuestsManager().getQuests().stream().sorted().forEach(qu -> {
			StringBuilder stb = new StringBuilder(formatLine(Lang.BOOK_NAME.toString(), qu.getName())
					+ formatLine("ID", qu.getId() + "")
					+ ((qu.hasOption(OptionStarterNPC.class)) ? formatLine(Lang.BOOK_STARTER.toString(),
							qu.getOption(OptionStarterNPC.class).getValue().getNpc().getName()) : "")
					//+ formatLine(Lang.BOOK_REWARDS.toString(), qu.getRewards().exp + " XP §3" + Lang.And.toString() + " §1" + qu.getRewards().itemsSize() + " " + Lang.Item.toString())
					+ formatLine(Lang.BOOK_SEVERAL.toString(), (qu.isRepeatable()) ? Lang.Yes.toString() : Lang.No.toString())
					/*+ formatLine(Lang.BOOK_LVL.toString(), "" + qu.lvlRequired)
					+ ((qu.hasQuestRequirement()) ? formatLine(Lang.BOOK_QUEST_REQUIREMENT.toString(), qu.getQuestRequired().getName()) : "")*/
					+ formatLine(Lang.BOOK_REQUIREMENTS.toString(), qu.getOptionValueOrDef(OptionRequirements.class).size() + "")
					+ "\n"
					+ formatLine(Lang.BOOK_STAGES.toString(), "")
					+ qu.getBranchesManager().getBranches().stream().mapToInt(branch -> branch.getRegularStages().size())
							.sum()
					+ " stages in " + qu.getBranchesManager().getBranches().size() + " branches");
			im.addPage(stb.toString());
		});
		if (QuestsAPI.getAPI().getQuestsManager().getQuests().isEmpty()) {
			im.addPage(Lang.BOOK_NOQUEST.toString());
		}
		
		is.setItemMeta(im);
		Utils.openBook(p, is);
	}
	
	private static String formatLine(String title, String object){
		return ChatColor.DARK_GREEN + ChatColor.BOLD.toString() + title + " :§r " + ChatColor.DARK_BLUE + object + "§r\n";
	}

}
