package com.example.nospace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

public class BetterString implements Serializable {
	private static ArrayList<BetterString> freeQueue = new ArrayList<BetterString>();

	public int length;
	public char[] myChars;
	
	// So that old String code doesn't have to change.
	public int length()
	{
		return length;
	}
	
	// So that old String code doesn't have to change.
	public char charAt(int index)
	{
		return myChars[index];
	}
	
	public BetterString()
	{
		length = 0;
		myChars = new char[5];
	}
	
	public BetterString(String s)
	{
		myChars = new char[s.length()];
		length = s.length();
		for (int i = 0; i < s.length(); i++) {
			myChars[i] = s.charAt(i);
		}
	}
	
	public void append(String s)
	{
		if (myChars.length < length + s.length()) {
			char[] temp = new char[(length + s.length()) * 2];
			for (int i = 0; i < length; i++) {
				temp[i] = myChars[i];
			}
			myChars = temp;
		}
		for (int i = 0; i < s.length(); i++) {
			myChars[length] = s.charAt(i);
			length++;
		}
	}
	
	public void append(BetterString s)
	{
		if (myChars.length < length + s.length) {
			char[] temp = new char[(length + s.length) * 2];
			for (int i = 0; i < length; i++) {
				temp[i] = myChars[i];
			}
			myChars = temp;
		}
		for (int i = 0; i < s.length; i++) {
			myChars[length] = s.myChars[i];
			length++;
		}
	}
	
	public void append(char c)
	{
		if (myChars.length == length) {
			char[] temp = new char[length * 2];
			for (int i = 0; i < length; i++) {
				temp[i] = myChars[i];
			}
			myChars = temp;
		}
		myChars[length] = c;
		length++;
	}
	
	public void set(int index, char c)
	{
		if (myChars.length <= index) {
			char[] temp = new char[index * 2];
			for (int i = 0; i < length; i++) {
				temp[i] = myChars[i];
			}
			myChars = temp;
		}
		myChars[index] = c;
		length = Math.max(index + 1, length);
	}
	
	public void clear()
	{
		length = 0;
	}
	
	@Override
	public String toString()
	{
		return (new String(myChars)).substring(0, length);
	}
	
	public static BetterString allocateString()
	{
		if (freeQueue.size() == 0) {
			return new BetterString();
		} else {
			return freeQueue.remove(freeQueue.size() - 1);
		}
	}
	
	// Note this doesn't free up the memory used by s.myChars.
	public static void deallocateString(BetterString s)
	{
		s.length = 0;
		freeQueue.add(s);
	}
	
	public static void deallocateStringHard(BetterString s)
	{
		s.length = 0;
		s.myChars = new char[5];
		freeQueue.add(s);
	}
	
	@Override
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof BetterString))return false;
	    BetterString otherStr = (BetterString)other;
	    
	    if (otherStr.length == length) {
	    	for (int i = 0; i < length; i++) {
	    		if (otherStr.myChars[i] != myChars[i]) {
	    			return false;
	    		}
	    	}
	    	return true;
	    } else {
	    	return false;
	    }
	}

	private int fastPow(int a, int b)
	{
	    int result = 1;
	    while(b > 0) {
	        if (b % 2 != 0) {
	            result *= a;
	            b--;
	        } 
	        a *= a;
	        b /= 2;
	    }
	
	    return result;
	}

	// cachedPows[i] = (int)(31^i)
	// Note: It's not really clear if this is faster than using fastPow directly since
	// accessing memory is so slow but it's slightly faster on my machine so I'm leaving
	// it here.
	private	static int[] cachedPows = {1, 31, 961, 29791, 923521, 28629151, 887503681, 1742810335, -1807454463, -196513505, -1796951359, 129082719, -293403007, -505558625, 1507551809, -510534177, 1353309697, -997072353, -844471871, -408824225, 211350913, -2038056289, 1244764481, -67006753, -2077209343, 31019807, 961614017, -254736545, 693101697, 11316127, 350799937, -2010103841, 2111290369, 1025491999, 1725480897, 1950300255, 329765761, 1632803999, -922683583, 1461579999, -1935660287, 124073247, -448696639, -1024693921, -1700740479, -1183347297, 1970939457, 969581023, -7759359, -240540129, 1133190593, 769170015, -1925533311, 438009503, 693392705, 20337375, 630458625, -1930619105, 280349889, 100911967, -1166696319, -1807847521, -208698303, 2120287199, 1304393729, 1781499935, -608076863, -1670513569, -246313087, 954228895, -483675327, -2109033249, -955521279, 443611423, 867052225, 1108815199, 13532801, 419516831, 120119873, -571251233, -528919039, 783378975, -1485055551, 1207918175, -1209242239, 1168196255, 1854345537, 1650136799, -385366783, 938531615, -970291007, -14250145, -441754495, -809487457, 675692609, -528365601, 800535553, -953201633, 515520449, -1198735265, 1493912449};
	@Override
	public int hashCode()
	{
		int ret = 0;
		if (length < cachedPows.length) {
			for (int i = 0; i < length; i++) {
				ret += myChars[i] * cachedPows[length - i - 1];
			}
		} else {
			for (int i = 0; i < length; i++) {
				ret += myChars[i] * fastPow(31, (length - i - 1));
			}
		}
		return ret;
	}
	
	public static void main(String[] args)
	{
		System.out.println("Started...");
		BetterString b1 = new BetterString("hello");
		BetterString b2 = new BetterString();
		System.out.println("b1 (hello): " + b1);
		System.out.println("b2 (): " + b2);
		
		b1.append("gurl");
		b2.append("OMG");
		
		System.out.println("b1 (hellogurl): " + b1);
		System.out.println("b2 (OMG): " + b2);
		
		b1.set(5, 'w');
		b2.set(0, 'S');
		
		System.out.println("b1 (hellowurl): " + b1);
		System.out.println("b2 (SMG): " + b2);
		
		b1.append("aaaaaaaaaaaaaaaa");
		b2.set(b2.length, 'a');
		
		System.out.println("b1 (hellowurlaaaaaaaaaaaaaaaa): " + b1);
		System.out.println("b2 (SMGa): " + b2);
		
		b1.clear();
		b1.append("a");
		b1.clear();
		b1.set(5, 'a');
		b2.clear();
		b2.append('a');
		
		System.out.println("b1 (a????a): " + b1);
		System.out.println("b2 (a): " + b2);
		
		BetterString b3 = new BetterString("hello");
		System.out.println("b3 length (5): " + b3.length);
		System.out.println("b3 capacity (?): " + b3.myChars.length);
		System.out.println("b3 (hello): " + b3);
		
		b3.append('w');
		System.out.println("b3 length (6): " + b3.length);
		System.out.println("b3 capacity (?): " + b3.myChars.length);
		System.out.println("b3 (hellow): " + b3);
		
		for (int i = 0; i < 100; i++) {
			System.out.print(b1.fastPow(31, i) + ", ");
		}
		System.out.println(b1.fastPow(31, 100));
		
		int N = 1000000;
		HashSet<BetterString> set = new HashSet<BetterString>();
		for (int i = 0; i < N; i++) {
			BetterString s = new BetterString("" + Math.random());
			set.add(s);
			if (!set.contains(s)) {
				System.out.println("WTF");
			}
		}
	}
}

