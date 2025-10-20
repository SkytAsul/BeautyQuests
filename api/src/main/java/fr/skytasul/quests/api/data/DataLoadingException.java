package fr.skytasul.quests.api.data;

public class DataLoadingException extends Exception {

	private static final long serialVersionUID = -3697128675441649649L;

	public DataLoadingException(String message) {
		super(message);
	}

	public DataLoadingException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataLoadingException(Throwable cause) {
		super(cause);
	}

}
