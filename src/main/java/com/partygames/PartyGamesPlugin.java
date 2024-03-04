package com.partygames;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
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
	private PartyService partyService;

	@Inject
	private WSClient wsClient;

	@Inject
	private PartyGamesConfig config;

	private PartyGamesPanel panel;
	private NavigationButton navButton;

	@Getter
	private List<PartyMember> partyMembers;

	@Override
	protected void startUp() throws Exception
	{
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
			panel.updateParty(partyMembers);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onUserSync(final UserSync event)
	{
		log.debug("user sync!");
	}

	private void UpdatePartyMembers()
	{
		partyMembers = partyService.getMembers();
	}

	@Provides
	PartyGamesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PartyGamesConfig.class);
	}
}
