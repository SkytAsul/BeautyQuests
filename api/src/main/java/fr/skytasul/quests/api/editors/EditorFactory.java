package fr.skytasul.quests.api.editors;

import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.editors.parsers.AbstractParser;

public interface EditorFactory {

	public @NotNull AbstractParser<XMaterial> getMaterialParser(boolean item, boolean block);

}
