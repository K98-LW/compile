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

	public boolean isBinaryOp(){
		switch(this.type){
			case PLUS:
			case MINUS:
			case MUL:
			case DIV:
			case EQ:
			case NEQ:
			case LT:
			case GT:
			case LE:
			case GE:
			case ASSIGN:
			case AS_KW:
				return true;
		}
		return false;
	}

	public int getPriority(){
		switch(this.type){
			case ASSIGN:
				return 1;
			case GT:
			case LT:
			case GE:
			case LE:
			case EQ:
			case NEQ:
				return 2;
			case PLUS:
			case MINUS:
				return 3;
			case MUL:
			case DIV:
				return 4;
			case AS_KW:
				return 5;
			default:
				return 6;
		}
	}

	public boolean isRightAssoc(){
		switch(this.type){
			case ASSIGN:
				return true;
			default:
				return false;
		}
	}
}
