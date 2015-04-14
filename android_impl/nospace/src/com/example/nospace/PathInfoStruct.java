package com.example.nospace;

import java.util.ArrayList;

public class PathInfoStruct implements Comparable<PathInfoStruct>
{    	
	private static ArrayList<PathInfoStruct> freeQueue = new ArrayList<PathInfoStruct>();
	
	BetterString word;
	int startPos;
	int endPos;
	double currentProb;
	PathInfoStruct backPointer;
	int inUse;
	
	public PathInfoStruct()
	{
		inUse = 0;
	}
	
	public static PathInfoStruct allocatePathInfoStruct()
	{
		if (freeQueue.size() == 0) {
			return new PathInfoStruct();
		} else {
			return freeQueue.remove(freeQueue.size() - 1);
		}
	}
	
	public static void deallocatePathInfoStruct(PathInfoStruct p)
	{
		p.inUse = 0;
		freeQueue.add(p);
	}

	public int compareTo(PathInfoStruct p)
	{
		if (endPos < p.endPos) {
			return -1;
		} else if (endPos > p.endPos) {
			return 1;
		} else {
			return word.toString().compareTo(p.word.toString());
		}
	}
}
