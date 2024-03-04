package com.partygames.data.events;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

@Value
@EqualsAndHashCode(callSuper = true)
public class AcceptChallengeEvent extends PartyMemberMessage
{
	UUID challengeId;
}
