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

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import behaviors.singleact.DeleteSuggestionAction;
import behaviors.singleact.ConvertSuggestionAction;


/**
 * Popup menu launched by right clicking on annotations. 
 * 
 * @author Yuvi Masory
 */
public class SuggestionTablePopupMenu extends JPopupMenu {

	protected SuggestionTablePopupMenu(Suggestion annToDelete, int rowIndex, SuggestionTable table, String rowRepr) {
		super();
		JMenuItem fakeTitle = new JMenuItem(rowRepr + "...");
		fakeTitle.setEnabled(false);
		JMenuItem del = new JMenuItem(
				new DeleteSuggestionAction(rowIndex));
		JMenuItem convertor = new JMenuItem(
				new ConvertSuggestionAction(rowIndex));
		add(fakeTitle);
		addSeparator();
		add(del);
		add(convertor);
	}
}
