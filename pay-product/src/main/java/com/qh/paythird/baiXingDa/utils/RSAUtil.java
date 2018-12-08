package com.qh.paythird.baiXingDa.utils;


import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;

public class RSAUtil {
	public static final String KEY_ALGORITHM = "RSA";
	//public static final String KEY_ALGORITHM = "RSA";
	public static final String C_ALGORITHM = "BC";
	public static final String split = " ";// 分隔符
	public static final int max = 117;// 加密分段长度//不可超过117

	private static RSAUtil me;
	static {
		Security.addProvider(new BouncyCastleProvider());  
	}
	private RSAUtil() {
		
	}// 单例

	public static RSAUtil create() {
		if (me == null) {
			me = new RSAUtil();
		}
		// 生成公钥、私钥
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM);
			kpg.initialize(1024);
			KeyPair kp = kpg.generateKeyPair();
			me.publicKey = (RSAPublicKey) kp.getPublic();
			me.privateKey = (RSAPrivateCrtKey) kp.getPrivate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return me;
	}

	private RSAPublicKey publicKey;
	private RSAPrivateCrtKey privateKey;

	/** 获取公钥 */
	public String getPublicKey() {
		return parseByte2HexStr(publicKey.getEncoded());
	}

	/** 获取私钥 */
	public String getPrivateKey() {
		return parseByte2HexStr(privateKey.getEncoded());
	}

	/** 加密-公钥 */
	public static  String encodeByPublicKey(String res, String key) {
		byte[] resBytes = res.getBytes();
		byte[] keyBytes = parseHexStr2Byte(key);// 先把公钥转为2进制
		StringBuffer result = new StringBuffer();// 结果
		// 如果超过了100个字节就分段
		if (keyBytes.length <= max) {// 不超过直接返回即可
			return encodePub(resBytes, keyBytes);
		} else {
			int size = resBytes.length / max + (resBytes.length % max > 0 ? 1 : 0);
			for (int i = 0; i < size; i++) {
				int len = i == size - 1 ? resBytes.length % max : max;
				byte[] bs = new byte[len];// 临时数组
				System.arraycopy(resBytes, i * max, bs, 0, len);
				result.append(encodePub(bs, keyBytes));
				if (i != size - 1)
					result.append(split);
			}
			return result.toString();
		}
	}

	/** 加密-私钥 */
	public static String encodeByPrivateKey(String res, String key) {
		byte[] resBytes = res.getBytes();
		byte[] keyBytes = parseHexStr2Byte(key);
		StringBuffer result = new StringBuffer();
		// 如果超过了100个字节就分段
		if (keyBytes.length <= max) {// 不超过直接返回即可
			return encodePri(resBytes, keyBytes);
		} else {
			int size = resBytes.length / max + (resBytes.length % max > 0 ? 1 : 0);
			for (int i = 0; i < size; i++) {
				int len = i == size - 1 ? resBytes.length % max : max;
				byte[] bs = new byte[len];// 临时数组
				System.arraycopy(resBytes, i * max, bs, 0, len);
				result.append(encodePri(bs, keyBytes));
				if (i != size - 1)
					result.append(split);
			}
			return result.toString();
		}
	}

	/** 解密-公钥 */
	public static String decodeByPublicKey(String res, String key) {
		byte[] keyBytes = parseHexStr2Byte(key);
		// 先分段
		String[] rs = res.split("\\" + split);
		// 分段解密
		if (rs != null) {
			int len = 0;
			// 组合byte[]
			byte[] result = new byte[rs.length * max];
			for (int i = 0; i < rs.length; i++) {
				byte[] bs = decodePub(parseHexStr2Byte(rs[i]), keyBytes);
				System.arraycopy(bs, 0, result, i * max, bs.length);
				len += bs.length;
			}
			byte[] newResult = new byte[len];
			System.arraycopy(result, 0, newResult, 0, len);
			// 还原字符串
			return new String(newResult);
		}
		return null;
	}

	/** 解密-私钥 */
	public static String decodeByPrivateKey(String res, String key) {
		byte[] keyBytes = parseHexStr2Byte(key);
		// 先分段
		String[] rs = res.split("\\" + split);
		// 分段解密
		if (rs != null) {
			int len = 0;
			// 组合byte[]
			byte[] result = new byte[rs.length * max];
			for (int i = 0; i < rs.length; i++) {
				byte[] bs = decodePri(parseHexStr2Byte(rs[i]), keyBytes);
				System.arraycopy(bs, 0, result, i * max, bs.length);
				len += bs.length;
			}
			byte[] newResult = new byte[len];
			System.arraycopy(result, 0, newResult, 0, len);
			// 还原字符串
			return new String(newResult);
		}
		return null;
	}

	/** 将二进制转换成16进制 */
	public static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}

	/** 将16进制转换为二进制 */
	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}

	/** 加密-公钥-无分段 */
	private static String encodePub(byte[] res, byte[] keyBytes) {
		X509EncodedKeySpec x5 = new X509EncodedKeySpec(keyBytes);// 用2进制的公钥生成x509
		try {
			KeyFactory kf = KeyFactory.getInstance(KEY_ALGORITHM);
			Key pubKey = kf.generatePublic(x5);// 用KeyFactory把x509生成公钥pubKey
			Cipher cp = Cipher.getInstance(kf.getAlgorithm(),C_ALGORITHM);// 生成相应的Cipher
			cp.init(Cipher.ENCRYPT_MODE, pubKey);// 给cipher初始化为加密模式，以及传入公钥pubKey
			return parseByte2HexStr(cp.doFinal(res));// 以16进制的字符串返回
		} catch (Exception e) {
			System.out.println("公钥加密失败");
			e.printStackTrace();
		}
		return null;
	}

	/** 加密-私钥-无分段 */
	private static String encodePri(byte[] res, byte[] keyBytes) {
		PKCS8EncodedKeySpec pk8 = new PKCS8EncodedKeySpec(keyBytes);
		try {
			KeyFactory kf = KeyFactory.getInstance(KEY_ALGORITHM);
			Key priKey = kf.generatePrivate(pk8);
			Cipher cp = Cipher.getInstance(kf.getAlgorithm(),C_ALGORITHM);
			cp.init(Cipher.ENCRYPT_MODE, priKey);
			return parseByte2HexStr(cp.doFinal(res));
		} catch (Exception e) {
			System.out.println("私钥加密失败");
			e.printStackTrace();
		}
		return null;
	}

	/** 解密-公钥-无分段 */
	private static byte[] decodePub(byte[] res, byte[] keyBytes) {
		X509EncodedKeySpec x5 = new X509EncodedKeySpec(keyBytes);
		try {
			KeyFactory kf = KeyFactory.getInstance(KEY_ALGORITHM);
			Key pubKey = kf.generatePublic(x5);
//			Cipher cp = Cipher.getInstance(kf.getAlgorithm(),C_ALGORITHM);
			//默认为：RSA/ECB/PKCS1Paddin PHP
			//java BC默认为：RSA/None/NoPadding
			Cipher cp = Cipher.getInstance(kf.getAlgorithm(),C_ALGORITHM);
			cp.init(Cipher.DECRYPT_MODE, pubKey);
			return cp.doFinal(res);
		} catch (Exception e) {
			System.out.println("公钥解密失败");
			e.printStackTrace();
		}
		return null;
	}

	/** 解密-私钥-无分段 */
	private static byte[] decodePri(byte[] res, byte[] keyBytes) {
		PKCS8EncodedKeySpec pk8 = new PKCS8EncodedKeySpec(keyBytes);
		try {
			KeyFactory kf = KeyFactory.getInstance(KEY_ALGORITHM);
			Key priKey = kf.generatePrivate(pk8);
			//System.out.println(kf.getAlgorithm());
			Cipher cp = Cipher.getInstance(kf.getAlgorithm(),C_ALGORITHM);
			//Cipher cp = Cipher.getInstance("RSA/None/NoPadding",C_ALGORITHM);
			cp.init(Cipher.DECRYPT_MODE, priKey);
			return cp.doFinal(res);
		} catch (Exception e) {
			System.out.println("私钥解密失败");
			e.printStackTrace();
		}
		return null;
	}
	public static void main(String[] args) {
//		String privateKey="30820276020100300D06092A864886F70D0101010500048202603082025C020100028181008BDFB86E48BC40D576D6DA2EC7812527CF9DBFE7A9A52DB14D6EBA0923D8EC513D368EC0B87E316EE216923AD6004F9A2E005F049DE7A2BAC4D311BEF57D2AD244512CAAE52402886B881CBC488F937AF8A462079D5452500FAD7B3EB588C8B350E3E36D58983958BE8BBC2C789801DAC52842D286857BC04E58692048C9F02B020301000102818000B2AA11EACCFDA9D300F96FB8511DA55834DA4B469E6B5EF21AF654AB581EBDBD0773A8E482C4AE5FF46AE05E46FC1EB1D210A74E3E64A277EEF1ACB93ED9A2D7082EC00F818309B18B4A54C1F7EEC76D3AC55760228E97B2BF1E508842D24B6425CE81B7A0416E64E12703AA9AEF5B127AD01E043CE93C29CB5315E63A1B19024100DFCC03BB72AA9FEC5C75CE1E909196B9CD3E85358A80BD34C676833F7D566A9A9128E45B804E88FF542DE8CF429F01BD63E7F3B10FDAD8E8076EA285C3502867024100A0003DE1F0119EBAEB1267A166085CC44B5752EB8917E7E4382290EE541A7248F0E58ACB484299232764F462FD2285FB0FEC12FB9B84B0187327B78DC02FEF9D0240669305AAE6B599B325F5C0D17585A545E7F29DEF9A59C35EEB0FEABC30E068E00B6468B61FAADF53D7EB6AE7842B890A9B3DF70DD2AC85FA635DAC140F1F304502407C2B33DB1F9FCCFB6A6AC219B341521CD40B54C28D860BE444DC2E7586B76F71C3E19FB0DAC73468DFADAD2B151FB1B2814CC7FE93568A02AF7B672B8EB6A4D1024100BEBBE273FD4CE592084B3615B37E7A41AFC3182A94781C53937D74702DC2321DB601A4D7BCD3653BD598B41ACCCAC2195A8F877C744D6924B853532817996131";
		String resOld="27B17443C13AD1FA82F17D60361F85E7E78A3E4C9CD6B7A06573C05E4C342160710A68B454A5B05E98D5EA01F6361F7AED96B6AD5EBE4B0EC893F9694187BFD7D468A0E9BEFC7ED65C4F235E43F909F99341266E7E0F71FD58AAFF7C0D426EF9FEAF5E0CB05BCB2A5E516275ABD6F0F4E53B670D65CCB4DBCAB260E335150A17";
//		System.out.println(encodeByPrivateKey("123456", privateKey));
		String base64="ZcjgOq+LeO+WYtE1F1tMY2+D7H93KOF9gh+Ry3CNXHBxVPXQxj7qbxbldbWVpHvbIfSs4v2qQBn0qM0BWYVESfiwc7WZWGtyu3me0tRDS2RLmg4WqQL9VBwrNSOGzx7QfykBTG1Y24gdZhyRBOk9fwvgzK0xcz/FVvMxwpzf7aI=";
		String lastBase64="J7F0Q8E60fqC8X1gNh+F5+eKPkyc1regZXPAXkw0IWBxCmi0VKWwXpjV6gH2Nh967Za2rV6+Sw7Ik/lpQYe/19RooOm+/H7WXE8jXkP5CfmTQSZufg9x/Viq/3wNQm75/q9eDLBbyypeUWJ1q9bw9OU7Zw1lzLTbyrJg4zUVChc=";
		String publicKey="30819F300D06092A864886F70D010101050003818D00308189028181008BDFB86E48BC40D576D6DA2EC7812527CF9DBFE7A9A52DB14D6EBA0923D8EC513D368EC0B87E316EE216923AD6004F9A2E005F049DE7A2BAC4D311BEF57D2AD244512CAAE52402886B881CBC488F937AF8A462079D5452500FAD7B3EB588C8B350E3E36D58983958BE8BBC2C789801DAC52842D286857BC04E58692048C9F02B0203010001";
		//String res=HexBinary.encode(new Base64().decode(lastBase64));
		//System.out.println(res);
//		System.out.println(decodeByPublicKey(resOld, publicKey));
		//System.out.println(decodeByPublicKey(res, publicKey));
		//ZcjgOq+LeO+WYtE1F1tMY2+D7H93KOF9gh+Ry3CNXHBxVPXQxj7qbxbldbWVpHvbIfSs4v2qQBn0qM0BWYVESfiwc7WZWGtyu3me0tRDS2RLmg4WqQL9VBwrNSOGzx7QfykBTG1Y24gdZhyRBOk9fwvgzK0xcz/FVvMxwpzf7aI=
	}
}