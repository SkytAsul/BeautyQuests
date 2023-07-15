package fr.skytasul.quests.api.utils.messaging;

import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;

public enum MessageType {

	PREFIXED() {
		@Override
		public @NotNull String process(@NotNull String msg) {
			return QuestsPlugin.getPlugin().getPrefix() + msg;
		}
	},
	UNPREFIXED() {
		@Override
		public @NotNull String process(@NotNull String msg) {
			return "§6" + msg;
		}
	},
	OFF() {
		@Override
		public @NotNull String process(@NotNull String msg) {
			return Lang.OffText.format(msg);
		}
	};

	public @NotNull String process(@NotNull String msg) {
		return msg;
	}

}
