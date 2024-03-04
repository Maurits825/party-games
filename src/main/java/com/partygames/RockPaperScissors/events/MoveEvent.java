package com.partygames.RockPaperScissors.events;

import com.partygames.RockPaperScissors.RockPaperScissors;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

@Value
@EqualsAndHashCode(callSuper = true)
public class MoveEvent extends PartyMemberMessage
{
	UUID challengeID;
	RockPaperScissors.Move move;
}
