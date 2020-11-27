package Reader;

public class Reader {
	private static Reader reader;
	private int location;
	private String string;
	
	private Reader() {
		this.location = 0;
		this.string = new String();
	}
	
	public static Reader getReader() {
		if(reader == null) {
			reader = new Reader();
		}
		return reader;
	}
	
	public void backToHead() {
		this.location = 0;
	}
	
	public boolean isEOF() {
		return this.location >= this.string.length();
	}
	
	public Reader lastChar() {
		if(this.location > 0) {
			this.location--;
		}
		
		return this;
	}
	
	public Reader nextChar() {
		if(this.location < this.string.length()) {
			this.location++;
		}
		
		return this;
	}
	
	public int getChar() {
		if(this.location >= this.string.length()) {
			return -1;
		}
		else {
			return this.string.charAt(this.location);
		}
	}
}
