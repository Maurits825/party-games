package com.partygames.RockPaperScissors;

import com.partygames.data.Challenge;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import net.runelite.client.party.PartyMember;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

public class RockPaperScissorGamePanel extends JPanel
{
	private final RockPaperScissors rockPaperScissors;

	private final JLabel name1 = new JLabel();
	private final JLabel name2 = new JLabel();
	private final JLabel avatar1 = new JLabel();
	private final JLabel avatar2 = new JLabel();

	private final JLabel status = new JLabel();

	private final JPanel container = new JPanel();
	private final JPanel movesPanel;

	private final JButton rockButton = new JButton();
	private final JButton paperButton = new JButton();
	private final JButton scissorsButton = new JButton();

	public RockPaperScissorGamePanel(RockPaperScissors rockPaperScissors)
	{
		this.rockPaperScissors = rockPaperScissors;

		Challenge challenge = rockPaperScissors.getChallenge();
		PartyMember challenger = challenge.getChallenger();
		PartyMember challengee = challenge.getChallengee();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new EmptyBorder(5, 0, 0, 0));
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		container.setBorder(new EmptyBorder(5, 5, 5, 5));

		//contains icon and names
		final JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
		headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		headerPanel.setBorder(new EmptyBorder(0, 0, 5, 0));

		SetAvatarStyle(avatar1, challenger);
		SetAvatarStyle(avatar2, challengee);

		headerPanel.add(avatar1);
		name1.setText(challenger.getDisplayName());
		headerPanel.add(name1);
		final JLabel vs = new JLabel();
		vs.setText("vs");
		headerPanel.add(vs);

		headerPanel.add(avatar2);
		name2.setText(challengee.getDisplayName());
		headerPanel.add(name2);

		container.add(headerPanel);
		movesPanel = createMovesPanel(rockPaperScissors);
		container.add(movesPanel);

		if (!rockPaperScissors.isLocalPlayerMove())
		{
			movesPanel.setVisible(false);
		}

		final JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
		footerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		status.setText(rockPaperScissors.getStatusText());
		footerPanel.add(status);

		container.add(footerPanel);
		add(container);
	}

	public void update()
	{
		movesPanel.setVisible(rockPaperScissors.isLocalPlayerMove());
		status.setText(rockPaperScissors.getStatusText());
	}

	private void SetAvatarStyle(JLabel avatar, PartyMember member)
	{
		Border border = BorderFactory.createLineBorder(Color.gray, 1);
		avatar.setBorder(border);
		avatar.setHorizontalAlignment(SwingConstants.CENTER);
		avatar.setVerticalAlignment(SwingConstants.CENTER);
		avatar.setPreferredSize(new Dimension(35, 35));
		ImageIcon icon = new ImageIcon(ImageUtil.resizeImage(member.getAvatar(), 32, 32));
		icon.getImage().flush();
		avatar.setIcon(icon);
	}

	private JPanel createMovesPanel(RockPaperScissors rockPaperScissors)
	{
		JPanel movesPanel = new JPanel(new GridLayout(1, 3));
		movesPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
		movesPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		movesPanel.setPreferredSize(new Dimension(0, 30));

		rockButton.setText("Rock");
		rockButton.setFocusable(false);
		movesPanel.add(rockButton);

		paperButton.setText("Paper");
		paperButton.setFocusable(false);
		movesPanel.add(paperButton);

		scissorsButton.setText("Scissors");
		scissorsButton.setFocusable(false);
		movesPanel.add(scissorsButton);

		rockButton.addActionListener(e -> rockPaperScissors.move(RockPaperScissors.Move.ROCK));
		paperButton.addActionListener(e -> rockPaperScissors.move(RockPaperScissors.Move.PAPER));
		scissorsButton.addActionListener(e -> rockPaperScissors.move(RockPaperScissors.Move.SCISSORS));

		return movesPanel;
	}
}
