package com.partygames.RockPaperScissors;

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

public class RockPaperScissorsPanel extends JPanel
{
	private static final String ERROR_PANEL = "ERROR_PANEL";
	private static final String RPS_PANEL = "RPS_PANEL";

	private final CardLayout cardLayout = new CardLayout();

	private final JPanel rpsPanel = new JPanel();
	private final JPanel container = new JPanel(cardLayout);

	private final JLabel status = new JLabel();
	private JPanel movesPanel;

	private final PluginErrorPanel errorPanel = new PluginErrorPanel();

	@Inject
	private RockPaperScissorsPanel()
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(new EmptyBorder(5, 5, 5, 5));

		rpsPanel.setLayout(new BoxLayout(rpsPanel, BoxLayout.Y_AXIS));
		rpsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		rpsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel errorWrapper = new JPanel(new BorderLayout());
		errorWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		errorWrapper.add(errorPanel, BorderLayout.NORTH);

		errorPanel.setContent("No active rock paper scissors game",
			"In the lobby tab you can challenge party members.");

		container.add(rpsPanel, RPS_PANEL);
		container.add(errorWrapper, ERROR_PANEL);

		cardLayout.show(container, ERROR_PANEL);

		add(container, BorderLayout.NORTH);
	}

	public void initialize(RockPaperScissors rockPaperScissors)
	{
		rpsPanel.removeAll();

		Challenge challenge = rockPaperScissors.getChallenge();

		//TODO long user names
//		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
		JPanel headerPanel = new JPanel(new DynamicGridLayout(1, 5));
		headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		headerPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

		headerPanel.add(getAvatar(challenge.getChallenger()));
		headerPanel.add(new JLabel(challenge.getChallenger().getDisplayName()));
		headerPanel.add(new JLabel("vs"));
		headerPanel.add(getAvatar(challenge.getChallengee()));
		headerPanel.add(new JLabel(challenge.getChallengee().getDisplayName()));

		rpsPanel.add(headerPanel);

		JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		statusPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		statusPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		status.setText(rockPaperScissors.getStatusText());
		statusPanel.add(status);
		rpsPanel.add(statusPanel);

		movesPanel = createMovesPanel(rockPaperScissors);
		rpsPanel.add(movesPanel);

		cardLayout.show(container, RPS_PANEL);
	}

	public void update(RockPaperScissors rockPaperScissors)
	{
		movesPanel.setVisible(rockPaperScissors.isLocalPlayerMove());
		status.setText(rockPaperScissors.getStatusText());
	}

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

	private JPanel createMovesPanel(RockPaperScissors rockPaperScissors)
	{
		JPanel movesPanel = new JPanel(new GridLayout(1, 3));
		movesPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		movesPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
//		movesPanel.setPreferredSize(new Dimension(0, 30));

		for (RockPaperScissors.Move move : RockPaperScissors.Move.values())
		{
			JButton moveButton = new JButton();
			moveButton.setText(move.getText());
			moveButton.setFocusable(false);
			moveButton.addActionListener(e -> rockPaperScissors.move(move));
			movesPanel.add(moveButton);
		}

		return movesPanel;
	}
}
