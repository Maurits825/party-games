package com.partygames.rockpaperscissors;

import com.partygames.data.Challenge;
import com.partygames.data.Game;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.util.ColorUtil;

@Slf4j
public class RockPaperScissors implements Game
{
	@Getter
	public enum Move
	{
		ROCK("Rock"),
		PAPER("Paper"),
		SCISSORS("Scissors");

		private final String text;

		Move(final String text)
		{
			this.text = text;
		}
	}

	public enum State
	{
		WAITING_PLAYER_MOVE,
		DRAW,
		PLAYER1_WIN,
		PLAYER2_WIN,
	}

	private final Map<Move, Move> winRules = Map.of(
		Move.ROCK, Move.SCISSORS,
		Move.SCISSORS, Move.PAPER,
		Move.PAPER, Move.ROCK
	);

	private final String MOVE_CENSOR = "???";
	private final String WIN_TEXT = "Sit rat";
	private final String LOSE_TEXT = " is my King";
	private final String DRAW_TEXT = "Run it back";
	private final int OVERHEAD_TEXT_DURATION = 250;

	@Data
	private class RpsPlayer
	{
		public Move move = null;
		public final boolean isLocalPlayer;
		public final PartyMember member;
		public final Player player;

		public RpsPlayer(PartyMember member, long localPlayerId)
		{
			this.member = member;
			isLocalPlayer = member.getMemberId() == localPlayerId;
			player = findPlayer(member.getDisplayName());
		}
	}

	private RpsPlayer player1;
	private RpsPlayer player2;

	@Getter
	private State state = State.WAITING_PLAYER_MOVE;
	private Challenge challenge;

	private final WSClient wsClient;
	private final EventBus eventBus;
	private final PartyService partyService;
	private final RockPaperScissorsPanel panel;
	private final Client client;

	private final long localPlayerId;

	@Getter
	private boolean isLocalPlayerMove;

	public RockPaperScissors(WSClient wsClient, EventBus eventBus, PartyService partyService, RockPaperScissorsPanel panel, Client client)
	{
		this.wsClient = wsClient;
		this.eventBus = eventBus;
		this.partyService = partyService;
		this.panel = panel;
		this.client = client;

		eventBus.register(this);
		wsClient.registerMessage(MoveEvent.class);

		localPlayerId = partyService.getLocalMember().getMemberId();
	}

	@Override
	public Challenge getChallenge()
	{
		return challenge;
	}

	@Override
	public void initialize(Challenge challenge)
	{
		this.challenge = challenge;
		isLocalPlayerMove = true;
		state = State.WAITING_PLAYER_MOVE;

		player1 = new RpsPlayer(challenge.getChallenger(), localPlayerId);
		player2 = new RpsPlayer(challenge.getChallengee(), localPlayerId);

		panel.initialize(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		wsClient.unregisterMessage(MoveEvent.class);
	}

	@Subscribe
	public void onMoveEvent(MoveEvent event)
	{
		if (!event.getChallengeId().equals(challenge.getChallengeEvent().getChallengeId()) || state != State.WAITING_PLAYER_MOVE)
		{
			return;
		}

		long moveMemberId = event.getMemberId();
		RpsPlayer playerUpdate = player1.member.getMemberId() == moveMemberId ? player1 : player2;
		playerUpdate.move = event.getMove();
		isLocalPlayerMove = isLocalPlayerMove && !playerUpdate.isLocalPlayer;

		if (player1.move != null && player2.move != null)
		{
			state = play();
		}

		panel.update(this);
	}

	public void move(Move move)
	{
		if (state == State.WAITING_PLAYER_MOVE)
		{
			partyService.send(new MoveEvent(localPlayerId, challenge.getChallengeEvent().getChallengeId(), move));
		}
	}

	public String getStatusText()
	{
		String p1Move = getPlayerMove(player1);
		String p2Move = getPlayerMove(player2);

		String status = p1Move + " vs " + p2Move;
		switch (state)
		{
			case WAITING_PLAYER_MOVE:
				break;
			case PLAYER1_WIN:
				status += ": " + player1.getMember().getDisplayName() + " won!";
				break;
			case PLAYER2_WIN:
				status += ": " + player2.getMember().getDisplayName() + " won!";
				break;
			case DRAW:
				status += ": draw!";
				break;
		}

		return status;
	}

	private String getPlayerMove(RpsPlayer player)
	{
		String move = MOVE_CENSOR;
		if (state != State.WAITING_PLAYER_MOVE || (player.isLocalPlayer && player.move != null))
		{
			move = player.move.getText();
		}
		return move;
	}

	private State play()
	{
		Move p1Move = player1.move;
		Move p2Move = player2.move;

		if (p1Move == p2Move)
		{
			setOverHeadText(player1.getPlayer(), DRAW_TEXT, Color.YELLOW, OVERHEAD_TEXT_DURATION);
			setOverHeadText(player2.getPlayer(), DRAW_TEXT, Color.YELLOW, OVERHEAD_TEXT_DURATION);
			return State.DRAW;
		}

		if (winRules.get(p1Move) == p2Move)
		{
			setOverHeadText(player1.getPlayer(), WIN_TEXT, Color.GREEN, OVERHEAD_TEXT_DURATION);
			setOverHeadText(player2.getPlayer(), player1.getMember().getDisplayName() + LOSE_TEXT, Color.RED, OVERHEAD_TEXT_DURATION);
			return State.PLAYER1_WIN;
		}
		else
		{
			setOverHeadText(player2.getPlayer(), WIN_TEXT, Color.GREEN, OVERHEAD_TEXT_DURATION);
			setOverHeadText(player1.getPlayer(), player2.getMember().getDisplayName() + LOSE_TEXT, Color.RED, OVERHEAD_TEXT_DURATION);
			return State.PLAYER2_WIN;
		}
	}

	private void setOverHeadText(Player player, String text, Color color, int duration)
	{
		if (player == null)
		{
			return;
		}

		player.setOverheadCycle(duration);
		player.setOverheadText(ColorUtil.wrapWithColorTag(text, color));
	}

	private Player findPlayer(String name)
	{
		List<Player> players = client.getPlayers();
		for (Player player : players)
		{
			if (name.equals(player.getName()))
			{
				return player;
			}
		}
		return null;
	}
}
