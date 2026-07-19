package fr.skytasul.quests.api.editors.parsers;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;

public class CollectionParser<T> implements AbstractParser<T> {
	
	protected Map<String, T> names;
	protected String namesString;

	public CollectionParser(Collection<T> collection, Function<T, String> namer) {
		names = collection.stream().collect(Collectors.toMap(namer, Function.identity()));
		namesString = String.join(", ", names.keySet());
	}

	@Override
	public T parse(String msg) throws ParsingError {
		T obj = names.get(processName(msg));
		if (obj == null)
			throw new ParsingError(Lang.NO_SUCH_ELEMENT.format(PlaceholderRegistry.of("available_elements", namesString)));
		return obj;
	}

	protected String processName(String msg) {
		return msg;
	}
	
	@Override
	public String getIndication() {
		return Lang.AVAILABLE_ELEMENTS.format(PlaceholderRegistry.of("available_elements", namesString));
	}

	public String getNames() {
		return namesString;
	}
	
}