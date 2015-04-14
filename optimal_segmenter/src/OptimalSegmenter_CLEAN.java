import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OptimalSegmenter {
	private static final double minProb = -1.0e100;
	private static final double errorProb = Math.log(.05);

	private static class Node
	{
		String str;
		public double updatedGVal;
		public Node previous;

		public Node(String s, double g, Node prev)
		{
			str = s;
			updatedGVal = g;
			previous = prev;
		}
	}

	// Read lines from a file containing unigrams. The lines should be formatted
	// exactly as follows: "w1\tcount\n" where w1 is a string and count is a double.
	// filename is the location of the file and numUnigramsToRead is the number of
	// lines to read from this file.
	public static HashMap<String, Double> readUnigrams(
			String filename,
			int numUnigramsToRead) throws IOException
			{
		BufferedReader unigramReader = new BufferedReader(new FileReader(filename));
		HashMap<String, Double> unigrams = new HashMap<String, Double>();

		int numLinesReadIn = 0;
		String line;
		double totalCount = 0; // Used to convert frequency counts to probabilities at the end.

		// Read lines from the file into our map data structure.
		while ((line = unigramReader.readLine()) != null
				&&  numLinesReadIn < numUnigramsToRead) {
			String[] stringValues = line.split("\t");

			if (stringValues.length != 2) {
				unigramReader.close();
				throw new IOException("Unigram input file should contain"
						+ "lines of the form: '%s\t%f\n'");
			}

			String w1 = new String(stringValues[0]);
			double count = Double.parseDouble(stringValues[1]);
			totalCount += count;

			if (unigrams.containsKey(w1)) {
				unigrams.put(w1, unigrams.get(w1) + count);
			} else {
				unigrams.put(w1, count);
			}

			numLinesReadIn++;
		}
		unigramReader.close();

		// Convert the frequency counts into normalized log probabilities.
		for (Map.Entry<String, Double> entry : unigrams.entrySet()) {
			unigrams.put(entry.getKey(), Math.log(entry.getValue()) - Math.log(totalCount));
		}

		return unigrams;
			}

	// Read lines from a file containing bigrams. The lines should be formatted
	// exactly as follows: "w1\tw2\tcount\n" where w1 is a string, w2 is a string and
	// count is a double. filename is the location of the file and numBigramsToRead is
	// the number of lines to read from this file.
	public static HashMap<String, HashMap<String, Double>> readBigrams(
			String filename,
			int numBigramsToRead) throws IOException
			{
		BufferedReader bigramReader = new BufferedReader(new FileReader(filename));
		HashMap<String, HashMap<String, Double>> bigrams =
				new HashMap<String, HashMap<String, Double>>();

		int numLinesReadIn = 0;
		String line;

		// Read lines from the file into our map data structure.
		while ((line = bigramReader.readLine()) != null
				&&  numLinesReadIn < numBigramsToRead) {
			String[] stringValues = line.split("\t");

			if (stringValues.length != 3) {
				bigramReader.close();
				throw new IOException("Bigram input file should contain"
						+ "lines of the form: '%s\t%s\t%f\n'");
			}

			String w1 = new String(stringValues[0]);
			String w2 = new String(stringValues[1]);
			double count = Double.parseDouble(stringValues[2]);

			HashMap<String, Double> distribution = bigrams.get(w1);
			if (distribution != null) {
				if (distribution.containsKey(w2)) {
					distribution.put(w2, distribution.get(w2) + count);
				} else {
					distribution.put(w2, count);
				}
			} else {
				distribution = new HashMap<String, Double>();
				distribution.put(w2, count);
				bigrams.put(w1, distribution);
			}

			numLinesReadIn++;
		}
		bigramReader.close();

		// Convert the frequency counts into normalized log probabilities.
		for (Map.Entry<String, HashMap<String, Double>> entry : bigrams.entrySet()) {
			double totalCount = 0;
			HashMap<String, Double> value = entry.getValue();
			for (Map.Entry<String, Double> subEntry : value.entrySet()) {
				totalCount += subEntry.getValue();
			}

			for (Map.Entry<String, Double> subEntry : value.entrySet()) {
				value.put(subEntry.getKey(), Math.log(subEntry.getValue())
						  - Math.log(totalCount));
			}
		}

		return bigrams;
			}

	// This is a scoring function based on a first-order Makov model.
	// If it can't find a bigram probability, it returns the unigram probability
	// instead. If it can't find either a bigram probability or a unigram
	// probability, it returns a smoothed value minProb.
	public static double g(
			String w1,
			String w2,
			double oldG,
			HashMap<String, Double> unigrams,
			HashMap<String, HashMap<String, Double>> bigrams)
	{	
		HashMap<String, Double> temp = bigrams.get(w1);
		if (w1.length() != 0 && temp != null) {
			Double val = temp.get(w2);
			if (val != null) {
				return oldG + val.doubleValue();
			} else {
				val = unigrams.get(w2);
				if (val != null) {
					return oldG + val.doubleValue();
				} else {
					return oldG + minProb;
				}
			}
		} else {
			Double val = unigrams.get(w2);
			if (val != null) {
				return oldG + val.doubleValue();
			} else {
				return oldG + minProb;
			}
		}
	}

	// Returns best_prev_list || s as described in the paper.
	public static Node argmaxNode(
			ArrayList<Node> previousNodes,
			String s,
			HashMap<String, Double> unigrams,
			HashMap<String, HashMap<String, Double>> bigrams)
	{
		double maxG = Double.NEGATIVE_INFINITY;
		int bestXIndex = -1;
		for (int i = 0; i < previousNodes.size(); i++) {
			Node x = previousNodes.get(i);
			double gXS = g(x.str, s, x.updatedGVal, unigrams, bigrams);
			if (gXS > maxG) {
				maxG = gXS;
				bestXIndex = i;
			}
		}
		Node bestPrevNode = previousNodes.get(bestXIndex);

		return new Node(s, maxG, bestPrevNode);
	}

	// Returns argmax(frontier[|w|]) as described in the paper.
	public static Node argmaxEnd(
			ArrayList<Node> nodes,
			HashMap<String, Double> unigrams,
			HashMap<String, HashMap<String, Double>> bigrams) {
		double maxG = Double.NEGATIVE_INFINITY;
		int bestNodeIndex = -1;
		for (int i = 0; i < nodes.size(); i++) {
			double currentG = g(nodes.get(i).str, "</s>",
								nodes.get(i).updatedGVal, unigrams, bigrams);
			if (currentG > maxG) {
				maxG = currentG;
				bestNodeIndex = i;
			}
		}

		return nodes.get(bestNodeIndex);
	}

	public static ArrayList<String> extractStringArray(Node n)
	{
		ArrayList<String> ret = new ArrayList<String>();

		while(n != null) {
			ret.add(n.str.toString());
			n = n.previous;
		}

		Collections.reverse(ret);
		return ret;
	}

	// A pair class. We use this so that perturbations can have different
	// effects on the scoring function.
	private static class Pair<X, Y>
	{
		public X first;
		public Y second;

		public Pair(X x, Y y)
		{
			first = x;
			second = y;
		}
	}

	// This function returns all perturbations of the input string. Right now
	// we consider single and two-character perturbations.
	public static String[] adjacencies = {"qwsxz", "vfghn", "xsdfv", "swerfvcx",
										  "wsdfr", "cdertgbv", "vfrtyhnb", "bgtyujmn",
										  "ujklo", "yhnmkiu", "jmloiu", "kiop", "nhjk",
										  "bghjm", "iklp", "ol", "asw", "edfgt",
										  "zaqwedcx", "rfghy", "yhjki", "cdfgb",
										  "qasde", "zasdc", "tghju", "xsa"};
	static char[] tempString = new char[100];
	public static ArrayList<Pair<String, Double>> perturbations(String s)
	{
		ArrayList<Pair<String, Double>> ret = new ArrayList<Pair<String, Double>>();
		s.getChars(0, s.length(), tempString, 0);

		// Single character perturbations.
		for (int i = 0; i < s.length(); i++) {
			char currentChar = s.charAt(i);
			if (!Character.isLetter(currentChar)) {
				continue;
			}

			int charIndex = Character.toLowerCase(currentChar) - 'a';
			for (int j = 0; j < adjacencies[charIndex].length(); j++) {
				if (Character.isLowerCase(currentChar)) {
					//s.getChars(0, s.length(), tempString, 0);
					tempString[i] = adjacencies[charIndex].charAt(j);
				} else {
					//s.getChars(0, s.length(), tempString, 0);
					tempString[i] =
						Character.toUpperCase(adjacencies[charIndex].charAt(j));
				}

				Pair<String, Double> perturbation = 
						new Pair<String, Double>(new String(tempString, 0,
															s.length()), errorProb);
				ret.add(perturbation);

				tempString[i] = s.charAt(i);
			}
		}

		// Two-character perturbations.
		for (int i = 0; i < s.length(); i++) {
			for (int j = i + 1; j < s.length(); j++) {
				char c1 = s.charAt(i);
				char c2 = s.charAt(j);

				if (!Character.isLetter(c1) || !Character.isLetter(c2)) {
					continue;
				}

				int index1 = Character.toLowerCase(c1) - 'a';
				int index2 = Character.toLowerCase(c2) - 'a';

				for (int k1 = 0; k1 < adjacencies[index1].length(); k1++) {
					for (int k2 = 0; k2 < adjacencies[index2].length(); k2++) {
						//s.getChars(0, s.length(), tempString, 0);

						if (Character.isLowerCase(c1)) {
							tempString[i] = adjacencies[index1].charAt(k1);
						} else {
							tempString[i] =
								Character.toUpperCase(adjacencies[index1].charAt(k1));
						}

						if (Character.isLetter(c2)) {
							tempString[j] = adjacencies[index2].charAt(k2);
						} else {
							tempString[j] =
								Character.toUpperCase(adjacencies[index2].charAt(k2));
						}

						Pair<String, Double> perturbation =
								new Pair<String, Double>(new String(tempString, 0,
																	s.length()), 2*errorProb);
						ret.add(perturbation);

						tempString[i] = s.charAt(i);
						tempString[j] = s.charAt(j);
					}
				}
			}
		}

		return ret;
	}


	// This is the heart of our class. This algorithm takes an input string and segments it
	// optimally. Everything in this function has been organized to resemble the algorithm
	// presented in the paper as closely as possible.
	public static ArrayList<String> segmentOptimal(
			String input,
			int maxLength,
			HashMap<String, Double> unigrams,
			HashMap<String, HashMap<String, Double>> bigrams)
			{
		ArrayList<ArrayList<Node>> frontier = new ArrayList<ArrayList<Node>>();
		frontier.add(new ArrayList<Node>());
		frontier.get(0).add(new Node(new String("<s>"), 0, null));

		for (int end = 1; end <= input.length(); end++) {
			frontier.add(new ArrayList<Node>());

			for (int start = Math.max(0, end - maxLength); start < end; start++) {
				String s = input.substring(start, end);
				Node bestPrevList = argmaxNode(frontier.get(start), s, unigrams, bigrams);
				frontier.get(end).add(bestPrevList);

				for (Pair<String, Double> sPrime : perturbations(s)) {
					if (!unigrams.containsKey(sPrime.first)) {
						continue;
					}

					bestPrevList = argmaxNode(frontier.get(start), sPrime.first, unigrams, bigrams);
					bestPrevList.updatedGVal += sPrime.second;

					frontier.get(end).add(bestPrevList);
				}
			}

			if (end - maxLength >= 0) {
				frontier.get(end - maxLength).clear();
			}
		}

		Node optimalNode = argmaxEnd(frontier.get(input.length()), unigrams, bigrams);

		return extractStringArray(optimalNode);
			}

	// An implementation of the "Greedy Bigram" segmentation algorithm.
	public static ArrayList<String> segmentGreedyBigram(
			String input,
			int maxLength,
			HashMap<String, Double> unigrams,
			HashMap<String, HashMap<String, Double>> bigrams)
			{
		ArrayList<String> ret = new ArrayList<String>();
		ret.add("<s>");

		int pos = 0;
		while (pos < input.length()) {
			double maxG = Double.NEGATIVE_INFINITY;
			int bestPos = -1;
			for (int i = pos + 1; i <= input.length(); i++) {
				double currentG = g(ret.get(ret.size() - 1),
									input.substring(pos, i), 0,
									unigrams, bigrams)/(i - pos);
				if (currentG > maxG) {
					maxG = currentG;
					bestPos = i;
				}
			}
			ret.add(input.substring(pos, bestPos));
			pos = bestPos;
		}

		return ret;
			}

	// Perturbs each character in a string to an adjacent character on an American keyboard
	// independently with probability .05.
	public static String addNoise(String line)
	{
		char[] temp = line.toCharArray();
		for (int i = 0; i < temp.length; i++) {
			if (Math.random() < .05 && Character.isLetter(temp[i])) {
				temp[i] = adjacencies[Character.toLowerCase(temp[i]) - 'a'].charAt((int)(Math.random()
						* adjacencies[Character.toLowerCase(temp[i]) - 'a'].length()));
			}
		}

		return new String(temp);
	}

	public static void main(String[] args) throws IOException
	{		
		// The files from which to get the unigram and bigram language model are specified
		// as arguments.
		String unigramFile = args[0];
		String bigramFile = args[1];

		// This corresponds to the length restriction (3.1) mentioned in the paper.
		int maxLength = Integer.parseInt(args[2]);

		// Reads in the language model from a text file.
		HashMap<String, Double> unigrams = readUnigrams(unigramFile, 500000);
		HashMap<String, HashMap<String, Double>> bigrams = readBigrams(bigramFile, 5000000);

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Enter an unsegmented string.");
		int numLinesRead = 0;

		String line;
		while ((line = br.readLine()) != null) {
			numLinesRead++;

			ArrayList<String> optSeg = segmentOptimal(line.toLowerCase(), maxLength, unigrams, bigrams);
			System.out.print(optSeg.get(1));
			for (int i = 2; i < optSeg.size(); i++) {
				if (optSeg.get(i).contains("'")) {
					System.out.print(optSeg.get(i));
				} else {
					System.out.print(" " + optSeg.get(i));	
				}
			}
			System.out.println();
		}
	}
}
