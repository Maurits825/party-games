package com.partygames.coinflip;

import com.partygames.data.Challenge;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.client.party.PartyMember;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ImageUtil;

public class CoinFlipPanel extends JPanel
{
	private static final String ERROR_PANEL = "ERROR_PANEL";
	private static final String COIN_FLIP_PANEL = "COIN_PANEL";

	private final CardLayout cardLayout = new CardLayout();

	private final JPanel coinFlipPanel = new JPanel();
	private final JPanel container = new JPanel(cardLayout);
	private final PluginErrorPanel errorPanel = new PluginErrorPanel();

	private final JLabel status = new JLabel();
	private JPanel pickPanel;

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

		Challenge challenge = coinFlip.getChallenge();
		JPanel headerPanel = getHeaderPanel(challenge);
		coinFlipPanel.add(headerPanel);

		JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		statusPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		statusPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		status.setText(coinFlip.getStatusText());
		statusPanel.add(status);
		coinFlipPanel.add(statusPanel);

		if (coinFlip.isLocalPlayerMove())
		{
			pickPanel = createCoinPickPanel(coinFlip);
			coinFlipPanel.add(pickPanel);
		}

		cardLayout.show(container, COIN_FLIP_PANEL);
	}

	public void update(CoinFlip coinFlip)
	{
		if (pickPanel != null)
		{
			pickPanel.setVisible(coinFlip.isLocalPlayerMove());
		}

		status.setText(coinFlip.getStatusText());
	}

	private JPanel getHeaderPanel(Challenge challenge)
	{
		JPanel headerPanel = new JPanel(new DynamicGridLayout(1, 5));
		headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		headerPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

		headerPanel.add(getAvatar(challenge.getChallenger()));
		headerPanel.add(new JLabel(challenge.getChallenger().getDisplayName()));
		headerPanel.add(new JLabel("vs"));
		headerPanel.add(getAvatar(challenge.getChallengee()));
		headerPanel.add(new JLabel(challenge.getChallengee().getDisplayName()));
		return headerPanel;
	}

	private JPanel createCoinPickPanel(CoinFlip coinFlip)
	{
		JPanel pickPanel = new JPanel(new GridLayout(1, 2));
		pickPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		pickPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		for (CoinFlip.CoinSide coinSide : CoinFlip.CoinSide.values())
		{
			JButton coinButton = new JButton();
			coinButton.setText(coinSide.getText());
			coinButton.setFocusable(false);
			coinButton.addActionListener(e ->
			{
				coinButton.setEnabled(false);
				coinFlip.flip(coinSide);
			});
			pickPanel.add(coinButton);
		}

		return pickPanel;
	}

	//TODO put in utils?
	private JLabel getAvatar(PartyMember member)
	{
		JLabel avatar = new JLabel();
		avatar.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
		avatar.setHorizontalAlignment(SwingConstants.CENTER);
		avatar.setVerticalAlignment(SwingConstants.CENTER);
		avatar.setPreferredSize(new Dimension(35, 35));

		if (member.getAvatar() != null)
		{
			ImageIcon icon = new ImageIcon(ImageUtil.resizeImage(member.getAvatar(), 32, 32));
			icon.getImage().flush();
			avatar.setIcon(icon);
		}

		return avatar;
	}
}
