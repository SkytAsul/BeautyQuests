package fr.skytasul.quests.utils.compatibility;

import java.lang.reflect.Constructor;

import org.bukkit.Location;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Holograms.CMIHologram;

import fr.skytasul.quests.api.AbstractHolograms;

public class BQCMI extends AbstractHolograms<CMIHologram> {
	
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
	public AbstractHolograms<CMIHologram>.BQHologram createHologram(Location lc, boolean defaultVisible) {
		try {
			CMIHologram hologram = holoConstructor.newInstance("BQ Hologram " + hashCode(), locationConstructor.newInstance(lc));
			CMI.getInstance().getHologramManager().addHologram(hologram);
			return new BQCMIHologram(hologram);
		}catch (ReflectiveOperationException ex) {
			throw new IllegalStateException(ex);
		}
	}
	
	public class BQCMIHologram extends BQHologram {
		
		protected BQCMIHologram(CMIHologram hologram) {
			super(hologram);
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
