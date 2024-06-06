//    This file is part of Penn TotalRecall <http://memory.psych.upenn.edu/TotalRecall>.
//
//    TotalRecall is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, version 3 only.
//
//    TotalRecall is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with TotalRecall.  If not, see <http://www.gnu.org/licenses/>.

package components.suggestions;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 *  
 * @author Yuvi Masory
 *
 */
public class Suggestion implements Comparable<Suggestion>{

	private double wordScore;
	private double time;
	private String text;
	
	public Suggestion(double time, double wordScore, String text) {
		this.time = time;
		this.wordScore = wordScore;
		this.text = text;
	}

	public Suggestion(String text, double wordScore) {
		this.time = -1.0;
		this.wordScore = wordScore;
		this.text = text;
	}

	public Suggestion(double time, String text) {
		this.time = time;
		this.wordScore = -1.0;
		this.text = text;
	}

	public Suggestion(double time, double wordScore) {
		this.time = time;
		this.wordScore = wordScore;
		this.text = "**WORD_EXPECTED**";
	}


	public double getTime() {
		return time;
	}
	
	public double getWordScore() {
		return wordScore;		
	}
	
	public String getText() {
		return text;
	}
	
	@Override
	public int hashCode() {
		return (text + time + wordScore).hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Suggestion) {
			Suggestion a = (Suggestion)o;
			if(getText().equals(a.getText())) {
				if(getTime() == a.getTime()) {
					if(getWordScore() == a.getWordScore()) {
						if(getWordScore() == a.getWordScore()) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "Suggestion: " + text + " " + time + " ms " + " conf: " + wordScore;
	}

	public int compareTo(Suggestion o) {
		return(new Double(getTime()).compareTo(o.getTime()));
	}
}
