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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * Custom <code>TableModel</code> for storing Suggestions of the open audio file.
 * 
 * @author Yuvi Masory
 */
public class SuggestionTableModel implements TableModel {
	
	private HashSet<TableModelListener> listeners;
	
	private ArrayList<Suggestion> sortedSuggs;
	
	//editing the table layout (e.g., adding a new column, switching the order of two columns) involves more than changing the next three lines
	//some of the methods below make assumptions about the number of columns and the Suggestion methods they hook up to
	//doing this in a perfectly programmed worled would involve storing an array of Method objects
	private static final int columnCount = 3;
	private static final Class<?>[] columnClasses = new Class<?>[] {Double.class, String.class, Double.class};
	private static final String[] columnNames = new String[] {"Time (ms)", "Word", "Model Conf"};
	
	private static final String colErr = "column index out of range";
	private static final String rowErr = "row index out of range";
	private static final String stateErr = "inconsistency in internal column handling";
	
	protected SuggestionTableModel() {
		if(columnCount != columnClasses.length || columnCount != columnNames.length) {
			throw new IllegalStateException(stateErr);
		}
		listeners = new HashSet<TableModelListener>();
		sortedSuggs = new ArrayList<Suggestion>();
	}

	public int getColumnCount() {
		return columnCount;
	}

	public int getRowCount() {
		return sortedSuggs.size();
	}
	
	public Class<?> getColumnClass(int columnIndex) {
		if(columnIndex > columnClasses.length || columnIndex < 0) {
			throw new IllegalArgumentException(colErr);
		}
		return columnClasses[columnIndex];
	}
	
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	public String getColumnName(int columnIndex) {
		if(columnIndex > columnClasses.length || columnIndex < 0) {
			throw new IllegalArgumentException(colErr);
		}
		return columnNames[columnIndex];
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if(rowIndex < 0 || rowIndex > sortedSuggs.size()) {
			throw new IllegalArgumentException(rowErr);
		}
		if(columnIndex > columnCount - 1) {
			throw new IllegalArgumentException(colErr);
		}
		Suggestion sugg = sortedSuggs.get(rowIndex);
		if(columnIndex == 0) {
			return sugg.getTime();
		}
		if(columnIndex == 1) {
			return sugg.getText();
		}
		if(columnIndex == 2) {
			return sugg.getWordScore();
		}
		throw new IllegalStateException(stateErr);
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		throw new UnsupportedOperationException("setting table values not supported, use add/remove Suggestion methods");
	}
	
	protected Suggestion getSuggestionAt(int rowIndex) {
		if(rowIndex < 0 || rowIndex > sortedSuggs.size()) {
			throw new IllegalArgumentException(rowErr);
		}
		return sortedSuggs.get(rowIndex);
	}

	protected Suggestion[] toArray() {
		return sortedSuggs.toArray(new Suggestion[sortedSuggs.size()]);
	}
	
	
	//adding duplicates is prevented by Suggestion-over deleting first Suggestion, performed in annotateaction
	protected void addElement(Suggestion sugg) {
		sortedSuggs.add(sugg);
		//then remove batch adding option below
		Collections.sort(sortedSuggs);
		for(TableModelListener tml: listeners) {
			tml.tableChanged(new TableModelEvent(this));
		}
	}
	
	//duplicate adds are possible with this method
	protected void addElements(Iterable<Suggestion> batch) {
		for(Suggestion el: batch) {
			sortedSuggs.add(el);
		}
		Collections.sort(sortedSuggs);
		for(TableModelListener tml: listeners) {
			tml.tableChanged(new TableModelEvent(this));
		}
	}

	protected void removeElementAt(int index) {
		if(index < 0 || index > sortedSuggs.size()) {
			throw new IllegalArgumentException(rowErr);
		}
		sortedSuggs.remove(index);
		for(TableModelListener tml: listeners) {
			tml.tableChanged(new TableModelEvent(this, Math.min(index, sortedSuggs.size()), sortedSuggs.size()));
		}
	}

	protected void removeAllElements() {
		sortedSuggs.clear();
	}

	public int size() {
		return sortedSuggs.size();
	}
}
