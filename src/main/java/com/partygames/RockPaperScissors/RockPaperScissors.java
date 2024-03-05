package com.partygames.RockPaperScissors;

import com.partygames.RockPaperScissors.events.MoveEvent;
import com.partygames.data.Challenge;
import com.partygames.data.Game;
import java.util.Map;
import javax.inject.Inject;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;

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

	@Data
	@RequiredArgsConstructor
	private class Player
	{
		public Move move = null;
		public final boolean isLocalPlayer;
		public final PartyMember member;
	}

	private Player player1;
	private Player player2;

	@Getter
	private State state = State.WAITING_PLAYER_MOVE;
	private Challenge challenge;

	private final WSClient wsClient;
	private final EventBus eventBus;
	private final PartyService partyService;
	private final RockPaperScissorsPanel panel;

	private final long localPlayerId;

	@Getter
	private boolean isLocalPlayerMove;

	@Inject
	public RockPaperScissors(WSClient wsClient, EventBus eventBus, PartyService partyService, RockPaperScissorsPanel panel)
	{
		this.wsClient = wsClient;
		this.eventBus = eventBus;
		this.partyService = partyService;
		this.panel = panel;

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

		player1 = new Player(challenge.getChallenger().getMemberId() == localPlayerId, challenge.getChallenger());
		player2 = new Player(challenge.getChallengee().getMemberId() == localPlayerId, challenge.getChallengee());

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
		log.debug("onMoveEvent");
		if (!event.getChallengeId().equals(challenge.getChallengeEvent().getChallengeId()) || state != State.WAITING_PLAYER_MOVE)
		{
			return;
		}

		long moveMemberId = event.getMemberId();
		Player playerUpdate = player1.member.getMemberId() == moveMemberId ? player1 : player2;
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
		Move p1Move = player1.move;
		Move p2Move = player2.move;

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
