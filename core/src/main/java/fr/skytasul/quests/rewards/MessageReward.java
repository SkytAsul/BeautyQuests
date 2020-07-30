package fr.skytasul.quests.rewards;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.rewards.AbstractReward;
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

	@Override
	public AbstractReward clone() {
		return new MessageReward(text);
	}
	
	@Override
	protected String[] getLore() {
		return new String[] { Lang.optionValue.format(text), "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, RewardsGUI gui, ItemStack clicked) {
		Lang.END_MESSAGE.send(p);
		TextEditor wt = new TextEditor(p, (obj) -> {
			this.text = (String) obj;
			ItemUtils.lore(clicked, getLore());
			gui.reopen(p);
		});
		wt.cancel = () -> gui.reopen(p);
		Editor.enterOrLeave(p, wt);
	}
	
	protected void save(Map<String, Object> datas) {
		datas.put("text", text);
	}
	
	protected void load(Map<String, Object> savedDatas) {
		text = (String) savedDatas.get("text");
	}
	
}
