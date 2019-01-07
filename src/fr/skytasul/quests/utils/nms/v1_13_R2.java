package fr.skytasul.quests.utils.nms;

import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.v1_13_R2.CraftParticle;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.ParticleEffect;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.v1_13_R2.ChatComponentText;
import net.minecraft.server.v1_13_R2.Packet;
import net.minecraft.server.v1_13_R2.PacketDataSerializer;
import net.minecraft.server.v1_13_R2.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_13_R2.PacketPlayOutWorldParticles;

public class v1_13_R2 implements NMS{
	
	public Object bookPacket(ByteBuf buf){
		return new PacketPlayOutCustomPayload(PacketPlayOutCustomPayload.c, new PacketDataSerializer(buf));
	}

	public Object worldParticlePacket(ParticleEffect effect, boolean paramBoolean, float paramFloat1, float paramFloat2,
			float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6, float paramFloat7, int paramInt,
			Object paramData) {
		return new PacketPlayOutWorldParticles(/*(ParticleParam) ReflectUtils.getFieldValue(Particles.class.getDeclaredField(effect.getFieldName()), null)*/CraftParticle.toNMS(effect.getBukkitParticle(), paramData), paramBoolean, paramFloat1, paramFloat2, paramFloat3, paramFloat4, paramFloat5, paramFloat6, paramFloat7, paramInt);
	}
	
	public void sendPacket(Player p, Object packet){
		Validate.isTrue(packet instanceof Packet, "The object specified is not a packet.");
		((CraftPlayer) p).getHandle().playerConnection.sendPacket((Packet<?>) packet);
	}
	
	
	public Object getIChatBaseComponent(String text){
		return new ChatComponentText(text);
	}
	
	
	/*public void openAnvilGUI(Player p, Inventory openAfter, ItemStack target, String beforeName){
    	openAnvilGUI(p, openAfter, target, beforeName, null);
    }
    
    public void openAnvilGUI(Player p, Inventory openAfter, ItemStack target, String beforeName, RunnableObj run){
    	//players.put(p, new Pair<Inventory, RunnableObj>(openAfter, run));
    	AnvilGUI gui = new AnvilGUI(p, new AnvilGUI.AnvilClickEventHandler() {
			public void onAnvilClick(fr.skytasul.quests.utils.nms.v1_12_R1.AnvilGUI.AnvilClickEvent e){
				if (e.getSlot() == AnvilGUI.AnvilSlot.OUTPUT){
					if (e.getName() == null){
						e.setWillClose(false);
						e.setWillDestroy(false);
						return;
					}else if (e.getName().equals("") || e.getName().equals(" ")){
						e.setWillClose(false);
						e.setWillDestroy(false);
						return;
					}
					e.setWillClose(true);
					e.setWillDestroy(true);
					if (target != null) ItemUtils.name(target, e.getName());
					if (run != null) run.run(e.getName());
				}else{
					e.setWillClose(false);
					e.setWillDestroy(false);
				}
			}
		});

		ItemStack i = ItemUtils.item(Material.NAME_TAG, beforeName, 0);

		gui.setSlot(AnvilGUI.AnvilSlot.INPUT_LEFT, i);

		gui.open();
    }
    
    private static class AnvilGUI{
    	class AnvilContainer extends ContainerAnvil {
            public AnvilContainer(EntityHuman entity){
                super(entity.inventory, entity.world,new BlockPosition(0, 0, 0), entity);
            }
     
            
            public boolean a(EntityHuman entityhuman){
                return true;
            }
        }
     
        public enum AnvilSlot {
            INPUT_LEFT(0),
            INPUT_RIGHT(1),
            OUTPUT(2);
     
            private int slot;
     
            private AnvilSlot(int slot){
                this.slot = slot;
            }
     
            public int getSlot(){
                return slot;
            }
     
            public static AnvilSlot bySlot(int slot){
                for(AnvilSlot anvilSlot : values()){
                    if(anvilSlot.getSlot() == slot){
                        return anvilSlot;
                    }
                }
     
                return null;
            }
        }
     
        public class AnvilClickEvent {
            private AnvilSlot slot;
     
            private String name;
     
            private boolean close = true;
            private boolean destroy = true;
     
            public AnvilClickEvent(AnvilSlot slot, String name){
                this.slot = slot;
                this.name = name;
            }
     
            public AnvilSlot getSlot(){
                return slot;
            }
     
            public String getName(){
                return name;
            }
     
            public boolean getWillClose(){
                return close;
            }
     
            public void setWillClose(boolean close){
                this.close = close;
            }
     
            public boolean getWillDestroy(){
                return destroy;
            }
     
            public void setWillDestroy(boolean destroy){
                this.destroy = destroy;
            }
        }
     
        public interface AnvilClickEventHandler {
            public void onAnvilClick(AnvilClickEvent event);
        }
     
        private Player player;
     
        private HashMap<AnvilSlot, ItemStack> items = new HashMap<>();
     
        private Inventory inv;
     
        private Listener listener;
     
        public AnvilGUI(Player player, final AnvilClickEventHandler handler){
            this.player = player;
     
            this.listener = new Listener(){
                @EventHandler
                public void onInventoryClick(InventoryClickEvent event){
                    if(event.getWhoClicked() instanceof Player){
                        Player clicker = (Player) event.getWhoClicked();
     
                        if(event.getInventory().equals(inv)){
                            event.setCancelled(true);
     
                            ItemStack item = event.getCurrentItem();
                            int slot = event.getRawSlot();
                            String name = "";
     
                            if(item != null){
                                if(item.hasItemMeta()){
                                    ItemMeta meta = item.getItemMeta();
     
                                    if(meta.hasDisplayName()){
                                        name = meta.getDisplayName();
                                    }
                                }
                            }
     
                            AnvilClickEvent clickEvent = new AnvilClickEvent(AnvilSlot.bySlot(slot), name);
     
                            handler.onAnvilClick(clickEvent);
     
                            if(clickEvent.getWillClose()){
                                clicker.closeInventory();
                            }
     
                            if(clickEvent.getWillDestroy() && !clickEvent.getWillClose()){
                                destroy();
                            }
                        }
                    }
                }
     
                @EventHandler
                public void onInventoryClose(InventoryCloseEvent event){
                    if(event.getPlayer() instanceof Player){
                        Inventory inv = event.getInventory();
     
                        if(inv.equals(AnvilGUI.this.inv)){
                            inv.clear();
                            destroy();
                        }
                    }
                }
     
                @EventHandler
                public void onPlayerQuit(PlayerQuitEvent event){
                    if(event.getPlayer().equals(getPlayer())){
                        destroy();
                    }
                }
            };
     
            Bukkit.getPluginManager().registerEvents(listener, BeautyQuests.getInstance()); //Replace with instance of main class
        }
     
        public Player getPlayer(){
            return player;
        }
     
        public void setSlot(AnvilSlot slot, ItemStack item){
            items.put(slot, item);
        }
     
        public void open(){
            EntityPlayer p = ((CraftPlayer) player).getHandle();
     
            AnvilContainer container = new AnvilContainer(p);
     
            //Set the items to the items from the inventory given
            inv = container.getBukkitView().getTopInventory();
     
            for(AnvilSlot slot : items.keySet()){
                inv.setItem(slot.getSlot(), items.get(slot));
            }
     
            //Counter stuff that the game uses to keep track of inventories
            int c = p.nextContainerCounter();
     
            //Send the packet
            p.playerConnection.sendPacket(new PacketPlayOutOpenWindow(c, "minecraft:anvil", new ChatMessage("Repairing", new Object[]{}), 0));
     
            //Set their active container to the container
            p.activeContainer = container;
     
            //Set their active container window id to that counter stuff
            p.activeContainer.windowId = c;
     
            //Add the slot listener
            p.activeContainer.addSlotListener(p);
        }
     
		public void destroy(){
        	if (players.get(player).getValue() != null) players.get(player).getValue().run(null);
            NMS.afterOpenGUI(player);
            player = null;
            items = null;
     
            HandlerList.unregisterAll(listener);
     
            listener = null;
        }
    }*/
	
}