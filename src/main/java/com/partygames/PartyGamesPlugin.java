package com.partygames;

import com.google.inject.Provides;
import com.partygames.data.ActiveGame;
import com.partygames.data.Challenge;
import com.partygames.data.GameType;
import com.partygames.data.events.AcceptChallengeEvent;
import com.partygames.data.events.ChallengeEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
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
	private List<ActiveGame> activeGames;

	@Override
	protected void startUp() throws Exception
	{
		activeChallenges = new ArrayList<>();
		activeGames = new ArrayList<>();

		panel = new PartyGamesPanel(this);
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

	public void challengeMember(PartyMember member)
	{
		log.debug("send challenge event");
		GameType gameType = GameType.ROCK_PAPER_SCISSORS; //default rps for now
		partyService.send(new ChallengeEvent(partyService.getLocalMember().getMemberId(), member.getMemberId(), gameType));
	}

	public void acceptChallenge(Challenge challenge)
	{
		log.debug("accept challenge");
		partyService.send(new AcceptChallengeEvent(challenge.getChallengeEvent().getChallengeId()));
	}

	public void playMove(String move)
	{
		log.debug("play move: " + move);
	}

	@Subscribe
	public void onChallengeEvent(ChallengeEvent event)
	{
		log.debug("on challenge event");
		Challenge challenge = new Challenge(partyService.getMemberById(event.getFromId()), partyService.getMemberById(event.getToId()), event);
		activeChallenges.add(challenge);
		panel.updateChallenges(activeChallenges);
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
		if (localMemberId == challengeEvent.getFromId() || localMemberId == challengeEvent.getToId())
		{
			activeChallenges.remove(challenge);
			activeGames.add(new ActiveGame(challenge));
			panel.updateChallenges(activeChallenges);
			panel.updateActiveGames(activeGames);
		}
	}

	private void updatePartyMembers()
	{
		partyMembers = partyService.getMembers();//TODO when joining party dont have the name data yet here
		SwingUtilities.invokeLater(() -> panel.updateParty(partyMembers));
	}

	@Provides
	PartyGamesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PartyGamesConfig.class);
	}
}
