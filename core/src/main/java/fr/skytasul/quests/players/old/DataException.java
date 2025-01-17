package fr.skytasul.quests.players.old;

public class DataException extends RuntimeException {

	private static final long serialVersionUID = -2135458159622298091L;

	public DataException(String message) {
		super(message);
	}

	public DataException(String message, Throwable cause) {
		super(message, cause);
	}

}
