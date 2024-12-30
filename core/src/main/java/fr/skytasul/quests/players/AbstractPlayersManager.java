package fr.skytasul.quests.players;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfigurationImplementation;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.events.accounts.PlayerAccountJoinEvent;
import fr.skytasul.quests.api.events.accounts.PlayerAccountLeaveEvent;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.players.accounts.AbstractAccount;
import fr.skytasul.quests.players.accounts.UUIDAccount;
import fr.skytasul.quests.utils.DebugUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractPlayersManager<A extends PlayerAccountImplementation> implements PlayersManager {

	protected final @NotNull Map<Player, A> cachedAccounts = new HashMap<>();
	protected final @NotNull Set<@NotNull SavableData<?>> accountDatas = new HashSet<>();
	private boolean loaded = false;

	public abstract void load(@NotNull AccountFetchRequest<A> request);

	protected abstract @NotNull CompletableFuture<Void> removeAccount(@NotNull A acc);

	public abstract @NotNull CompletableFuture<Integer> removeQuestDatas(@NotNull Quest quest);

	public abstract @NotNull CompletableFuture<Integer> removePoolDatas(@NotNull QuestPool pool);

	public void load() throws DataException {
		if (loaded) throw new IllegalStateException("Already loaded");
		loaded = true;
	}

	public boolean isLoaded() {
		return loaded;
	}

	@Override
	public abstract void save();

	@Override
	public void addAccountData(@NotNull SavableData<?> data) {
		if (loaded)
			throw new IllegalStateException("Cannot add account data after players manager has been loaded");
		if (PlayerAccountImplementation.FORBIDDEN_DATA_ID.contains(data.getId()))
			throw new IllegalArgumentException("Forbidden account data id " + data.getId());
		if (accountDatas.stream().anyMatch(x -> x.getId().equals(data.getId())))
			throw new IllegalArgumentException("Another account data already exists with the id " + data.getId());
		if (data.getDataType().isPrimitive())
			throw new IllegalArgumentException("Primitive account data types are not supported");
		accountDatas.add(data);
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Registered account data " + data.getId());
	}

	@Override
	public @NotNull Collection<@NotNull SavableData<?>> getAccountDatas() {
		return accountDatas;
	}

	protected @NotNull Optional<String> getIdentifier(@NotNull OfflinePlayer p) {
		if (QuestsConfigurationImplementation.getConfiguration().hookAccounts()) {
			if (!p.isOnline()) {
				QuestsPlugin.getPlugin().getLogger()
						.warning("Cannot fetch player identifier of an offline player with AccountsHook");
				return Optional.empty();
			}
			return Optional.of("Hooked|" + BqAccountsHook.getPlayerCurrentIdentifier(p.getPlayer()));
		} else {
			return Optional.of(p.getUniqueId().toString());
		}
	}

	protected @NotNull Optional<? extends AbstractAccount> newAbstractAccount(@NotNull OfflinePlayer player) {
		if (QuestsConfigurationImplementation.getConfiguration().hookAccounts()) {
			if (!player.isOnline()) {
				QuestsPlugin.getPlugin().getLogger()
						.warning("Trying to fetch the account of an offline player with AccountsHook");
				return Optional.empty();
			}
			return Optional.of(BqAccountsHook.getPlayerAccount(player.getPlayer()));
		} else {
			return Optional.of(new UUIDAccount(player.getUniqueId()));
		}
	}

	protected @NotNull AbstractAccount newAbstractAccount(@NotNull Player player) {
		// same as above, but removes the Optional because we are sure to have an account
		if (QuestsConfigurationImplementation.getConfiguration().hookAccounts()) {
			return BqAccountsHook.getPlayerAccount(player);
		} else {
			return new UUIDAccount(player.getUniqueId());
		}
	}

	protected @NotNull Optional<? extends AbstractAccount> newAbstractAccount(@NotNull String identifier) {
		if (QuestsConfigurationImplementation.getConfiguration().hookAccounts()) {
			String internalIdentifier = identifier.substring("Hooked|".length());
			return BqAccountsHook.getAccountFromIdentifier(internalIdentifier);
		} else {
			return Optional.of(new UUIDAccount(UUID.fromString(identifier)));
		}
	}

	public synchronized void loadPlayer(@NotNull Player p) {
		cachedPlayerNames.put(p.getUniqueId(), p.getName());

		long time = System.currentTimeMillis();
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Loading player " + p.getName() + "...");
		cachedAccounts.remove(p);
		Bukkit.getScheduler().runTaskAsynchronously(BeautyQuests.getInstance(), () -> {
			for (int i = 1; i >= 0; i--) {
				try {
					if (!tryLoad(p, time))
						return;
				} catch (Exception ex) {
					QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error ocurred while trying to load datas of " + p.getName() + ".", ex);
				}
				if (i > 0)
					QuestsPlugin.getPlugin().getLoggerExpanded().severe("Doing " + i + " more attempt.");
			}
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Datas of " + p.getName() + " have failed to load. This may cause MANY issues.");
		});
	}

	private boolean tryLoad(@NotNull Player p, long time) {
		if (!p.isOnline()) {
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.warning("Player " + p.getName() + " has quit the server while loading its datas. This may be a bug.");
			return false;
		}

		AccountFetchRequest<A> request = new AccountFetchRequest<>(p, time, true, true);
		load(request);

		if (!request.isFinished() || request.getAccount() == null) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("The account of " + p.getName() + " has not been properly loaded.");
			return true;
		}

		if (!p.isOnline()) {
			if (request.isAccountCreated()) {
				QuestsPlugin.getPlugin().getLoggerExpanded().debug(
						"New account registered for " + p.getName() + "... but deleted as player left before loading.");
				removeAccount(request.getAccount()).whenComplete(
						QuestsPlugin.getPlugin().getLoggerExpanded().logError("An error occurred while removing newly created account"));
			}
			return false;
		}

		if (request.isAccountCreated())
			QuestsPlugin.getPlugin().getLoggerExpanded().debug(
					"New account registered for " + p.getName() + " (" + request.getAccount().abstractAcc.getIdentifier()
							+ "), index " + request.getAccount().index + " via " + DebugUtils.stackTraces(2, 4));

		if (!request.getAccount().getOfflinePlayer().equals(p)) {
			QuestsPlugin.getPlugin().getLogger()
					.severe("UUID mismatch between player " + p.getName() + " (" + p.getUniqueId() + ") and loaded account "
							+ request.getAccount().debugName());
			return false;
		}

		cachedAccounts.put(p, request.getAccount());

		String loadMessage =
				"Completed load of " + p.getName() + " (" + request.getAccount().debugName() + ") datas within "
						+ (System.currentTimeMillis() - time) + " ms (" + request.getAccount().getQuestsDatas().size()
						+ " quests, " + request.getAccount().getPoolDatas().size() + " pools)";

		if (request.getLoadedFrom() != null)
			loadMessage += " | Loaded from " + request.getLoadedFrom();

		QuestsPlugin.getPlugin().getLoggerExpanded().debug(loadMessage);

		Bukkit.getScheduler().runTask(BeautyQuests.getInstance(), () -> {

			if (p.isOnline()) {
				Bukkit.getPluginManager()
						.callEvent(new PlayerAccountJoinEvent(request.getAccount(), p, request.isAccountCreated()));
			} else {
				QuestsPlugin.getPlugin().getLoggerExpanded().warning(
						"Player " + p.getName() + " has quit the server while loading its datas. This may be a bug.");

				if (request.isAccountCreated())
					removeAccount(request.getAccount()).whenComplete(
							QuestsPlugin.getPlugin().getLoggerExpanded().logError("An error occurred while removing newly created account"));
			}
		});
		return false;
	}

	public synchronized void unloadPlayer(@NotNull Player p) {
		A acc = cachedAccounts.get(p);
		if (acc == null) return;
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Unloading player " + p.getName() + "... (" + acc.getQuestsDatas().size() + " quests, " + acc.getPoolDatas().size() + " pools)");
		Bukkit.getPluginManager().callEvent(new PlayerAccountLeaveEvent(acc, p));
		acc.unload();
		cachedAccounts.remove(p);
	}

	@Override
	public @UnknownNullability PlayerAccountImplementation getAccount(@NotNull Player p) {
		if (BeautyQuests.getInstance().getNpcManager().isNPC(p))
			return null;
		if (!p.isOnline()) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Trying to fetch the account of an offline player (" + p.getName() + ")");
			QuestsPlugin.getPlugin().getLoggerExpanded().debug("(via " + DebugUtils.stackTraces(2, 5) + ")");
		}

		return cachedAccounts.get(p);
	}

	private static Map<UUID, String> cachedPlayerNames = new HashMap<>();
	private static Gson gson = new Gson();
	private static long lastOnlineFailure = 0;

	public static synchronized @Nullable String getPlayerName(@NotNull UUID uuid) {
		if (cachedPlayerNames.containsKey(uuid))
			return cachedPlayerNames.get(uuid);

		String name = Bukkit.getOfflinePlayer(uuid).getName();
		if (name == null && Bukkit.getOnlineMode()) {
			try {
				if (System.currentTimeMillis() - lastOnlineFailure < 30_000) {
					QuestsPlugin.getPlugin().getLoggerExpanded().debug("Trying to fetch a name from an UUID but it failed within 30 seconds.");
					return null;
				}

				HttpURLConnection connection = (HttpURLConnection) new URL(
						"https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString()).openConnection();
				connection.setReadTimeout(5000);

				JsonObject profile = gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())),
						JsonObject.class);
				JsonElement nameElement = profile.get("name");
				if (nameElement == null) {
					name = null;
					QuestsPlugin.getPlugin().getLoggerExpanded().debug("Cannot find name for UUID " + uuid.toString());
				} else {
					name = nameElement.getAsString();
				}
			} catch (Exception e) {
				QuestsPlugin.getPlugin().getLoggerExpanded().warning("Cannot connect to the mojang servers. UUIDs cannot be parsed.");
				lastOnlineFailure = System.currentTimeMillis();
				return null;
			}
		}

		cachedPlayerNames.put(uuid, name);
		return name;
	}

	public static class AccountFetchRequest<A extends PlayerAccountImplementation> {
		private final OfflinePlayer player;
		private final long joinTimestamp;
		private final boolean allowCreation;
		private final boolean shouldCache;

		private boolean finished = false;
		private boolean created;
		private A account;
		private String loadedFrom;

		public AccountFetchRequest(OfflinePlayer player, long joinTimestamp, boolean allowCreation, boolean shouldCache) {
			this.player = player;
			this.joinTimestamp = joinTimestamp;
			this.allowCreation = allowCreation;
			this.shouldCache = shouldCache;

			if (allowCreation && !player.isOnline())
				throw new IllegalArgumentException("Cannot create an account for an offline player.");
		}

		public OfflinePlayer getOfflinePlayer() {
			return player;
		}

		public Player getOnlinePlayer() {
			if (player.isOnline())
				return player.getPlayer();
			throw new IllegalStateException("The player " + player.getName() + " is offline.");
		}

		public long getJoinTimestamp() {
			return joinTimestamp;
		}

		/**
		 * @return <code>true</code> if an account must be created when no account can be loaded
		 */
		public boolean mustCreateMissing() {
			return allowCreation;
		}

		/**
		 * @return <code>true</code> if the loaded account should be cached internally (usually because this
		 *         account will get associated with an online player)
		 */
		public boolean shouldCache() {
			return shouldCache;
		}

		public String getDebugPlayerName() {
			String name = player.getName();
			if (name == null)
				name = player.getUniqueId().toString();
			return name;
		}

		/**
		 * This method must be called when the request results in a successfully loaded account.
		 *
		 * @param account account that has been loaded
		 * @param from source of the saved account
		 */
		public void loaded(A account, String from) {
			ensureAvailable();
			this.account = account;
			this.loadedFrom = from;
			this.created = false;
		}

		/**
		 * This method must be called when the request results in the creation of a new account.
		 * <p>
		 * It <strong>cannot</strong> be called when the {@link AccountFetchRequest#mustCreateMissing()}
		 * method returns false.
		 *
		 * @param account account that has been created
		 */
		public void created(A account) {
			if (!mustCreateMissing())
				throw new IllegalStateException(
						"This method cannot be called as this request does not allow account creation");
			ensureAvailable();
			this.account = account;
			this.created = true;
		}

		/**
		 * This method must be called when the request cannot load any account associated with the player
		 * and the {@link AccountFetchRequest#mustCreateMissing()} returns false.
		 */
		public void notLoaded() {
			if (mustCreateMissing())
				throw new IllegalStateException(
						"This method cannot be called as this request requires account creation if no account can be loaded");
			ensureAvailable();
		}

		private void ensureAvailable() {
			if (finished)
				throw new IllegalStateException("This request has already been completed");
			this.finished = true;
		}

		public boolean isFinished() {
			return finished;
		}

		public A getAccount() {
			return account;
		}

		public boolean isAccountCreated() {
			return created;
		}

		public String getLoadedFrom() {
			return loadedFrom;
		}

	}

}
