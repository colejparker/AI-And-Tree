
package main;

import java.util.*;

public class Eval extends Slot {

	Slot slot;
	ArrayList<Eval> eval = new ArrayList<Eval>();
	/*
	protected int pen_coursemin;
	protected int pen_labsmin;
	protected int pen_notpaired;
	protected int pen_section;
	*/
	protected int value;
	public int constant = 25;
	
	public Eval(){
		
	}
	/*
	public Eval(int pen_coursemin, int pen_labsmin, int pen_notpaired, int pen_section ){
		this.pen_coursemin = pen_coursemin;
		this.pen_labsmin = pen_labsmin;
		this.pen_notpaired = pen_notpaired;
		this.pen_section = pen_section;
	}
	*/
	

	public int totalEval(Node n, Course c, Slot s) {
		value = (getEvalMinFilled(n,c) * Main.wminfilled) + (getEvalPref(n) * Main.wpref) + (getEvalPair(n,c) * Main.wpair) + (getEvalSecDiff(n, c ,s) * Main.wsecdiff);
		return value;
	}
	/*
	 * For each slot s minimal numbers coursemin(s) and labmin(s) that indicate how many courses and labs goes into the slot s
	 * Accept input penalty values pen_coursemin and pen_labsmin
	 * For each course below coursemin we will get pen_coursemin and same for labsmin added to the Eval value of an assignment
	 * 
	 * */
	public int getEvalMinFilled(Node node, Course course){
		for(Pair p : node.getPr()) {
			int currentCourseMin = p.getSlot().getCourseMin(); 
			int currentLabMin = p.getSlot().getLabMin();
			int labCounter = 0;
			int courseCounter = 0;
			for (Course c : p.getSlot().getCourses()) {
				if (c.isLab() == true){					
					labCounter++;
				}
				if (c.isLab() == false){
					courseCounter++;
				}
			}
			if (labCounter < currentLabMin){
				value--;
			}
			
			if (courseCounter < currentCourseMin){
				value--;
			}
		}
		return value;
	}
	
	/*
	 * Certain preferences regarding which time slots the courses and labs are scheduled
	 * Preferred pairings, rank the (course/lab,slot) pairing 
	 * */
	public int getEvalPref(Node node){
		for (Pair p : Main.unwantedPairings) {
			if (node.getPr().contains(p)) {
				value-=constant;
			}
			for (ArrayList<Object> o : Main.preferredPairings) {
				Integer i = 0;
				Slot s = new Slot();
				Course c = new Course();
				for (Object m : o) {
					if (m instanceof Integer){
							 i = (Integer) m;
					}
					else if(m instanceof Slot){
							s = (Slot) m;
					}
					else{
							c = (Course) m;
					}
				}
					Pair p1 = new Pair(c,s);
					if (node.getPr().contains(p1)) {
						value += i;
					}
			}
		}
		return value;
	}
	
	/*
	 * courses/labs scheduled at the same time
	 * there will be a list of pair(a,b) with a,b in Courses + Labs 
	 * a parameter pen_notpaired such that for every pair(a,b) statement for which assign(a) is not equal to
	 * assign(b) you have to add pen_notpaired
	 * */
	public int getEvalPair(Node node, Course course){	
		for (ArrayList<Course> courses: Main.pairedCourses) {
			Course course1 = courses.get(0);
			Course foundCourse1 = null;
			Course course2 = courses.get(1);
			Course foundCourse2 = null;
			for (Pair p : node.getPr()) {
				if (p.getCourse().getName() == course1.getName()) {
					foundCourse1 = p.getCourse();
				}
				if(p.getCourse().getName() == course2.getName()){
					foundCourse2 = p.getCourse();
				}
			}
			if ((foundCourse1 != null) && (foundCourse2 != null)){
				  if (foundCourse1.getSlot() == foundCourse2.getSlot()){
				  value += constant;
				  }
			}
		}
		return value;

	}
	

	/*
	 * Different sections of a course should be scheduled at different times 
	 * For each section that is scheduled into the same slot then we add a penalty pen_section to the Eval value
	 * slot and course
	 * */
	public int getEvalSecDiff(Node node, Course course, Slot slot){
		for(Course c : course.getSlot().getCourses()){
		 	String courseName = c.getName().replaceAll(" " , "").substring(0,6);
		 	String courseSec = c.getName().replaceAll(" ", "").substring(7, 11);
		 	Slot s = c.getSlot();
		 	for (Course c2 : course.getSlot().getCourses()){
		 		String courseName2 = c2.getName().replaceAll(" " , "").substring(0,6);
		 		String courseSec2 = c2.getName().replaceAll(" ", "").substring(7, 11);
		 		Slot s1 = c2.getSlot();
		 		if ((s == s1) && (courseName == courseName2) && (courseSec != courseSec2)){
		 			value--;
		 		}
		 	}
		}
		return value;
	}
	
}
