package net.ultech.cyproject.recorddomain;

public class RecordInfo {
	public RecordInfo(String username, int score, int rank) {
		super();
		this.username = username;
		this.score = score;
		this.rank = rank;
	}

	public RecordInfo() {

	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	private String username;
	private int score;
	private int rank;
}
