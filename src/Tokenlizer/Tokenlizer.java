package Tokenlizer;

import Reader.Reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final public class Tokenlizer {
	private static Tokenlizer tokenlizer;
	private Reader reader;
	private StringBuilder tokenValue;
	
	private List<Token> tokenList;
	
	private Tokenlizer() {}
	
	public static Tokenlizer getInstance() {
		if(tokenlizer  == null) {
			tokenlizer = new Tokenlizer();
		}
		return tokenlizer;
	}
	
	public void init() throws IOException {
		this.reader = Reader.getInstance();
		this.tokenValue = new StringBuilder();
		this.tokenList = new ArrayList<Token>();
	}
	
	public List<Token> analyze() throws TokenlizerError {
		this.reader.backToHead();
		
		while(!reader.isEOF()) {
			this.tokenValue.delete(0, tokenValue.length());
			
			int c = this.reader.getChar();
			if(isDigit(c)) {
				numberState();
			}
			else if(isAlpha(c) || c == '_') {
				wordState();
			}
			else if(isBlank(c)) {
				this.reader.nextChar();
			}
			else {
				switch(c) {
					case '+':
						saveToken(new Token(TokenType.PLUS, "+"));
						break;
					case '-':
						if(this.reader.nextChar().getChar() == '>') {
							saveToken(new Token(TokenType.ARROW, "->"));							
						}
						else {
							saveToken(new Token(TokenType.MINUS, "-"));
							this.reader.lastChar();
						}
						break;
					case '*':
						saveToken(new Token(TokenType.MUL, "*"));
						break;
					case '/':
						if(this.reader.nextChar().getChar() == '/') {
							commentState();
						}
						else {
							saveToken(new Token(TokenType.DIV, "/"));
							this.reader.lastChar();
						}
						break;
					case '=':
						if(this.reader.nextChar().getChar() == '=') {
							saveToken(new Token(TokenType.EQ, "=="));							
						}
						else {
							saveToken(new Token(TokenType.ASSIGN, "="));
							this.reader.lastChar();
						}
						break;
					case '!':
						if(this.reader.nextChar().getChar() == '=') {
							saveToken(new Token(TokenType.NEQ, "!="));							
						}
						else {
							throw new TokenlizerError("Unknown sign '!'.");
						}
						break;
					case '<':
						if(this.reader.nextChar().getChar() == '=') {
							saveToken(new Token(TokenType.LE, "<="));
						}
						else {
							saveToken(new Token(TokenType.LT, "<"));
							this.reader.lastChar();
						}
						break;
					case '>':
						if(this.reader.nextChar().getChar() == '=') {
							saveToken(new Token(TokenType.GE, ">="));
						}
						else {
							saveToken(new Token(TokenType.GT, ">"));
							this.reader.lastChar();
						}
						break;
					case '(':
						saveToken(new Token(TokenType.L_PAREN, "("));
						break;
					case ')':
						saveToken(new Token(TokenType.R_PAREN, ")"));
						break;
					case '{':
						saveToken(new Token(TokenType.L_BRACE, "{"));
						break;
					case '}':
						saveToken(new Token(TokenType.R_BRACE, "}"));
						break;
					case ',':
						saveToken(new Token(TokenType.COMMA, ","));
						break;
					case ':':
						saveToken(new Token(TokenType.COLON, ":"));
						break;
					case ';':
						saveToken(new Token(TokenType.SEMICOLON, ";"));
						break;
					case '\'':
						charState();
						break;
					case '"':
						stringState();
						break;
					default:
						throw new TokenlizerError("Unknown char.");						
				}
				this.reader.nextChar();
			}
		}
		
		return this.tokenList;
	}
	
	private void numberState() throws TokenlizerError {
		int c = this.reader.getChar();
		
		while(!isBlank(c) && !this.reader.isEOF()) {
			if(c == '.') {
				this.tokenValue.append((char)c);
				this.reader.nextChar();
				doubleState();
				return;
			}
			else if(!isDigit(c)) {
				this.reader.lastChar();
				break;
			}

			this.tokenValue.append((char)c);
			this.reader.nextChar();
			
			c = this.reader.getChar();
		}
		saveToken(new Token(TokenType.UINT_LITERAL, tokenValue.toString()));
		
		if(!this.reader.isEOF()) {
			this.reader.nextChar();
		}
	}
	
	private void doubleState() throws TokenlizerError {
		boolean eFlag = false;
		int c = this.reader.getChar();
		
		if(!isDigit(c)) {
			throw new TokenlizerError("After point must be a number.");
		}
		
		while(!isBlank(c) && this.reader.isEOF()) {
			this.tokenValue.append((char)c);
			this.reader.nextChar();
			
			if(c == 'e' || c == 'E') {
				if(eFlag) {
					throw new TokenlizerError("More than one 'e' in a double.");
				}
				
				if(this.reader.isEOF() || !isDigit(this.reader.getChar())){
					throw new TokenlizerError("After 'E' must be a number.");
				}
				
				eFlag = true;
			}
			else if(!isDigit(c)) {
				throw new TokenlizerError(String.format("Unknown %c in a double.", c));
			}
			
			c = this.reader.getChar();
		}
		saveToken(new Token(TokenType.DOUBLE_LITERAL, this.tokenValue.toString()));
		
		if(!this.reader.isEOF()) {
			this.reader.nextChar();
		}
	}
	
	private void wordState() throws TokenlizerError {
		int c = this.reader.getChar();
		
		while(!isBlank(c) && !this.reader.isEOF()) {
			if(!isDigit(c) && !isAlpha(c) && c != '_') {
				this.reader.lastChar();
				break;
			}

			this.tokenValue.append((char)c);
			this.reader.nextChar();

			c = this.reader.getChar();
		}
		
		TokenType tokenType = analyzeWord();
		saveToken(new Token(tokenType, this.tokenValue.toString()));
		
		if(!this.reader.isEOF()) {
			this.reader.nextChar();
		}
	}
	
	private TokenType analyzeWord() {
		switch(this.tokenValue.toString()) {
			case "fn":
				return TokenType.FN_KW;
			case "let":
				return TokenType.LET_KW;
			case "const":
				return TokenType.CONST_KW;
			case "as":
				return TokenType.AS_KW;
			case "while":
				return TokenType.WHILE_KW;
			case "if":
				return TokenType.IF_KW;
			case "else":
				return TokenType.ELSE_KW;
			case "return":
				return TokenType.RETURN_KW;
			case "break":
				return TokenType.BREAK_KW;
			case "continue":
				return TokenType.CONTINUE_KW;
			default:
				return TokenType.IDENT;
		}
	}
	
	private void charState() throws TokenlizerError {
		int c = this.reader.nextChar().getChar();
		
		if(reader.isEOF()) {
			throw new TokenlizerError("Unexcpeted end of a char.");
		}
		
		this.reader.nextChar();
		// '\' [\\"'nrt]
		if(c == '\\') {
			this.tokenValue.append(getEscapeChar());
		}
		else {
			this.tokenValue.append((char)c);
		}
		
		// '
		c = this.reader.getChar();
		
		if(this.reader.isEOF()) {
			throw new TokenlizerError("Unexcpeted end of a char.");
		}
		else if(c != '\'') {
			throw new TokenlizerError("Lack \' at the end of char.");
		}
		
		saveToken(new Token(TokenType.CHAR_LITERAL, this.tokenValue.toString()));
		this.reader.nextChar();
	}
	
	private char getEscapeChar() throws TokenlizerError {
		int c = this.reader.nextChar().getChar();
		
		if(this.reader.isEOF()) {
			throw new TokenlizerError("Unexcpeted end of a char.");
		}
		
		this.reader.nextChar();
		switch(c) {
			case '\\':
				return '\\';
			case '"':
				return '"';
			case '\'':
				return '\'';
			case 'n':
				return '\n';
			case 'r':
				return '\r';
			case 't':
				return '\t';
			default:
				throw new TokenlizerError(String.format("Unknown char \\%c.", c));
		}
	}
	
	private void stringState() throws TokenlizerError {
		int c = this.reader.nextChar().getChar();
		
		while(c != '"') {
			if(this.reader.isEOF() || c == '\n' || c == '\r') {
				throw new TokenlizerError("Unexcepted end of a string.");
			}
			
			if(c == '\\') {
				this.reader.nextChar();
				this.tokenValue.append(getEscapeChar());
			}
			else {
				this.tokenValue.append((char)c);
				this.reader.nextChar();
			}
			
			c = this.reader.getChar();
		}
		
		saveToken(new Token(TokenType.STRING_LITERAL, tokenValue.toString()));
		this.reader.getChar();
	}
	
	private void commentState() {
		while(!this.reader.isEOF() && this.reader.getChar() != '\n') {
			this.reader.nextChar();
		}
		if(!this.reader.isEOF()) {
			this.reader.nextChar();
		}
	}
	
	private void saveToken(Token token) {
		this.tokenList.add(token);
	}
	
	private boolean isDigit(int c) {
		return c >= '0' && c <= '9';
	}
	
	private boolean isAlpha(int c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}
	
	private boolean isBlank(int c) {
		char blank[] = {' ', '\n', '\r', '\t'};
		for(int i=0; i<blank.length; i++) {
			if(c == blank[i]) {
				return true;
			}
		}
		return false;
	}
}
