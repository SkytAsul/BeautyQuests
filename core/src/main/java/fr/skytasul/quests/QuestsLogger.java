package fr.skytasul.quests;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.utils.Utils;

public class QuestsLogger extends PluginLogger {

	private final Date launchDate = new Date();
	
	private File file;
	private PrintWriter stream;
	
	private SimpleDateFormat format = new SimpleDateFormat("[HH:mm:ss] ");
	private Date date = new Date(System.currentTimeMillis());
	
	private BukkitRunnable run;
	private boolean something = false;
	
	public QuestsLogger(Plugin plugin) {
		super(plugin);
		try {
			file = new File(plugin.getDataFolder(), "latest.log");
			if (file.exists()) {
				Files.move(file.toPath(), new File(plugin.getDataFolder(), "latest.log_old").toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			file.createNewFile();
			stream = new PrintWriter(new FileWriter(file));
			write("---- BEAUTYQUESTS LOGGER - OPENED " + launchDate.toString() + " ----");
		}catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public boolean isEnabled() {
		return stream != null;
	}
	
	@Override
	public void log(LogRecord logRecord) {
		if (logRecord != null) write("[" + (logRecord.getLevel() == null ? "NONE" : logRecord.getLevel().getName()) + "]: " + logRecord.getMessage());
		super.log(logRecord);
	}
	
	public void write(String msg){
		if (!isEnabled()) return;
		date.setTime(System.currentTimeMillis());
		stream.println(format.format(date) + msg);
		something = true;
	}
	
	public void close() {
		Date endDate = new Date();
		info("Logger was open during " + Utils.millisToHumanString(endDate.getTime() - launchDate.getTime()));
		write("---- BEAUTYQUESTS LOGGER - CLOSED " + endDate.toString() + " ----");
		if (!isEnabled()) return;
		if (run != null) run.cancel();
		stream.close();
		stream = null;
		super.log(new LogRecord(Level.INFO, "BeautyQuests logger stream closed."));
	}
	
	void launchFlushTimer(){
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

}
