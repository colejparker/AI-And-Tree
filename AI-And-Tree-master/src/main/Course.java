package main;

public class Course {
	private boolean lab;
	private String name;
	private int idNumber;
	private Slot slot;
	public Course(boolean lab, String name, int idNumber) {
		slot = null;
		this.lab = lab;
		this.name = name;
		this.idNumber = idNumber;
	}
	public Course() {
	}
	public boolean isLab() {return this.lab;}
	public String getName() {return this.name;}
	public int getIDnumber() {return this.idNumber;}
	public Slot getSlot() {return this.slot;}
	public void setSlot(Slot s) { this.slot = s;}
	public String toString() {
		return this.name;
	}
}
