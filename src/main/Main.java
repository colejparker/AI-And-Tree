package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
	static ArrayList<Slot> slots = new ArrayList();	//idNumbers start at 1
	
	static ArrayList<Course> courses = new ArrayList(); //idNumbers start at 1
	static int labStart;
	
	static ArrayList<ArrayList<Course>> incompatibleCourses = new ArrayList();
	static ArrayList<Pair> unwantedPairings = new ArrayList();
	static ArrayList<ArrayList<Object>> preferredPairings = new ArrayList();
	static ArrayList<ArrayList<Course>> pairedCourses = new ArrayList();
	static ArrayList<Pair> partialAssignments = new ArrayList();
	
	static int wpref;
	static int wsecdiff;
	static int wpair;
	static int wminfilled;
	
	public static void main(String[] args)
	{
		Parser parser = new Parser();
		String inputText = "";
		String[] seperatedText;
		String filename = "input.txt";
		switch(args.length) {
		case 0:
			wminfilled = wpref = wpair = wsecdiff = 25;
			break;
		case 1:
			wminfilled = Integer.parseInt(args[0]);
			wpref = 25;
			wpair = 25;
			wsecdiff = 25;
			break;
		case 2:
			wminfilled = Integer.parseInt(args[0]);
			wpref = Integer.parseInt(args[1]);
			wpair = 25;
			wsecdiff = 25;
			break;
		case 3:
			wminfilled = Integer.parseInt(args[0]);
			wpref = Integer.parseInt(args[1]);
			wpair = Integer.parseInt(args[2]);
			wsecdiff = 25;
			break;
		case 4:
			wminfilled = Integer.parseInt(args[0]);
			wpref = Integer.parseInt(args[1]);
			wpair = Integer.parseInt(args[2]);
			wsecdiff = Integer.parseInt(args[3]);
			break;
		}
		inputText = parser.readFileAsString(filename);
		seperatedText = inputText.split("\n");
		slots = parser.populateSlots();
		slots = parser.getSlots(slots, seperatedText);
		courses = parser.populateCourses(seperatedText);
		incompatibleCourses = parser.getIncompatibleCourses(courses, seperatedText);
		unwantedPairings = parser.getUnwantedPairings(courses, slots, seperatedText);
		preferredPairings = parser.getPreferredPairings(courses, slots, seperatedText);
		pairedCourses = parser.getPairedCourses(courses, seperatedText);
		partialAssignments = parser.getPartialAssignments(courses, slots, seperatedText);
	}
	
	
	
	
}
