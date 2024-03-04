package com.partygames;

import com.google.inject.Provides;
import com.partygames.data.Challenge;
import com.partygames.data.events.ChallengeEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
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
	private List<Challenge> activeChallenges = new ArrayList<>();

	@Override
	protected void startUp() throws Exception
	{
		activeChallenges = new ArrayList<>();

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
			UpdatePartyMembers();
		}

		wsClient.registerMessage(ChallengeEvent.class);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		wsClient.unregisterMessage(ChallengeEvent.class);
	}

	@Subscribe
	public void onUserSync(final UserSync event)
	{
		log.debug("user sync!");
	}

	public void challengeMember(PartyMember member)
	{
		log.debug("send challenge event");
		partyService.send(new ChallengeEvent(partyService.getLocalMember().getMemberId(), member.getMemberId()));
	}

	public void acceptChallenge(Challenge challenge)
	{
		log.debug("accept challenge");
	}

	@Subscribe
	public void onChallengeEvent(ChallengeEvent event)
	{
		log.debug("on challenge event");
		Challenge challenge = new Challenge(partyService.getMemberById(event.getFromId()), partyService.getMemberById(event.getToId()), event);
		activeChallenges.add(challenge);
		panel.updateChallenges(activeChallenges);
	}

	private void UpdatePartyMembers()
	{
		partyMembers = partyService.getMembers();
		panel.updateParty(partyMembers);
	}

	@Provides
	PartyGamesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PartyGamesConfig.class);
	}
}
