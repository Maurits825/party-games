package com.partygames.CoinFlip;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.PluginErrorPanel;

public class CoinFlipPanel extends JPanel
{
	private static final String ERROR_PANEL = "ERROR_PANEL";
	private static final String COIN_FLIP_PANEL = "COIN_PANEL";

	private final CardLayout cardLayout = new CardLayout();

	private final JPanel coinFlipPanel = new JPanel();
	private final JPanel container = new JPanel(cardLayout);
	private final PluginErrorPanel errorPanel = new PluginErrorPanel();

	@Inject
	private CoinFlipPanel()
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(new EmptyBorder(5, 5, 5, 5));

		coinFlipPanel.setLayout(new BoxLayout(coinFlipPanel, BoxLayout.Y_AXIS));
		coinFlipPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		coinFlipPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel errorWrapper = new JPanel(new BorderLayout());
		errorWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		errorWrapper.add(errorPanel, BorderLayout.NORTH);

		errorPanel.setContent("No active coin flip game",
			"In the lobby tab you can challenge party members.");

		container.add(coinFlipPanel, COIN_FLIP_PANEL);
		container.add(errorWrapper, ERROR_PANEL);

		cardLayout.show(container, ERROR_PANEL);

		add(container, BorderLayout.NORTH);
	}

	public void initialize(CoinFlip coinFlip)
	{
		coinFlipPanel.removeAll();

		cardLayout.show(container, COIN_FLIP_PANEL);
	}
}
