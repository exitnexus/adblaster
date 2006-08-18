package com.nexopia.adblaster;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class ProgressIndicator {
	static JProgressBar bar;
	static JFrame frame;
	
	static {
		bar = new JProgressBar(0, 100);
		bar.setValue(0);
		bar.setStringPainted(true);
		frame = new JFrame();
		frame.setContentPane(bar);
		frame.pack();
		frame.setVisible(true);
	}
	public static void show(int i, int max){
		bar.setMaximum(max);
		bar.setValue(i);
	}
	
	public static void main(String args[]){
		int i = 0;
		while(true){
			i++;
			show(i, 10000000);
		}
	}

	public static void setTitle(String string) {
		frame.setTitle(string);
	}

}
