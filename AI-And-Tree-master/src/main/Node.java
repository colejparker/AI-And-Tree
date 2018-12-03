package main;

import java.util.ArrayList;

public class Node {
	private Node parent;
	private ArrayList<Node> children = new ArrayList();
	private ArrayList<Pair> pr = new ArrayList();
	private boolean solved = false;
	public Node(Node parent, ArrayList<Node> children, ArrayList<Pair> pr) {
		this.parent = parent;
		this.pr = pr;
		this.children = children;
	}
	public Node() {
	}
	public Node(Node another) {
		for (Node n : another.children) {
			this.children.add(n);
		}
		this.parent = another.parent;
		for (Pair p : another.pr) {
			this.pr.add(p);
		}
	}
	public Node getParent() {return this.parent;}
	public ArrayList<Node> getChildren() {return this.children;}
	public ArrayList<Pair> getPr() {return this.pr;}
	public boolean getSolved() {return this.solved;}
	public void addChild(Node n) {
		this.children.add(n);
	}
	public void addToPr(Pair p) {
		this.pr.add(p);
	}
	public void nodeSolved() {
		this.solved = true;
	}
	// Still need to add functions for pr mutation
}
