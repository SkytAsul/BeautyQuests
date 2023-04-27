package fr.skytasul.quests.utils.compatibility.npcs;

import java.util.function.BiPredicate;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelPlugin;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.players.PlayerAccountImplementation;
import fr.skytasul.quests.structure.QuestImplementation;

public class BQSentinel {
	
	private BQSentinel() {}
	
	public static void initialize() {
		new BQSentinelIntegration();
		// we must use a nested class otherwise the JVM throws a NoClassDefFoundException
	}
	
	private static class BQSentinelIntegration extends SentinelIntegration {
		
		public BQSentinelIntegration() {
			SentinelPlugin.instance.registerIntegration(this);
		}
		
		@Override
		public String getTargetHelp() {
			return "quest_in:QUEST_ID, quest_finished:QUEST_ID";
		}
		
		@Override
		public String[] getTargetPrefixes() {
			return new String[] { "quest_in", "quest_finished" };
		}
		
		@Override
		public boolean isTarget(LivingEntity ent, String prefix, String value) {
			switch (prefix) {
			case "quest_in":
				return test(ent, value, QuestImplementation::hasStarted);
			case "quest_finished":
				return test(ent, value, QuestImplementation::hasFinished);
			}
			return false;
		}
		
		private boolean test(LivingEntity ent, String value, BiPredicate<QuestImplementation, PlayerAccountImplementation> test) {
			if (ent instanceof Player) {
				PlayerAccount acc = PlayersManager.getPlayerAccount((Player) ent);
				if (acc != null) {
					try {
						int questID = Integer.parseInt(value);
						Quest quest = QuestsAPI.getAPI().getQuestsManager().getQuest(questID);
						return quest != null && test.test(quest, acc);
					}catch (NumberFormatException ex) {}
				}
			}
			return false;
		}
	}
	
}
