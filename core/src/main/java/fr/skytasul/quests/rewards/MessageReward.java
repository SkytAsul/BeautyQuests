package fr.skytasul.quests.rewards;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.editors.TextEditor;
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

	@Override
	public List<String> give(Player p) {
		Utils.sendOffMessage(p, text);
		return null;
	}

	@Override
	public AbstractReward clone() {
		return new MessageReward(text);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { Lang.optionValue.format(text), "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Lang.WRITE_MESSAGE.send(event.getPlayer());
		new TextEditor<String>(event.getPlayer(), () -> {
			if (text == null) event.getGUI().remove(this);
			event.reopenGUI();
		}, obj -> {
			this.text = obj;
			event.updateItemLore(getLore());
			event.reopenGUI();
		}).enter();
	}
	
	@Override
	protected void save(Map<String, Object> datas) {
		datas.put("text", text);
	}
	
	@Override
	protected void load(Map<String, Object> savedDatas) {
		text = (String) savedDatas.get("text");
	}
	
}
