package utils;

/**
 * 
 * Description:ʮ������ת������ 
 * 
 * @author hzg
 * @Date   ����4:19:29
 *
 */
public class HexUtils {
	private static String hexStr = "0123456789ABCDEF";

	/**
	 * 
	 * @Description: ʮ�������ַ���תbyte����
	 * 
	 * @author hzg
	 * 
	 * @Note
	 * <br><b>Date:</b> 2018��1��5�� ����4:59:33
	 *
	 * @param 
	 * @param hexString
	 * @return
	 */
	public static byte[] hexStringToBinary(String hexString) {
		// hexString�ĳ��ȶ�2ȡ������Ϊbytes�ĳ���
		int len = hexString.length() / 2;
		byte[] bytes = new byte[len];
		byte high = 0;// �ֽڸ���λ
		byte low = 0;// �ֽڵ���λ

		for (int i = 0; i < len; i++) {
			// ������λ�õ���λ
			high = (byte) ((hexStr.indexOf(hexString.charAt(2 * i))) << 4);
			low = (byte) hexStr.indexOf(hexString.charAt(2 * i + 1));
			bytes[i] = (byte) (high | low);// �ߵ�λ��������
		}
		return bytes;
	}

	/**
	 * 
	 * @Description: byte����תʮ�������ַ���
	 * 
	 * @author hzg
	 * 
	 * @Note
	 * <br><b>Date:</b> 2018��1��5�� ����4:59:33
	 *
	 * @param 
	 * @param bytes
	 * @return
	 */
	public static String binaryToHexString(byte[] bytes) {
		String result = "";
		String hex = "";
		for (int i = 0; i < bytes.length; i++) {
			// �ֽڸ�4λ
			hex = String.valueOf(hexStr.charAt((bytes[i] & 0xF0) >> 4));
			// �ֽڵ�4λ
			hex += String.valueOf(hexStr.charAt(bytes[i] & 0x0F));
			result += hex;
		}
		return result;
	}
}
