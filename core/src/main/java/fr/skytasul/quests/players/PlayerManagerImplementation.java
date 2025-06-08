package fr.skytasul.quests.players;

import fr.skytasul.quests.api.data.DataSavingException;
import fr.skytasul.quests.api.players.PlayerManager;
import fr.skytasul.quests.api.players.PlayerQuester;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterData;
import fr.skytasul.quests.api.questers.events.QuesterJoinEvent;
import fr.skytasul.quests.api.questers.events.QuesterLeaveEvent;
import fr.skytasul.quests.api.utils.logger.LoggerExpanded;
import fr.skytasul.quests.questers.QuesterManagerImplementation;
import fr.skytasul.quests.questers.data.QuesterDataManager.QuesterFetchRequest;
import fr.skytasul.quests.questers.data.QuesterDataManager.QuesterFetchResult;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.QuestUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.*;

public class PlayerManagerImplementation implements PlayerManager, Listener {

	private static final LoggerExpanded LOGGER = LoggerExpanded.get("BeautyQuests.PlayerManager");
	public static final @NotNull Key KEY = Key.key("beautyquests", "players");

	private final @NotNull Map<@NotNull Player, @NotNull AbstractPlayerQuesterImplementation> cachedQuesters =
			new HashMap<>();

	private @NotNull QuesterManagerImplementation questerManager;

	private boolean playersLoaded = false;

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
		if (questerManager.getPlugin().getNpcManager().isNPC(p))
			return null;
		if (!p.isOnline()) {
			LOGGER
					.severe("Trying to fetch the account of an offline player (" + p.getName() + ")");
			LOGGER.debug("(via " + DebugUtils.stackTraces(2, 5) + ")");
		}

		return cachedQuesters.get(p);
	}

	protected @NotNull Optional<String> getIdentifier(@NotNull OfflinePlayer p) {
		return Optional.of(p.getUniqueId().toString());
	}

	protected AbstractPlayerQuesterImplementation createQuester(@NotNull String identifier,
			@NotNull QuesterData dataHandler) {
		return new PlayerQuesterImplementation(this, dataHandler, UUID.fromString(identifier));
	}

	protected void load(@NotNull Player player) {
		String identifier = getIdentifier(player).orElseThrow();

		LOGGER.debug("Loading quester for {} (identifier: {})", player.getName(), identifier);

		questerManager.getDataManager().loadQuester(new QuesterFetchRequest(KEY, identifier, true, true))
				.whenComplete(LOGGER.logError(result -> {
					if (!result.type().isSuccess()) {
						LOGGER.severe("Failed to load {}'s quester instance", player.getName());
						return;
					}
					boolean isCreation = result.type() == QuesterFetchResult.Type.SUCCESS_CREATED;
					if (isCreation)
						LOGGER.debug("Created data for quester {0}", identifier);

					var quester = createQuester(identifier, result.dataHandler());

					cachedQuesters.put(player, quester);

					QuestUtils.runOrSync(() -> {
						if (player.isOnline()) {
							Bukkit.getPluginManager().callEvent(new QuesterJoinEvent(quester, player, isCreation));
						} else {
							LOGGER.warning("{} has left the server while loading its data.", player.getName());

							if (isCreation) {
								// TODO remove quester
							}
						}
					});
				}, "Failed to load quester data", questerManager.getPlugin().getAudiences().player(player)));
	}

	protected void unload(@NotNull Player player) {
		LOGGER.debug("Unloading quester for {}", player.getName());

		var quester = cachedQuesters.get(player);
		if (quester == null)
			throw new IllegalArgumentException("Player does not have quester loaded");

		Bukkit.getPluginManager().callEvent(new QuesterLeaveEvent(quester, player));
		try {
			quester.save();
		} catch (DataSavingException ex) {
			LOGGER.severe("Failed to save {}'s data", ex, quester.getDetailedName());
		}
		quester.unload();
		cachedQuesters.remove(player);
	}

	public void loadOnlinePlayers() {
		if (playersLoaded)
			throw new IllegalStateException("Online players have already been loaded");

		for (var player : Bukkit.getOnlinePlayers()) {
			load(player);
		}
		Bukkit.getPluginManager().registerEvents(this, questerManager.getPlugin());
		playersLoaded = true;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e) {
		load(e.getPlayer());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		unload(e.getPlayer());
	}

}
