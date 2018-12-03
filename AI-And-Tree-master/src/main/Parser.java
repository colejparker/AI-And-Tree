package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Parser {
	public ArrayList<Slot> getSlots(ArrayList<Slot> slots, String[] text) {
		boolean checkingLabs = false;
		boolean startOfSlots = false;
		for (String l : text) {
			if (l.contains("Course slots:")) {
				startOfSlots = true;
			}
			if (startOfSlots) {
					String tempText = l;
					String day = "";
					String time =  "";
					int newMax = 0;
					int newMin = 0;
					int index = tempText.indexOf(',');
					if (index != -1) {
						day = tempText.substring(0, index);
						tempText = tempText.substring(index+1);
						index = tempText.indexOf(',');
						if (index != -1) {
							time = tempText.substring(0, index).replaceAll(" ", "");
						}
						
					}
					if (!day.equals("") && !time.equals("")) {
						tempText = tempText.substring(index+1);
						for (Slot sl : slots) {
							if (sl.getDay().equals(day) && sl.getStartTime().equals(time)) {
								index = tempText.indexOf(',');
								try {
									newMax = Integer.parseInt(tempText.substring(0, index).replaceAll("\\s", ""));
								} catch (Exception e) {
									//e.printStackTrace();
								}
								tempText = tempText.substring(index+1);
								try {
									newMin = Integer.parseInt(tempText.replaceAll("\\s", ""));
								} catch (Exception e) {
									//e.printStackTrace();
								}
								if (checkingLabs) {
									sl.setLabMax(newMax);
									sl.setLabMin(newMin);
								} else {
									sl.setCourseMax(newMax);
									sl.setCourseMin(newMin);
								}
								
							}
						}
					}
					if (l.contains("Lab slots:")) {
						checkingLabs = true;
					}
					if (l.contains("Courses:")) {
						return slots;
					}
				}
			}
		return slots;
	}
	
	public static ArrayList<Course> populateCourses(String[] text) {
		ArrayList<Course> courses = new ArrayList();
		int i=1;
		boolean startCourses = false;
		boolean isLab = false;
		for (String l : text) {
			if (l.contains("compatible")) {
				return courses;
			}
			if (startCourses && (l.contains("LEC") || l.contains("TUT") || l.contains("LAB"))) {
				if (l.contains("TUT") || l.contains("LAB")) {
					isLab = true;
				}
				courses.add(new Course(isLab, l.replace("\n", "").replace("\r", ""), i));
				i++;
				isLab = false;
			}
			if (l.contains("Courses:")) {
				startCourses = true;
			} 
			
		}
		return courses;
	}
	
	public static ArrayList<ArrayList<Course>> getIncompatibleCourses(ArrayList<Course> courses, String[] text) {
		boolean startSearching = false;
		ArrayList<ArrayList<Course>> toReturn = new ArrayList();
		for (String l : text) {
			if ((l.contains("nwanted") || l.replaceAll("\r", "").replaceAll(" ", "").equals("")) && startSearching) {
				return toReturn;
			}
			if (startSearching) {
				ArrayList<Course> toAdd = new ArrayList();
				String[] seperatedCourses = l.split(",");
				String firstCourse = seperatedCourses[0].replaceAll(",", "").replaceAll("\n", "").replaceAll(" ", "").replaceAll("\r", "").toLowerCase();
				String secondCourse = seperatedCourses[1].replaceAll(",", "").replaceAll("\n", "").replaceAll(" ", "").replaceAll("\r", "").toLowerCase();
				for (Course c : courses) {
					String cName = c.getName().replaceAll(",", "").replaceAll("\n", "").replaceAll(" ", "").replaceAll("\r", "").toLowerCase();
					if (cName.equals(firstCourse) || cName.equals(secondCourse)) {
						toAdd.add(c);
					}
				}
				if (toAdd.size() == 2) {
					toReturn.add(toAdd);
				}
			}
			if (l.contains("compatible")) {
				startSearching = true;
			}
		}
		return toReturn;
	}
	
	public static ArrayList<Pair> getUnwantedPairings(ArrayList<Course> courses, ArrayList<Slot> slots, String[] text) {
		boolean startSearching = false;
		ArrayList<Pair> toReturn = new ArrayList();
		Course course = new Course();
		Slot slot = new Slot();
		boolean courseFound = false;
		boolean slotFound = false;
		for (String l : text) {
			if ((l.contains("references") || l.replaceAll("\r", "").replaceAll(" ", "").equals("")) && startSearching) {
				return toReturn;
			}
			if (startSearching) {
				int index = l.indexOf(",");
				String[] seperatedCourses = {l.substring(0, index), l.substring(index+1)};
				for (String s : seperatedCourses) {
					if (s.contains("LEC") || s.contains("TUT") || s.contains("LAB")) {
						String courseText = seperatedCourses[0].replaceAll(",", "").replaceAll("\n", "").replaceAll(" ", "").replaceAll("\r", "").toLowerCase();
						for (Course c : courses) {
							String cName = c.getName().replaceAll(",", "").replaceAll("\n", "").replaceAll(" ", "").replaceAll("\r", "").toLowerCase();
							if (cName.equals(courseText)) {
								course = c;
								courseFound = true;
							}
						}
					} else {
						for (Slot sl : slots) {
							if (s.contains(sl.getDay()) && s.contains(sl.getStartTime())) {
								slot = sl;
								slotFound = true;
							}
						}
					}
				}
				if (slotFound && courseFound) {
					toReturn.add(new Pair(course, slot));
				}
			}
			if (l.contains("Unwanted")) {
				startSearching = true;
			}
		}
		return null;
	}
	
	public static ArrayList<ArrayList<Object>> getPreferredPairings(ArrayList<Course> courses, ArrayList<Slot> slots, String[] text) {
		boolean startSearching = false;
		ArrayList<ArrayList<Object>> toReturn = new ArrayList();
		for (String l : text) {
			if ((l.contains("air") || l.replaceAll("\r", "").replaceAll(" ", "").equals("")) && startSearching) {
				return toReturn;
			}
			if(startSearching) {
				ArrayList<Object> toAdd = new ArrayList();
				String[] seperatedText = l.split(",");
				String day = "";
				String time = "";
				Course course = new Course();
				for (String s : seperatedText) {
					if (s.equals("MO") || s.equals("TU") || s.equals("FR")) {
						day = s;
					} else if (s.contains(":")) {
						time = s;
					} else if (s.contains("LEC") || s.contains("TUT") || s.contains("LAB")) {
						String courseText = s.replaceAll(",", "").replaceAll("\n", "").replaceAll(" ", "").replaceAll("\r", "").toLowerCase();
						for (Course c : courses) {
							String cName = c.getName().replaceAll(",", "").replaceAll("\n", "").replaceAll(" ", "").replaceAll("\r", "").toLowerCase();
							if (cName.equals(courseText)) {
								toAdd.add(c);
							}
						}
					} else {
						toAdd.add(Integer.parseInt(s.replaceAll(",", "").replaceAll("\n", "").replaceAll(" ", "").replaceAll("\r", "").toLowerCase()));
					}
				}
				for (Slot sl : slots) {
					if (day.contains(sl.getDay()) && time.contains(sl.getStartTime())) {
						if ((course.isLab() && sl.isLabSlot()) || (!course.isLab() && sl.isCourseSlot())) {
							toAdd.add(sl);
						}
						
					}
				}
				if (toAdd.size() == 3) {
					toReturn.add(toAdd);
				}
			}
			if (l.contains("references")) {
				startSearching = true;
			}
			
		}
		return null;
	}
	
	public static ArrayList<ArrayList<Course>> getPairedCourses(ArrayList<Course> courses, String[] text) {
		ArrayList<ArrayList<Course>> toReturn = new ArrayList();
		boolean startSearching = false;
		for (String l : text) {
			if ((l.contains("artial") || l.replaceAll("\r", "").replaceAll(" ", "").equals("")) && startSearching) {
				return toReturn;
			}
			if (startSearching) {
				ArrayList<Course> toAdd = new ArrayList();
				String[] seperatedText = l.split(",");
				for (String s : seperatedText) {
					String courseText = s.replaceAll(",", "").replaceAll("\n", "").replaceAll(" ", "").replaceAll("\r", "").toLowerCase();
					for (Course c : courses) {
						String cName = c.getName().replaceAll(",", "").replaceAll("\n", "").replaceAll(" ", "").replaceAll("\r", "").toLowerCase();
						if (cName.equals(courseText)) {
							toAdd.add(c);
						}
					}
				}
				toReturn.add(toAdd);
			}
			if (l.contains("air:")) {
				startSearching = true;
			}
		}
		return null;
	}
	
	public static ArrayList<Pair> getPartialAssignments(ArrayList<Course> courses, ArrayList<Slot> slots, String[] text) {
		boolean startSearching = false;
		ArrayList<Pair> toReturn = new ArrayList();
		Course course = new Course();
		Slot slot = new Slot();
		boolean courseFound = false;
		boolean slotFound = false;
		for (String l : text) {
			if (l.replaceAll("\r", "").replaceAll(" ", "").equals("") && startSearching) {
				return toReturn;
			}
			if (startSearching) {
				int index = l.indexOf(",");
				String[] seperatedCourses = {l.substring(0, index), l.substring(index+1)};
				for (String s : seperatedCourses) {
					if (s.contains("LEC") || s.contains("TUT") || s.contains("LAB")) {
						String courseText = seperatedCourses[0].replaceAll(",", "").replaceAll("\n", "").replaceAll(" ", "").replaceAll("\r", "").toLowerCase();
						for (Course c : courses) {
							String cName = c.getName().replaceAll(",", "").replaceAll("\n", "").replaceAll(" ", "").replaceAll("\r", "").toLowerCase();
							if (cName.equals(courseText)) {
								course = c;
								courseFound = true;
							}
						}
					} else {
						for (Slot sl : slots) {
							if (s.contains(sl.getDay()) && s.contains(sl.getStartTime())) {
								slot = sl;
								slotFound = true;
							}
						}
					}
				}
				if (slotFound && courseFound) {
					toReturn.add(new Pair(course, slot));
				}
			}
			if (l.contains("artial")) {
				startSearching = true;
			}
		}
		return toReturn;
	}
	
	public static ArrayList<Slot> populateSlots() {
		int i = 0;
		ArrayList<String> slotStrings = new ArrayList<String>(Arrays.asList(
				"MO, 8:00-9:00",
				"MO, 9:00-10:00",
				"MO, 10:00-11:00",
				"MO, 11:00-12:00",
				"MO, 12:00-13:00",
				"MO, 13:00-14:00",
				"MO, 14:00-15:00",
				"MO, 15:00-16:00",
				"MO, 16:00-17:00",
				"MO, 17:00-18:00",
				"MO, 18:00-19:00",
				"MO, 19:00-20:00",
				"MO, 20:00-21:00",
				"TU, 8:00-9:30",
				"TU, 9:30-11:00",
				"TU, 11:00-12:30",
				"TU, 12:30-14:00",
				"TU, 14:00-15:30",
				"TU, 15:30-17:00",
				"TU, 17:00-18:30",
				"TU, 18:30-20:00",
				"TU, 8:00-9:00",
				"TU, 9:00-10:00",
				"TU, 10:00-11:00",
				"TU, 11:00-12:00",
				"TU, 12:00-13:00",
				"TU, 13:00-14:00",
				"TU, 14:00-15:00",
				"TU, 15:00-16:00",
				"TU, 16:00-17:00",
				"FR, 8:00-10:00",
				"FR, 10:00-12:00",
				"FR, 12:00-14:00",
				"FR, 14:00-16:00",
				"FR, 16:00-18:00",
				"FR, 18:00-20:00"
				));
		boolean[] labSlots = new boolean[]{true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true};
		boolean[] courseSlots = new boolean[]{true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		ArrayList<Slot> newSlots = new ArrayList();
		for (String s: slotStrings) {
			
			newSlots.add(new Slot(s, i+1, labSlots[i], courseSlots[i]));
			i++;
		}
		return newSlots;
	}
	
	public static String readFileAsString(String filename) {
		String text = "";
		try {
			text = new String(Files.readAllBytes(Paths.get(filename)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return text;
	}
}
