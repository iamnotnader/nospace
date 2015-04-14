import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class Fix1Grams {
	public static void main(String[] args) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\daddy\\Documents\\cos497_project\\1grams_linux\\vocab_cs"));
		FileWriter out = new FileWriter("C:\\Users\\daddy\\Documents\\cos497_project\\1grams_linux\\vocab_cs_lower");
		
		String line;
		while ((line = br.readLine()) != null) {
			out.write(line.toLowerCase());
			out.write('\n');
		}
		out.flush();
		out.close();
		br.close();
	}
	
}
