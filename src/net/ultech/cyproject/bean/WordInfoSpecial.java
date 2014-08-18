package net.ultech.cyproject.bean;

import net.ultech.cyproject.R;

public class WordInfoSpecial implements Comparable<WordInfoSpecial> {

	public WordInfoSpecial(String name, int countOfLast) {
		this.name = name;
		this.countOfLast = countOfLast;
	}

	private int countOfLast;
	private String name;

	public int getCountOfLast() {
		return countOfLast;
	}

	public void setCountOfLast(int countOfLast) {
		this.countOfLast = countOfLast;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(WordInfoSpecial another) {
		if (this.getCountOfLast() < another.getCountOfLast())
			return -1;
		else if (this.getCountOfLast() > another.getCountOfLast())
			return 1;
		else
			return 0;
	}
}
