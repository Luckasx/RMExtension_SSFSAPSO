package com.rapidminer.extension.utils;

import java.util.List;


import com.rapidminer.operator.IOObject;

public class UtilsERM {

	  /**
	  * Defines a custom format for the stack trace as String.
	  */
	  public static String getCustomStackTrace(Throwable aThrowable) {
	    //add the class name and any message passed to constructor
	    StringBuilder result = new StringBuilder( "BOO-BOO: " );
	    result.append(aThrowable.toString());
	    String NEW_LINE = System.getProperty("line.separator");
	    result.append(NEW_LINE);

	    //add each element of the stack trace
	    for (StackTraceElement element : aThrowable.getStackTrace()){
	      result.append(element);
	      result.append(NEW_LINE);
	    }
	    return result.toString();
	  }
	  
	  /*
	   * Return the first object of cls param type in the list
	   */
	  public static Object getIOObjectFromIOList(List<IOObject> lio, Class cls){
		  
		  Object ret = null;
		  
		  for(IOObject io : lio) {
			  if(io.getClass() == cls) {
				  return io;
			  }
		  }
		 return ret;
		
	  }
}
