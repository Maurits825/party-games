package com.partygames;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JPanel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.PluginPanel;

@Slf4j
public class PartyGamesPanel extends PluginPanel
{
	private final PartyGamesPlugin plugin;
	private final JPanel basePanel = new JPanel();
	private final JButton readyButton = new JButton();
	private final JButton testButton = new JButton();

	@Inject
	PartyGamesPanel(final PartyGamesPlugin plugin)
	{
		this.plugin = plugin;
		this.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 2, 4, 2);

		c.gridx = 0;
		c.gridy = 0;
		this.add(readyButton, c);

		c.gridx = 1;
		c.gridy = 0;
		this.add(testButton, c);

		readyButton.setText("Ready");
		readyButton.setFocusable(false);

		testButton.setText("Test");
		testButton.setFocusable(false);

		readyButton.addActionListener(e ->
		{
			log.info("ready button");
		});

		testButton.addActionListener(e ->
		{
			log.info("test button");
		});
	}
}
