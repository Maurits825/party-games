package com.partygames.data;

public interface Game
{
	Challenge getChallenge();

	void initialize(Challenge challenge);

	void shutDown();
}
