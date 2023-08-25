package fr.skytasul.quests.utils.nms;

import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import net.minecraft.server.v1_16_R3.*;

public class v1_16_R3 extends NMS{

	@Override
	public void openBookInHand(Player p) {
		PacketPlayOutOpenBook packet = new PacketPlayOutOpenBook(EnumHand.MAIN_HAND);
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}

	@Override
	public double entityNameplateHeight(Entity en){
		return en.getHeight();
	}
	
	public Object getIChatBaseComponent(String text){
		return new ChatComponentText(text);
	}

	public Object getEnumChatFormat(int value){
		return EnumChatFormat.a(value);
	}
	
	@Override
	public List<String> getAvailableBlockProperties(Material material) {
		Block block = IRegistry.BLOCK.get(new MinecraftKey(material.getKey().getKey()));
		BlockStateList<Block, IBlockData> stateList = block.getStates();
		return stateList.d().stream().map(IBlockState::getName).collect(Collectors.toList());
	}
	
	@Override
	public List<String> getAvailableBlockTags() {
		return Tags.c().a().keySet().stream().map(MinecraftKey::toString).collect(Collectors.toList());
	}
	
}