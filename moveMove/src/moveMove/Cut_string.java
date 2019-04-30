package moveMove;

public class Cut_string {
	public String cut_before_underbar(String str) {
		int i = 0;
		while(!((str.charAt(i) + "").equals("_"))) {
			i++;
		}
		return str.substring(i + 1, str.length());
	}
	public String cut_extension_name(String str) {
		int i = str.length() - 1;
		while(!((str.charAt(i) + "").equals("."))) {
			i--;
		}
		return str.substring(0, i);
	}
	public String get_extension_name(String str) {
		int i = str.length() - 1;
		while(!((str.charAt(i) + "").equals("."))) {
			i--;
		}
		return str.substring(i+1, str.length());
	}
}
