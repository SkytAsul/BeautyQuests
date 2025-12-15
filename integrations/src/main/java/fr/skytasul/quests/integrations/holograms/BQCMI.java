package fr.skytasul.quests.integrations.holograms;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Holograms.CMIHologram;
import fr.skytasul.quests.api.holograms.BqHologram;
import fr.skytasul.quests.api.holograms.BqHologramManager;
import org.bukkit.Location;
import java.lang.reflect.Constructor;

public class BQCMI implements BqHologramManager {

	private Constructor<CMIHologram> holoConstructor;
	private Constructor<?> locationConstructor;

	public BQCMI() {
		try {
			Class<?> locationClass;
			try {
				locationClass = Class.forName("net.Zrips.CMILib.Container.CMILocation");
			}catch (ClassNotFoundException ex) {
				locationClass = Class.forName("com.Zrips.CMI.Containers.CMILocation");
			}
			locationConstructor = locationClass.getDeclaredConstructor(Location.class);
			holoConstructor = CMIHologram.class.getDeclaredConstructor(String.class, locationClass);
		}catch (ReflectiveOperationException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@Override
	public boolean supportPerPlayerVisibility() {
		return false;
	}

	@Override
	public boolean supportItems() {
		return false;
	}

	@Override
	public BQCMIHologram createHologram(Location lc, boolean defaultVisible) {
		try {
			CMIHologram hologram = holoConstructor.newInstance("BQ Hologram " + hashCode(), locationConstructor.newInstance(lc));
			CMI.getInstance().getHologramManager().addHologram(hologram);
			return new BQCMIHologram(hologram);
		}catch (ReflectiveOperationException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public class BQCMIHologram implements BqHologram {

		private final CMIHologram hologram;

		protected BQCMIHologram(CMIHologram hologram) {
			this.hologram = hologram;
		}

		@Override
		public void appendTextLine(String text) {
			hologram.addLine(text);
			hologram.update();
		}

		@Override
		public void teleport(Location lc) {
			hologram.setLoc(lc);
		}

		@Override
		public void delete() {
			CMI.getInstance().getHologramManager().removeHolo(hologram);
		}

	}

	public static boolean areHologramsEnabled() {
		return CMI.getInstance().getHologramManager() != null;
	}

}
