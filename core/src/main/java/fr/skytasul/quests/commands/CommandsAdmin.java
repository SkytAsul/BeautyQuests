package fr.skytasul.quests.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.commands.OutsideEditor;
import fr.skytasul.quests.api.commands.revxrsal.annotation.*;
import fr.skytasul.quests.api.commands.revxrsal.bukkit.BukkitCommandActor;
import fr.skytasul.quests.api.commands.revxrsal.bukkit.annotation.CommandPermission;
import fr.skytasul.quests.api.commands.revxrsal.exception.CommandErrorException;
import fr.skytasul.quests.api.commands.revxrsal.orphan.OrphanCommand;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.MinecraftNames;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.gui.creation.QuestCreationSession;
import fr.skytasul.quests.gui.misc.ListBook;
import fr.skytasul.quests.npcs.BqNpcImplementation;
import fr.skytasul.quests.players.AdminMode;
import fr.skytasul.quests.players.PlayersManagerDB;
import fr.skytasul.quests.players.PlayersManagerYAML;
import fr.skytasul.quests.structure.QuestImplementation;
import fr.skytasul.quests.utils.Database;
import fr.skytasul.quests.utils.QuestUtils;
import fr.skytasul.quests.utils.nms.NMS;

public class CommandsAdmin implements OrphanCommand {

	@Subcommand ("create")
	@CommandPermission (value = "beautyquests.command.create")
	@OutsideEditor
	public void create(Player player, @Optional @Flag Integer id) {
		QuestCreationSession session = new QuestCreationSession(player);
		if (id != null) {
			if (id.intValue() < 0) throw new CommandErrorException(Lang.NUMBER_NEGATIVE.toString());
			if (QuestsAPI.getAPI().getQuestsManager().getQuest(id) != null)
				throw new CommandErrorException("Invalid quest ID: another quest exists with ID {0}", id);

			session.setCustomID(id);
		}
		session.openStagesGUI(player);
	}

	@Subcommand ("edit")
	@CommandPermission (value = "beautyquests.command.edit")
	@OutsideEditor
	public void edit(Player player, @Optional Quest quest) {
		if (quest != null) {
			QuestCreationSession session = new QuestCreationSession(player);
			session.setQuestEdited((QuestImplementation) quest);
			session.openStagesGUI(player);
		}else {
			Lang.CHOOSE_NPC_STARTER.send(player);
			QuestsPlugin.getPlugin().getEditorManager().getFactory().createNpcSelection(player, () -> {
			}, npc -> {
				if (npc == null) return;
				if (!npc.getQuests().isEmpty()) {
					QuestsPlugin.getPlugin().getGuiManager().getFactory().createQuestSelection(clickedQuest -> {
						QuestCreationSession session = new QuestCreationSession(player);
						session.setQuestEdited((QuestImplementation) quest);
						session.openStagesGUI(player);
					}, null, npc.getQuests()).open(player);
				}else {
					Lang.NPC_NOT_QUEST.send(player);
				}
			}).start();
		}
	}

	@Subcommand ("remove")
	@CommandPermission (value = "beautyquests.command.remove")
	@OutsideEditor
	public void remove(BukkitCommandActor actor, @Optional Quest quest) {
		if (quest != null) {
			doRemove(actor, quest);
		}else {
			Lang.CHOOSE_NPC_STARTER.send(actor.requirePlayer());
			QuestsPlugin.getPlugin().getEditorManager().getFactory().createNpcSelection(actor.getAsPlayer(), () -> {
			}, npc -> {
				if (npc == null) return;
				if (!npc.getQuests().isEmpty()) {
					QuestsPlugin.getPlugin().getGuiManager().getFactory().createQuestSelection(clickedQuest -> {
						doRemove(actor, clickedQuest);
					}, null, npc.getQuests()).open(actor.getAsPlayer());
				}else {
					Lang.NPC_NOT_QUEST.send(actor.getAsPlayer());
				}
			}).start();
		}
	}

	private void doRemove(BukkitCommandActor sender, Quest quest) {
		if (sender.isPlayer()) {
			QuestsPlugin.getPlugin().getGuiManager().getFactory().createConfirmation(() -> {
				quest.delete(false, false);
				Lang.SUCCESFULLY_REMOVED.send(sender.getSender(), quest);
			}, null, Lang.INDICATION_REMOVE.format(quest)).open(sender.getAsPlayer());
		}else {
			quest.delete(false, false);
			Lang.SUCCESFULLY_REMOVED.send(sender.getSender(), quest);
		}
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
			QuestsPlugin.getPlugin().getLoggerExpanded().info("Datas saved ~ manual save from " + actor.getName());
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
		QuestsPlugin.getPlugin().getLoggerExpanded().info("Creating backup due to " + actor.getName() + "'s manual command.");
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
		QuestsPlugin.getPlugin().getGuiManager().closeAndExit(player);
		QuestsPlugin.getPlugin().getEditorManager().leave(player);
	}

	@Subcommand ("reopenInventory")
	@SecretCommand
	public void reopenInventory(Player player) {
		if (QuestsPlugin.getPlugin().getGuiManager().hasGuiOpened(player)) {
			QuestsPlugin.getPlugin().getGuiManager().getOpenedGui(player).open(player);
		}
	}

	@Subcommand ("list")
	@CommandPermission ("beautyquests.command.list")
	public void list(Player player) {
		if (NMS.isValid()) {
			ListBook.openQuestBook(player);
		} else
			MessageUtils.sendMessage(player, "Version not supported", MessageType.DefaultMessageType.PREFIXED);
	}

	@Subcommand ("downloadTranslations")
	@CommandPermission ("beautyquests.command.manage")
	public void downloadTranslations(BukkitCommandActor actor, @Optional String lang, @Switch boolean overwrite) {
		if (MinecraftVersion.MAJOR < 13)
			throw new CommandErrorException(Lang.VERSION_REQUIRED.quickFormat("version", "≥ 1.13"));

		if (lang == null)
			throw new CommandErrorException(Lang.COMMAND_TRANSLATION_SYNTAX.toString());

		String version = MinecraftVersion.VERSION_STRING;
		String url = MinecraftNames.LANG_DOWNLOAD_URL.replace("%version%", version).replace("%language%", lang);

		try {
			File destination = new File(BeautyQuests.getInstance().getDataFolder(), lang + ".json");
			if (destination.isDirectory())
				throw new CommandErrorException(Lang.ERROR_OCCURED.quickFormat("error", lang + ".json is a directory"));
			if (!overwrite && destination.exists())
				throw new CommandErrorException(Lang.COMMAND_TRANSLATION_EXISTS.quickFormat("file_name", lang + ".json"));

			try (ReadableByteChannel channel = Channels.newChannel(new URL(url).openStream())) {
				destination.createNewFile();
				try (FileOutputStream output = new FileOutputStream(destination)) {
					output.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
					Lang.COMMAND_TRANSLATION_DOWNLOADED.quickSend(actor.getSender(), "lang", lang);
				}
			}catch (FileNotFoundException ex) {
				throw new CommandErrorException(
						Lang.COMMAND_TRANSLATION_NOT_FOUND.format(PlaceholderRegistry.of("lang", lang, "version", version)));
			}
		}catch (IOException e) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while downloading translation.", e);
			throw new CommandErrorException(
					Lang.ERROR_OCCURED.quickFormat("error", "IO Exception when downloading translation."));
		}
	}

	@Subcommand ("migrateDatas")
	@CommandPermission ("beautyquests.command.manage")
	public void migrateDatas(BukkitCommandActor actor) {
		if (!(QuestsPlugin.getPlugin().getPlayersManager() instanceof PlayersManagerYAML))
			throw new CommandErrorException("§cYou can't migrate YAML datas to a DB system if you are already using the DB system.");

		QuestUtils.runAsync(() -> {
			actor.reply("§aConnecting to the database.");
			Database db = null;
			try {
				// no try-with-resource because the database is used in another thread
				db = new Database(BeautyQuests.getInstance().getConfig().getConfigurationSection("database"));
				db.testConnection();
				actor.reply("§aConnection to database etablished.");
				final Database fdb = db;
				QuestUtils.runSync(() -> {
					actor.reply("§aStarting migration...");
					try {
						actor.reply(PlayersManagerDB.migrate(fdb,
								(PlayersManagerYAML) QuestsPlugin.getPlugin().getPlayersManager()));
					}catch (Exception ex) {
						actor.error("An exception occured during migration. Process aborted. " + ex.getMessage());
						QuestsPlugin.getPlugin().getLoggerExpanded().severe("Error during data migration", ex);
					}
				});
			} catch (Exception ex) {
				actor.error("§cConnection to database has failed. Aborting. " + ex.getMessage());
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while connecting to the database for datas migration.", ex);
				if (db != null)
					db.close();
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
	public String testNPC(BukkitCommandActor actor, BqNpc npc) {
		((BqNpcImplementation) npc).toggleDebug();
		return npc.toString();
	}

	public enum ItemHologram {
		TALK, LAUNCH, NOLAUNCH;
	}

}
