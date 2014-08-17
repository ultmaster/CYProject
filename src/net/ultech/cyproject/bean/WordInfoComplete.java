package net.ultech.cyproject.bean;

public class WordInfoComplete extends WordInfo {

	public WordInfoComplete(String name, String first, int count, String spell,
			String content, String derivation, String samples) {
		super(name, first, count);
		this.spell = spell;
		this.content = content;
		this.derivation = derivation;
		this.samples = samples;
	}

	public WordInfoComplete() {

	}

	public String getSpell() {
		return spell;
	}

	public void setSpell(String spell) {
		this.spell = spell;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDerivation() {
		return derivation;
	}

	public void setDerivation(String derivation) {
		this.derivation = derivation;
	}

	public String getSamples() {
		return samples;
	}

	public void setSamples(String samples) {
		this.samples = samples;
	}

	private String spell;
	private String content;
	private String derivation;
	private String samples;

}
