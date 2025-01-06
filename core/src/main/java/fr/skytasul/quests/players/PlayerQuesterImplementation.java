package fr.skytasul.quests.players;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.players.PlayerQuester;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.players.accounts.AbstractAccount;
import fr.skytasul.quests.questers.AbstractQuesterImplementation;
import fr.skytasul.quests.questers.QuesterPoolDataImplementation;
import fr.skytasul.quests.questers.QuesterQuestDataImplementation;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointers;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

public class PlayerQuesterImplementation extends AbstractQuesterImplementation implements PlayerQuester, ForwardingAudience {

	public static final List<String> FORBIDDEN_DATA_ID = Arrays.asList("identifier", "quests", "pools");

	public final AbstractAccount abstractAcc;
	protected final Map<Integer, QuesterQuestDataImplementation> questDatas = new HashMap<>();
	protected final Map<Integer, QuesterPoolDataImplementation> poolDatas = new HashMap<>();
	protected final Map<SavableData<?>, Object> additionalDatas = new HashMap<>();
	public final int index;

	private @Nullable PlaceholderRegistry placeholders;

	protected PlayerQuesterImplementation(@NotNull AbstractAccount account, int index) {
		this.abstractAcc = account;
		this.index = index;
	}

	@Override
	public @NotNull Iterable<? extends Audience> audiences() {
		return abstractAcc.isCurrent() ? List.of(QuestsPlugin.getPlugin().getAudiences().player(abstractAcc.getPlayer()))
				: List.of();
	}

	@Override
	public @NotNull OfflinePlayer getOfflinePlayer() {
		return abstractAcc.getOfflinePlayer();
	}

	@Override
	public @NotNull Optional<Player> getPlayer() {
		return Optional.ofNullable(abstractAcc.getPlayer());
	}

	@Override
	public boolean isOnline() {
		return abstractAcc.isCurrent();
	}

	// TODO improve memory usage of lists
	@Override
	public @NotNull Collection<Player> getOnlinePlayers() {
		return abstractAcc.isCurrent() ? List.of(abstractAcc.getPlayer()) : List.of();
	}

	@Override
	public @NotNull Collection<OfflinePlayer> getOfflinePlayers() {
		return List.of(abstractAcc.getOfflinePlayer());
	}

	@Override
	protected QuesterQuestDataImplementation createQuestDatas(@NotNull Quest quest) {
		return new QuesterQuestDataImplementation(this, quest.getId());
	}

	@Override
	protected QuesterPoolDataImplementation createPoolDatas(@NotNull QuestPool pool) {
		return new QuesterPoolDataImplementation(this, pool.getId());
	}

	@Override
	protected void createdPointers(@NotNull Pointers.Builder builder) {
		super.createdPointers(builder);
		builder.withStatic(Identity.UUID, abstractAcc.getOfflinePlayer().getUniqueId());
	}

	@Override
	public boolean equals(Object object) {
		if (object == this)
			return true;
		if (object == null)
			return false;
		if (object.getClass() != this.getClass())
			return false;
		return abstractAcc.equals(((PlayerQuesterImplementation) object).abstractAcc);
	}

	@Override
	public int hashCode() {
		int hash = 1;

		hash = hash * 31 + index;
		hash = hash * 31 + abstractAcc.hashCode();

		return hash;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public @NotNull String getName() {
		return abstractAcc.isCurrent() ? abstractAcc.getPlayer().getName() : debugName();
	}

	@Override
	public @NotNull String getNameAndID() {
		return abstractAcc.isCurrent() ? abstractAcc.getPlayer().getName() + " (#%d)".formatted(index) : debugName();
	}

	@Override
	public @NotNull String debugName() {
		return abstractAcc.getIdentifier() + " (#%d)".formatted(index);
	}

}
