package fr.skytasul.quests.rewards;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreationRunnables;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.RewardsGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class MessageReward extends AbstractReward {

	public String text;
	
	public MessageReward(){
		super("textReward");
	}
	
	public MessageReward(String text){
		super("textReward");
		this.text = text;
	}

	public String give(Player p){
		Utils.sendOffMessage(p, text);
		return null;
	}

	
	protected void save(Map<String, Object> datas){
		datas.put("text", text);
	}

	protected void load(Map<String, Object> savedDatas){
		text = (String) savedDatas.get("text");
	}

	public static class Creator implements RewardCreationRunnables<MessageReward> {

		public void itemClick(Player p, Map<String, Object> datas, RewardsGUI gui, ItemStack clicked) {
			Lang.END_MESSAGE.send(p);
			TextEditor wt = new TextEditor(p, (obj) -> {
				datas.put("text", obj);
				gui.reopen(p, false);
				ItemUtils.lore(clicked, (String) obj);
			});
			wt.cancel = () -> {
				if (!datas.containsKey("text")) gui.removeReward(datas);
				gui.reopen(p, false);
			};
			Editor.enterOrLeave(p, wt);
		}

		public void edit(Map<String, Object> datas, MessageReward reward, ItemStack is) {
			datas.put("text", reward.text);
			ItemUtils.lore(is, reward.text);
		}

		public MessageReward finish(Map<String, Object> datas) {
			return new MessageReward((String) datas.get("text"));
		}

	}

}
