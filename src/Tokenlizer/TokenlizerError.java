package Tokenlizer;

@SuppressWarnings("serial")
public class TokenlizerError extends Exception {
	public TokenlizerError(String msg) {
		super();
		System.out.println(msg);
	}
}
