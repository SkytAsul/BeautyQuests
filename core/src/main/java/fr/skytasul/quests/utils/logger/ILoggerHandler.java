package fr.skytasul.quests.utils.logger;

public interface ILoggerHandler {
	
	public static final ILoggerHandler EMPTY_LOGGER = new LoggerHandlerEmpty();

	void write(String msg);
	
	class LoggerHandlerEmpty implements ILoggerHandler {
		
		private LoggerHandlerEmpty() {}
		
		@Override
		public void write(String msg) {}
	}
	
}