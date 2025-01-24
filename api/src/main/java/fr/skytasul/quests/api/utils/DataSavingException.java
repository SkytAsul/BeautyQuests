package fr.skytasul.quests.api.utils;

public class DataSavingException extends Exception {

	private static final long serialVersionUID = 6381359339423992538L;

	public DataSavingException(String message) {
		super(message);
	}

	public DataSavingException(String message, Throwable cause) {
		super(message, cause);
	}

}
