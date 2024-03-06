package com.partygames;

import com.partygames.data.Challenge;
import com.partygames.data.GameType;
import com.partygames.data.events.AcceptChallengeEvent;
import com.partygames.data.events.ChallengeEvent;
import com.partygames.ui.ChallengeBanner;
import com.partygames.ui.PartyMemberBanner;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
import net.runelite.client.ui.components.PluginErrorPanel;

@Slf4j
public class PartyGamesLobbyPanel extends JPanel
{
	private final PartyGamesPlugin plugin;
	private final JButton testButton1 = new JButton();
	private final JButton testButton2 = new JButton();

	private static final String ERROR_PANEL = "ERROR_PANEL";
	private static final String LOBBY_PANEL = "LOBBY_PANEL";
	private final CardLayout cardLayout = new CardLayout();
	private final JPanel container = new JPanel(cardLayout);
	private final PluginErrorPanel noPartyPanel = new PluginErrorPanel();

	private final JComponent partyMembersPanel = new DragAndDropReorderPane();
	private final JPanel challengesPanel = new JPanel();

	private final JPanel emptyPartyPanel = new JPanel(new BorderLayout());
	private final JPanel emptyPendingChallengesPanel = new JPanel(new BorderLayout());

	private final Map<Long, PartyMemberBanner> partyMemberBanners = new HashMap<>();
	private final Map<UUID, ChallengeBanner> challengeBanners = new HashMap<>();

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

		challengesPanel.setLayout(new BoxLayout(challengesPanel, BoxLayout.Y_AXIS));
		layoutPanel.add(challengesPanel);

		testButton1.addActionListener(e ->
		{
			log.info("add active game");
			addActiveGame();
		});

		testButton2.addActionListener(e ->
		{
			log.info("refresh view");
			updateAllPartyMemberBanner();
			updateParty();
		});

		JPanel errorWrapper = new JPanel(new BorderLayout());
		errorWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		errorWrapper.add(noPartyPanel, BorderLayout.NORTH);

		noPartyPanel.setContent("Not in a party", "Use the Party plugin to join a party.");

		emptyPartyPanel.add(new JLabel("No other members"), BorderLayout.WEST);
		emptyPendingChallengesPanel.add(new JLabel("No pending challenges"), BorderLayout.WEST);

		container.add(layoutPanel, LOBBY_PANEL);
		container.add(errorWrapper, ERROR_PANEL);

		cardLayout.show(container, ERROR_PANEL);

		add(container, BorderLayout.NORTH);

		updateParty();
	}

	public void updateAllPartyMemberBanner()
	{
		for (PartyMemberBanner banner : partyMemberBanners.values())
		{
			banner.update();
		}
	}

	public void updatePartyMemberBanner(long memberId)
	{
		if (partyMemberBanners.containsKey(memberId))
		{
			partyMemberBanners.get(memberId).update();
		}
	}

	public void addPartyMember(PartyMember member)
	{
		if (!partyMemberBanners.containsKey(member.getMemberId()))
		{
			PartyMemberBanner memberBanner = new PartyMemberBanner(member, plugin);
			partyMemberBanners.put(member.getMemberId(), memberBanner);
			partyMembersPanel.add(memberBanner);
			partyMembersPanel.revalidate();
		}

		updateParty();
	}

	public void removePartyMember(long memberId)
	{
		PartyMemberBanner banner = partyMemberBanners.remove(memberId);

		if (banner != null)
		{
			partyMembersPanel.remove(banner);
			partyMembersPanel.revalidate();
		}

		updateParty();
	}

	public void removeAllPartyMembers()
	{
		partyMemberBanners.forEach((key, value) -> partyMembersPanel.remove(value));
		partyMembersPanel.revalidate();
		partyMemberBanners.clear();

		updateParty();
	}

	public void updateParty()
	{
		partyMembersPanel.remove(emptyPartyPanel);
		challengesPanel.remove(emptyPendingChallengesPanel);

		if (plugin.getPartyService().isInParty())
		{
			cardLayout.show(container, LOBBY_PANEL);
			if (partyMemberBanners.size() == 0)
			{
				partyMembersPanel.add(emptyPartyPanel);
			}
			if (challengeBanners.size() == 0)
			{
				challengesPanel.add(emptyPendingChallengesPanel);
			}
		}
		else
		{
			cardLayout.show(container, ERROR_PANEL);
		}
	}

	public void addPendingChallenge(Challenge challenge)
	{
		if (!challengeBanners.containsKey(challenge.getChallengeEvent().getChallengeId()))
		{
			ChallengeBanner challengeBanner = new ChallengeBanner(challenge, plugin);
			challengeBanners.put(challenge.getChallengeEvent().getChallengeId(), challengeBanner);
			challengesPanel.add(challengeBanner);
			challengesPanel.revalidate();
		}

		updateParty();
	}

	public void removePendingChallenge(Challenge challenge)
	{
		ChallengeBanner challengeBanner = challengeBanners.remove(challenge.getChallengeEvent().getChallengeId());

		if (challengeBanner != null)
		{
			challengesPanel.remove(challengeBanner);
			challengesPanel.revalidate();
		}

		updateParty();
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
