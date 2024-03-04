package com.partygames;

import com.partygames.data.Challenge;
import com.partygames.data.events.ChallengeEvent;
import com.partygames.ui.ChallengeBanner;
import com.partygames.ui.PartyMemberBanner;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
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
import net.runelite.client.ui.components.DragAndDropReorderPane;

@Slf4j
public class PartyGamesPanel extends PluginPanel
{
	private final PartyGamesPlugin plugin;
	private final JPanel basePanel = new JPanel();
	private final JButton readyButton = new JButton();
	private final JButton testButton = new JButton();

	private JComponent partyMembersPanel = new DragAndDropReorderPane();
	private JComponent challengesPanel = new DragAndDropReorderPane();

	private List<PartyMemberBanner> partyMemberBanners = new ArrayList<>();
	private List<ChallengeBanner> challengeBanners = new ArrayList<>();

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
		layoutPanel.add(challengesPanel);

		readyButton.addActionListener(e ->
		{
			log.info("ready button");
		});

		testButton.addActionListener(e ->
		{
			log.info("test button");
			updateParty(plugin.getPartyMembers());
		});
	}

	public void updateParty(List<PartyMember> partyMembers)
	{
		partyMembersPanel.removeAll();
		partyMemberBanners.clear();

		JLabel header = new JLabel();
		header.setText("Members");
		partyMembersPanel.add(header);

		for (PartyMember member : partyMembers)
		{
			log.debug("party member: " + member.getDisplayName());
			PartyMemberBanner memberBanner = new PartyMemberBanner(member, plugin);
			partyMemberBanners.add(memberBanner);
			partyMembersPanel.add(memberBanner);
		}
	}

	public void updateChallenges(List<Challenge> challenges)
	{
		//just redraw everything for now
		challengesPanel.removeAll();
		challengeBanners.clear();

		JLabel header = new JLabel();
		header.setText("Challenges");
		challengesPanel.add(header);

		for (Challenge challenge : challenges)
		{
			ChallengeBanner challengeBanner = new ChallengeBanner(challenge, plugin);
			challengeBanners.add(challengeBanner);
			challengesPanel.add(challengeBanner);
		}

		challengesPanel.revalidate();
	}
}
