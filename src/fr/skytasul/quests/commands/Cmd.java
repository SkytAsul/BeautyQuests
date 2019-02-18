package fr.skytasul.quests.commands;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
public @interface Cmd {

	/**
	 * If true, the command will not be executed if the executor is not a Player
	 */
	public boolean player() default false;
	
	/**
	 * If arguments amount is lower than this value, the command will not be executed
	 */
	public int min() default 0;
	
	/**
	 * <b>Available :</b>
	 * <ul>
	 * <li> PLAYERS : <i>list of players online</i>
	 * <li> QUESTSID : <i>list of all quests IDs</i>
	 * <li> xxx|yyy|zzz : <i>available values, separated by a pipe (|)</i>
	 * </ul>
	 * In the case of PLAYERS and QUESTSID, they will be directly replaced by an instance of Player or Quest when command executing (no need for String parsing)
	 */
	public String[] args() default {};
	
	/**
	 * Needed permission to execute this command (if empty, no permission will be required)<br>
	 * Final permission will be : <b>beautyquests.command.XXXX</b>
	 */
	public String permission() default "";
	
}
