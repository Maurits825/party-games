package com.partygames;

import com.partygames.ui.PartyMemberBanner;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.party.PartyMember;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

@Slf4j
public class PartyGamesPanel extends PluginPanel
{
	private final PartyGamesPlugin plugin;
	private final JPanel basePanel = new JPanel();
	private final JButton readyButton = new JButton();
	private final JButton testButton = new JButton();

	private JPanel partyMembersPanel = new JPanel();

	@Inject
	PartyGamesPanel(final PartyGamesPlugin plugin)
	{
		this.plugin = plugin;

		setBorder(new EmptyBorder(10, 5, 10, 5));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());

		final JPanel layoutPanel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(layoutPanel, BoxLayout.Y_AXIS);
		layoutPanel.setLayout(boxLayout);
		add(layoutPanel, BorderLayout.NORTH);

		final JPanel topPanel = new JPanel();
		topPanel.setBorder(new EmptyBorder(0, 0, 4, 0));
		topPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 2, 4, 2);

		c.gridx = 0;
		c.gridy = 0;
		topPanel.add(readyButton, c);

		c.gridx = 1;
		c.gridy = 0;
		topPanel.add(testButton, c);

		readyButton.setText("Ready");
		readyButton.setFocusable(false);

		testButton.setText("Test");
		testButton.setFocusable(false);

		layoutPanel.add(topPanel);
		layoutPanel.add(partyMembersPanel);

		readyButton.addActionListener(e ->
		{
			log.info("ready button");
		});

		testButton.addActionListener(e ->
		{
			log.info("test button");
		});
	}

	public void updateParty(List<PartyMember> partyMembers)
	{
		partyMembersPanel.removeAll();

		for (PartyMember member : partyMembers)
		{
			JPanel memberBanner = new PartyMemberBanner(member);
			partyMembersPanel.add(memberBanner);
		}
	}
}
