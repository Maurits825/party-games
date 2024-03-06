package com.partygames.coinflip;

import com.partygames.data.Challenge;
import com.partygames.data.Game;
import java.util.Random;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;

public class CoinFlip implements Game
{
	@Getter
	public enum CoinSide
	{
		HEADS("Heads"),
		TAILS("Tails");

		private final String text;

		CoinSide(final String text)
		{
			this.text = text;
		}
	}

	public enum State
	{
		WAITING_PICK,
		PLAYER1_WIN,
		PLAYER2_WIN,
	}

	private State state;

	private Challenge challenge;

	private final WSClient wsClient;
	private final EventBus eventBus;
	private final PartyService partyService;
	private final CoinFlipPanel panel;
	private final Client client;
	private final long localPlayerId;

	private CoinFlipEvent flipEvent;

	private Random random;

	@Getter
	private boolean isLocalPlayerMove;

	public CoinFlip(WSClient wsClient, EventBus eventBus, PartyService partyService, CoinFlipPanel panel, Client client)
	{
		this.wsClient = wsClient;
		this.eventBus = eventBus;
		this.partyService = partyService;
		this.panel = panel;
		this.client = client;

		eventBus.register(this);
		wsClient.registerMessage(CoinFlipEvent.class);

		localPlayerId = partyService.getLocalMember().getMemberId();
		random = new Random(System.nanoTime());
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
		state = State.WAITING_PICK;

		isLocalPlayerMove = localPlayerId == challenge.getChallengee().getMemberId();

		panel.initialize(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		wsClient.unregisterMessage(CoinFlipEvent.class);
	}

	@Subscribe
	public void onCoinFlipEvent(CoinFlipEvent event)
	{
		if (!event.getChallengeId().equals(challenge.getChallengeEvent().getChallengeId()) || state != State.WAITING_PICK)
		{
			return;
		}

		isLocalPlayerMove = false;
		if (event.getPicked() == event.getFlipped())
		{
			state = State.PLAYER2_WIN;
		}
		else
		{
			state = State.PLAYER1_WIN;
		}

		flipEvent = event;
		panel.update(this);
	}

	public void flip(CoinSide picked)
	{
		if (isLocalPlayerMove)
		{
			CoinSide flipped = random.nextBoolean() ? CoinSide.HEADS : CoinSide.TAILS;
			partyService.send(new CoinFlipEvent(localPlayerId, challenge.getChallengeEvent().getChallengeId(), picked, flipped));
		}
	}

	public String getStatusText()
	{
		switch (state)
		{
			case WAITING_PICK:
				return "Waiting for " + challenge.getChallengee().getDisplayName() + " to pick a side";
			case PLAYER1_WIN:
				return getWinText() + challenge.getChallenger().getDisplayName() + " won!";
			case PLAYER2_WIN:
				return getWinText() + challenge.getChallengee().getDisplayName() + " won!";
			default:
				return "";
		}
	}

	private String getWinText()
	{
		return "<html>" + challenge.getChallengee().getDisplayName() + " picked " + flipEvent.getPicked().getText() + "<br /> Flip: " + flipEvent.getFlipped().getText() + ". ";
	}
}
