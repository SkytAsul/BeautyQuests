package fr.skytasul.quests.commands;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.bukkit.entity.Player;

import fr.skytasul.quests.Quest;
import net.citizensnpcs.api.npc.NPC;

@Retention(RUNTIME)
@Target(METHOD)
public @interface Cmd {

	/**
	 * If true, the command will not be executed if the executor is not a Player
	 * @return true if the command <i>need</i> to be executed by a player
	 */
	public boolean player() default false;
	
	/**
	 * Need {@link #player()} to be true
	 * @return if the player must not be in an inventory/editor system to execute the command
	 */
	public boolean noEditorInventory() default false;
	
	/**
	 * If arguments amount is lower than this value, the command will not be executed
	 * @return minimal amount of arguments for the command to be executed
	 */
	public int min() default 0;
	
	/**
	 * <b>Available :</b>
	 * <ul>
	 * <li> PLAYERS : <i>list of players online</i>
	 * <li> QUESTSID : <i>list of all quests IDs</i>
	 * <li> NPCSID : <i>list of all NPCs IDs</i>
	 * <li> xxx|yyy|zzz : <i>available values, separated by a pipe (|)</i>
	 * </ul>
	 * In the case of PLAYERS QUESTSID and NPCSID, they will be directly replaced by an instance of {@link Player}/{@link Quest}/{@link NPC} when command executing (no need for String parsing)
	 * @return String array of possibles arguments
	 */
	public String[] args() default {};
	
	/**
	 * Needed permission to execute this command (if empty, no permission will be required)<br>
	 * Final permission will be : <b>beautyquests.command.XXXX</b>
	 * @return name of the permission
	 */
	public String permission() default "";
	
}
