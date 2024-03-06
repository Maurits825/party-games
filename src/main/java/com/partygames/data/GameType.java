package com.partygames.data;

import lombok.Getter;

@Getter
public enum GameType
{
	ROCK_PAPER_SCISSORS("Rock Paper Scissors"),
	COIN_FLIP("Coin Flip");

	private final String text;

	GameType(final String text)
	{
		this.text = text;
	}
}
