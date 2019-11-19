package com.loumeng.Bluetooth;

public class greatpw {

	 public static byte[] hexStr2Bytes(String paramString) {
	        int i = paramString.length() / 2;

	        byte[] arrayOfByte = new byte[i];
	        int j = 0;
	        while (true) {
	            if (j >= i)
	                return arrayOfByte;
	            int k = 1 + j * 2;
	            int l = k + 1;
	            arrayOfByte[j] = (byte) (0xFF & Integer.decode(
	                    "0x" + paramString.substring(j * 2, k)
	                            + paramString.substring(k, l)).intValue());
	            ++j;
	        }
	    }
		public static int highCRC(byte[] data){
			int CRC=0;
			int genPoly = 0X33;
			for(int i=0;i<data.length-3; i++){
				CRC = data[i]^CRC;
				for(int j=0;j<8;j++){
					if((CRC & 0x80) != 0){
						CRC = (CRC << 1) ^ genPoly;
					}else{
						CRC <<= 1;
					}
				}
			}
			CRC &= 0xff;
			return CRC;
		}
		public static int lowCRC(byte[] data){
			int CRC=0;
			int genPoly = 0X33;
			for(int i=3;i<data.length; i++){
				CRC = data[i]^CRC;
				for(int j=0;j<8;j++){
					if((CRC & 0x80) != 0){
						CRC = (CRC << 1) ^ genPoly;
					}else{
						CRC <<= 1;
					}
				}
			}
			CRC &= 0xff;
			return CRC;
		}
	 public static String greatpassword(String adress){
		 adress=adress.replace(":", "");
		 byte[] data =hexStr2Bytes(adress);
	    	int highpw=highCRC(data);

		    int lowpw =lowCRC(data);

		    String St_highpw= String.valueOf(highpw);
		    String St_lowpw= String.valueOf(lowpw);
		    //android studio ʹ�����д���
//	    String St_highpw= String.format("%03", highpw);
//	    String St_lowpw= String.format("%03", lowpw);
		
		 return St_highpw+St_lowpw;
	 }


}
