package fr.skytasul.quests.api.editors;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.parsers.AbstractParser;
import fr.skytasul.quests.api.editors.parsers.AbstractParser.ParsingError;
import fr.skytasul.quests.api.utils.messaging.DefaultErrors;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import java.util.function.Consumer;

/**
 * A textual editor that uses the in-game chatbox to input a single line of text.
 * <p>
 * This editor should not be instanciated directly, prefer using the EditorFactory.
 * 
 * @param <T> type of object returned by the parser
 */
@Deprecated(forRemoval = true)
public class TextEditor<T> extends AbstractChatEditor {

	protected Consumer<T> run;
	protected Runnable reset;
	protected String resetWord;
	protected AbstractParser<T> parser;
	protected boolean useStripped = false;

	public TextEditor(Player p, Runnable cancel, Consumer<T> end) {
		this(p, cancel, end, null, null, null);
	}

	public TextEditor(Player p, Runnable cancel, Consumer<T> end, Runnable nul) {
		this(p, cancel, end, nul, "null", null);
	}

	public TextEditor(Player p, Runnable cancel, Consumer<T> end, AbstractParser<T> parser) {
		this(p, cancel, end, null, null, parser);
	}

	public TextEditor(Player p, Runnable cancel, Consumer<T> end, Runnable nul, AbstractParser<T> parser) {
		this(p, cancel, end, nul, "null", parser);
	}

	public TextEditor(Player p, Runnable cancel, Consumer<T> end, Runnable resetCallback, String resetWord, AbstractParser<T> parser) {
		super(p, cancel);
		this.run = end;
		this.parser = parser;
		this.reset = resetCallback;
		this.resetWord = resetWord;
	}

	public TextEditor<T> passNullIntoEndConsumer() {
		Validate.isTrue(reset == null);
		reset = () -> run.accept(null);
		resetWord = "null";
		return this;
	}

	public TextEditor<T> useStrippedMessage() {
		useStripped = true;
		return this;
	}

	@Override
	public boolean chat(String msg, String strippedMessage){
		if (strippedMessage.equals(resetWord)) {
			stop();
			reset.run();
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
