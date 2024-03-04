package com.partygames.RockPaperScissors;

import com.partygames.PartyGamesPanel;
import com.partygames.RockPaperScissors.events.MoveEvent;
import com.partygames.data.Challenge;
import com.partygames.data.Game;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
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

	private State state = State.WAITING_PLAYER_MOVE;
	private Challenge challenge;

	private RockPaperScissorGamePanel gamePanel;

	private final WSClient wsClient;
	private final PartyService partyService;
	private final PartyGamesPanel panel;

	@Inject
	public RockPaperScissors(WSClient wsClient, PartyService partyService, PartyGamesPanel panel)
	{
		this.wsClient = wsClient;
		this.partyService = partyService;
		this.panel = panel;
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
		
		wsClient.registerMessage(MoveEvent.class);

		gamePanel = panel.getRockPaperScissorsPanel().addRPSGame(this);
	}

	@Override
	public void shutDown()
	{
		wsClient.unregisterMessage(MoveEvent.class);
	}

	@Subscribe
	public void onMoveEvent(MoveEvent event)
	{
		log.debug("onMoveEvent");
	}

	public void move(Move move)
	{
		partyService.send(new MoveEvent(challenge.getChallengeEvent().getChallengeId(), move));
	}
}
