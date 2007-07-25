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


/**
 *
 * @author baxter
 */
public class XMLIntObjectHashMapConverter implements Converter {
	
	/** Creates a new instance of XMLBannerConverter */
	public XMLIntObjectHashMapConverter() {
		super();
	}
	
	public boolean canConvert(Class clazz) {
		return clazz.equals(IntObjectHashMap.class);
	}
	
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
		IntObjectHashMap map = (IntObjectHashMap)value;
		int[] mapKeys = map.getKeyArray(); 
		for (int i=0; i< mapKeys.length; i++) {
			if (mapKeys[i] == 0) //keys of 0 are empty
				continue;
			writer.startNode("entry");
			writer.startNode("key");
			writer.setValue("" + mapKeys[i]);
			writer.endNode();
			writer.startNode("value");
			if (map.get(mapKeys[i]) != null) {
				context.convertAnother(map.get(mapKeys[i]));
			}
			writer.endNode();
			writer.endNode();
		}
	}
	
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		return null; //TODO: Implement this if we ever want to unmarshal the XML data.
	}
}
