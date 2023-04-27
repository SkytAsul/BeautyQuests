package fr.skytasul.quests.api.npcs;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.AbstractHolograms;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.stages.types.Locatable.Located;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.options.OptionHologramLaunch;
import fr.skytasul.quests.options.OptionHologramLaunchNo;
import fr.skytasul.quests.options.OptionHologramText;
import fr.skytasul.quests.options.OptionStarterNPC;

public abstract class BQNPC implements Located.LocatedEntity {
	
	private Map<Quest, List<Player>> quests = new TreeMap<>();
	private Set<QuestPool> pools = new TreeSet<>();
	
	private List<Entry<Player, Object>> hiddenTickets = new ArrayList<>();
	private Map<Object, BiPredicate<Player, PlayerAccount>> startable = new HashMap<>();
	
	private BukkitTask launcheableTask;
	
	/* Holograms */
	private boolean debug = false;
	private BukkitTask hologramsTask;
	private boolean hologramsRemoved = true;
	private Hologram hologramText = new Hologram(false, QuestsAPI.hasHologramsManager() && !QuestsConfiguration.isTextHologramDisabled(), Lang.HologramText.toString());
	private Hologram hologramLaunch = new Hologram(false, QuestsAPI.hasHologramsManager() && QuestsAPI.getHologramsManager().supportItems(), QuestsConfiguration.getHoloLaunchItem());
	private Hologram hologramLaunchNo = new Hologram(false, QuestsAPI.hasHologramsManager() && QuestsAPI.getHologramsManager().supportItems() && QuestsAPI.getHologramsManager().supportPerPlayerVisibility(), QuestsConfiguration.getHoloLaunchNoItem());
	private Hologram hologramPool = new Hologram(false, QuestsAPI.hasHologramsManager() && QuestsAPI.getHologramsManager().supportPerPlayerVisibility(), Lang.PoolHologramText.toString()) {
		@Override
		public double getYAdd() {
			return hologramText.canAppear && hologramText.visible ? 0.55 : 0;
		}
	};
	private final boolean holograms;
	
	protected BQNPC() {
		holograms = hologramText.enabled || hologramLaunch.enabled || hologramLaunchNo.enabled || hologramPool.enabled;
		launcheableTask = startLauncheableTasks();
	}
	
	public abstract int getId();
	
	public abstract @NotNull String getName();
	
	public abstract boolean isSpawned();
	
	@Override
	public abstract @NotNull Entity getEntity();
	
	@Override
	public abstract @NotNull Location getLocation();
	
	public abstract void setSkin(@Nullable String skin);
	
	/**
	 * Sets the "paused" state of the NPC navigation
	 * 
	 * @param paused should the navigation be paused
	 * @return <code>true</code> if the navigation was
	 * paused before this call, <code>false</code> otherwise
	 */
	public abstract boolean setNavigationPaused(boolean paused);
	
	private BukkitTask startLauncheableTasks() {
		return new BukkitRunnable() {
			private int timer = 0;
			
			@Override
			public void run() {
				if (!isSpawned()) return;
				if (!(getEntity() instanceof LivingEntity)) return;
				LivingEntity en = (LivingEntity) getEntity();
				
				if (timer-- == 0) {
					timer = QuestsConfiguration.getRequirementUpdateTime();
					return;
				}
				
				quests.values().forEach(List::clear);
				
				Set<Player> playersInRadius = new HashSet<>();
				Location lc = en.getLocation();
				for (Player p : lc.getWorld().getPlayers()) {
					PlayerAccount acc = PlayersManager.getPlayerAccount(p);
					if (acc == null) continue;
					if (lc.distanceSquared(p.getLocation()) > QuestsConfiguration.getStartParticleDistanceSquared()) continue;
					playersInRadius.add(p);
					for (Entry<Quest, List<Player>> quest : quests.entrySet()) {
						if (quest.getKey().isLauncheable(p, acc, false)) {
							quest.getValue().add(p);
							break;
						}
					}
				}
				
				if (QuestsConfiguration.showStartParticles()) {
					quests.forEach((quest, players) -> QuestsConfiguration.getParticleStart().send(en, players));
				}
				
				if (hologramPool.canAppear) {
					for (Player p : playersInRadius) {
						boolean visible = false;
						for (QuestPool pool : pools) {
							if (pool.canGive(p, PlayersManager.getPlayerAccount(p))) {
								visible = true;
								break;
							}
						}
						hologramPool.setVisible(p, visible);
					}
				}
				if (hologramLaunch.canAppear || hologramLaunchNo.canAppear) {
					List<Player> launcheable = new ArrayList<>();
					List<Player> unlauncheable = new ArrayList<>();
					for (Iterator<Player> iterator = playersInRadius.iterator(); iterator.hasNext();) {
						Player player = iterator.next();
						if (hiddenTickets.stream().anyMatch(entry -> entry.getKey() == player)) {
							iterator.remove();
							continue;
						}
						PlayerAccount acc = PlayersManager.getPlayerAccount(player);
						boolean launchYes = false;
						boolean launchNo = false;
						for (Entry<Quest, List<Player>> qu : quests.entrySet()) {
							if (!qu.getKey().hasStarted(acc)) {
								boolean pLauncheable = qu.getValue().contains(player);
								if (hologramLaunch.enabled && pLauncheable) {
									launchYes = true;
									break; // launcheable take priority over not launcheable
								}else if (hologramLaunchNo.enabled && !pLauncheable) {
									launchNo = true;
								}
							}
						}
						if (launchYes) {
							launcheable.add(player);
							iterator.remove();
						}else if (launchNo) {
							unlauncheable.add(player);
							iterator.remove();
						}
					}
					hologramLaunch.setVisible(launcheable);
					hologramLaunchNo.setVisible(unlauncheable);
				}
				
			}
		}.runTaskTimer(BeautyQuests.getInstance(), 20L, 20L);
	}
	
	private BukkitTask startHologramsTask() {
		return new BukkitRunnable() {
			@Override
			public void run() {
				LivingEntity en = null; // check if NPC is spawned and living
				if (isSpawned() && getEntity() instanceof LivingEntity)
					en = (LivingEntity) getEntity();
				if (en == null) {
					if (!hologramsRemoved) removeHolograms(false); // if the NPC is not living and holograms have not been already removed before
					return;
				}
				hologramsRemoved = false;
				
				if (hologramText.canAppear && hologramText.visible) hologramText.refresh(en);
				if (hologramLaunch.canAppear) hologramLaunch.refresh(en);
				if (hologramLaunchNo.canAppear) hologramLaunchNo.refresh(en);
				if (hologramPool.canAppear) hologramPool.refresh(en);
			}
		}.runTaskTimer(BeautyQuests.getInstance(), 20L, 1L);
	}
	
	public Set<Quest> getQuests() {
		return quests.keySet();
	}
	
	public Hologram getHologramText() {
		return hologramText;
	}
	
	public Hologram getHologramLaunch() {
		return hologramLaunch;
	}
	
	public Hologram getHologramLaunchNo() {
		return hologramLaunchNo;
	}
	
	public void addQuest(Quest quest) {
		if (quests.containsKey(quest)) return;
		quests.put(quest, new ArrayList<>());
		if (hologramText.enabled && quest.hasOption(OptionHologramText.class)) hologramText.setText(quest.getOption(OptionHologramText.class).getValue());
		if (hologramLaunch.enabled && quest.hasOption(OptionHologramLaunch.class)) hologramLaunch.setItem(quest.getOption(OptionHologramLaunch.class).getValue());
		if (hologramLaunchNo.enabled && quest.hasOption(OptionHologramLaunchNo.class)) hologramLaunchNo.setItem(quest.getOption(OptionHologramLaunchNo.class).getValue());
		hologramText.visible = true;
		addStartablePredicate((p, acc) -> quest.isLauncheable(p, acc, false), quest);
		updatedObjects();
	}
	
	public boolean removeQuest(Quest quest) {
		boolean b = quests.remove(quest) == null;
		removeStartablePredicate(quest);
		updatedObjects();
		if (quests.isEmpty()) {
			hologramText.visible = false;
			hologramText.delete();
		}
		return b;
	}
	
	public boolean hasQuestStarted(Player p) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		return quests.keySet().stream().anyMatch(quest -> quest.hasStarted(acc));
	}
	
	public Set<QuestPool> getPools() {
		return pools;
	}
	
	public void addPool(QuestPool pool) {
		if (!pools.add(pool)) return;
		if (hologramPool.enabled && (pool.getHologram() != null)) hologramPool.setText(pool.getHologram());
		addStartablePredicate(pool::canGive, pool);
		updatedObjects();
	}
	
	public boolean removePool(QuestPool pool) {
		boolean b = pools.remove(pool);
		removeStartablePredicate(pool);
		updatedObjects();
		if (pools.isEmpty()) hologramPool.delete();
		return b;
	}
	
	public void addStartablePredicate(BiPredicate<Player, PlayerAccount> predicate, Object holder) {
		startable.put(holder, predicate);
	}
	
	public void removeStartablePredicate(Object holder) {
		startable.remove(holder);
	}
	
	public void hideForPlayer(Player p, Object holder) {
		hiddenTickets.add(new AbstractMap.SimpleEntry<>(p, holder));
	}
	
	public void removeHiddenForPlayer(Player p, Object holder) {
		for (Iterator<Entry<Player, Object>> iterator = hiddenTickets.iterator(); iterator.hasNext();) {
			Entry<Player, Object> entry = iterator.next();
			if (entry.getKey() == p && entry.getValue() == holder) {
				iterator.remove();
				return;
			}
		}
	}
	
	public boolean canGiveSomething(Player p) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		return startable.values().stream().anyMatch(predicate -> predicate.test(p, acc));
	}
	
	private void removeHolograms(boolean cancelRefresh) {
		hologramText.delete();
		hologramLaunch.delete();
		hologramLaunchNo.delete();
		hologramPool.delete();
		hologramsRemoved = true;
		if (cancelRefresh && hologramsTask != null) {
			hologramsTask.cancel();
			hologramsTask = null;
		}
	}
	
	private boolean isEmpty() {
		return quests.isEmpty() && pools.isEmpty();
	}
	
	private void updatedObjects() {
		if (isEmpty()) {
			removeHolograms(true);
		}else if (holograms && hologramsTask == null) {
			hologramsTask = startHologramsTask();
		}
	}
	
	public void unload() {
		removeHolograms(true);
		if (launcheableTask != null) {
			launcheableTask.cancel();
			launcheableTask = null;
		}
	}
	
	public void delete(String cause) {
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Removing NPC Starter " + getId());
		for (Quest qu : quests.keySet()) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("Starter NPC #" + getId() + " has been removed from quest " + qu.getId() + ". Reason: " + cause);
			qu.removeOption(OptionStarterNPC.class);
		}
		quests = null;
		for (QuestPool pool : pools) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("NPC #" + getId() + " has been removed from pool " + pool.getID() + ". Reason: " + cause);
			pool.unloadStarter();
		}
		unload();
	}
	
	public void toggleDebug() {
		if (debug)
			debug = false;
		else debug = true;
	}
	
	@Override
	public String toString() {
		String npcInfo = "NPC #" + getId() + ", " + quests.size() + " quests, " + pools.size() + " pools";
		String hologramsInfo;
		if (!holograms) {
			hologramsInfo = "no holograms";
		}else if (hologramsRemoved) {
			hologramsInfo = "holograms removed";
		}else {
			hologramsInfo = "holograms:";
			hologramsInfo += "\n- text=" + hologramText.toString();
			hologramsInfo += "\n- launch=" + hologramLaunch.toString();
			hologramsInfo += "\n- launchNo=" + hologramLaunchNo.toString();
			hologramsInfo += "\n- pool=" + hologramPool.toString();
		}
		return npcInfo + " " + hologramsInfo;
	}
	
	public class Hologram {
		final boolean enabled;
		boolean visible;
		boolean canAppear;
		AbstractHolograms<?>.BQHologram hologram;
		
		String text;
		ItemStack item;
		
		public Hologram(boolean visible, boolean enabled, String text) {
			this.visible = visible;
			this.enabled = enabled;
			setText(text);
		}
		
		public Hologram(boolean visible, boolean enabled, ItemStack item) {
			this.visible = visible;
			this.enabled = enabled;
			setItem(item);
		}
		
		public void refresh(LivingEntity en) {
			Location lc = Utils.upLocationForEntity(en, getYAdd());
			if (debug) System.out.println("refreshing " + toString() + " (hologram null: " + (hologram == null) + ")");
			if (hologram == null) {
				create(lc);
			}else {
				hologram.teleport(lc);
			}
		}
		
		public double getYAdd() {
			return item == null ? 0 : 1;
		}
		
		public void setVisible(List<Player> players) {
			if (hologram != null) hologram.setPlayersVisible(players);
		}
		
		public void setVisible(Player p, boolean visibility) {
			if (hologram != null) hologram.setPlayerVisibility(p, visibility);
		}
		
		public void setText(String text) {
			if (Objects.equals(text, this.text)) return;
			this.text = text;
			canAppear = enabled && !StringUtils.isEmpty(text) && !"none".equals(text);
			delete(); // delete to regenerate with new text
		}
		
		public void setItem(ItemStack item) {
			if (Objects.equals(item, this.item)) return;
			this.item = item;
			canAppear = enabled && item != null;
			if (canAppear && QuestsConfiguration.isCustomHologramNameShown() && item.hasItemMeta() && item.getItemMeta().hasDisplayName())
				this.text = item.getItemMeta().getDisplayName();
			delete(); // delete to regenerate with new item
		}
		
		public void create(Location lc) {
			if (hologram != null) return;
			hologram = QuestsAPI.getHologramsManager().createHologram(lc, visible);
			if (text != null) hologram.appendTextLine(text);
			if (item != null) hologram.appendItem(item);
		}
		
		public void delete() {
			if (debug) System.out.println("deleting " + toString());
			if (hologram == null) return;
			hologram.delete();
			hologram = null;
		}
		
		@Override
		public String toString() {
			if (!enabled) return "disabled";
			if (!canAppear) return "cannot appear";
			return (visible ? "visible" : "invisible") + " by default, "
					+ (item == null ? "" : item.getType().name() + ", ")
					+ (text == null ? "no text" : "text=" + text) + ", "
					+ (hologram == null ? " not spawned" : "spawned");
		}
		
	}
	
}