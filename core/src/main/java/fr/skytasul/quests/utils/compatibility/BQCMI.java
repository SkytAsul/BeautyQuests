package fr.skytasul.quests.utils.compatibility;

import java.util.List;

import org.bukkit.Location;

import com.Zrips.CMI.CMI;
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
		CMIHologram hologram = new CMIHologram("BQ Hologram " + hashCode(), lc);
		CMI.getInstance().getHologramManager().addHologram(hologram);
		return new BQCMIHologram(hologram);
	}
	
	public class BQCMIHologram extends BQHologram {
		
		protected BQCMIHologram(CMIHologram hologram) {
			super(hologram);
		}
		
		@Override
		public void appendTextLine(String text) {
			List<String> lines = hologram.getLinesAsList();
			lines.add(text);
			hologram.setLines(lines);
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
	
}
