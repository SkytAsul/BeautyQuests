package fr.skytasul.quests.utils.logger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerExpanded {
	
	private Logger logger;
	
	public LoggerExpanded(Logger logger) {
		this.logger = logger;
	}
	
	public void info(String msg) {
		logger.info(msg);
	}
	
	public void warning(String msg) {
		logger.log(Level.WARNING, msg);
	}
	
	public void warning(String msg, Throwable throwable) {
		logger.log(Level.WARNING, msg, throwable);
	}
	
	public void severe(String msg) {
		logger.log(Level.SEVERE, msg);
	}
	
	public void severe(String msg, Throwable throwable) {
		logger.log(Level.SEVERE, msg, throwable);
	}
}
