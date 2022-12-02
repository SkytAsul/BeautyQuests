package fr.skytasul.quests.utils.logger;

import java.util.logging.Handler;

public interface ILoggerHandler {
	
	public static final ILoggerHandler EMPTY_LOGGER = new LoggerHandlerEmpty();

	void write(String msg, String... prefixes);
	
	Handler getSubhandler(String prefix);
	
	class LoggerHandlerEmpty implements ILoggerHandler {
		
		private LoggerHandlerEmpty() {}
		
		@Override
		public void write(String msg, String... prefixes) {}
		
		@Override
		public Handler getSubhandler(String prefix) {
			return null;
		}
		
	}
	
}