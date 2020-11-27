package Tokenlizer;

final public class Token {
	private TokenType type;
	private String value;
	
	public Token(TokenType type, String value) {
		this.type = type;
		this.value = value;
	}
	
	public TokenType getTokenType() {
		return this.type;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public Integer getIntegerValue() {
		try {
			return new Integer(this.value);			
		} catch(NumberFormatException e) {
			return null;
		}
	}
}
