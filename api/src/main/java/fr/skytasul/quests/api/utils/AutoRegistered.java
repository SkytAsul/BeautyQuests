package fr.skytasul.quests.api.utils;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.bukkit.event.Listener;

/**
 * Marks classes that will be automatically registered as event listener if they implement the
 * {@link Listener} interface.
 * 
 * @author SkytAsul
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface AutoRegistered {
}
