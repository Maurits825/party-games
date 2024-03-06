package com.partygames.rockpaperscissors;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

@Value
@EqualsAndHashCode(callSuper = true)
public class MoveEvent extends PartyMemberMessage
{
	long memberId;
	UUID challengeId;
	RockPaperScissors.Move move;
}
