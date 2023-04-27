package fr.skytasul.quests.api.editors.checkers;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.localization.Lang;

public class CollectionParser<T> implements AbstractParser<T> {
	
	protected Map<String, T> names;
	protected String namesString;

	public CollectionParser(Collection<T> collection, Function<T, String> namer) {
		names = collection.stream().collect(Collectors.toMap(namer, Function.identity()));
		namesString = String.join(", ", names.keySet());
	}

	@Override
	public T parse(Player p, String msg) throws Throwable {
		T obj = names.get(processName(msg));
		if (obj == null) Lang.NO_SUCH_ELEMENT.send(p, namesString);
		return obj;
	}

	protected String processName(String msg) {
		return msg;
	}
	
	@Override
	public void sendIndication(Player p) {
		Lang.AVAILABLE_ELEMENTS.send(p, namesString);
	}

	public String getNames() {
		return namesString;
	}
	
}