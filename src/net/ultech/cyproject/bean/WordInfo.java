package net.ultech.cyproject.bean;

import net.ultech.cyproject.R;

public class WordInfo {
	public WordInfo(String name, String first, int count) {
		this.name = name;
		this.first = first;
		this.count = count;
	}

	public WordInfo() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	private String name;
	private String first;
	private int count;
}
