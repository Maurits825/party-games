package com.partygames.ui;

import com.partygames.PartyGamesPlugin;
import com.partygames.data.GameType;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.components.TitleCaseListCellRenderer;
import net.runelite.client.util.ImageUtil;

public class PartyMemberBanner extends JPanel
{
	private final JLabel name = new JLabel();
	private final JLabel avatar = new JLabel();
	private boolean avatarSet;

	private final PartyMember member;
	private final PartyService partyService;
	private final PartyGamesPlugin plugin;

	final JComboBox<GameType> gameTypeBox = new JComboBox<>(GameType.values());
	final JButton challengeButton = new JButton();

	public PartyMemberBanner(PartyService partyService, PartyMember member, PartyGamesPlugin plugin)
	{
		this.member = member;
		this.partyService = partyService;
		this.plugin = plugin;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 0, 5, 0));

		final JPanel container = new JPanel();
		container.setLayout(new BorderLayout());
		container.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		container.setBorder(new EmptyBorder(5, 5, 5, 5));

		Border border = BorderFactory.createLineBorder(Color.gray, 1);
		avatar.setBorder(border);

		avatar.setHorizontalAlignment(SwingConstants.CENTER);
		avatar.setVerticalAlignment(SwingConstants.CENTER);
		avatar.setPreferredSize(new Dimension(35, 35));

		/* Contains the avatar and the names */
		final JPanel headerPanel = new JPanel();
		headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		headerPanel.setLayout(new BorderLayout());
		headerPanel.setBorder(new EmptyBorder(0, 0, 3, 0));

		final JPanel namesPanel = new JPanel();
		namesPanel.setLayout(new DynamicGridLayout(2, 1));
		namesPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		namesPanel.setBorder(new EmptyBorder(2, 5, 2, 5));
		namesPanel.add(name);

		headerPanel.add(avatar, BorderLayout.WEST);
		headerPanel.add(namesPanel, BorderLayout.CENTER);

		final JPanel challengePanel = new JPanel();
		challengePanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 2;
		challengePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		challengePanel.setBorder(new EmptyBorder(0, 0, 0, 0));

		gameTypeBox.setRenderer(new TitleCaseListCellRenderer());
		gameTypeBox.setSelectedItem(GameType.ROCK_PAPER_SCISSORS);
		gameTypeBox.addItemListener(e -> updateChallengePanel((GameType) gameTypeBox.getSelectedItem()));
		gameTypeBox.setFocusable(false);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		challengePanel.add(gameTypeBox, c);

		challengeButton.setText("Challenge");
		challengeButton.setFocusable(false);
		challengeButton.addActionListener(e -> {
			challengeButton.setEnabled(false);
			plugin.challengeMember(member, (GameType) gameTypeBox.getSelectedItem());
		});

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 1;
		challengePanel.add(challengeButton, c);
		updateChallengePanel((GameType) gameTypeBox.getSelectedItem());

		container.add(headerPanel, BorderLayout.NORTH);
		container.add(challengePanel, BorderLayout.SOUTH);
		add(container, BorderLayout.NORTH);

		update();
	}

	public void update()
	{
		//TODO try with party service to see diference in loading data??
		if (!avatarSet && member.getAvatar() != null)
		{
			ImageIcon icon = new ImageIcon(ImageUtil.resizeImage(member.getAvatar(), 32, 32));
			icon.getImage().flush();
			avatar.setIcon(icon);

			avatarSet = true;
		}

		boolean isLoggedIn = member.isLoggedIn();
		name.setForeground(isLoggedIn ? Color.WHITE : Color.GRAY);
		name.setText(member.getDisplayName());

		updateChallengePanel((GameType) gameTypeBox.getSelectedItem());
	}

	private void updateChallengePanel(GameType gameType)
	{
		if (plugin.pendingChallengeExists(member, gameType))
		{
			challengeButton.setText("Pending");
			challengeButton.setEnabled(false);
		}
		else
		{
			challengeButton.setText("Challenge");
			challengeButton.setEnabled(true);
		}
	}
}
