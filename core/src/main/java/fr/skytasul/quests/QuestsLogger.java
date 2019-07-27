package fr.skytasul.quests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.scheduler.BukkitRunnable;

public class QuestsLogger extends PluginLogger {

	private File file;
	private PrintWriter stream;
	
	private SimpleDateFormat format = new SimpleDateFormat("[HH:mm:ss] ");
	private Date date = new Date(System.currentTimeMillis());
	
	private BukkitRunnable run;
	private boolean something = false;
	
	public QuestsLogger(Plugin plugin) throws Throwable {
		super(plugin);
		file = new File(plugin.getDataFolder(), "latest.log");
		if (!file.exists()) file.createNewFile();
		stream = new PrintWriter(new FileWriter(file));
		write("---- BEAUTYQUESTS LOGGER - OPENED " + new Date(System.currentTimeMillis()).toString() + " ----");
	}
	
	
	public void log(LogRecord logRecord) {
		if (logRecord != null) write("[" + (logRecord.getLevel() == null ? "NONE" : logRecord.getLevel().getName()) + "]: " + logRecord.getMessage());
		super.log(logRecord);
	}
	
	public void write(String msg){
		date.setTime(System.currentTimeMillis());
		stream.println(format.format(date) + msg);
		something = true;
	}
	
	public void close() throws IOException{
		write("---- BEAUTYQUESTS LOGGER - CLOSED " + new Date(System.currentTimeMillis()).toString() + " ----");
		if (run != null) run.cancel();
		stream.close();
		stream = null;
		super.log(new LogRecord(Level.INFO, "BeautyQuests logger stream closed."));
	}
	
	void launchFlushTimer(){
		if (stream == null) return;
		run = new BukkitRunnable() {
			public void run() {
				if (!something) return;
				stream.flush();
				something = false;
			}
		};
		run.runTaskTimerAsynchronously(BeautyQuests.getInstance(), 2L, 50L);
	}

}
