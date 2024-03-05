package com.partygames.RockPaperScissors;

import com.partygames.PartyGamesPanel;
import com.partygames.RockPaperScissors.events.MoveEvent;
import com.partygames.data.Challenge;
import com.partygames.data.Game;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;

@Slf4j
public class RockPaperScissors implements Game
{
	public enum Move
	{
		ROCK,
		PAPER,
		SCISSORS,
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

	@Data
	@RequiredArgsConstructor
	private class Player
	{
		public Move move = null;
		public final boolean isLocalPlayer;
		public final String name;
	}

	@Getter
	private State state = State.WAITING_PLAYER_MOVE;
	private Challenge challenge;

	private RockPaperScissorGamePanel gamePanel;

	private final WSClient wsClient;
	private final EventBus eventBus;
	private final PartyService partyService;
	private final PartyGamesPanel panel;

	private final long localPlayerId;

	@Getter
	private boolean isLocalPlayerMove;

	private Map<Long, Player> players;

	@Inject
	public RockPaperScissors(WSClient wsClient, EventBus eventBus, PartyService partyService, PartyGamesPanel panel)
	{
		this.wsClient = wsClient;
		this.eventBus = eventBus;
		this.partyService = partyService;
		this.panel = panel;

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
		isLocalPlayerMove = localPlayerId == challenge.getChallenger().getMemberId() || localPlayerId == challenge.getChallengee().getMemberId();

		players = new HashMap<>();
		players.put(challenge.getChallenger().getMemberId(), new Player(challenge.getChallenger().getMemberId() == localPlayerId, challenge.getChallenger().getDisplayName()));
		players.put(challenge.getChallengee().getMemberId(), new Player(challenge.getChallengee().getMemberId() == localPlayerId, challenge.getChallengee().getDisplayName()));

		eventBus.register(this);
		wsClient.registerMessage(MoveEvent.class);

		gamePanel = panel.getRockPaperScissorsPanel().addRPSGame(this);
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
		log.debug("onMoveEvent");
		if (!event.getChallengeId().equals(challenge.getChallengeEvent().getChallengeId()) || state != State.WAITING_PLAYER_MOVE)
		{
			return;
		}

		long moveMemberId = event.getMemberId();
		if (players.containsKey(moveMemberId))
		{
			Player player = players.get(moveMemberId);
			if (player.move == null)
			{
				player.move = event.getMove();
				isLocalPlayerMove = isLocalPlayerMove && moveMemberId != localPlayerId;
				gamePanel.update();
			}
		}

		for (Player player : players.values())
		{
			if (player.move == null)
			{
				return;
			}
		}

		state = play();
		gamePanel.update();
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
		Player player1 = players.get(challenge.getChallenger().getMemberId());
		Player player2 = players.get(challenge.getChallengee().getMemberId());

		String p1Move = getPlayerMove(player1);
		String p2Move = getPlayerMove(player2);

		String status = p1Move + " vs " + p2Move;
		switch (state)
		{
			case WAITING_PLAYER_MOVE:
				break;
			case PLAYER1_WIN:
				status += ": " + player1.name + " won!";
				break;
			case PLAYER2_WIN:
				status += ": " + player2.name + " won!";
				break;
			case DRAW:
				status += ": draw!";
				break;
		}

		return status;
	}

	private String getPlayerMove(Player player)
	{
		String move = MOVE_CENSOR;
		if (state != State.WAITING_PLAYER_MOVE || (player.isLocalPlayer && player.move != null))
		{
			move = player.move.name();
		}
		return move;
	}

	private State play()
	{
		Move p1Move = players.get(challenge.getChallenger().getMemberId()).move;
		Move p2Move = players.get(challenge.getChallengee().getMemberId()).move;

		if (p1Move == p2Move)
		{
			return State.DRAW;
		}

		if (winRules.get(p1Move) == p2Move)
		{
			return State.PLAYER1_WIN;
		}
		else
		{
			return State.PLAYER2_WIN;
		}
	}
}
