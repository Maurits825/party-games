package com.partygames;

import com.partygames.CoinFlip.CoinFlipPanel;
import com.partygames.RockPaperScissors.RockPaperScissorsPanel;
import java.awt.BorderLayout;
import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;

public class PartyGamesPanel extends PluginPanel
{
	// this panel will hold the lobby panel or a game panel
	private final JPanel display = new JPanel();

	private final MaterialTabGroup tabGroup = new MaterialTabGroup(display);

	@Getter
	private final PartyGamesLobbyPanel partyGamesLobbyPanel;
	@Getter
	private final RockPaperScissorsPanel rockPaperScissorsPanel;
	@Getter
	private final CoinFlipPanel coinFlipPanel;

	@Inject
	private PartyGamesPanel(PartyGamesLobbyPanel partyGamesLobbyPanel, RockPaperScissorsPanel rockPaperScissorsPanel, CoinFlipPanel coinFlipPanel)
	{
		this.partyGamesLobbyPanel = partyGamesLobbyPanel;
		this.rockPaperScissorsPanel = rockPaperScissorsPanel;
		this.coinFlipPanel = coinFlipPanel;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		MaterialTab lobbyTab = new MaterialTab("Lobby", tabGroup, partyGamesLobbyPanel);
		MaterialTab RPSTab = new MaterialTab("RPS", tabGroup, rockPaperScissorsPanel);
		MaterialTab coinFlipTab = new MaterialTab("Coin", tabGroup, coinFlipPanel);

		tabGroup.setBorder(new EmptyBorder(5, 0, 0, 0));
		tabGroup.addTab(lobbyTab);
		tabGroup.addTab(RPSTab);
		tabGroup.addTab(coinFlipTab);
		tabGroup.select(lobbyTab); // selects the default selected tab

		add(tabGroup, BorderLayout.NORTH);
		add(display, BorderLayout.CENTER);
	}
}
