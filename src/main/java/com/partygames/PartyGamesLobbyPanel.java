package com.partygames;

import com.partygames.data.Challenge;
import com.partygames.data.GameType;
import com.partygames.data.events.AcceptChallengeEvent;
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
import net.runelite.client.ui.components.DragAndDropReorderPane;

@Slf4j
public class PartyGamesLobbyPanel extends JPanel
{
	private final PartyGamesPlugin plugin;
	private final JButton testButton1 = new JButton();
	private final JButton testButton2 = new JButton();

	private final JComponent partyMembersPanel = new DragAndDropReorderPane();
	private final JComponent challengesPanel = new DragAndDropReorderPane();

	private final List<PartyMemberBanner> partyMemberBanners = new ArrayList<>();
	private final List<ChallengeBanner> challengeBanners = new ArrayList<>();

	@Inject
	private PartyGamesLobbyPanel(PartyGamesPlugin plugin)
	{
		this.plugin = plugin;

		setBorder(new EmptyBorder(10, 5, 10, 5));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());

		final JPanel layoutPanel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(layoutPanel, BoxLayout.Y_AXIS);
		layoutPanel.setLayout(boxLayout);
		layoutPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		add(layoutPanel, BorderLayout.NORTH);

		final JPanel topPanel = new JPanel();
		topPanel.setBorder(new EmptyBorder(0, 0, 4, 0));
		topPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 2, 4, 2);

		c.gridx = 0;
		c.gridy = 0;
		topPanel.add(testButton1, c);

		c.gridx = 1;
		c.gridy = 0;
		topPanel.add(testButton2, c);

		testButton1.setText("Add game");
		testButton1.setFocusable(false);

		testButton2.setText("Refresh");
		testButton2.setFocusable(false);

		layoutPanel.add(topPanel);
		layoutPanel.add(getHeaderTextPanel("Members"));
		layoutPanel.add(partyMembersPanel);

		layoutPanel.add(getHeaderTextPanel("Challenges"));
		layoutPanel.add(challengesPanel);

		testButton1.addActionListener(e ->
		{
			log.info("add active game");
			addActiveGame();
		});

		testButton2.addActionListener(e ->
		{
			log.info("refresh view");
			updatePartyMembers(plugin.getPartyMembers());
			updateChallenges(plugin.getActiveChallenges());
		});
	}

	public void updatePartyMembers(List<PartyMember> partyMembers)
	{
		partyMembersPanel.removeAll();
		partyMemberBanners.clear();

		for (PartyMember member : partyMembers)
		{
			log.debug("banner - party member: " + member.getDisplayName());
			PartyMemberBanner memberBanner = new PartyMemberBanner(plugin.getPartyService(), member, plugin);
			partyMemberBanners.add(memberBanner);
			partyMembersPanel.add(memberBanner);
		}

		partyMembersPanel.revalidate();
	}

	public void updateChallenges(List<Challenge> challenges)
	{
		challengesPanel.removeAll();
		challengeBanners.clear();

		for (Challenge challenge : challenges)
		{
			ChallengeBanner challengeBanner = new ChallengeBanner(challenge, plugin);
			challengeBanners.add(challengeBanner);
			challengesPanel.add(challengeBanner);
		}

		challengesPanel.revalidate();
	}

	private JPanel getHeaderTextPanel(String text)
	{
		JLabel headerText = new JLabel();
		headerText.setText(text);

		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BorderLayout());
		headerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		headerPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		headerPanel.add(headerText, BorderLayout.CENTER);
		return headerPanel;
	}

	private void addActiveGame()
	{
		long localId = plugin.getPartyService().getLocalMember().getMemberId();
		long otherId = plugin.getPartyMembers().size() == 0 ? localId : plugin.getPartyMembers().get(0).getMemberId();
		ChallengeEvent challengeEvent = new ChallengeEvent(localId, otherId, GameType.ROCK_PAPER_SCISSORS);
		plugin.onChallengeEvent(challengeEvent);

		AcceptChallengeEvent acceptChallengeEvent = new AcceptChallengeEvent(challengeEvent.getChallengeId());
		plugin.onAcceptChallengeEvent(acceptChallengeEvent);
	}
}
