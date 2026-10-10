package com.atlas.cc.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileOutputStream4690;

public final class AtlasCCLogger {

	public enum Level {
		NONE(1),
        ERROR(2),
        WARN(3),
        INFO(4),
        DEBUG(5);

        private final int value;

        Level(int v) {
            this.value = v;
        }

        public int getValue() {
            return value;
        }
    }
	
	
	private static final int MAX_INDEX=3;
	
	private static final String BASE_NAME_PREFIX="C:\\silogs\\atls"; //we know that dir will exist!
	private Level currentLevel=Level.DEBUG;
	private boolean reportClassName=true;
	private boolean reportMethodName=true;
	
	private static volatile AtlasCCLogger INSTANCE;
	private static final Object INIT_LOCK= new Object();
	
	private final String identifier;
	//private final PrintWriter fileWriter;
	private FileOutputStream4690 fileOut;
	private final SimpleDateFormat timestampFormat=new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
	
	public static AtlasCCLogger init(String identifier) {
		if (INSTANCE==null) {
			synchronized(INIT_LOCK) {
				if (INSTANCE== null){
					INSTANCE=new AtlasCCLogger(identifier);
				}
			}
		}
		return INSTANCE;
	}
	
	public static AtlasCCLogger getInstance() {
		if (INSTANCE==null) {
			init("000");
		}
		return INSTANCE;
	}
	
	public void setLevel(Level level) {
		if (level!=null) {
			this.currentLevel=level;
			info("Level set to:"+level.name());
		}
		if (level==Level.NONE && fileOut!=null) {
			close(); //close any open file
		}
	}
	
	
	private AtlasCCLogger(String identifier) {
		// TODO Auto-generated constructor stub
		System.out.println("AtlasCCLogger called with:"+identifier);
		this.identifier=identifier;
		this.fileOut=createWriterWithRollover();
	}
	
	private FileOutputStream4690 createWriterWithRollover() {
		System.out.println("AtlasCCLogger opening file");
		String baseName=BASE_NAME_PREFIX+identifier;
		try {
			for(int i=MAX_INDEX; i>=0; i--) {
				File4690 f=new File4690(baseName+"."+i);
				if (f.exists()) {
					if (i==MAX_INDEX) {
						System.out.println("AtlasCCLogger deleting:"+f.getCanonicalPath());
						if (!f.delete()) {
							System.out.println("AtlasCCLogger: Failed to delete "+f.getCanonicalPath());
						}
					} else {
						System.out.println("AtlasCCLogger renaming:"+f.getCanonicalPath());
						File4690 dest=new File4690(baseName+"."+(i+1));
						if (!f.renameTo(dest)) {
							System.out.println("AtlasCCLogger: Failed to rename "+f.getCanonicalPath()+" to "+dest.getCanonicalPath());
							System.out.println("AtlasCCLogger: fromExists-"+f.exists());
							System.out.println("AtlasCCLogger: toExists-"+dest.exists());
							System.out.println("AtlasCCLogger: fromCanWrite-"+f.canWrite());
							System.out.println("AtlasCCLogger: toCanWrite-"+
										(dest.getParentFile()!=null && dest.getParentFile().canWrite()));
							
							
						}
					}
				}
			}
			File4690 logFile=new File4690(baseName+".0");
			System.out.println("AtlasCCLogger file:"+logFile.getCanonicalPath());
			//return new PrintWriter(new FileWriter(logFile,true),true);
			return new FileOutputStream4690(logFile,true);
		} catch (IOException e) {
			System.out.println("AtlasCCLogger: Failed to open log file:"+e.getMessage());
			return null;
		}
	}

	private String getCaller() {
		
		StackTraceElement[] stack=Thread.currentThread().getStackTrace();
		String loggerClass=AtlasCCLogger.class.getName();
		//System.out.println("loggerClass:"+loggerClass);
		//System.out.println("Thread.... :"+Thread.class.getName());
		String result="unknown";
		int l=0;
		for(StackTraceElement element : stack) {
			String className=element.getClassName();
			l +=1;
			//System.out.println("STACK-"+l+":"+className);
			
			if (!className.equals(loggerClass) && (!className.equals(Thread.class.getName()))) {
				String simpleClassName=className;
				int pos=simpleClassName.lastIndexOf('.');
				if (pos>=0) {
					simpleClassName=simpleClassName.substring(pos+1);
				}
				result=simpleClassName.trim();
				if (reportMethodName) {
					result +="."+element.getMethodName();
				}
				break;
			}
		}
		
		return result;
	}
	
	public void info(String msg) {
		log(Level.INFO,msg);
	}
	
	public void warn(String msg) {
		log(Level.WARN, msg);
	}
	
	public void error(String msg) {
		log(Level.ERROR, msg);
	}
	public void debug(String msg) {
		log(Level.DEBUG, msg);
	}

	public synchronized void log(Level level,String message) {
		if (level.getValue()>currentLevel.getValue()) {
			return;
		}
		
		String ts=timestampFormat.format(new Date());
		String output="";
		if (reportClassName) {
			String className=getCaller();
			output=String.format("%s [%s] [%s] %s\r\n", ts,level.name(),className,message);
		} else {
			output=String.format("%s [%s] %s\r\n", ts,level.name(),message);
		}
		if (level==Level.ERROR || level==Level.WARN) {
			System.out.println(output);
		}
		if (fileOut!=null) {
			//fileWriter.println(output);
			try {
				fileOut.write(output.getBytes("UTF-8"));
				fileOut.flush();
			} catch (IOException e) {
				System.err.println("AtlasCCLogger write failed:"+e.getMessage());
			}
		
			
		}
	}
	
	public synchronized void close() {
		if (fileOut!=null) {
			try {
				fileOut.flush();
				fileOut.close();
			} catch (Exception e) {
				//
			}
			fileOut=null;
		}
		
	}
			
}

