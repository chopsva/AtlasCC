package com.atlas.OrderInjection.fileUtils;

import java.util.Arrays;

import com.ibm.OS4690.FlexosException;
import com.ibm.OS4690.InvalidParameterException;
import com.ibm.OS4690.KeyedFile;
import com.ibm.OS4690.POSFile;

public class AtlasKeyedFile {

	public int recordLength;
	public int keyLength;
	public String filename;
	KeyedFile theFile;
	byte recordBuffer[];

	POSFile posFile;
	int seqBlockPointer;
	byte blockBuffer[];
	int blockPointer;
	int recordsPerBlock;
	
	public AtlasKeyedFile() {
		theFile=null;
		recordLength=0;
		keyLength=0;
		recordBuffer=new byte[508]; //508 is max record size for IBM
		filename="";
	}
	
	//open will only be called internally
	private boolean openFile() {
		boolean theReturn=false;
		if (theFile!=null) {
			return true; //already exits 
		}

		try {
			theFile=new KeyedFile(filename,"rw",KeyedFile.SHARED_READ_WRITE_ACCESS);
			theReturn=true;
		} catch (FlexosException e) {
			// TODO Auto-generated catch block
			theFile=null;
			e.printStackTrace();
			
		} catch (InvalidParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			theFile=null;
		}

		try {
			recordLength=theFile.getRecordSize();
			keyLength=theFile.getKeyLength();
		}
		catch (FlexosException e) {
			recordLength=0;
			keyLength=0;
			theReturn=false;
		}
		
		return theReturn;
	}
	
	//wrapper of the 4690OS read() uses recordBuffer
	//assumes recordBuffer is primed and ready
	private boolean readFile() {
		boolean theReturn=false;
		if (theFile==null) {
			if (!openFile()) {
				return false;
			}
		}
		try {
			int result=theFile.read(recordBuffer,KeyedFile.NO_LOCK);
			theReturn=true;
		} catch (FlexosException e) {
			// TODO Auto-generated catch block
			closeFile(); //os error, close the file 
			//e.printStackTrace();
			
		} catch (InvalidParameterException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
//			IGMLogger.getInstance().debug(e);
		}
//		IGMLogger.getInstance().debug("read:");
//		dumpBuffer(recordBuffer);
		return theReturn;
	}
	
	protected boolean writeFile() {
		boolean theReturn=false;
		if (theFile==null) {
			if (!openFile()){
				return false;
			}
 		}
		//need a buffer of the exact size
		byte[] writeBuffer=new byte[recordLength];
		for(int i=0;i<recordLength;i++) {
			writeBuffer[i]=recordBuffer[i];
		}
		
//		dumpBuffer(writeBuffer);
		
		
		try {
			int result=theFile.write(writeBuffer, KeyedFile.NO_LOCK,KeyedFile.NO_HOLD);
			theReturn=true;
		} catch (FlexosException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
			theFile=null;
			recordLength=0;
			keyLength=0;
			closeFile();
		} catch (InvalidParameterException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
		}
		return theReturn;
	}
/// above access by KEY
//---------------------------------------------------------
/// below access seq. 
	private boolean openSeqFile() {
		boolean result=false;
		if (posFile!=null) 
			return true;
		
		
		try {
			posFile=new POSFile(filename,"r",POSFile.SHARED_READ_WRITE_ACCESS);
			
			result=true;
		} catch (FlexosException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
		} catch (InvalidParameterException e) {
			// TODO Auto-generated catch block
			
		}
		
		return result;
	}
	
	public void closeSeqFile() {
		try {
			posFile.closeFull();
		} catch (FlexosException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
		}
		posFile=null;
	}
	
	public void initSequentialRead() {
		if (posFile==null) {
			if (!openSeqFile()) {
				return; 
			}
		}
		blockBuffer=new byte[512];
		
		blockPointer=0;
		seekAndLoadBlock(blockPointer);
		int key1=blockBuffer[54] & 0xff;
		int key2=blockBuffer[55] & 0xff;
		keyLength=(key2 * 256) + key1;
		
		int rec1=blockBuffer[46] & 0xff;
		int rec2=blockBuffer[47] & 0xff;
		recordLength=(rec2 * 256) + rec1;
		
		recordsPerBlock=508/recordLength;
		
		
		
		blockPointer=1; //first block is pointers
		seqBlockPointer=0;
		
		seekAndLoadBlock(blockPointer);
		
	}
	
	private boolean seekAndLoadBlock(int block) {
		//load the numbered block
		//assume file opened
		boolean theReturn=false;
		
		try {
			posFile.seek(block * 512);
			int t=posFile.read(blockBuffer, 0, posFile.FROM_CURRENT_FILE_POINTER, posFile.READ_FROM_DISK, 512);
			if (t!=-1) {
				theReturn=true;
			}
		} catch (FlexosException e) {
			// TODO Auto-generated catch block
			closeSeqFile();
			//e.printStackTrace();
			
		}
		
		return theReturn;
	}
	
	public boolean loadNextRecord() {
		boolean theReturn=false;
		if (seqBlockPointer>=recordsPerBlock) {
			blockPointer+=1;
			seqBlockPointer=0;
			if (!seekAndLoadBlock(blockPointer)) {
				return false;
			}
		}
		
		int startOffset=(seqBlockPointer * recordLength)+4;
		for(int i=0;i<recordLength;i++) {
			recordBuffer[i]=blockBuffer[startOffset+i];
		}
		theReturn=true;
		
		seqBlockPointer+=1;
		return theReturn;
	}
	
	
	
	public void writeToRecordBuffer(byte[] toWrite,int offset,int length) {
		//copies toWrite to the recordBuffer
		for(int i=0;i<length;i++) {
			recordBuffer[i+offset]=toWrite[i];
		}
	}
	
	
	public void dumpBuffer(byte[] b) {
		for(int i=0;i<b.length;i++) {
//			IGMLogger.getInstance().debug((b[i]& 0xFF) + " ");
		}
//		IGMLogger.getInstance().debug("-");
	}
	
	public void closeFile() {
		if (theFile==null) {
			return;
		}
		try {
			theFile.closeFull();
		} catch (FlexosException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
		}
		theFile=null;
		recordLength=0;
		keyLength=0;
	}

	protected void clearRecordBuffer() {
		Arrays.fill( recordBuffer, (byte) 1 );
	}
	
	protected void blankRecordBuffer() {
		Arrays.fill( recordBuffer, (byte)0); 
	}
	
	private String padKey(String theKey) {
		return padString(theKey,keyLength*2);
	}
	
	private String padString(String incomming,int length) {
		String thePaddedString=incomming;
		if (incomming.length()!=length) {
			for(int i=incomming.length();i<length;i++) {
				thePaddedString="0"+thePaddedString;
			}
		}
		return thePaddedString;
	}
	
	public boolean findRecord(String theKey) {
		openFile(); //need to "open" the file to prime record & key lengths
		
		boolean theReturn=false;
		byte[] keyBuffer=stringToBCD(theKey,keyLength);
		dumpBuffer(keyBuffer);
		clearRecordBuffer();
		writeToRecordBuffer(keyBuffer,0,keyLength);
		theReturn=readFile();
		
		return theReturn;
	}
	
	public void primeNewRecord(String theKey) {
		openFile();
		byte[] keyBuffer=stringToBCD(theKey,keyLength);
		blankRecordBuffer();
		writeToRecordBuffer(keyBuffer,0,keyLength);
		//buffer now just has the key and all 0s
	}
	
	public byte[] stringToBCD(String theValue,int theByteLength) {
		byte[] result=new byte[theByteLength];
		String workValue=padString(theValue,theByteLength*2); //pad the value here
//		IGMLogger.getInstance().debug("worjValeu:"+workValue);
		byte[] workBytes=workValue.getBytes();
		int temp=0;	
		int bp=0;
		for(int p=0;p<=((theByteLength*2)-1); p=p+2) {
//			IGMLogger.getInstance().debug("a:"+p);
			temp=((workBytes[p]-48) * 16) + (workBytes[p+1]-48);
			Integer tempI=new Integer(temp);
//			IGMLogger.getInstance().debug("b:"+bp);
			result[bp]=tempI.byteValue();
//			IGMLogger.getInstance().debug(temp);
//			IGMLogger.getInstance().debug((workBytes[p]-48) + " " + (workBytes[p+1]-48) + '=' + tempI );
			bp=bp+1;
		}
//		IGMLogger.getInstance().debug("thevalue:"+theValue);
//		dumpBuffer(result);
		
		return result;
	}
	
	public int getByteAt(int offset) {
		int val=0;
		val= ((int)recordBuffer[offset] & 0xff);
		return val;
	}
	
	public void putByteAt(byte val,int offset) {
		recordBuffer[offset]=val;
	}
	
	
	public int getShortAt(int offset) {
		int val=0;
		val= ((int)recordBuffer[offset] & 0xff) + (((int)recordBuffer[offset+1] & 0xff))*256;
		
		return val;
	}

	public void putShortAt(int val,int offset) {
		int highNib,lowNib;
		highNib=val/256;
		lowNib=val-(highNib*256);
		recordBuffer[offset]=(byte)(lowNib & 0xff);
		recordBuffer[offset+1]=(byte)(highNib & 0xff);
//		IGMLogger.getInstance().debug("hi:"+highNib);
//		IGMLogger.getInstance().debug("lo:"+lowNib);
	}

	
	public int getIntAt(int offset) {
		int val=0;
		val= ((int)recordBuffer[offset] & 0xff) + (((int)recordBuffer[offset+1] & 0xff))*256 + 
			(((int)recordBuffer[offset+2] & 0xff))*65536 + (((int)recordBuffer[offset+3] & 0xff))*16777216;  //
		
		return val;
	}

	public void putIntAt(int val, int offset) {
		int high=val/65536;
		int low=val-(high*65536);
		putShortAt(low,offset);
		putShortAt(high,offset+2);
		
	}
	
	public String getStringAt(int offset,int len) {
		byte[] r=new byte[len];
		for(int i=0;i<len;i++) {
			r[i]=recordBuffer[offset+i];
		}
		return new String(r);
	}
	
	public void putStringAt(String val, int offset, int len) {
		String workString=val;
		if (val.length()<len) {
			for(int i=val.length();i<len;i++)
				workString=workString+" ";
		}
		writeToRecordBuffer(workString.getBytes(),offset,len);
		
	}
	
	public String getBCDAt(int offset, int len) {
		String tempString=new String("");
		long tempLong,highNib,lowNib=0;
		
		for(int i=offset;i<offset+len;i++) {
			tempLong=(int)recordBuffer[i] & 0xff;
			highNib=tempLong/16;
			lowNib=tempLong-(highNib*16);
			tempString=tempString+highNib+lowNib;
		}
		return tempString;
	}	

	public void putBCDAt(String val,int offset, int len) {
		byte[] workBCD=stringToBCD(val,len);
		writeToRecordBuffer(workBCD,offset,len);
	}
	
//--utils
	public String stripLeadingZeros(String incomming) {
		return incomming.replaceFirst("^0+(?!$)","");
	}
	
	
	public static void main(String args[]){
		AtlasKeyedFile k=new AtlasKeyedFile();
		k.filename="EAMCSCF1";
		System.out.println(k.findRecord("9998"));
		System.out.println(k.getStringAt(2,8));
		k.closeFile();
		
//		k.filename="EAMITEMR";
	
//		IGMLogger.getInstance().debug("try reading itemr 78616208000");
//		IGMLogger.getInstance().debug("->"+k.findRecord("78616208000"));
//		
//		IGMLogger.getInstance().debug("UPC:"+k.getBCDAt(0, 6));
//		IGMLogger.getInstance().debug("NAME:"+k.getStringAt(24, 18));
//		IGMLogger.getInstance().debug("DEPT:"+k.getBCDAt(10,2));
//		
//		k.putBCDAt("626", 10, 2);
//		k.putStringAt("NEW NAME", 24, 18);
//		k.putByteAt((byte)32, 6); //qty req.
//		k.putShortAt(626, 42); //usrexit1
//		k.writeFile();
//		k.closeFile();
	}
}