package fr.skytasul.quests.rewards;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
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
	public String[] getLore() {
		return new String[] { Lang.optionValue.format(text), "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		Lang.END_MESSAGE.send(p);
		new TextEditor<String>(p, () -> {
			if (text == null) gui.remove(this);
			gui.reopen();
		}, obj -> {
			this.text = obj;
			ItemUtils.lore(clicked, getLore());
			gui.reopen();
		}).enterOrLeave(p);
	}
	
	protected void save(Map<String, Object> datas) {
		datas.put("text", text);
	}
	
	protected void load(Map<String, Object> savedDatas) {
		text = (String) savedDatas.get("text");
	}
	
}
