package com.partygames.ui;

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
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.party.PartyMember;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.util.ImageUtil;

@Slf4j
public class PartyMemberBanner extends JPanel
{
	private final JLabel name = new JLabel();
	private final JLabel avatar = new JLabel();
	private final JButton challengeButton = new JButton();

	public PartyMemberBanner(PartyMember member)
	{
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);

		Border border = BorderFactory.createLineBorder(Color.gray, 1);
		avatar.setBorder(border);

		avatar.setHorizontalAlignment(SwingConstants.CENTER);
		avatar.setVerticalAlignment(SwingConstants.CENTER);
		avatar.setPreferredSize(new Dimension(35, 35));
		ImageIcon icon = new ImageIcon(ImageUtil.resizeImage(member.getAvatar(), 32, 32));
		icon.getImage().flush();
		avatar.setIcon(icon);
		add(avatar, BorderLayout.WEST);

		final JPanel namesPanel = new JPanel();
		namesPanel.setLayout(new DynamicGridLayout(2, 1));
		namesPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		namesPanel.setBorder(new EmptyBorder(2, 5, 2, 5));
		name.setText(member.getDisplayName());
		namesPanel.add(name, BorderLayout.CENTER);
		add(namesPanel);

		challengeButton.setText("Challenge");
		challengeButton.setFocusable(false);
		add(challengeButton, BorderLayout.EAST);

		challengeButton.addActionListener(e ->
		{
			log.info("challenge button");
		});
	}
}
