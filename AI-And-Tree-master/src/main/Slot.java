package main;

import java.util.ArrayList;

public class Slot {
	private static final int MO = 1;
	private static final int TU = 2;
	private static final int FR = 3;
	private String fullName;
	private String day;
	private String time;
	private String startTime;
	private int idNumber;
	private int courseMin;
	private int courseMax;
	private int labMin;
	private int labMax;
	private boolean isLabSlot;
	private boolean isCourseSlot;
	private ArrayList<Course> courses = new ArrayList();
	public Slot(String fullName, int idNumber, boolean isLabSlot, boolean isCourseSlot) {
		this.fullName = fullName;
		this.idNumber = idNumber;
		this.isLabSlot = isLabSlot;
		this.isCourseSlot = isCourseSlot;
		int index = fullName.indexOf(',');
		this.day = fullName.substring(0, index);
		fullName = fullName.substring(index+1);
		this.time = fullName.replaceAll(" ", "");
		index = time.indexOf('-');
		this.startTime = time.substring(0, index);
		courseMin = 0;
		courseMax = 9999;
		labMin = 0;
		labMax = 9999;
	}
	public Slot() {
		
	}
	public String getName() {return this.fullName;}
	public String getDay() {return this.day;}
	public String getTime() {return this.time;}
	public String getStartTime() { return this.startTime;}
	public int getIDNumber() {return this.idNumber;}
	public int getCourseMin() {return this.courseMin;}
	public int getCourseMax() {return this.courseMax;}
	public int getLabMin() {return this.labMin;}
	public int getLabMax() {return this.labMax;}
	public boolean isLabSlot() {return this.isLabSlot;}
	public boolean isCourseSlot() {return this.isCourseSlot;}
	public ArrayList<Course> getCourses() {return this.courses;}
	public void setCourseMin(int courseMin) {this.courseMin = courseMin;}
	public void setCourseMax(int courseMax) {this.courseMax = courseMax;}
	public void setLabMin(int labMin) {this.labMin = labMin;}
	public void setLabMax(int labMax) {this.labMax = labMax;}
	public void addCourse(Course c) { courses.add(c);}
	public String toString() {
		return this.day + ", " + this.startTime;
//		if (this.isCourseSlot && this.isLabSlot) {
//			return this.day + ", start time: " + this.startTime + ", course slots: " + courseMin + "-" + courseMax + ", lab slots: " + labMin + "-" + labMax; 
//		} else if (this.isCourseSlot) {
//			return this.day + ", start time: " + this.startTime + ", course slots: " + courseMin + "-" + courseMax; 
//		} else {
//			return this.day + ", start time: " + this.startTime + ", lab slots: " + labMin + "-" + labMax; 
//		}
	}
}
