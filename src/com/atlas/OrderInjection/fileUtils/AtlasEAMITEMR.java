package com.atlas.OrderInjection.fileUtils;

import com.atlas.cc.utils.AtlasCCLogger;


public class AtlasEAMITEMR extends AtlasKeyedFile {
	
	public AtlasEAMITEMR() {
		super();
		filename="EAMITEMR";
	}
	
	public int getDept(String upc) {
		int result=0;
		if (findRecord(upc)) {
			int offset=10;
			if (keyLength==7) {
				offset=11;
			}
			result=Integer.parseInt(getBCDAt(offset,2));
		}
		return result;
	}

	public boolean getScaleFlag(String upc) {
		//ind0_bit6 0x40
		boolean result=false;
		int ind0Offset=(keyLength==7) ? 7 : 6;
		if (findRecord(upc)) {
			int ind0=getByteAt(ind0Offset);
			if ((ind0 & 0x40)==0x40) {
				result=true;
			}
			System.out.println("scale "+upc+" IND0:"+ind0+" scaleFlag:"+result);
			AtlasCCLogger.getInstance().info("scale "+upc+" IND0:"+ind0+" scaleFlag:"+result);
		}
		return result;
	}
	
	public String getTaxes(String upc) {
		//System.out.println("getTaxes"+upc);
		String taxes="";
		
		if (findRecord(upc)) {
			int ind1Offset=7;
			
			if (keyLength==7) {
				ind1Offset=8;
			}
			int ind1=getIntAt(ind1Offset);
			if ((ind1 & 0x80)!=0) {
				taxes +="1";
			}
			if ((ind1 & 0x40)!=0) {
				taxes +="2";
			}
			if ((ind1 & 0x20)!=0) {
				taxes +="3";
			}
			if ((ind1 & 0x10)!=0) {
				taxes +="4";
			}
			if ((ind1 & 0x08)!=0) {
				taxes +="F";
			}
		}
		return taxes;
	}

	public String getFamilyCodes(String upc) {
		String result="000000";
		int famOffset=12;
		if (findRecord(upc)) {
			if (keyLength==7) {
				famOffset=13;
			}
		}
		result=getBCDAt(famOffset,3);
		
		return String.format("%6s", result).replace(' ', '0');
	}
	
	public int getItemType(String upc) {
		int result=0;
		int typeOffset=9;
		if (findRecord(upc)) {
			if (keyLength==7) {
				typeOffset=10;
			}
			int tmp=Integer.parseInt(getBCDAt(typeOffset,1));
			AtlasCCLogger.getInstance().debug("from ITEMR type:"+tmp);
			result=tmp/10;
		}
		AtlasCCLogger.getInstance().debug("returning:"+result);
		return result;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("test AtlasEAMITEMR");
		AtlasEAMITEMR ae=new AtlasEAMITEMR();
		
		System.out.println("78616208000 dept:"+ae.getDept("78616208000"));
		System.out.println("78616208000 tax:"+ae.getTaxes("78616208000"));
		

	}

}
