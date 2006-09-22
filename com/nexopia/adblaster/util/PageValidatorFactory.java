package com.nexopia.adblaster.util;

import java.lang.reflect.InvocationTargetException;


public class PageValidatorFactory {
	Class klass;
	Object args[];
	
	public PageValidatorFactory(Class c, Object args[]){
		klass = c;
		this.args = args;
	}
	
	public PageValidator make(){
		try {
			return (PageValidator)klass.getConstructors()[0].newInstance(args);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
