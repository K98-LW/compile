package Reader;

import java.io.*;

public class Reader {
	private static Reader reader;
	private int location;
	private String string;
	
	private Reader(String path) throws IOException {
		this.location = 0;

		StringBuilder stringBuilder = new StringBuilder();
		System.out.println("Reader path: " + path);
		File file = new File(path);
//		System.out.println(file.exists());
		FileInputStream fileInputStream = new FileInputStream(file);
		BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
		while(bufferedInputStream.available() > 0){
			stringBuilder.append((char)bufferedInputStream.read());
		}
		bufferedInputStream.close();
		fileInputStream.close();

		// output
		file = new File(path);
		fileInputStream = new FileInputStream(file);
		bufferedInputStream = new BufferedInputStream(fileInputStream);
		while(bufferedInputStream.available() > 0){
			System.out.print((char)bufferedInputStream.read());
		}
		bufferedInputStream.close();
		fileInputStream.close();

		this.string = stringBuilder.toString();
	}
	
	public static Reader getInstance(String path) throws IOException {
		if(reader == null) {
			try {
				reader = new Reader(path);
			} catch (IOException e){
				System.out.println("File not found.");
			} catch(Exception e){
				System.out.println("Other error.");
			}
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
