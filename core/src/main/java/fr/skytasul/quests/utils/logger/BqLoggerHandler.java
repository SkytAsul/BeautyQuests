package fr.skytasul.quests.utils.logger;

import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.logger.LoggerExpanded;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.*;

public class BqLoggerHandler extends FileHandler {

	public BqLoggerHandler(Plugin plugin) throws IOException {
		super(plugin.getDataFolder().toPath().resolve("latest.log").toString(), 0, 2);

		super.setFormatter(new BqLoggerFormatter());
		super.setLevel(LoggerExpanded.DEBUG_LEVEL);

		// TODO remove: migration 2.0
		Files.deleteIfExists(plugin.getDataFolder().toPath().resolve("latest.log_old"));
		Files.deleteIfExists(plugin.getDataFolder().toPath().resolve("latest.log"));
	}

	private class BqLoggerFormatter extends Formatter {

		private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("[HH:mm:ss]");

		private final Date launchDate = new Date();
		private final List<String> seenErrors = new ArrayList<>();

		@Override
		public String format(LogRecord logRecord) {
			var stb = new StringBuilder();

			stb.append(DATE_FORMAT.format(Date.from(logRecord.getInstant())));

			if (logRecord.getLoggerName() != null)
				stb.append(" [")
						.append(logRecord.getLoggerName())
						.append(']');

			stb.append(" [")
					.append(logRecord.getLevel().getName())
					.append("] ");

			if (logRecord.getParameters() != null) {
				if (logRecord.getMessage().contains("{}"))
					publish(new LogRecord(Level.WARNING, "Bad parameter usage in a log message. Nag the author about it!"));
			}
			stb.append(super.formatMessage(logRecord));

			if (logRecord.getThrown() != null) {
				stb.append('\n');

				if (seenErrors.size() == 1_000) {
					seenErrors.clear();
					stb.append("Too much errors have been memorized. Cleaned up.\n");
				}

				var throwableStr = throwableToString(logRecord.getThrown());
				int throwableIndex = seenErrors.indexOf(throwableStr);
				if (throwableIndex == -1) {
					// first time we see this error
					throwableIndex = seenErrors.size();
					seenErrors.add(throwableStr);
					stb.append("ERROR #");
					stb.append(throwableIndex);
					stb.append(":\n");
					stb.append(throwableStr);
				} else {
					stb.append("ERROR #");
					stb.append(throwableIndex);
				}
			}

			stb.append('\n');

			return stb.toString();
		}

		private @NotNull String throwableToString(@NotNull Throwable ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			pw.close();
			return sw.toString();
		}

		@Override
		public String getHead(Handler h) {
			return "---- BEAUTYQUESTS LOGGER - OPENED " + launchDate.toString() + " ----\n";
		}

		@Override
		public String getTail(Handler h) {
			Date endDate = new Date();
			return """
					Logger was open during %s.
					---- BEAUTYQUESTS LOGGER - CLOSED %s ----
					""".formatted(Utils.millisToHumanString(endDate.getTime() - launchDate.getTime()), endDate);
		}
	}

}
