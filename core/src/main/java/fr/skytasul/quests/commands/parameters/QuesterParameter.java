package fr.skytasul.quests.commands.parameters;

import fr.skytasul.quests.api.players.PlayerManager;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterManager;
import fr.skytasul.quests.api.questers.QuesterProvider;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.EmptyEntitySelectorException;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QuesterParameter implements ParameterType<BukkitCommandActor, Quester> {

	protected static class SuggestionProviderImplementation
			implements @NotNull SuggestionProvider<@NotNull BukkitCommandActor> {
		private final @NotNull QuesterManager questerManager;
		private final @NotNull PlayerManager playerManager;

		public SuggestionProviderImplementation(@NotNull QuesterManager questerManager,
				@NotNull PlayerManager playerManager) {
			this.questerManager = questerManager;
			this.playerManager = playerManager;
		}

		@Override
		public @NotNull Collection<String> getSuggestions(@NotNull ExecutionContext<@NotNull BukkitCommandActor> context) {
			var list = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
			for (var questerProvider : questerManager.getQuesterProviders()) {
				if (questerProvider == playerManager)
					continue;
				for (var quester : questerProvider.getLoadedQuesters()) {
					list.add(questerProvider.key().asString() + "|" + quester.getIdentifier());
				}
			}
			return list;
		}
	}

	private static final Pattern FULL_IDENTIFIER_PATTERN = Pattern.compile("^([a-z0-9_.-]+:[a-z0-9/._-]+)\\|(.*)$");

	private final @NotNull QuesterManager questerManager;
	private final @NotNull PlayerManager playerManager;

	public QuesterParameter(@NotNull QuesterManager questerManager, @NotNull PlayerManager playerManager) {
		this.questerManager = questerManager;
		this.playerManager = playerManager;
	}

	@Override
	public Quester parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> context) {
		var string = input.readString();
		var player = Bukkit.getPlayer(string);
		if (player != null)
			return playerManager.getQuester(player);

		return parseQuester(string, questerManager).orElseThrow(() -> new EmptyEntitySelectorException(string));
	}

	@Override
	public @NotNull SuggestionProvider<@NotNull BukkitCommandActor> defaultSuggestions() {
		return new SuggestionProviderImplementation(questerManager, playerManager);
	}

	public static Optional<Quester> parseQuester(@NotNull String string, @NotNull QuesterManager questerManager) {
		var questerMatcher = FULL_IDENTIFIER_PATTERN.matcher(string);
		if (questerMatcher.matches()) {
			QuesterProvider provider;
			var providerKey = Key.key(questerMatcher.group(1));
			try {
				provider = questerManager.getQuesterProvider(providerKey);
			} catch (IllegalArgumentException ex) {
				throw new CommandErrorException("%s is not a known quester provider".formatted(providerKey));
			}

			String identifier = questerMatcher.group(2);
			for (var quester : provider.getLoadedQuesters()) {
				if (quester.getIdentifier().equals(identifier))
					return Optional.of(quester);
			}
			throw new EmptyEntitySelectorException(string);
		}
		return Optional.empty();
	}

}
