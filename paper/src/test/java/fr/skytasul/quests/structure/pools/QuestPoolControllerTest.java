package fr.skytasul.quests.structure.pools;

import static fr.skytasul.quests.test.TestUtils.awaitFuture;
import static fr.skytasul.quests.test.TestUtils.loadPlugin;
import static fr.skytasul.quests.test.TestUtils.waitForEvent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.pools.QuestPoolController;
import fr.skytasul.quests.api.pools.QuestPoolData;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.events.QuesterJoinEvent;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.events.questers.QuesterQuestFinishEvent;
import fr.skytasul.quests.api.quests.events.questers.QuesterQuestLaunchEvent;
import fr.skytasul.quests.api.requirements.RequirementList;
import fr.skytasul.quests.api.rewards.RewardList;
import fr.skytasul.quests.options.OptionQuestPool;
import fr.skytasul.quests.options.OptionRepeatable;
import fr.skytasul.quests.options.OptionRequirements;
import fr.skytasul.quests.options.OptionTimer;
import fr.skytasul.quests.requirements.PermissionsRequirement;
import fr.skytasul.quests.requirements.PermissionsRequirement.Permission;
import fr.skytasul.quests.stages.StageDeath;
import fr.skytasul.quests.structure.QuestBranchImplementation;
import fr.skytasul.quests.structure.QuestImplementation;
import fr.skytasul.quests.structure.StageControllerImplementation;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class QuestPoolControllerTest {

	private ServerMock server;
	private BeautyQuests plugin;

	private QuestPoolController pool;
	private @NotNull PlayerMock player;
	private @NotNull Quester quester;

	@BeforeEach
	void setUp() {
		server = MockBukkit.mock();
		plugin = loadPlugin();

		var joinEventWaiter = waitForEvent(QuesterJoinEvent.class);
		player = server.addPlayer();
		quester = joinEventWaiter.assertFired(1000).getQuester();
	}

	@AfterEach
	void tearDown() {
		MockBukkit.unmock();
	}

	@Test
	void testMaxQuests() throws InterruptedException, ExecutionException {
		pool = plugin.getPoolsManager()
				.registerPool(new QuestPoolData("test", null, null, 2, 1, false, 0, false, false, new RequirementList(),
						new RewardList(), new RewardList()));

		for (int i = 0; i < 3; i++)
			createQuest(i, false);

		givePool();
		givePool();

		var canGiveResult = pool.canGive(player);
		assertEquals(QuestPoolController.CanGiveResultType.MAX_QUESTS, canGiveResult.type());
	}

	@Test
	void testQuestsPerLaunch() throws InterruptedException, ExecutionException {
		pool = plugin.getPoolsManager()
				.registerPool(new QuestPoolData("test", null, null, 4, 2, false, 0, false, false, new RequirementList(),
						new RewardList(), new RewardList()));

		for (int i = 0; i < 3; i++)
			createQuest(i, false);

		assertThat(givePool(), hasSize(2));
	}

	@Test
	void testCooldown() throws InterruptedException, ExecutionException {
		pool = plugin.getPoolsManager()
				.registerPool(new QuestPoolData("test", null, null, 4, 2, false, 500, false, false, new RequirementList(),
						new RewardList(), new RewardList()));

		for (int i = 0; i < 3; i++)
			createQuest(i, false);

		assertThat(givePool(), hasSize(2));
		assertEquals(QuestPoolController.CanGiveResultType.ON_COOLDOWN, pool.canGive(player).type());

		Thread.sleep(500);
		assertEquals(QuestPoolController.CanGiveResultType.OK, pool.canGive(player).type());
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void testNoRedo(boolean avoidDuplicates) throws InterruptedException, ExecutionException {
		pool = plugin.getPoolsManager()
				.registerPool(
						new QuestPoolData("test", null, null, 1, 1, false, 0, avoidDuplicates, false, new RequirementList(),
								new RewardList(), new RewardList()));

		var quest0 = createQuest(0, false, "quest0");
		var quest1 = createQuest(1, true, "quest1");

		var permissions = player.addAttachment(plugin);

		var canGiveResult = pool.canGive(player);
		assertEquals(QuestPoolController.CanGiveResultType.NONE_AVAILABLE, canGiveResult.type());

		permissions.setPermission("quest0", true);
		player.recalculatePermissions();

		assertThat(givePool(), contains(0));
		finshQuest(quest0);

		permissions.setPermission("quest1", true);
		player.recalculatePermissions();

		assertThat(givePool(), contains(1));
		finshQuest(quest1);

		canGiveResult = pool.canGive(player);
		assertEquals(QuestPoolController.CanGiveResultType.ALL_COMPLETED, canGiveResult.type());
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void testRedoAllowed(boolean avoidDuplicates) throws InterruptedException, ExecutionException {
		pool = plugin.getPoolsManager()
				.registerPool(
						new QuestPoolData("test", null, null, 1, 1, true, 0, avoidDuplicates, false, new RequirementList(),
								new RewardList(), new RewardList()));

		var quest0 = createQuest(0, false, "quest0");
		var quest1 = createQuest(1, true, "quest1");

		var permissions = player.addAttachment(plugin);

		var canGiveResult = pool.canGive(player);
		assertEquals(QuestPoolController.CanGiveResultType.NONE_AVAILABLE, canGiveResult.type());

		permissions.setPermission("quest0", true);
		player.recalculatePermissions();

		assertThat(givePool(), contains(0));
		finshQuest(quest0);

		permissions.setPermission("quest1", true);
		player.recalculatePermissions();

		assertThat(givePool(), contains(1));
		finshQuest(quest1);

		assertThat(givePool(), contains(1));
		finshQuest(quest1);
	}

	@RepeatedTest(value = 10) // since random is involved, we run this test multiple times
	void testAvoidDuplicatesWork() throws InterruptedException, ExecutionException {
		pool = plugin.getPoolsManager()
				.registerPool(new QuestPoolData("test", null, null, 1, 1, false, 0, true, false, new RequirementList(),
						new RewardList(), new RewardList()));

		var quests = IntStream.range(0, 3).mapToObj(id -> createQuest(id, true)).toList();

		List<Integer> doneQuests = new ArrayList<>();
		for (int i = 0; i < quests.size(); i++) {
			var givenQuests = givePool();
			assertThat(givenQuests, hasSize(1));
			int startedQuest = givenQuests.get(0);

			assertThat(doneQuests, not(hasItem(startedQuest)));
			doneQuests.add(startedQuest);

			finshQuest(quests.get(startedQuest));
		}
	}


	private void finshQuest(Quest quest) {
		var finishWaiter = waitForEvent(QuesterQuestFinishEvent.class);
		quest.finish(quester);
		finishWaiter.assertFired();
	}

	private List<Integer> givePool() throws InterruptedException, ExecutionException {
		var canGiveResult = pool.canGive(player);
		assertEquals(QuestPoolController.CanGiveResultType.OK, canGiveResult.type());

		var launchWaiter = waitForEvent(QuesterQuestLaunchEvent.class);
		var giveResult = pool.give(player);
		awaitFuture(giveResult, 1000, "Pool never gave a quest");
		assertThat(giveResult.get(), startsWith("started quest(s) #"));
		launchWaiter.assertFired(10);

		String[] startedQuests = giveResult.get().substring("started quest(s) ".length()).split(", ");
		return Stream.of(startedQuests).map(x -> Integer.parseInt(x.substring(1))).toList();
	}

	private Quest createQuest(int id, boolean repeatable, String... permissions) {
		var quest = new QuestImplementation(plugin.getQuestsManager(), id, null);

		if (repeatable) {
			var repeatableOption = new OptionRepeatable();
			repeatableOption.setValue(repeatable);
			quest.addOption(repeatableOption);
			var timerOption = new OptionTimer();
			timerOption.setValue(0);
			quest.addOption(timerOption);
		}

		if (permissions.length > 0) {
			var requirementsOption = new OptionRequirements();
			requirementsOption.setValue(new RequirementList(List.of(
					new PermissionsRequirement(null, null, Stream.of(permissions).map(Permission::fromString).toList()))));
			quest.addOption(requirementsOption);
		}

		var poolOption = new OptionQuestPool();
		poolOption.setValue((QuestPoolControllerImplementation) pool);
		quest.addOption(poolOption);

		var branch = new QuestBranchImplementation(quest.getBranchesManager());
		var stageController =
				new StageControllerImplementation<>(branch, QuestsAPI.getAPI().getStages().getType(StageDeath.class).get());
		stageController.setStage(new StageDeath(stageController, List.of()));
		branch.addRegularStage(stageController);
		quest.getBranchesManager().addBranch(branch);

		plugin.getQuestsManager().addQuest(quest);
		return quest;
	}

}
