package fr.skytasul.quests.rewards;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreationRunnables;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.RewardsGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.compatibility.SkillAPI;

public class XPReward extends AbstractReward {

	public int exp = 0;

	public XPReward() {
		super("expReward");
	}

	public XPReward(int exp) {
		this();
		this.exp = exp;
	}

	public String give(Player p) {
		if (Dependencies.skapi && QuestsConfiguration.xpOverridedSkillAPI()) {
			SkillAPI.giveExp(p, exp);
		}else p.giveExp(exp);
		return exp + " " + Lang.Exp.toString();
	}

	protected void save(Map<String, Object> datas) {
		datas.put("xp", exp);
	}

	protected void load(Map<String, Object> savedDatas) {
		exp = (int) savedDatas.get("xp");
	}

	public static class Creator implements RewardCreationRunnables {

		public void itemClick(Player p, Map<String, Object> datas, RewardsGUI gui, ItemStack clicked) {
			String last = "" + (datas.containsKey("xp") ? datas.get("xp") : 0);
			Utils.sendMessage(p, Lang.XP_GAIN.toString(), last);
			Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
				Utils.sendMessage(p, Lang.XP_EDITED.toString(), last, obj);
				datas.put("xp", (int) obj);
				gui.reopen(p, false);
				ItemUtils.lore(clicked, obj + " " + Lang.Exp.toString());
			}, new NumberParser(Integer.class, true)));
		}

		public void edit(Map<String, Object> datas, AbstractReward reward, ItemStack is) {
			XPReward rew = (XPReward) reward;
			datas.put("xp", rew.exp);
			ItemUtils.lore(is, rew.exp + " " + Lang.Exp.toString());
		}

		public AbstractReward finish(Map<String, Object> datas) {
			return new XPReward((int) datas.get("xp"));
		}

	}

}
