package com.partygames.CoinFlip;

import com.partygames.data.Challenge;
import com.partygames.data.Game;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;
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

	private Challenge challenge;

	private final WSClient wsClient;
	private final EventBus eventBus;
	private final PartyService partyService;
	private final CoinFlipPanel panel;
	private final Client client;
	private final long localPlayerId;

	public CoinFlip(WSClient wsClient, EventBus eventBus, PartyService partyService, CoinFlipPanel panel, Client client)
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
	}

	@Override
	public void shutDown()
	{

	}
}
