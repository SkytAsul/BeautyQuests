package fr.skytasul.quests.api.editors;

import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.editors.parsers.AbstractParser;
import fr.skytasul.quests.api.npcs.BqNpc;

public interface EditorFactory {

	public @NotNull AbstractParser<XMaterial> getMaterialParser(boolean item, boolean block);

	public @NotNull Editor createNpcSelection(@NotNull Player player, @NotNull Runnable cancel,
			@NotNull Consumer<BqNpc> callback);

}
