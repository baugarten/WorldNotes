package com.project.geonotes;

public class UtilEncrypt {
	public static byte[] computeHash(String pass) throws Exception {
	     java.security.MessageDigest d =null;
	     d = java.security.MessageDigest.getInstance("SHA-1");
	     d.reset();
	     d.update(pass.getBytes());
	     return  d.digest();
	  }
	public static String byteArrayToHexString(byte[] b){
	     StringBuffer sb = new StringBuffer(b.length * 2);
	     for (int i = 0; i < b.length; i++){
	       int v = b[i] & 0xff;
	       if (v < 16) {
	         sb.append('0');
	       }
	       sb.append(Integer.toHexString(v));
	     }
	     return sb.toString().toUpperCase();
	  }
}
