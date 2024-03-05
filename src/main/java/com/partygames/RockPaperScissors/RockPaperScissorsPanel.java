package com.partygames.RockPaperScissors;

import com.partygames.PartyGamesPlugin;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.PluginErrorPanel;

public class RockPaperScissorsPanel extends JPanel
{
	private static final String ERROR_PANEL = "ERROR_PANEL";
	private static final String RPS_PANEL = "RPS_PANEL";

	private final GridBagConstraints constraints = new GridBagConstraints();
	private final CardLayout cardLayout = new CardLayout();

	private final JPanel rpsPanel = new JPanel();
	private final JPanel centerPanel = new JPanel(cardLayout);

	private final PluginErrorPanel errorPanel = new PluginErrorPanel();

	private final List<RockPaperScissorGamePanel> rockPaperScissorGamePanelList = new ArrayList<>();

	@Inject
	private RockPaperScissorsPanel(PartyGamesPlugin plugin)
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel container = new JPanel();
		container.setLayout(new BorderLayout());
		container.setBorder(new EmptyBorder(10, 10, 10, 10));
		container.setBackground(ColorScheme.DARK_GRAY_COLOR);

		rpsPanel.setLayout(new GridBagLayout());
		rpsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;

		// This panel wraps the rps panel
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wrapper.add(rpsPanel, BorderLayout.NORTH);

		JScrollPane rpsWrapper = new JScrollPane(wrapper);
		rpsWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		rpsWrapper.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
		rpsWrapper.getVerticalScrollBar().setBorder(new EmptyBorder(0, 5, 0, 0));
		rpsWrapper.setVisible(false);

		// This panel wraps the error panel and limits its height
		JPanel errorWrapper = new JPanel(new BorderLayout());
		errorWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		errorWrapper.add(errorPanel, BorderLayout.NORTH);

		errorPanel.setContent("No active rock paper scissors game",
			"In the lobby tab you can challenge party members.");

		centerPanel.add(rpsWrapper, RPS_PANEL);
		centerPanel.add(errorWrapper, ERROR_PANEL);
		cardLayout.show(centerPanel, ERROR_PANEL);

		container.add(centerPanel, BorderLayout.CENTER);

		add(container, BorderLayout.CENTER);
	}

	public RockPaperScissorGamePanel addRPSGame(RockPaperScissors rpsGame)
	{
		RockPaperScissorGamePanel gamePanel = new RockPaperScissorGamePanel(rpsGame);
//		rockPaperScissorGamePanelList.add(gamePanel); //TODO test, if needed ->
		rpsPanel.add(gamePanel, constraints);
		constraints.gridy++;
		rpsPanel.revalidate(); //TODO needed?

		cardLayout.show(centerPanel, RPS_PANEL);

		return gamePanel;
	}
}
