/*
 * Encog(tm) Workbench v2.5
 * http://www.heatonresearch.com/encog/
 * http://code.google.com/p/encog-java/
 
 * Copyright 2008-2010 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package org.encog.workbench.tabs.analyst;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.encog.app.analyst.AnalystError;
import org.encog.app.analyst.AnalystListener;
import org.encog.app.analyst.EncogAnalyst;
import org.encog.neural.networks.training.Train;
import org.encog.workbench.EncogWorkBench;
import org.encog.workbench.tabs.EncogCommonTab;
import org.encog.workbench.util.EncogFonts;


public class AnalystProgressTab extends EncogCommonTab implements
		Runnable, ActionListener, AnalystListener {

	/**
	 * The start button.
	 */
	private final JButton buttonStart;

	/**
	 * The stop button.
	 */
	private final JButton buttonStop;

	/**
	 * The close button.
	 */
	private final JButton buttonClose;

	/**
	 * The body of the dialog box is stored in this panel.
	 */
	private final JPanel panelBody;

	/**
	 * The buttons are hold in this panel.
	 */
	private final JPanel panelButtons;

	/**
	 * The background thread that processes training.
	 */
	private Thread thread;

	/**
	 * Has training been canceled.
	 */
	private boolean cancel;


	/**
	 * The font to use for headings.
	 */
	private Font headFont;

	/**
	 * The font for body text.
	 */
	private Font bodyFont;

	private String status;
	
	private EncogAnalyst analyst;
	private String targetTask = "";
	private String currentTaskName = "";
	private int currentTaskNumber;
	private int totalTasks;
	private boolean shouldExit;

	/**
	 * Construct the dialog box.
	 * 
	 * @param owner
	 *            The owner of the dialog box.
	 */
	public AnalystProgressTab(EncogAnalyst analyst, String targetTask) {
		super(null);
		
		this.status = "Waiting to start.";
		this.analyst = analyst;
		this.targetTask = targetTask;
		
		this.buttonStart = new JButton("Start");
		this.buttonStop = new JButton("Stop");
		this.buttonClose = new JButton("Close");

		this.buttonStart.addActionListener(this);
		this.buttonStop.addActionListener(this);
		this.buttonClose.addActionListener(this);

		setLayout(new BorderLayout());
		this.panelBody = new AnalystStatusPanel(this);
		this.panelButtons = new JPanel();
		this.panelButtons.add(this.buttonStart);
		this.panelButtons.add(this.buttonStop);
		this.panelButtons.add(this.buttonClose);
		add(this.panelBody, BorderLayout.CENTER);
		add(this.panelButtons, BorderLayout.SOUTH);

		this.buttonStop.setEnabled(false);

		this.bodyFont = EncogFonts.getInstance().getBodyFont();
		this.headFont = EncogFonts.getInstance().getHeadFont();
	}

	private void performClose() {
		
	}

	/**
	 * Track button presses.
	 * 
	 * @param e
	 *            Event info.
	 */
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == this.buttonClose) {
			dispose();
		} else if (e.getSource() == this.buttonStart) {
			performStart();
		} else if (e.getSource() == this.buttonStop) {
			performStop();
		}
	}
	
	public boolean close()
	{
		if (this.thread == null) {
			performClose();
			return true;
		} else {
		//	this.shouldExit = true;
			this.cancel = true;
			return false;
		}		
	}



	public void paintStatus(final Graphics g) {
		g.setColor(Color.white);
		final int width = getWidth();
		final int height = getHeight();
		g.fillRect(0, 0, width, height);
		g.setColor(Color.black);
		g.setFont(this.headFont);
		final FontMetrics fm = g.getFontMetrics();
		int y = fm.getHeight();
		g.drawString("Current Status:", 10, y);
		y += fm.getHeight();
		g.drawString("Running Task:", 10, y);
		y += fm.getHeight();
		g.drawString("Current Command Name:", 10, y);
		y += fm.getHeight();
		g.drawString("Current Command Number:", 10, y);
		

		y = fm.getHeight();
		g.drawString("Elapsed Time:", 300, y);
		y += fm.getHeight();
		g.drawString("Command Elapsed Time:", 300, y);
		y += fm.getHeight();
		g.drawString("Performance:", 300, y);

		y = fm.getHeight();
		g.setFont(this.bodyFont);

		g.drawString(this.status, 175, y);
		y += fm.getHeight();
		g.drawString(this.currentTaskName, 175, y);
		y += fm.getHeight();
		g.drawString("" + 100.0 * 0 + "%", 175, y);
		y = fm.getHeight();

	}

	/**
	 * Start the training.
	 */
	private void performStart() {

		if (!EncogWorkBench.getInstance().getMainWindow().getTabManager()
				.checkTrainingOrNetworkOpen())
			return;

		this.buttonStart.setEnabled(false);
		this.buttonStop.setEnabled(true);
		this.cancel = false;
		this.status = "Started";
		this.thread = new Thread(this);
		this.thread.start();
	}

	/**
	 * Request that the training stop.
	 */
	private void performStop() {
		this.status = "Canceled";
		this.cancel = true;
	}

	

	/**
	 * Process the background thread. Cycle through training iterations. If the
	 * cancel flag is set, then exit.
	 */
	public void run() {

		try {
			analyst.addAnalystListener(this);
			analyst.executeTask(this.targetTask);		
			analyst.removeAnalystListener(this);
			EncogWorkBench.getInstance().getMainWindow().getTree().refresh();
			
			shutdown();
			stopped();

			if (this.shouldExit) {
				dispose();
			}
		} catch (AnalystError ex) {
			EncogWorkBench.getInstance().outputLine("***Encog Analyst Error");
			EncogWorkBench.getInstance().outputLine(ex.getMessage());
		} catch (Throwable t) {
			EncogWorkBench.displayError("Error", t);
			shutdown();
			stopped();
			dispose();
		} 
	}


	/**
	 * Implemented by subclasses to perform any shutdown after training.
	 */
	public void shutdown()
	{
		
	}

	/**
	 * Implemented by subclasses to perform any activity before training.
	 */
	public void startup()
	{
		
	}

	/**
	 * Called when training has stopped.
	 */
	private void stopped() {
		this.thread = null;
		this.buttonStart.setEnabled(true);
		this.buttonStop.setEnabled(false);
		this.cancel = true;
	}

	public void saveNetwork() {

	}

	public void requestShutdown() {
		// TODO Auto-generated method stub
		
	}

	public boolean shouldShutDown() {
		// TODO Auto-generated method stub
		return false;
	}

	public void reportCommandBegin(int total, int current, String name) {
		String str = "Beginning Command #" + current + "/" + total + ": " + name;
		EncogWorkBench.getInstance().outputLine(str);		
	}

	public void reportCommandEnd() {
		// TODO Auto-generated method stub
		
	}

	public void reportTrainingBegin() {
		// TODO Auto-generated method stub
		
	}

	public void reportTrainingEnd() {
		// TODO Auto-generated method stub
		
	}

	public void reportTraining(Train train) {
		// TODO Auto-generated method stub
		
	}

	public void report(int total, int current, String message) {
		// TODO Auto-generated method stub
		
	}

}