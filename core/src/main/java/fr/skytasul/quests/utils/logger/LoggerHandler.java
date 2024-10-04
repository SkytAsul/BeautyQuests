package fr.skytasul.quests.utils.logger;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.logger.ILoggerHandler;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LoggerHandler extends Handler implements ILoggerHandler {

	private final Date launchDate = new Date();
	
	private PrintWriter stream;
	
	private SimpleDateFormat format = new SimpleDateFormat("[HH:mm:ss] ");
	private Date date = new Date(System.currentTimeMillis());
	
	private BukkitRunnable run;
	private boolean something = false;
	
	private List<String> errors = new ArrayList<>();
	
	public LoggerHandler(Plugin plugin) throws IOException {
		super.setFormatter(new Formatter() {
			@Override
			public String format(LogRecord record) {
				return super.formatMessage(record);
			}
		});
		
		Path path = plugin.getDataFolder().toPath().resolve("latest.log");
		if (Files.exists(path))
			Files.move(path, plugin.getDataFolder().toPath().resolve("latest.log_old"), StandardCopyOption.REPLACE_EXISTING);
		stream = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW));
		write("---- BEAUTYQUESTS LOGGER - OPENED " + launchDate.toString() + " ----");
	}
	
	public boolean isEnabled() {
		return stream != null;
	}
	
	@Override
	public void publish(LogRecord logRecord) {
		log(logRecord, null);
	}

	private void log(LogRecord logRecord, String prefix) {
		try {
			if (logRecord != null) {
				write(getFormatter().format(logRecord), prefix, logRecord.getLevel() == null ? "NONE" : logRecord.getLevel().getName());
				if (logRecord.getThrown() != null) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					logRecord.getThrown().printStackTrace(pw);
					pw.close();
					String throwable = sw.toString();
					int index = errors.indexOf(throwable);
					if (index == -1) {
						index = errors.size();
						write("new #" + index + ": " + throwable, "ERROR", prefix);
						errors.add(throwable);
					}else write("existing #" + index, "ERROR", prefix);
				}
			}
		}catch (Exception ex) {
			reportError("An error occurred while trying to publish a log record.", ex, ErrorManager.GENERIC_FAILURE);
		}
	}
	
	@Override
	public synchronized void write(String msg, String... prefixes) {
		if (!isEnabled()) return;
		date.setTime(System.currentTimeMillis());
		stream.print(format.format(date));
		for (String prefix : prefixes) {
			if (prefix != null && !prefix.isEmpty()) stream.print("[" + prefix + "] ");
		}
		stream.println(msg);
		something = true;
	}
	
	@Override
	public void close() {
		if (stream == null) return;
		Date endDate = new Date();
		write("Logger was open during " + Utils.millisToHumanString(endDate.getTime() - launchDate.getTime()));
		write("---- BEAUTYQUESTS LOGGER - CLOSED " + endDate.toString() + " ----");
		if (!isEnabled()) return;
		if (run != null) {
			run.cancel();
			run = null;
		}
		stream.close();
		stream = null;
	}
	
	public void launchFlushTimer() {
		if (run != null) return;
		if (!isEnabled()) return;
		run = new BukkitRunnable() {
			@Override
			public void run() {
				if (!something) return;
				stream.flush();
				something = false;
			}
		};
		run.runTaskTimerAsynchronously(BeautyQuests.getInstance(), 2L, 50L);
	}

	@Override
	public void flush() {
		run.run();
	}
	
	@Override
	public Handler getSubhandler(String prefix) {
		return new Subhandler(prefix);
	}
	
	private class Subhandler extends Handler {
		
		private String prefix;
		
		private Subhandler(String prefix) {
			this.prefix = prefix;
		}
		
		@Override
		public void publish(LogRecord logRecord) {
			log(logRecord, prefix);
		}
		
		@Override
		public void flush() {}
		
		@Override
		public void close() throws SecurityException {}
		
	}
	
}
