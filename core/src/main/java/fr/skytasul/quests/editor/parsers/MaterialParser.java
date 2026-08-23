package fr.skytasul.quests.editor.parsers;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.editors.parsers.AbstractParser;
import fr.skytasul.quests.api.localization.Lang;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MaterialParser implements AbstractParser<XMaterial> {

	private boolean item, block;

	public MaterialParser(boolean item, boolean block) {
		this.item = item;
		this.block = block;
	}

	@Override
	public XMaterial parse(@NotNull String msg) throws ParsingError {
		XMaterial tmp = XMaterial.matchXMaterial(msg).orElse(null);
		if (tmp == null){
			Material mat = Material.matchMaterial(msg);
			if (mat != null) tmp = XMaterial.matchXMaterial(mat);
			if (tmp == null) {
				if (block)
					throw new ParsingError(Lang.UNKNOWN_BLOCK_TYPE.getValue());
				else
					throw new ParsingError(Lang.UNKNOWN_ITEM_TYPE.getValue());
			}
		}
		if (item) {
			if (!tmp.get().isItem())
				throw new ParsingError(Lang.INVALID_ITEM_TYPE.getValue());
		} else if (block) {
			if (!tmp.get().isBlock())
				throw new ParsingError(Lang.INVALID_BLOCK_TYPE.getValue());
		}
		return tmp;
	}

	@Override
	public @Nullable String serialize(@NotNull XMaterial value) {
		return value.name();
	}

}
