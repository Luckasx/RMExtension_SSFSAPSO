package com.rapidminer.extension.utils;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.rapidminer.tools.LogService;



	public class SwarmLogger {
		
		
		Logger logger;
		FileHandler handler;
		String caminho_arq;
		
		StringBuilder sblog;
		
		public SwarmLogger(String id_ts, boolean deletePreviousLogFile)  {
			
			try 
			{
				sblog = new StringBuilder();
				
				caminho_arq = "c:\\temp\\" + id_ts + ".log";
				//caminho_arq = "c:\\temp\\ultimo.log";
				
				if(deletePreviousLogFile) {
					try {
					File file = new File(caminho_arq);
					file.delete();
					deletePreviousLogFile = false;
					}
					catch(Exception f)
					{
						LogService.getRoot().log(Level.SEVERE,"Catched error deleting the previous log::: " + f.getMessage());
					}
				}
				
				 handler = new FileHandler( caminho_arq, false);
				 
				 handler.setFormatter(new MyFormatter());
				
				logger = Logger.getLogger("com.javacodegeeks.snippets.core");
				
				//logger.addHandler(handler);
			}
			catch(Exception e) {
				LogService.getRoot().log(Level.SEVERE,"Catched error:: " + UtilsERM.getCustomStackTrace(e));
			}
		}
		
		public void log(String args, boolean forced) throws Exception {
			
			sblog.append(args);
			
			if(sblog.length() > 1000000 || forced ) {
			
				boolean openedFile = false;
				
				this.logger.addHandler(handler);
				
				boolean append = true;
			    
			    logger.log(Level.INFO, sblog.toString());
			    
			    this.logger.removeHandler(handler);
			    
			    //clear the string to log
			    sblog.setLength(0);
			}
		    
		    
		}
		
		
	}
	
	class MyFormatter extends Formatter {
	    // Create a DateFormat to format the logger timestamp.
	    private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

	    @Override
	    public String format(LogRecord record) {
	        StringBuilder builder = new StringBuilder(1000);
	        builder.append(df.format(new Date(record.getMillis()))).append(" - ");
	        //builder.append("[").append(record.getSourceClassName()).append(".");
	        //builder.append(record.getSourceMethodName()).append("] - ");
	        builder.append("[").append(record.getLevel()).append("] - ");
	        builder.append(formatMessage(record));
	        builder.append("\n");
	        return builder.toString();
	    }

	    public String getHead(Handler h) {
	        return super.getHead(h);
	    }

	    public String getTail(Handler h) {
	        return super.getTail(h);
	    }

		
	}
