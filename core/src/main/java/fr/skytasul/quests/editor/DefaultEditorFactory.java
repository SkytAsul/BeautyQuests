package fr.skytasul.quests.editor;

import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.editors.EditorFactory;
import fr.skytasul.quests.api.editors.parsers.AbstractParser;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.editor.parsers.MaterialParser;

public class DefaultEditorFactory implements EditorFactory {

	public static final MaterialParser ITEM_PARSER = new MaterialParser(true, false);
	public static final MaterialParser BLOCK_PARSER = new MaterialParser(false, true);
	public static final MaterialParser ANY_PARSER = new MaterialParser(false, false);

	@Override
	public @NotNull AbstractParser<XMaterial> getMaterialParser(boolean item, boolean block) {
		if (item && !block)
			return ITEM_PARSER;
		if (block && !item)
			return BLOCK_PARSER;
		if (block && item)
			return ANY_PARSER;

		throw new IllegalArgumentException("Material parser must be either for items, for blocks or both, not neither.");
	}

}
