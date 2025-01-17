package fr.skytasul.quests.players;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.accounts.PlayerAccountJoinEvent;
import fr.skytasul.quests.api.events.accounts.PlayerAccountLeaveEvent;
import fr.skytasul.quests.api.players.PlayerQuester;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.questers.QuesterManagerImplementation;
import fr.skytasul.quests.questers.data.DataSavingException;
import fr.skytasul.quests.questers.data.QuesterDataHandler;
import fr.skytasul.quests.questers.data.QuesterDataManager.QuesterFetchRequest;
import fr.skytasul.quests.questers.data.QuesterDataManager.QuesterFetchResult;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.QuestUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.*;

public class PlayerManagerImplementation implements PlayersManager {

	private static final @NotNull Key KEY = Key.key("BeautyQuests", "players");

	private final @NotNull Map<@NotNull Player, @NotNull AbstractPlayerQuesterImplementation> cachedQuesters =
			new HashMap<>();

	private @NotNull QuesterManagerImplementation questerManager;

	public PlayerManagerImplementation(@NotNull QuesterManagerImplementation questerManager) {
		this.questerManager = questerManager;
	}

	@Override
	public @NotNull Key key() {
		return KEY;
	}

	@Override
	public @NotNull @UnmodifiableView Collection<? extends PlayerQuester> getLoadedQuesters() {
		return cachedQuesters.values();
	}

	// @Override
	public @NotNull Optional<Quester> getQuester(@NotNull String identifier) {
		var uuid = UUID.fromString(identifier);
		var player = Bukkit.getOfflinePlayer(uuid);
		if (player.isOnline())
			return Optional.ofNullable(cachedQuesters.get(player.getPlayer()));
		// manage offline player
		return Optional.empty();
	}

	@Override
	public @UnknownNullability PlayerQuester getQuester(@NotNull Player p) {
		if (BeautyQuests.getInstance().getNpcManager().isNPC(p))
			return null;
		if (!p.isOnline()) {
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.severe("Trying to fetch the account of an offline player (" + p.getName() + ")");
			QuestsPlugin.getPlugin().getLoggerExpanded().debug("(via " + DebugUtils.stackTraces(2, 5) + ")");
		}

		return cachedQuesters.get(p);
	}

	protected @NotNull Optional<String> getIdentifier(@NotNull OfflinePlayer p) {
		return Optional.of(p.getUniqueId().toString());
	}

	protected AbstractPlayerQuesterImplementation createQuester(@NotNull String identifier,
			@NotNull QuesterDataHandler dataHandler) {
		return new PlayerQuesterImplementation(this, dataHandler, UUID.fromString(identifier));
	}

	protected void load(@NotNull Player player) {
		String identifier = getIdentifier(player).orElseThrow();

		questerManager.getDataManager().loadQuester(new QuesterFetchRequest(KEY, identifier, true))
				.whenComplete(QuestsPlugin.getPlugin().getLoggerExpanded().logError(result -> {
					if (!result.type().isSuccess()) {
						QuestsPlugin.getPlugin().getLoggerExpanded().severe("Failed to load {}'s quester instance",
								player.getName());
						return;
					}
					boolean isCreation = result.type() == QuesterFetchResult.Type.SUCCESS_CREATED;

					var quester = createQuester(identifier, result.dataHandler());

					if (!isCreation)
						result.dataHandler().load(quester);

					cachedQuesters.put(player, quester);

					QuestUtils.runOrSync(() -> {
						if (player.isOnline()) {
							Bukkit.getPluginManager().callEvent(new PlayerAccountJoinEvent(quester, player, isCreation));
						} else {
							QuestsPlugin.getPlugin().getLoggerExpanded()
									.warningArgs("{} has left the server while loading its data.", player.getName());

							if (isCreation) {
								// TODO remove quester
							}
						}
					});
				}, "Failed to load quester data", QuestsPlugin.getPlugin().getAudiences().player(player)));
	}

	protected void unload(@NotNull Player player) {
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Unloading quester for {}", player.getName());

		var quester = cachedQuesters.get(player);
		if (quester == null)
			throw new IllegalArgumentException("Player does not have quester loaded");

		Bukkit.getPluginManager().callEvent(new PlayerAccountLeaveEvent(quester, player));
		try {
			quester.save();
		} catch (DataSavingException ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Failed to save {}'s data", ex, quester.getDetailedName());
		}
		quester.unload();
		cachedQuesters.remove(player);
	}

	public void loadOnlinePlayers() {
		for (var player : Bukkit.getOnlinePlayers()) {
			load(player);
		}
	}

}
