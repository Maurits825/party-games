package com.partygames.RockPaperScissors;

import com.partygames.PartyGamesPanel;
import com.partygames.RockPaperScissors.events.MoveEvent;
import com.partygames.data.Challenge;
import com.partygames.data.Game;
import javax.inject.Inject;
import lombok.Getter;
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
		PLAYER1_WIN,
		PLAYER2_WIN,
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

	private Move challengerMove;
	private Move challengeeMove;

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

		//todo make this simpler/better??
		if (event.getMemberId() == challenge.getChallenger().getMemberId())
		{
			challengerMove = event.getMove();
			isLocalPlayerMove = isLocalPlayerMove && challenge.getChallenger().getMemberId() != localPlayerId;
		}
		else if (event.getMemberId() == challenge.getChallengee().getMemberId())
		{
			challengeeMove = event.getMove();
			isLocalPlayerMove = isLocalPlayerMove && challenge.getChallenger().getMemberId() != localPlayerId;
		}

		if (challengerMove != null && challengeeMove != null)
		{
			play();
		}
	
		gamePanel.update();
	}

	public void move(Move move)
	{
		if (state == State.WAITING_PLAYER_MOVE)
		{
			partyService.send(new MoveEvent(localPlayerId, challenge.getChallengeEvent().getChallengeId(), move));
		}
	}

	private void play()
	{
		//TODO do rps game
		state = State.PLAYER1_WIN;
	}
}
