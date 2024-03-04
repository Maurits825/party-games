package com.partygames.data;

import lombok.Getter;

public class ActiveGame
{
	@Getter
	private final Challenge challenge;


	public ActiveGame(Challenge challenge)
	{
		this.challenge = challenge;
	}
}
