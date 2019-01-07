package fr.skytasul.quests.utils;

public class MissingDependencyException extends RuntimeException{

	private static final long serialVersionUID = 8636504175650105867L;
	
	public MissingDependencyException(String depend){
		super("Missing dependency: " + depend);
	}
	
}