package com.partygames.coinflip;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

@Value
@EqualsAndHashCode(callSuper = true)
public class CoinFlipEvent extends PartyMemberMessage
{
	long memberId;
	UUID challengeId;
	CoinFlip.CoinSide picked;
	CoinFlip.CoinSide flipped;
}
