package com.partygames.ui;

import com.partygames.PartyGamesPlugin;
import com.partygames.data.Challenge;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import net.runelite.client.party.PartyMember;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.util.ImageUtil;

public class ChallengeBanner extends JPanel
{
	private final JLabel avatar = new JLabel();
	private final JButton acceptButton = new JButton();
	private final JLabel pendingLabel = new JLabel();

	public ChallengeBanner(Challenge challenge, PartyGamesPlugin plugin)
	{
		PartyMember challenger = challenge.getChallenger();
		PartyMember challengee = challenge.getChallengee();

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);

		Border border = BorderFactory.createLineBorder(Color.gray, 1);
		avatar.setBorder(border);
		avatar.setHorizontalAlignment(SwingConstants.CENTER);
		avatar.setVerticalAlignment(SwingConstants.CENTER);
		avatar.setPreferredSize(new Dimension(35, 35));
		ImageIcon icon = new ImageIcon(ImageUtil.resizeImage(challenger.getAvatar(), 32, 32));
		icon.getImage().flush();
		avatar.setIcon(icon);
		add(avatar, BorderLayout.WEST);

		final JPanel namesPanel = new JPanel();
		namesPanel.setLayout(new DynamicGridLayout(2, 1));
		namesPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		namesPanel.setBorder(new EmptyBorder(2, 5, 2, 5));
		final JLabel name = new JLabel();
		name.setText(challenger.getDisplayName() + " -> " + challengee.getDisplayName());
		namesPanel.add(name, BorderLayout.NORTH);

		final JLabel gameType = new JLabel();
		gameType.setText(challenge.getChallengeEvent().getGameType().getText());
		namesPanel.add(gameType, BorderLayout.SOUTH);

		add(namesPanel, BorderLayout.CENTER);

		//TODO wrap in panel for border stuff
		boolean isLocalPlayer = plugin.getPartyService().getLocalMember().getMemberId() == challenger.getMemberId();
		if (isLocalPlayer)
		{
			pendingLabel.setText("Pending");
			add(pendingLabel, BorderLayout.SOUTH);
		}
		else
		{
			acceptButton.setText("Accept");
			acceptButton.setFocusable(false);
			add(acceptButton, BorderLayout.SOUTH);
		}

		acceptButton.addActionListener(e -> plugin.acceptChallenge(challenge));
	}
}
