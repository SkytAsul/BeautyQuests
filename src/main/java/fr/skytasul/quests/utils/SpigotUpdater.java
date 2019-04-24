package fr.skytasul.quests.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import fr.skytasul.quests.BeautyQuests;

/**
 * @author <a href="https://inventivetalent.org">inventivetalent</a>, SkytAsul
 */
public class SpigotUpdater extends Thread {
	private final Plugin plugin;
	private boolean enabled;
	private URL url;

	public SpigotUpdater(Plugin plugin, int resourceID) throws IOException {
		this.enabled = true;
		if (plugin == null) {
			throw new IllegalArgumentException("Plugin cannot be null");
		} else if (resourceID == 0) {
			throw new IllegalArgumentException("Resource ID cannot be null (0)");
		} else {
			this.plugin = plugin;
			this.url = new URL("https://api.inventivetalent.org/spigot/resource-simple/" + resourceID);
			this.enabled = BeautyQuests.getInstance().getConfig().getBoolean("checkUpdates");
			super.start();
		}
	}

	public synchronized void start() {
	}

	public void run() {
		if (this.plugin.isEnabled()) {
			if (this.enabled) {

				HttpURLConnection connection = null;

				try {
					connection = (HttpURLConnection) this.url.openConnection();
					connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
					connection.setRequestMethod("GET");
					BufferedReader e = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					String e11 = "";

					for (String line = null; (line = e.readLine()) != null; e11 = e11 + line) {
						;
					}

					e.close();
					JSONObject json = null;

					try {
						json = (JSONObject) (new JSONParser()).parse(e11);
					} catch (ParseException var9) {
						;
					}

					String currentVersion = null;
					if (json != null && json.containsKey("version")) {
						String version = (String) json.get("version");
						if (version != null && !version.isEmpty()) {
							currentVersion = version;
						}
					}

					if (currentVersion == null) return;

					String v = this.plugin.getDescription().getVersion();
					if (v.contains("_")){
						plugin.getLogger().info("You are using a snapshot version! (" + v + ") Current version: " + currentVersion);
					}else if (!currentVersion.equals(v)) {
						try {
							boolean beta = false;
							String[] current = currentVersion.split("\\.");
							String[] last = this.plugin.getDescription().getVersion().split("\\.");
							for (int i = 0;; i++){
								if (i == current.length){
									beta = true;
									break;
								}else if (i == last.length) break;
								int c = Integer.parseInt(current[i]);
								int l = Integer.parseInt(last[i]);
								if (c > l) break;
								if (c < l){
									beta = true;
									break;
								}
							}
							if (beta){
								sendNewVersionMessage("Are you a beta-tester ? Current version: " + currentVersion + "!");
							}else sendNewVersionMessage("You are using an old version of plugin. New version: " + currentVersion + "!");
						}catch (Throwable ex){
							sendNewVersionMessage("Found another online version: " + currentVersion + "!");
						}
					}
				} catch (IOException var10) {}

			}
		}
	}
	
	private void sendNewVersionMessage(String msg){
		this.plugin.getLogger().info(msg + " (Your version is " + this.plugin.getDescription().getVersion() + ")");
	}
}