import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Checker
{
	public static void printStats(BufferedReader in_golden, BufferedReader in_opt) throws IOException
	{
		String line_opt;
		String line_golden;

		double totalLines = 0;
		double totalSpaces = 0;

		double noError = 0;
		double missingSpace = 0;
		double extraSpace = 0;
		double wrongChar = 0;
		double total = 0;

		while ((line_opt = in_opt.readLine()) != null) {
			line_golden = in_golden.readLine();

			int pos_golden = 0;
			int pos_opt = 0;

			while (pos_golden < line_golden.length() && pos_opt < line_opt.length()) {
				if (line_golden.charAt(pos_golden) == line_opt.charAt(pos_opt)) {
					// No error.
					noError++;
					pos_golden++;
					pos_opt++;
				} else if (line_golden.charAt(pos_golden) == ' ') {
					// Missing space.
					missingSpace++;
					pos_golden++;

				} else if (line_opt.charAt(pos_opt) == ' ') {
					// Extra space.
					extraSpace++;
					pos_opt++;
				} else {
					// Wrong character.
					wrongChar++;
					pos_opt++;
					pos_golden++;
				}
			}
		
			total += line_golden.length();
			int num_golden_spaces = line_golden.split(" ").length - 1;
			int num_opt_spaces = line_opt.split(" ").length - 1;

			totalLines++;
			totalSpaces += num_golden_spaces;
			
		}
		System.out.println("Total Lines: " + totalLines);
		System.out.println("Total Spaces: " + totalSpaces);
		System.out.println("No Error: " + noError);
		System.out.println("Missing Space: " + missingSpace);
		System.out.println("Extra Space: " + extraSpace);
		System.out.println("Wrong Character: " + wrongChar);
		System.out.println("TOTAL Characters: " + total);
	}
	
	public static void main(String[] args) throws IOException
	{
		BufferedReader in_golden = new BufferedReader(new FileReader("stripped_golden_noapost"));

		System.out.println("Optimal no errors.");
		printStats(in_golden, new BufferedReader(new FileReader("opt_seg_noapose")));
		
		System.out.println();
		System.out.println("Suboptimal no errors");
		printStats(new BufferedReader(new FileReader("stripped_golden_noapost")), new BufferedReader(new FileReader("greedy_bigram_noapost")));
		
		System.out.println();
		System.out.println("Optimal WITH errors");
		printStats(new BufferedReader(new FileReader("stripped_golden_noapost")), new BufferedReader(new FileReader("src/opt_seg_noapost_noisy")));
		
		System.out.println();
		System.out.println("Suboptimal WITH errors");
		printStats(new BufferedReader(new FileReader("stripped_golden_noapost")), new BufferedReader(new FileReader("src/greedy_bigram_noapost_noisy")));

	}
}
