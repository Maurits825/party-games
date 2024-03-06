package com.partygames;

import com.google.inject.Provides;
import com.partygames.RockPaperScissors.RockPaperScissors;
import com.partygames.data.Challenge;
import com.partygames.data.Game;
import com.partygames.data.GameType;
import com.partygames.data.events.AcceptChallengeEvent;
import com.partygames.data.events.ChallengeEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PartyChanged;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.party.messages.UserSync;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Party Games"
)
public class PartyGamesPlugin extends Plugin
{
	private static final BufferedImage ICON = ImageUtil.loadImageResource(PartyGamesPlugin.class, "icon.png");

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	@Getter
	private PartyService partyService;

	@Inject
	private WSClient wsClient;

	@Inject
	private PartyGamesConfig config;

	private PartyGamesPanel panel;
	private NavigationButton navButton;

	@Getter
	private List<PartyMember> partyMembers;

	@Getter
	private List<Challenge> activeChallenges;
	@Getter
	private final Map<GameType, Game> activeGames = new HashMap<>();

	@Override
	protected void startUp() throws Exception
	{
		panel = injector.getInstance(PartyGamesPanel.class);

		activeChallenges = new ArrayList<>();
		activeGames.clear();

		navButton = NavigationButton.builder()
			.tooltip("Party Games")
			.icon(ICON)
			.priority(7)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		if (partyService.isInParty())
		{
			updatePartyMembers();
		}

		wsClient.registerMessage(ChallengeEvent.class);
		wsClient.registerMessage(AcceptChallengeEvent.class);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		wsClient.unregisterMessage(ChallengeEvent.class);
		wsClient.unregisterMessage(AcceptChallengeEvent.class);

		for (Game game : activeGames.values())
		{
			game.shutDown();
		}
	}

	@Subscribe
	public void onUserSync(final UserSync event)
	{
		this.updatePartyMembers();
	}

	@Subscribe
	public void onPartyChanged(final PartyChanged event)
	{
		this.updatePartyMembers();
	}

	public void challengeMember(PartyMember member, GameType gameType)
	{
		log.debug("send challenge event");
		partyService.send(new ChallengeEvent(partyService.getLocalMember().getMemberId(), member.getMemberId(), gameType));
	}

	public void acceptChallenge(Challenge challenge)
	{
		log.debug("accept challenge");
		partyService.send(new AcceptChallengeEvent(challenge.getChallengeEvent().getChallengeId()));
	}

	@Subscribe
	public void onChallengeEvent(ChallengeEvent event)
	{
		log.debug("on challenge event");
		Challenge challenge = new Challenge(partyService.getMemberById(event.getFromId()), partyService.getMemberById(event.getToId()), event);
		activeChallenges.add(challenge);
		panel.getPartyGamesLobbyPanel().updateChallenges(activeChallenges);
	}

	@Subscribe
	public void onAcceptChallengeEvent(AcceptChallengeEvent acceptChallengeEvent)
	{
		log.debug("on accept challenge event");
		Challenge challenge = null;
		for (Challenge c : activeChallenges)
		{
			if (c.getChallengeEvent().getChallengeId().equals(acceptChallengeEvent.getChallengeId()))
			{
				challenge = c;
				break;
			}
		}

		if (challenge == null)
		{
			return;
		}

		long localMemberId = partyService.getLocalMember().getMemberId();
		ChallengeEvent challengeEvent = challenge.getChallengeEvent();
		activeChallenges.remove(challenge);
		panel.getPartyGamesLobbyPanel().updateChallenges(activeChallenges);

		if (localMemberId == challengeEvent.getFromId() || localMemberId == challengeEvent.getToId())
		{
			GameType gameType = challengeEvent.getGameType();
			if (!activeGames.containsKey(gameType))
			{
				activeGames.put(gameType, getGame(gameType));
			}
			activeGames.get(gameType).initialize(challenge);
		}
	}

	private Game getGame(GameType gameType)
	{
		switch (gameType)
		{
			case ROCK_PAPER_SCISSORS:
			default:
				return new RockPaperScissors(wsClient, eventBus, partyService, panel.getRockPaperScissorsPanel());
		}
	}

	private void updatePartyMembers()
	{
		log.debug("updatePartyMembers");
		partyMembers = partyService.getMembers();//TODO when joining party dont have the name data yet here
		panel.getPartyGamesLobbyPanel().updateAll(); //TODO just redraw everything for now
	}

	@Provides
	PartyGamesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PartyGamesConfig.class);
	}
}
