package com.atlas.OrderInjection.fileUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;

import com.ibm.OS4690.File4690;
import com.ibm.OS4690.FileOutputStream4690;

public final class WriteToTLog {

    private WriteToTLog() {
        // Utility class
    }

    /**
     * Converts hexadecimal text to binary bytes and appends them to a file.
     *
     * Supported examples:
     *   "48 65 6C 6C 6F"
     *   "48656C6C6F"
     *   "0x48 0x65 0x6C 0x6C 0x6F"
     *
     * @param fileName  file to append to
     * @param hexString hexadecimal byte data
     * @throws IOException if the file cannot be written
     * @throws IllegalArgumentException if the hexadecimal string is invalid
     */
    public static void appendHex(String fileName, String hexString)
            throws IOException {

        appendHex(new File4690(fileName), hexString);
    }

    public static void appendHex(File4690 file, String hexString)
            throws IOException {

        byte[] data = hexStringToBytes(hexString);

        // The "true" argument enables append mode.
        try (BufferedOutputStream output =
                     new BufferedOutputStream(
                             new FileOutputStream4690(file, true))) {

            output.write(data);
        }
    }

    /**
     * Converts hexadecimal text into a byte array.
     */
    public static byte[] hexStringToBytes(String hexString) {

        if (hexString == null) {
            throw new IllegalArgumentException(
                    "Hexadecimal string cannot be null.");
        }

        // Remove common separators and optional 0x prefixes.
        String cleaned = hexString
                .replaceAll("(?i)0x", "")
                .replaceAll("[\\s,;:-]", "");

        if (cleaned.isEmpty()) {
            return new byte[0];
        }

        if ((cleaned.length() % 2) != 0) {
            throw new IllegalArgumentException(
                    "Hexadecimal string must contain an even number of digits.");
        }

        if (!cleaned.matches("[0-9A-Fa-f]+")) {
            throw new IllegalArgumentException(
                    "Hexadecimal string contains invalid characters: "
                            + hexString);
        }

        byte[] result = new byte[cleaned.length() / 2];

        for (int i = 0; i < cleaned.length(); i += 2) {
            int high = Character.digit(cleaned.charAt(i), 16);
            int low = Character.digit(cleaned.charAt(i + 1), 16);

            result[i / 2] = (byte) ((high << 4) | low);
        }

        return result;
    }
    
    private static String getCurrentTLog() {
    	AtlasKeyedFile k=new AtlasKeyedFile();
    	String result="";
		k.filename="EAMCSCF1";
		if (k.findRecord("9998")) {
			result="c:\\adx_idt4\\"+k.getStringAt(2,8)+".dat";
		}
		k.closeFile();
		System.out.println("Current TLOG:"+result);
    	return result;
    }
    
    public static boolean writeHex(String trans) {
    	boolean result=true;
        try {
            WriteToTLog.appendHex(
                    getCurrentTLog(),
                    trans
            );

            System.out.println("Hexadecimal bytes appended successfully.");

        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            result=false;
        }
    	return result;
    }

    public static void main(String[] args) {
    	//String newTrans="22003A06003A00603A26072412013A003A00033AF6003AF6003AF3953AF03AF53AF13A3AF53A643A003A222C22013AF1003AF3693AF6263A0000003A723AF03A013AF03AF13A222C22053A413AF3953AF03A3A00222C22073A3A263AF6263A3A3AF3693AF1003A3A3A3A3A3A3A3A3A220D0A";
    	String newTrans="22003a06003a00753a24072516203a003af43af6263af6263af8003af03af13af13af13af13af03a003a222c22013af1003af3953a103a0000003a723af03a003af03af13a222c22013af1013af3953a113a0000003a643af03a003af03af23a222c22053a113af8003af03a3a00222c22073af03a103af03af03af03af7903af03af03af03af03af03af03af03af03af03af0220d0a";
        try {
            WriteToTLog.appendHex(
                    getCurrentTLog(),
                    newTrans
            );

            System.out.println("Hexadecimal bytes appended successfully.");

        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}