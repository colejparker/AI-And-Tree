package main;

import java.io.IOException;
import java.util.Random;
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
	
	static Node baseNode;
	static Node currentNode;
	
	static int wpref;
	static int wsecdiff;
	static int wpair;
	static int wminfilled;
	
	public static void main(String[] args)
	{
		Parser parser = new Parser();
		baseNode = new Node(null, new ArrayList<Node>(), new ArrayList<Pair>());
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
		for (Pair p : partialAssignments) {
			slots.get(slots.indexOf(p.getSlot())).addCourse(p.getCourse());;
			courses.remove(p.getCourse());
			baseNode.addToPr(p);
		}
		for (Course c : courses) {
			baseNode.addToPr(new Pair(c, null));
		}
		currentNode = baseNode;
		currentNode = div(currentNode);
	}
	
	private static Node div(Node n) {
		//gets rid of already assigned courses from the list to be chosen.
		Node copyNode = new Node(n);
		for(Pair p : copyNode.getPr()) {
			if(p.getSlot() != null) {
				n.getPr().remove(p);
			}
		}
		
		//select random course to divide up.
		Random rand = new Random();
		int randomNum = rand.nextInt(n.getPr().size());
		
		//create a node for each slot for the randomly chosen pair and make it the child of the current node
		for (Slot s : slots) {
			Node newNode = new Node(n, new ArrayList<Node>(), n.getPr());
			newNode.getPr().get(randomNum).setSlot(s);
			n.addChild(newNode);
		}
		return n;
	}
	
	static Node solutionNode;
	static int lowestPenalty = 0;
	
	private static void TraverseNodes(Node currentNodeN) {
		while(currentNodeN != baseNode) {
			if(currentNodeN.getChildren().size() == 0) {
				div(currentNodeN);
			}
			boolean allFilled = false;
			for(Pair pr : currentNodeN.getPr()) {
				if(pr.getSlot() == null) {
					allFilled = true;
					break; 
				}
			}
			boolean childrenYes = true;
			for(Node n : currentNodeN.getChildren()) {
				if (!n.getSolved()) {
					childrenYes = false;
					break;
				}
			}
			if (childrenYes) {
				currentNodeN.nodeSolved();
			}
			if(allFilled) {
				currentNodeN.nodeSolved();
				if(Constr(currentNodeN)){
					int currentEval = Eval(currentNodeN);
					if(currentEval > lowestPenalty) {
						lowestPenalty = currentEval;
						solutionNode = currentNodeN;
					}
				}
			}
			if(currentNodeN.getSolved()) {
				int nextNodeIndex = currentNodeN.getParent().getChildren().indexOf(currentNodeN) +1;
				if (nextNodeIndex > currentNodeN.getParent().getChildren().size()) {
					while (currentNodeN.getParent().getSolved()) {
						currentNodeN = currentNodeN.getParent();
					}
					
				}
				if(currentNodeN != baseNode) {
					currentNodeN = currentNodeN.getParent().getChildren().get(nextNodeIndex);
				}
			}
		}
	}
	
	
	static boolean cMaxBool;
	static boolean noConflict;
	static boolean notCompatible;
	static boolean notWanted;
	static boolean constrTotal;
	static boolean deptConstrTotal;
	
	//Slots we have already checked for determining courseMax evaluation (True of false)
	static ArrayList<Slot> prevSlots = new ArrayList();
	//Number of courses in a slot is a single entry
	static ArrayList<Integer> nCourses = new ArrayList();
	//Number of labs in a slot is a single entry
	static ArrayList<Integer> nLabs = new ArrayList();
	
	
	//Done normal constraint functions and most of hard constr, need to test them
	private static boolean Constr(Node n) {
		constrTotal = CourseMax(n) && Conflict(n) && Compatible(n) && Wanted(n);
		deptConstrTotal = EveningCourses(n) && DiffSlots(n) && NoTuesday(n) && SpecialCourses(n);
		return constrTotal && deptConstrTotal;
	}
	
	
	
	//Loops through all pairs in a node, then compares them to themselves to see if a course is scheduled
	//in the same slot. It then counts up the courses that are chedules in the same slot and stores the 
	//number of courses in a specific slot in a list, it also stores the slots in a list.
	//At the end it compares the coursemax of a slot with the number of courses scheduled in the slot
	//Returns the boolean 
	private static boolean CourseMax(Node n) {
		int numCourses;
		int numLabs;
		cMaxBool = true;
		for(Pair p : n.getPr()) {
			numCourses = 0;
			numLabs = 0;
			if(prevSlots.contains(p.getSlot())){
				continue;
			}else{
				for(Pair ps : n.getPr()) {
					if(p.getSlot() == ps.getSlot() && p.getCourse() != ps.getCourse() && ps.getCourse().isLab() == true) {
						numLabs++;
					}else if(p.getSlot() == ps.getSlot() && p.getCourse() != ps.getCourse()) {
						numCourses++;	
					}
				}
			}
			nCourses.add(numCourses);
			nLabs.add(numLabs);
			prevSlots.add(p.getSlot());
		}
		for(int i=0; i <= nCourses.size(); i++) {
			if(nCourses.get(i) == prevSlots.get(i).getCourseMax()){
				cMaxBool = cMaxBool && true;
			}else{
				cMaxBool = cMaxBool && false;
			}
		}
		for(int i=0; i <= nLabs.size(); i++) {
			if(nLabs.get(i) == prevSlots.get(i).getLabMax()){
				cMaxBool = cMaxBool && true;
			}else{
				cMaxBool = cMaxBool && false;
			}
		}
		return cMaxBool;
	}
		
	//Look through all the slots we have in the node, then see what courses and labs are in the slot
	//Then we add these labs and slots to lists and compare the first 8 characters to see if they
	//match and therefore we have a conflict in scheduling
	private static boolean Conflict(Node n){
		ArrayList<String> courseName = new ArrayList();
		ArrayList<String> labName = new ArrayList();
		noConflict = true;
		
		for(Slot s : prevSlots){
			for(Pair pr : n.getPr()){
				if(pr.getSlot() == s && pr.getCourse().isLab() != true){
					courseName.add(pr.getCourse().getName());
				}else if(pr.getSlot() == s && pr.getCourse().isLab() == true){
					labName.add(pr.getCourse().getName());
				}
			}
			
			for(int i=0; i <= courseName.size(); i++){
				for(int j=0; j <= labName.size(); j++){
					if(courseName.get(i).regionMatches(0, labName.get(i), 0, 8)){
						noConflict = noConflict && false;
					}
				}
			}
		}
		return noConflict;
	}
	
	//Loop through all incompatible courses and check them against the pairings we have in our node
	//to find any cases of incompatible nodes being put in the same slot. If we find any, the 
	//node will not pass the hard constraints as this will return a false value.
	//All non-compatible lists of courses are in pairs in the array. Can access them using indices
	//Just have to find which slots the unwanted courses are assigned to and compare them
	private static boolean Compatible(Node n){
		notCompatible = true;
		ArrayList<Slot> incompatibleSlotPairs = new ArrayList();  
		
		for(int i=0; i<incompatibleCourses.size(); i++) {
			for(Pair pr : n.getPr()) {
				if(pr.getCourse() == incompatibleCourses.get(i).get(0)) {
					incompatibleSlotPairs.add(pr.getSlot());
				}else if(pr.getCourse() == incompatibleCourses.get(i).get(1)) {
					incompatibleSlotPairs.add(pr.getSlot());
				}
			}
		}
		
		for(int i=0; i<incompatibleSlotPairs.size(); i=i+2) {
			if(incompatibleSlotPairs.get(i) == incompatibleSlotPairs.get(i+1)) {
				notCompatible = false; 
				break;
			}
		}
		return notCompatible;
	}
	
	
	//Checks each pairing in the node to see if they match the unwanted pairings. If they do, the 
	//function will return false and therefore the node will not pass the hard constraints
	private static boolean Wanted(Node n){
		notWanted = true;
		
		for(Pair pr : n.getPr()){
			for(Pair pr2 : unwantedPairings){
				if(pr.getCourse() == pr2.getCourse() && pr.getSlot() == pr2.getSlot()){
					notWanted = notWanted && false;
				}
			}
		}
		return notWanted;
	}
	
	//Likely going to be done at the end of the code when we have the solution. Shouldn't cause any issues.
	/*private static boolean AdditionalSlots(Node n) {
		
	}*/

	
	//Check what lecture number a course has and see if it should be/is in an evening slot. 
	//If it belongs in an evening slot but is not in one, the node will not pass hard constraints
	private static boolean EveningCourses(Node n) {
		boolean eveningCourse = true;
		
		for(Pair pr : n.getPr()) {
			if(pr.getCourse().isLab() == false && pr.getCourse().getName().contains("LEC 9")) {
				if(pr.getSlot().getStartTime().equals("18:00") || pr.getSlot().getStartTime().equals("19:00") ||pr.getSlot().getStartTime().equals("20:00")) {
					continue;
				}else {
					eveningCourse = false;
					break;
				}
			}
		}
		return eveningCourse;
	}
	
	//Check for any 500 level courses scheduled in the same slot
	//If there are any, 
	private static boolean DiffSlots(Node n) {
		boolean diffCourseSlots = true;
		//List of all slots with 500 level courses
		ArrayList<Slot> diffCourses = new ArrayList(); 
		
		for(Pair pr : n.getPr()) {
			if(pr.getCourse().isLab() == false && (pr.getCourse().getName().contains("CPSC 5") || pr.getCourse().getName().contains("SENG 5")) == true) {
				diffCourses.add(pr.getSlot());
			}
		}
		
		for(Slot s : diffCourses) {
			for(Slot ss : diffCourses) {
				if(s == ss) {
					diffCourseSlots = diffCourseSlots && false;
				}
			}
			if(diffCourseSlots == false) {
				break;
			}
		}
		
		return diffCourseSlots;
	}
	
	//Look to see if any course is in the Tuesday 11:00-12:30 slot. 
	//if yes, the node fails the hard constraints
	private static boolean NoTuesday(Node n) {
		boolean noTuesdayClasses = true;
		
		for(Pair pr : n.getPr()) {
			if(pr.getCourse().isLab() != true && pr.getSlot().getStartTime().contains("11:00")) {
				noTuesdayClasses = false;
				break;
			}
		}
		
		return noTuesdayClasses;
	}
	
	//Check to see when 813 and 913 are scheduled in the correct slot if they exist
	//Check if 313 and 813 overlap, then if 413 and 913 overlap.
	//In any overlap case exist, the node fails hard constraints
	private static boolean SpecialCourses(Node n) {
		ArrayList<Integer> classOccupied = new ArrayList();
		boolean noOverlap = true;
		//Check slot times of 813 and 913
		for(Pair pr : n.getPr()) {
			if(pr.getCourse().getName().contains("CPSC 813") && ((pr.getSlot().getStartTime().equals("18:00") == false) || pr.getSlot().getDay().equals("TU") == false)) {
				noOverlap = false;
				break;
			}else if (pr.getCourse().getName().contains("CPSC 913") && ((pr.getSlot().getStartTime().equals("18:00") == false) || pr.getSlot().getDay().equals("TU") == false)) {
				noOverlap = false;
				break;
			}else if(pr.getCourse().getName().contains("CPSC 813") && pr.getSlot().getStartTime().equals("18:00") == true && pr.getSlot().getDay().equals("TU") == true){
				classOccupied.add(813);
			}else if(pr.getCourse().getName().contains("CPSC 913") && pr.getSlot().getStartTime().equals("18:00") == true && pr.getSlot().getDay().equals("TU") == true){
				classOccupied.add(913);
			}
		}
		//See if 813 and 313 overlap or 913 and 413 overlap
		for(Pair pr : n.getPr()){
			if(pr.getCourse().getName().contains("CPSC 313") && pr.getSlot().getStartTime().equals("18:00") && pr.getSlot().getDay().equals("TU")){
				if(classOccupied.contains(813)){
					noOverlap = false;
					break;
				}
			}else if(pr.getCourse().getName().contains("CPSC 413") && pr.getSlot().getStartTime().equals("18:00") && pr.getSlot().getDay().equals("TU")){
				if(classOccupied.contains(913)){
					noOverlap = false;
					break;
				}
			}
		}
		
		return noOverlap;
	}
	
	
	
}
