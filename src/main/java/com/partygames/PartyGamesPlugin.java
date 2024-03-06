package com.partygames;

import com.google.inject.Provides;
import com.partygames.CoinFlip.CoinFlip;
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
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PartyChanged;
import net.runelite.client.events.PartyMemberAvatar;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.party.events.UserJoin;
import net.runelite.client.party.events.UserPart;
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
	private PartyGamesLobbyPanel lobbyPanel;
	private NavigationButton navButton;

	@Getter
	private List<PartyMember> partyMembers = new ArrayList<>();
	@Getter
	private List<Challenge> pendingChallenges = new ArrayList<>();

	@Getter
	private final Map<GameType, Game> activeGames = new HashMap<>();

	@Override
	protected void startUp() throws Exception
	{
		panel = injector.getInstance(PartyGamesPanel.class);
		lobbyPanel = panel.getPartyGamesLobbyPanel();

		pendingChallenges = new ArrayList<>();
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
			for (PartyMember member : partyService.getMembers())
			{
				if (member.getMemberId() != getLocalPlayerId())
				{
					lobbyPanel.addPartyMember(member);
				}
			}
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
	public void onUserJoin(final UserJoin event)
	{
		if (isLocalPlayer(event.getMemberId()))
		{
			return;
		}
		log.debug("onUserJoin");
		SwingUtilities.invokeLater(() -> lobbyPanel.addPartyMember(partyService.getMemberById(event.getMemberId())));
	}

	@Subscribe
	public void onUserPart(final UserPart event)
	{
		if (isLocalPlayer(event.getMemberId()))
		{
			return;
		}
		SwingUtilities.invokeLater(() -> lobbyPanel.removePartyMember(event.getMemberId()));
	}

	@Subscribe
	public void onPartyMemberAvatar(PartyMemberAvatar event)
	{
		SwingUtilities.invokeLater(() -> lobbyPanel.updatePartyMemberBanner(event.getMemberId()));
	}

	@Subscribe
	public void onUserSync(final UserSync event)
	{
		log.debug("onUserSync"); //TODO do stuff?
	}

	@Subscribe
	public void onPartyChanged(final PartyChanged event)
	{
		log.debug("onPartyChanged");
		SwingUtilities.invokeLater(lobbyPanel::removeAllPartyMembers);
	}

	public void challengeMember(PartyMember member, GameType gameType)
	{
		partyService.send(new ChallengeEvent(partyService.getLocalMember().getMemberId(), member.getMemberId(), gameType));
	}

	public void acceptChallenge(Challenge challenge)
	{
		partyService.send(new AcceptChallengeEvent(challenge.getChallengeEvent().getChallengeId()));
	}

	@Subscribe
	public void onChallengeEvent(ChallengeEvent event)
	{
		long localPlayerId = getLocalPlayerId();
		if (event.getFromId() == localPlayerId || event.getToId() == localPlayerId)
		{
			Challenge challenge = new Challenge(partyService.getMemberById(event.getFromId()), partyService.getMemberById(event.getToId()), event);
			pendingChallenges.add(challenge);

			lobbyPanel.updateAllPartyMemberBanner();
			lobbyPanel.addPendingChallenge(challenge);
		}
	}

	@Subscribe
	public void onAcceptChallengeEvent(AcceptChallengeEvent acceptChallengeEvent)
	{
		log.debug("on accept challenge event");
		Challenge challenge = null;
		for (Challenge c : pendingChallenges)
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

		long localMemberId = getLocalPlayerId();
		ChallengeEvent challengeEvent = challenge.getChallengeEvent();
		pendingChallenges.remove(challenge);

		lobbyPanel.updateAllPartyMemberBanner();
		lobbyPanel.removePendingChallenge(challenge);

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

	public Long getLocalPlayerId()
	{
		if (partyService.isInParty())
		{
			return partyService.getLocalMember().getMemberId();
		}
		return null;
	}

	public boolean isLocalPlayer(long id)
	{
		return partyService.getLocalMember() != null && partyService.getLocalMember().getMemberId() == id;
	}

	public boolean pendingChallengeExists(PartyMember member, GameType gameType)
	{
		long localPlayerId = partyService.getLocalMember().getMemberId();
		for (Challenge challenge : pendingChallenges)
		{
			if (gameType != challenge.getChallengeEvent().getGameType())
			{
				continue;
			}

			if (localPlayerId == challenge.getChallenger().getMemberId() && member.getMemberId() == challenge.getChallengee().getMemberId())
			{
				return true;
			}

			if (localPlayerId == challenge.getChallengee().getMemberId() && member.getMemberId() == challenge.getChallenger().getMemberId())
			{
				return true;
			}
		}
		return false;
	}

	private Game getGame(GameType gameType)
	{
		switch (gameType)
		{
			case COIN_FLIP:
				return new CoinFlip(wsClient, eventBus, partyService, panel.getCoinFlipPanel(), client);
			case ROCK_PAPER_SCISSORS:
			default:
				return new RockPaperScissors(wsClient, eventBus, partyService, panel.getRockPaperScissorsPanel(), client);
		}
	}

	@Provides
	PartyGamesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PartyGamesConfig.class);
	}
}
