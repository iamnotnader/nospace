import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class Stripper
{
	private enum State {
		inWord, inWhiteSpace, beginningOfSentence
	}
	
	public static void main(String[] args) throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader("golden_text"));
		FileWriter out = new FileWriter("stripped_golden");
		
		int x;
		char c;
		State s = State.beginningOfSentence;
		while ((x = in.read()) != -1) {
			c = (char)x;
			//System.out.println("STATE: " + s + " " + c);
			switch (s) {
			case inWord: {
				if (c == '.' || c == '!' || c == '?') {
					out.write('\n');
					s = State.beginningOfSentence;
				} else if (c == '\t' || c == ' ' || c == '\n') {
					s = State.inWhiteSpace;
				} else if (Character.isLetter(c) || c == '\'') {
					out.write(Character.toLowerCase(c));
				}
				break;
			} case inWhiteSpace: {
				if (c == '.' || c == '!' || c == '?') {
					out.write('\n');
					s = State.beginningOfSentence;
				} else if (c == '\t' || c == ' ' || c == '\n') {
				} else if (Character.isLetter(c) || c == '\'') {
					out.write(' ');
					out.write(Character.toLowerCase(c));
					s = State.inWord;
				}
				break;
			} case beginningOfSentence: {
				if (c == '.' || c == '!' || c == '?') {
				} else if (c == '\t' || c == ' ' || c == '\n') {
				} else if (Character.isLetter(c) || c == '\'') {
					out.write(Character.toLowerCase(c));
					s = State.inWord;
				}
				break;
			}
			
			}
		}
		out.close();
		in.close();
		
		in = new BufferedReader(new FileReader("stripped_golden"));
		out = new FileWriter("stripped_golden_noapost");
		
		String line;
		while ((line = in.readLine()) != null) {
			if (line.contains("\'")) {
				continue;
			}
			out.write(line);
			out.write('\n');
		}
		out.close();
		in.close();
		
		in = new BufferedReader(new FileReader("stripped_golden_noapost"));
		out = new FileWriter("unspaced_text_noapost");
		
		while ((x = in.read()) != -1) {
			c = (char)x;
			if (c != ' ') {
				out.write(c);
			}
		}
	}
	
}
