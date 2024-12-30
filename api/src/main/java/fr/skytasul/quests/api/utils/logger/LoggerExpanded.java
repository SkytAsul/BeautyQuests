package fr.skytasul.quests.api.utils.logger;

import fr.skytasul.quests.api.utils.messaging.DefaultErrors;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggerExpanded {

	public static final @NotNull Level DEBUG_LEVEL = new Level("DEBUG", 450) {
		private static final long serialVersionUID = 4081184158724594L;
	};

	private final @NotNull Logger logger;
	private final @NotNull ILoggerHandler handler;

	private final Map<Object, Long> times = new HashMap<>();

	public LoggerExpanded(@NotNull Logger logger, @Nullable ILoggerHandler handler) {
		this.logger = logger;
		this.handler = handler == null ? ILoggerHandler.EMPTY_LOGGER : handler;
	}

	public @NotNull ILoggerHandler getHandler() {
		return handler;
	}

	public void info(@Nullable String msg) {
		logger.info(msg);
	}

	public void warning(@Nullable String msg) {
		logger.log(Level.WARNING, msg);
	}

	public void warning(@Nullable String msg, @Nullable Throwable throwable) {
		logger.log(Level.WARNING, msg, throwable);
	}

	public void namedWarning(@Nullable String msg, @NotNull Object type, int seconds, Object... args) {
		Long time = times.get(type);
		if (time == null || time.longValue() + seconds * 1000 < System.currentTimeMillis()) {
			logger.log(Level.WARNING, msg, args);
			times.put(type, System.currentTimeMillis());
		}
	}

	// TODO rename following method to "warning" after fixing above calls
	public void warningArgs(@Nullable String msg, Object... args) {
		logger.log(Level.WARNING, msg, args);
	}

	public void severe(@Nullable String msg) {
		logger.log(Level.SEVERE, msg);
	}

	public void severe(@Nullable String msg, @Nullable Throwable throwable) {
		logger.log(Level.SEVERE, msg, throwable);
	}

	public void severe(@Nullable String msg, Object... args) {
		logger.log(Level.SEVERE, msg, args);
	}

	public void debug(@Nullable String msg) {
		logger.log(DEBUG_LEVEL, msg);
	}

	public void debug(@Nullable String msg, Object... args) {
		logger.log(DEBUG_LEVEL, msg, args);
	}

	public void debug(@Nullable String msg, Throwable cause, Object... args) {
		var log = new LogRecord(DEBUG_LEVEL, msg);
		log.setParameters(args);
		log.setThrown(cause);
		logger.log(log);
	}

	public <T> BiConsumer<T, Throwable> logError(@Nullable Consumer<T> consumer, @Nullable String friendlyErrorMessage,
			@Nullable CommandSender sender) {
		return (object, ex) -> {
			if (ex == null) {
				if (consumer != null)
					consumer.accept(object);
			} else {
				if (ex instanceof CompletionException) {
					CompletionException exCompl = (CompletionException) ex;
					if (exCompl.getCause() != null)
						ex = exCompl.getCause();
				}

				if (sender != null)
					DefaultErrors.sendGeneric(sender, friendlyErrorMessage);
				severe(friendlyErrorMessage, ex);
			}
		};
	}

	public <T> BiConsumer<T, Throwable> logError(@Nullable String friendlyErrorMessage, @Nullable CommandSender sender) {
		return logError(null, friendlyErrorMessage, sender);
	}

	public <T> BiConsumer<T, Throwable> logError(@Nullable String friendlyErrorMessage) {
		return logError(null, friendlyErrorMessage, null);
	}

	public <T> BiConsumer<T, Throwable> logError() {
		return logError(null, null, null);
	}

}
