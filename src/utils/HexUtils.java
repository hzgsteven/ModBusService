package utils;

/**
 * 
 * Description:十六进制转换工具 
 * 
 * @author hzg
 * @Date   下午4:19:29
 *
 */
public class HexUtils {
	private static String hexStr = "0123456789ABCDEF";

	/**
	 * 
	 * @Description: 十六进制字符串转byte数组
	 * 
	 * @author hzg
	 * 
	 * @Note
	 * <br><b>Date:</b> 2018年1月5日 下午4:59:33
	 *
	 * @param 
	 * @param hexString
	 * @return
	 */
	public static byte[] hexStringToBinary(String hexString) {
		// hexString的长度对2取整，作为bytes的长度
		int len = hexString.length() / 2;
		byte[] bytes = new byte[len];
		byte high = 0;// 字节高四位
		byte low = 0;// 字节低四位

		for (int i = 0; i < len; i++) {
			// 右移四位得到高位
			high = (byte) ((hexStr.indexOf(hexString.charAt(2 * i))) << 4);
			low = (byte) hexStr.indexOf(hexString.charAt(2 * i + 1));
			bytes[i] = (byte) (high | low);// 高地位做或运算
		}
		return bytes;
	}

	/**
	 * 
	 * @Description: byte数组转十六进制字符串
	 * 
	 * @author hzg
	 * 
	 * @Note
	 * <br><b>Date:</b> 2018年1月5日 下午4:59:33
	 *
	 * @param 
	 * @param bytes
	 * @return
	 */
	public static String binaryToHexString(byte[] bytes) {
		String result = "";
		String hex = "";
		for (int i = 0; i < bytes.length; i++) {
			// 字节高4位
			hex = String.valueOf(hexStr.charAt((bytes[i] & 0xF0) >> 4));
			// 字节低4位
			hex += String.valueOf(hexStr.charAt(bytes[i] & 0x0F));
			result += hex;
		}
		return result;
	}
}
