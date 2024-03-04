package com.partygames.RockPaperScissors;

import com.partygames.PartyGamesPlugin;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.PluginErrorPanel;

public class RockPaperScissorsPanel extends JPanel
{
	private static final String ERROR_PANEL = "ERROR_PANEL";
	private static final String RPS_PANEL = "RPS_PANEL";

	// The rps container, this will hold all the rps games
	private final JPanel rpsPanel = new JPanel();

	// The error panel, this displays an error message
	private final PluginErrorPanel errorPanel = new PluginErrorPanel();

	private final GridBagConstraints constraints = new GridBagConstraints();
	private final CardLayout cardLayout = new CardLayout();

	private final List<RockPaperScissorGamePanel> rockPaperScissorGamePanelList = new ArrayList<>();

	// The center panel, this holds either the error panel or the rps panel
	private final JPanel container = new JPanel(cardLayout);


	@Inject
	private RockPaperScissorsPanel(PartyGamesPlugin plugin)
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.BRAND_ORANGE); //TODO color

		//TODO what does do
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;

		// This panel wraps the games panel -- and guarantees the scrolling behaviour?
		JPanel rpsWrapper = new JPanel(new BorderLayout());
		rpsWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		rpsWrapper.add(rpsPanel, BorderLayout.NORTH);
		rpsPanel.setLayout(new BoxLayout(rpsPanel, BoxLayout.Y_AXIS));
		rpsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		rpsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		/* This panel wraps the error panel and limits its height */
		JPanel errorWrapper = new JPanel(new BorderLayout());
		errorWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		errorWrapper.add(errorPanel, BorderLayout.NORTH);

		errorPanel.setContent("No active rock paper scissors game",
			"In the lobby tab you can challenge party members.");

		container.add(rpsWrapper, RPS_PANEL);
		container.add(errorWrapper, ERROR_PANEL);
		cardLayout.show(container, ERROR_PANEL);

		add(container, BorderLayout.CENTER);
	}

	public RockPaperScissorGamePanel addRPSGame(RockPaperScissors rpsGame)
	{
		RockPaperScissorGamePanel gamePanel = new RockPaperScissorGamePanel(rpsGame);
		rockPaperScissorGamePanelList.add(gamePanel);
		rpsPanel.add(gamePanel);
		rpsPanel.revalidate(); //TODO needed?

		cardLayout.show(container, RPS_PANEL);

		return gamePanel;
	}
}
