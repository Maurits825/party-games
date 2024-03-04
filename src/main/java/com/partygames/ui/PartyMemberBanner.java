package com.partygames.ui;

import com.partygames.PartyGamesPlugin;
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

public class PartyMemberBanner extends JPanel
{
	private final JLabel name = new JLabel();
	private final JLabel avatar = new JLabel();
	private final JButton challengeButton = new JButton();

	public PartyMemberBanner(PartyMember member, PartyGamesPlugin plugin)
	{
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 5, 0, 5));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);

		/* The box's wrapping container */
		final JPanel container = new JPanel();
		container.setLayout(new BorderLayout());
		container.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		container.setBorder(new EmptyBorder(0, 0, 1, 0));

		Border border = BorderFactory.createLineBorder(Color.gray, 1);
		avatar.setBorder(border);

		avatar.setHorizontalAlignment(SwingConstants.CENTER);
		avatar.setVerticalAlignment(SwingConstants.CENTER);
		avatar.setPreferredSize(new Dimension(35, 35));

		if (member.getAvatar() != null)
		{
			ImageIcon icon = new ImageIcon(ImageUtil.resizeImage(member.getAvatar(), 32, 32));
			icon.getImage().flush();
			avatar.setIcon(icon);
		}

		/* Contains the avatar and the names */
		final JPanel headerPanel = new JPanel();
		headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		headerPanel.setLayout(new BorderLayout());
		headerPanel.setBorder(new EmptyBorder(0, 0, 3, 0));

		final JPanel namesPanel = new JPanel();
		namesPanel.setLayout(new DynamicGridLayout(2, 1));
		namesPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		namesPanel.setBorder(new EmptyBorder(2, 5, 2, 5));
		name.setText(member.getDisplayName());
		namesPanel.add(name);

		headerPanel.add(avatar, BorderLayout.WEST);
		headerPanel.add(namesPanel, BorderLayout.CENTER);

		challengeButton.setText("Challenge");
		challengeButton.setFocusable(false);
		headerPanel.add(challengeButton, BorderLayout.EAST);

		container.add(headerPanel, BorderLayout.NORTH);
		add(container, BorderLayout.NORTH);

		challengeButton.addActionListener(e -> plugin.challengeMember(member));
	}
}
