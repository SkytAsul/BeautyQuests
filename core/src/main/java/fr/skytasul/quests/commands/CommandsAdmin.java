package fr.skytasul.quests.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.sql.SQLException;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.SelectNPC;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.creation.QuestCreationSession;
import fr.skytasul.quests.gui.misc.ConfirmGUI;
import fr.skytasul.quests.gui.misc.ListBook;
import fr.skytasul.quests.gui.pools.PoolsManageGUI;
import fr.skytasul.quests.gui.quests.ChooseQuestGUI;
import fr.skytasul.quests.players.AdminMode;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.players.PlayersManagerDB;
import fr.skytasul.quests.players.PlayersManagerYAML;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Database;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.nms.NMS;

import revxrsal.commands.annotation.Flag;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.SecretCommand;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Switch;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.orphan.OrphanCommand;

public class CommandsAdmin implements OrphanCommand {
	
	@Subcommand ("create")
	@CommandPermission (value = "beautyquests.command.create")
	@OutsideEditor
	public void create(Player player, @Optional @Flag Integer id) {
		QuestCreationSession session = new QuestCreationSession();
		if (id != null) {
			if (id.intValue() < 0) throw new CommandErrorException(Lang.NUMBER_NEGATIVE.toString());
			if (QuestsAPI.getQuests().getQuest(id) != null)
				throw new CommandErrorException("Invalid quest ID: another quest exists with ID {0}", id);
			
			session.setCustomID(id);
		}
		session.openMainGUI(player);
	}
	
	@Subcommand ("edit")
	@CommandPermission (value = "beautyquests.command.edit")
	@OutsideEditor
	public void edit(Player player, @Optional Quest quest) {
		if (quest != null) {
			new QuestCreationSession(quest).openMainGUI(player);
		}else {
			Lang.CHOOSE_NPC_STARTER.send(player);
			new SelectNPC(player, () -> {}, npc -> {
				if (npc == null) return;
				if (!npc.getQuests().isEmpty()) {
					new ChooseQuestGUI(npc.getQuests(), questClicked -> {
						if (questClicked == null) return;
						new QuestCreationSession(questClicked).openMainGUI(player);
					}).create(player);
				}else {
					Lang.NPC_NOT_QUEST.send(player);
				}
			}).enter();
		}
	}
	
	@Subcommand ("remove")
	@CommandPermission (value = "beautyquests.command.remove")
	@OutsideEditor
	public void remove(BukkitCommandActor actor, @Optional Quest quest) {
		if (quest != null) {
			remove(actor.getSender(), quest);
		}else {
			Lang.CHOOSE_NPC_STARTER.send(actor.requirePlayer());
			new SelectNPC(actor.getAsPlayer(), () -> {}, npc -> {
				if (npc == null) return;
				if (!npc.getQuests().isEmpty()) {
					new ChooseQuestGUI(npc.getQuests(), questClicked -> {
						if (questClicked == null) return;
						remove(actor.getSender(), questClicked);
					}).create(actor.getAsPlayer());
				}else {
					Lang.NPC_NOT_QUEST.send(actor.getAsPlayer());
				}
			}).enter();
		}
	}
	
	private void remove(CommandSender sender, Quest quest) {
		if (sender instanceof Player) {
			Inventories.create((Player) sender, new ConfirmGUI(() -> {
				quest.remove(true, true);
				Lang.SUCCESFULLY_REMOVED.send(sender, quest.getName());
			}, ((Player) sender)::closeInventory, Lang.INDICATION_REMOVE.format(quest.getName())));
		}else {
			quest.remove(true, true);
			Lang.SUCCESFULLY_REMOVED.send(sender, quest.getName());
		}
	}
	
	@Subcommand ("pools")
	@CommandPermission ("beautyquests.command.pools")
	public void pools(Player player) {
		PoolsManageGUI.get().create(player);
	}
	
	@Subcommand ("reload")
	@CommandPermission ("beautyquests.command.manage")
	public void reload(BukkitCommandActor actor) {
		BeautyQuests.getInstance().performReload(actor.getSender());
	}
	
	@Subcommand ("save")
	@CommandPermission ("beautyquests.command.manage")
	public void save(BukkitCommandActor actor) {
		try {
			BeautyQuests.getInstance().saveAllConfig(false);
			actor.reply("§aDatas saved!");
			BeautyQuests.logger.info("Datas saved ~ manual save from " + actor.getName());
		}catch (Throwable e) {
			e.printStackTrace();
			actor.error("Error while saving the data file.");
		}
	}
	
	@Subcommand ("backup")
	@CommandPermission ("beautyquests.command.manage")
	public void backup(BukkitCommandActor actor, @Switch boolean force) {
		if (!force) save(actor);
		
		boolean success = true;
		BeautyQuests.logger.info("Creating backup due to " + actor.getName() + "'s manual command.");
		Path backup = BeautyQuests.getInstance().backupDir();
		if (!BeautyQuests.getInstance().createFolderBackup(backup)) {
			Lang.BACKUP_QUESTS_FAILED.send(actor.getSender());
			success = false;
		}
		if (!BeautyQuests.getInstance().createDataBackup(backup)) {
			Lang.BACKUP_PLAYERS_FAILED.send(actor.getSender());
			success = false;
		}
		if (success) Lang.BACKUP_CREATED.send(actor.getSender());
	}
	
	@Subcommand ("adminMode")
	@CommandPermission ("beautyquests.command.adminMode")
	public void adminMode(BukkitCommandActor actor) {
		AdminMode.toggle(actor.getSender());
	}
	
	@Subcommand ("exitEditor")
	@SecretCommand
	public void exitEditor(Player player) {
		Editor.leave(player);
		Inventories.closeAndExit(player);
	}
	
	@Subcommand ("reopenInventory")
	@SecretCommand
	public void reopenInventory(Player player) {
		if (Inventories.isInSystem(player)) {
			Inventories.openInventory(player);
		}
	}
	
	@Subcommand ("list")
	@CommandPermission ("beautyquests.command.list")
	public void list(Player player) {
		if (NMS.isValid()) {
			ListBook.openQuestBook(player);
		}else Utils.sendMessage(player, "Version not supported");
	}
	
	@Subcommand ("downloadTranslations")
	@CommandPermission ("beautyquests.command.manage")
	public void downloadTranslations(BukkitCommandActor actor, @Optional String lang, @Switch boolean overwrite) {
		if (NMS.getMCVersion() < 13)
			throw new CommandErrorException(Lang.VERSION_REQUIRED.format("≥ 1.13"));
		
		if (lang == null)
			throw new CommandErrorException(Lang.COMMAND_TRANSLATION_SYNTAX.toString());
		
		String version = NMS.getVersionString();
		String url = MinecraftNames.LANG_DOWNLOAD_URL.replace("%version%", version).replace("%language%", lang);
		
		try {
			File destination = new File(BeautyQuests.getInstance().getDataFolder(), lang + ".json");
			if (destination.isDirectory())
				throw new CommandErrorException(Lang.ERROR_OCCURED.format(lang + ".json is a directory"));
			if (!overwrite && destination.exists())
				throw new CommandErrorException(Lang.COMMAND_TRANSLATION_EXISTS.format(lang + ".json"));
			
			try (ReadableByteChannel channel = Channels.newChannel(new URL(url).openStream())) {
				destination.createNewFile();
				try (FileOutputStream output = new FileOutputStream(destination)) {
					output.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
					Lang.COMMAND_TRANSLATION_DOWNLOADED.send(actor.getSender(), lang);
				}
			}catch (FileNotFoundException ex) {
				throw new CommandErrorException(Lang.COMMAND_TRANSLATION_NOT_FOUND.format(lang, version));
			}
		}catch (IOException e) {
			BeautyQuests.logger.severe("An error occurred while downloading translation.", e);
			throw new CommandErrorException(Lang.ERROR_OCCURED.format("IO Exception when downloading translation."));
		}
	}
	
	@Subcommand ("migrateDatas")
	@CommandPermission ("beautyquests.command.manage")
	public void migrateDatas(BukkitCommandActor actor) {
		if (!(PlayersManager.manager instanceof PlayersManagerYAML))
			throw new CommandErrorException("§cYou can't migrate YAML datas to a DB system if you are already using the DB system.");
		
		Utils.runAsync(() -> {
			actor.reply("§aConnecting to the database.");
			try (Database db = new Database(BeautyQuests.getInstance().getConfig().getConfigurationSection("database"))) {
				db.testConnection();
				actor.reply("§aConnection to database etablished.");
				final Database fdb = db;
				Utils.runSync(() -> {
					actor.reply("§aStarting migration...");
					try {
						actor.reply(PlayersManagerDB.migrate(fdb, (PlayersManagerYAML) PlayersManager.manager));
					}catch (Exception ex) {
						actor.error("An exception occured during migration. Process aborted. " + ex.getMessage());
						BeautyQuests.logger.severe("Error during data migration", ex);
					}
				});
			}catch (SQLException ex) {
				actor.error("§cConnection to database has failed. Aborting. " + ex.getMessage());
				BeautyQuests.logger.severe("An error occurred while connecting to the database for datas migration.", ex);
			}
		});
	}
	
	@Subcommand ("setItem")
	@CommandPermission ("beautyquests.command.setItem")
	public void setItem(Player player, ItemHologram position) {
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item.getType() == Material.AIR) {
			BeautyQuests.getInstance().getDataFile().set(position.name().toLowerCase() + "Item", null);
			Lang.ITEM_REMOVED.send(player);
			return;
		}
		BeautyQuests.getInstance().getDataFile().set(position.name().toLowerCase() + "Item", item.serialize());
		Lang.ITEM_CHANGED.send(player);
	}
	
	@Subcommand ("setFirework")
	@CommandPermission ("beautyquests.command.setItem")
	public void setFirework(Player player, @Switch boolean remove) {
		if (remove) {
			BeautyQuests.getInstance().getDataFile().set("firework", "none");
			Lang.FIREWORK_REMOVED.send(player);
			Lang.RESTART_SERVER.send(player);
		}else {
			ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
			if (meta instanceof FireworkMeta) {
				BeautyQuests.getInstance().getDataFile().set("firework", meta);
				Lang.FIREWORK_EDITED.send(player);
				Lang.RESTART_SERVER.send(player);
			}else {
				Lang.FIREWORK_INVALID_HAND.send(player);
			}
		}
	}
	
	@Subcommand ("testNPC")
	@CommandPermission (value = "beautyquests.command.create")
	@SecretCommand
	public void testNPC(BukkitCommandActor actor, BQNPC npc) {
		Utils.sendMessage(actor.getSender(), npc.toString());
		npc.toggleDebug();
	}
	
	public enum ItemHologram {
		TALK, LAUNCH, NOLAUNCH;
	}
	
}
