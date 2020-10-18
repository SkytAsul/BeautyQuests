package fr.skytasul.quests.utils.compatibility;

import org.bukkit.Location;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMILocation;
import com.Zrips.CMI.Modules.Holograms.CMIHologram;

import fr.skytasul.quests.api.AbstractHolograms;

public class BQCMI extends AbstractHolograms<CMIHologram> {
	
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
		CMIHologram hologram = new CMIHologram("BQ Hologram " + hashCode(), new CMILocation(lc));
		CMI.getInstance().getHologramManager().addHologram(hologram);
		return new BQCMIHologram(hologram);
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
