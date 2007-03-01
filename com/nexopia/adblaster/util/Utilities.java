/*
 * Created on Jun 21, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster.util;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFileChooser;

import com.nexopia.adblaster.struct.Banner;

/**
 * @author wolfe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Utilities {
	
	static File bv_dir;
	
	public static File getDir(String name){
		JFileChooser u_jfc = new JFileChooser();
	
		u_jfc.setDialogTitle("Choose the " + name + " directory to load");
		u_jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
	    int returnVal = u_jfc.showOpenDialog(null);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       System.out.println("You chose to open this file: " +
	            u_jfc.getSelectedFile().getName());
	    } else {
	    	System.exit(0);
	    }
		return u_jfc.getSelectedFile();
	}
	
	public static Vector<Integer> stringToVector(String string) {
		StringTokenizer st = new StringTokenizer(string, ",");
		Vector<Integer> v = new Vector<Integer>();
		while (st.hasMoreElements()) {
			v.add(new Integer(Integer.parseInt(st.nextToken())));
		}
		return v;
	}
	
	public static Vector<Integer> stringToNegationVector(String string) {
		StringTokenizer st = new StringTokenizer(string, ",");
		Vector<Integer> v = new Vector<Integer>();
		while (st.hasMoreElements()) {
			v.add(new Integer(Integer.parseInt(st.nextToken())));
		}
		
		if (v.isEmpty()) {
			v.add((Integer.NEGATE));
		} else if (v.get(0).intValue() == 0) {
			v.setElementAt(Integer.NEGATE, 0);
		} else {
			v.insertElementAt(Integer.IDENTITY, 0);
		}
		
		return v;
	}

	public static Banner priorityChoose(Vector<Banner> valid) {
		// TODO Auto-generated method stub
		return null;
	}

	static PrintStream stream = System.out;
	
	public static void bannerDebug(String debugLog) {
		stream.println(debugLog);
		
	}
	
	public static void setDebugLog(OutputStream stream){
		Utilities.stream = new PrintStream(stream); 
	}
}
