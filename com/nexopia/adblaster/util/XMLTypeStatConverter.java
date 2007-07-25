/*
 * XMLBannerConverter.java
 *
 * Created on July 24, 2007, 2:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.nexopia.adblaster.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.nexopia.adblaster.struct.TypeStat;


/**
 *
 * @author baxter
 */
public class XMLTypeStatConverter implements Converter {
	
	/** Creates a new instance of XMLBannerConverter */
	public XMLTypeStatConverter() {
		super();
	}
	
	public boolean canConvert(Class clazz) {
		return clazz.equals(TypeStat.class);
	}
	
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
	}
	
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		return null; //TODO: Implement this if we ever want to unmarshal the XML data.
	}
}
