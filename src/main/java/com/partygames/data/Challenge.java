package com.partygames.data;

import com.partygames.data.events.ChallengeEvent;
import lombok.Getter;
import net.runelite.client.party.PartyMember;

public class Challenge
{
	@Getter
	private final PartyMember challenger;
	@Getter
	private final PartyMember challengee;

	@Getter
	private final ChallengeEvent challengeEvent;

	public Challenge(PartyMember challenger, PartyMember challengee, ChallengeEvent challengeEvent)
	{
		this.challenger = challenger;
		this.challengee = challengee;
		this.challengeEvent = challengeEvent;
	}
}
