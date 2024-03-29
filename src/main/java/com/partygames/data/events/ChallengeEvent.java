package com.partygames.data.events;

import com.partygames.data.GameType;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

@Value
@EqualsAndHashCode(callSuper = true)
public class ChallengeEvent extends PartyMemberMessage
{
	long fromId;
	long toId;
	GameType gameType;
	UUID challengeId = UUID.randomUUID();
}
