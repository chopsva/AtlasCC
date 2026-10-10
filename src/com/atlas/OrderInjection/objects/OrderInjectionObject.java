package com.atlas.OrderInjection.objects;

import java.nio.charset.StandardCharsets;

public class OrderInjectionObject {

	public OrderInjectionObject() {
		// TODO Auto-generated constructor stub
	}
	
	public String getTLogString() {
		return null;
	}
	
	public byte[] getTLogBytes()  {
		
		return hexToBytes(getTLogString());
	}
	
	protected String valueToPacked(int value) {
		String result="";
		//result=intToHex(value);
		if (result.length()%2==1) {
			result="f"+result;
		}
		return result;
	}
	//intToPacked value=99 -> "63", 5 -> "F5"
	protected String intToPacked(int value) {
		String result=intToHex(value);
		return stringToPacked(result);
	}

	// value=99 -> "63", 5 ->"5"
	protected String intToHex(int value) {
		return Integer.toHexString(value);
	}

	// value=99 -> "99", 5 ->"F5"
	protected String stringToPacked(String value) {
		String result="";
		if (value.length()%2==1) {
			result="f";
		}
		result +=value;
		return result;
	}
	
	//len=3 value=99 -> "099", "5"->005
	protected String intToBCD(int value,int len) {
		String formatString="%0"+len+"d";
		return String.format(formatString, value);
	}
	
	// value=99 
	protected String intToPackedBCD(int value) {
		return stringToPacked(Integer.toString(value));
	}
	
	protected byte[] hexToBytes(String hex) {
	    if (hex == null) {
	        throw new IllegalArgumentException("Hex string cannot be null");
	    }

	    hex = hex.replaceAll("\\s+", "");

	    if ((hex.length() & 1) != 0) {
	        throw new IllegalArgumentException(
	                "Hex string must contain an even number of characters");
	    }

	    byte[] result = new byte[hex.length() / 2];

	    for (int i = 0; i < hex.length(); i += 2) {
	        int high = Character.digit(hex.charAt(i), 16);
	        int low = Character.digit(hex.charAt(i + 1), 16);

	        if (high == -1 || low == -1) {
	            throw new IllegalArgumentException(
	                    "Invalid hex characters at position " + i);
	        }

	        result[i / 2] = (byte) ((high << 4) | low);
	    }

	    return result;
	}
		
	protected String stringToHex(String value) {
		 byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		    StringBuilder hex = new StringBuilder(bytes.length * 2);

		    for (byte b : bytes) {
		        hex.append(String.format("%02X", b & 0xFF));
		    }

		    return hex.toString();
	}
	
		
	
}
