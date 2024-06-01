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

import info.GUIConstants;
import info.MyShapes;
import info.SysInfo;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import components.MyFrame;
import control.CurAudio;

/**
 * A custom interface component for displaying committed annotations to the user.
 * 
 * @author Yuvi Masory
 */
public class SuggestionDisplay extends JScrollPane {
	
	private static final String title = "Suggestions";
	
	private static SuggestionDisplay instance;
	private static SuggestionTable table;

	/**
	 * Creates a new instance of the component, initializing internal components, key bindings, listeners, 
	 * and various aspects of appearance.
	 */
	private SuggestionDisplay() {		
		table = SuggestionTable.getInstance();
		getViewport().setView(table);
		setPreferredSize(GUIConstants.annotationDisplayDimension);
		setMaximumSize(GUIConstants.annotationDisplayDimension);
		
		setBorder(MyShapes.createMyUnfocusedTitledBorder(title));
		
		//since SuggestionDisplay is a clickable area, we must write focus handling code for the event it is clicked on
		//passes focus to the table if it is focusable (not empty), otherwise giving focus to the frame
		addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {
				if(table.isFocusable()) {
					table.requestFocusInWindow();
				}
				else {
					MyFrame.getInstance().requestFocusInWindow();
				}
			}
		});
		
		//overrides JScrollPane key bindings for the benefit of SeekAction's key bindings
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "none");
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "none");
	}
	
	
	public static Suggestion[] getSuggestionsInOrder() {
		return table.getModel().toArray();
	}
	
	public static void addSuggestion(Suggestion sugg) {
		if(sugg == null) {
			throw new IllegalArgumentException("annotation/s cannot be null");
		}
		if(SysInfo.sys.forceListen) {
			CurAudio.getListener().offerGreatestProgress(CurAudio.getMaster().millisToFrames(sugg.getTime()));
		}
		table.getModel().addElement(sugg);
	}
	
	public static void addSuggestions(Iterable<Suggestion> suggs) {
		if(suggs == null) {
			throw new IllegalArgumentException("annotations cannot be null");
		}
		if(SysInfo.sys.forceListen) {
			for(Suggestion a: suggs) {
				CurAudio.getListener().offerGreatestProgress(CurAudio.getMaster().millisToFrames(a.getTime()));
			}
		}
		table.getModel().addElements(suggs);
	}
	
	public static void removeSuggestion(int rowIndex) {
		table.getModel().removeElementAt(rowIndex);
	}
	
	public static void removeAllSuggestions() {
		table.getModel().removeAllElements();
	}
	
	
	
	
	

	public static SuggestionDisplay getInstance() {
		if (instance == null) {
			instance = new SuggestionDisplay();
		}
		return instance;
	}


	public static int getNumSuggestions() {
		return table.getModel().size();
	}
}
