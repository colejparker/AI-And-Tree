package main;
//Class for a paired up class and slot
public class Pair {
	private Course course;
	private Slot slot;
	public Pair(Course course, Slot slot) {
		this.course = course;
		this.slot = slot;
	}
	public Course getCourse() {return course;}
	public Slot getSlot() {return slot;}
	public void setCourse(Course course) {this.course = course;}
	public void setSlot(Slot slot) {this.slot = slot;}
	public String toString() {
		return "Course: " + this.course + " Slot: " + this.slot;
	}
}
