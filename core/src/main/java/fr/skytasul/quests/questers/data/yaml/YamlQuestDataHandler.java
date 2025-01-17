package fr.skytasul.quests.questers.data.yaml;

import fr.skytasul.quests.questers.QuesterQuestDataImplementation;
import fr.skytasul.quests.questers.data.QuesterQuestDataHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

public class YamlQuestDataHandler implements QuesterQuestDataHandler {

	private ConfigurationSection config = new YamlConfiguration();

	public YamlQuestDataHandler(ConfigurationSection config) {
		this.config = config;
	}

	@Override
	public void load(@NotNull QuesterQuestDataImplementation questData) {
		questData.setTimesFinished(config.getInt("timesFinished"));
		questData.setTimer(config.getLong("timer"));
		questData.setBranch(config.getInt("currentBranch"));
		questData.setStage(config.getInt("currentStage"));
		questData.setQuestFlow(config.getString("questFlow"));
	}

	@Override
	public CompletableFuture<Void> remove() {
		return null;
	}

	@Override
	public void setTimesFinished(int times) {
		config.set("timesFinished", times);
	}

	@Override
	public void setTimer(long timer) {
		config.set("timer", timer);
	}

	@Override
	public void setBranch(int branch) {
		config.set("currentBranch", branch);
	}

	@Override
	public void setStage(int stage) {
		config.set("currentStage", stage);
	}

	@Override
	public void setAdditionalData(@NotNull String key, @Nullable Object value) {
		config.set("datas." + key, value);
	}

	@Override
	public void setQuestFlow(@NotNull String flow) {
		config.set("questFlow", flow);
	}

}
