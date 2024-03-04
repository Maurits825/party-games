package com.partygames;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface PartyGamesConfig extends Config
{
	@ConfigItem(
		keyName = "viewOtherActiveGames",
		name = "View other active games",
		description = "View other active games"
	)
	default boolean viewOtherActiveGames()
	{
		return false;
	}
}
