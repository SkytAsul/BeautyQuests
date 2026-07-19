package fr.skytasul.quests.api.editors;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.parsers.AbstractParser;
import fr.skytasul.quests.api.editors.parsers.AbstractParser.ParsingError;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.messaging.DefaultErrors;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import java.util.function.Consumer;

public class TextEditor<T> extends AbstractChatEditor {

	protected Consumer<T> run;
	protected Runnable nul;
	protected AbstractParser<T> parser;
	protected boolean useStripped = false;
	protected boolean nullIntoConsumer = false;

	public TextEditor(Player p, Runnable cancel, Consumer<T> end) {
		this(p, cancel, end, null, null);
	}

	public TextEditor(Player p, Runnable cancel, Consumer<T> end, Runnable nul) {
		this(p, cancel, end, nul, null);
	}

	public TextEditor(Player p, Runnable cancel, Consumer<T> end, AbstractParser<T> parser) {
		this(p, cancel, end, null, parser);
	}

	public TextEditor(Player p, Runnable cancel, Consumer<T> end, Runnable nul, AbstractParser<T> parser) {
		super(p, cancel);
		this.run = end;
		this.parser = parser;
		this.nul = nul;
	}

	public TextEditor<T> passNullIntoEndConsumer() {
		Validate.isTrue(nul == null);
		nullIntoConsumer = true;
		return this;
	}

	public TextEditor<T> useStrippedMessage() {
		useStripped = true;
		return this;
	}

	@Override
	public boolean chat(String msg, String strippedMessage){
		if (strippedMessage.equals("null")) {
			if (nul == null && !nullIntoConsumer) {
				Lang.ARG_NOT_SUPPORTED.send(player, PlaceholderRegistry.of("arg", "null"));
				return false;
			}
			stop();
			if (nullIntoConsumer) {
				run.accept(null);
			}else nul.run();
			return true;
		}

		if (parser == null) {
			stop();
			run.accept((T) (useStripped ? strippedMessage : msg));
			return true;
		} else {
			try {
				T parsedValue = parser.parse(strippedMessage);
				stop();
				run.accept(parsedValue);
				return true;
			} catch (ParsingError ex) {
				MessageUtils.sendMessage(player, ex.getMessage(), MessageType.DefaultMessageType.PREFIXED);
				return false;
			} catch (Exception ex) {
				DefaultErrors.sendGeneric(player, strippedMessage + " parsingText");
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("Failure to parse text: {0}", ex, strippedMessage);
				return false;
			}
		}
	}

	@Override
	public void begin() {
		super.begin();
		if (parser != null) {
			var indication = parser.getIndication();
			if (indication != null)
				MessageUtils.sendMessage(player, indication, MessageType.DefaultMessageType.PREFIXED);
		}
	}

}
