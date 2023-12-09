package fr.skytasul.quests.utils.compatibility;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.utils.IntegrationManager.BQDependency;

public final class InternalIntegrations {

	public static final BQDependency PlayerBlockTracker = new BQDependency("PlayerBlockTracker");
	public static final BQDependency AccountsHook = new BQDependency("AccountsHook");

	static {
		BeautyQuests.getInstance().getIntegrationManager().addDependency(PlayerBlockTracker);
		BeautyQuests.getInstance().getIntegrationManager().addDependency(AccountsHook);
	}

	private InternalIntegrations() {}

}
