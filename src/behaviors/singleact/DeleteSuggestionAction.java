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

package behaviors.singleact;

import info.Constants;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Action;

import util.GiveMessage;
import util.OSPath;

import components.MyMenu;
import components.suggestions.Suggestion;
import components.suggestions.SuggestionDisplay;
import components.suggestions.SuggestionFileParser;

import control.CurAudio;

/**
 * Deletes an suggestion that has already been committed to a temporary suggestion file.
 * 
 * If the suggestions is the last available, also deletes the temporary suggestion file, which should at this point be empty.
 * 
 * @author Yuvi Masory
 */
public class DeleteSuggestionAction extends IdentifiedSingleAction {

	private int rowIndex;
	private Suggestion suggToDelete;

	/**
	 * Creates an <code>Action</code> that will delete the suggestion matching the provided argument.
	 * 
	 * @param rowIndex
	 * @param suggToDelete
	 */
	public DeleteSuggestionAction(int rowIndex) {
		this.rowIndex = rowIndex;
		this.suggToDelete = SuggestionDisplay.getSuggestionsInOrder()[rowIndex];
		this.putValue(Action.NAME, "Delete Suggestion");
	}

	/**
	 * Performs the action by calling {@link SuggestionFileParser#removeSuggestion(Suggestion, File)}.
	 * 
	 * Warns on failure using dialogs.
	 * 
	 * @param e The <code>ActionEvent</code> provided by the trigger
	 */
	@Override	
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		String curFileName = CurAudio.getCurrentAudioFileAbsolutePath();
		String desiredPath = OSPath.basename(curFileName) + "." + Constants.temporarySuggestionFileExtension;
		File oFile = new File(desiredPath);
		
		boolean success = false;
		try {
			success = SuggestionFileParser.removeSuggestion(suggToDelete, oFile);
		}
		catch(IOException ex) {
			ex.printStackTrace();
			success = false;
		}
		if(success) {
			SuggestionDisplay.removeSuggestion(rowIndex);
			
			//no suggestions left after removal, so delete file too
			if(SuggestionDisplay.getNumSuggestions() == 0) {
				if(oFile.delete() == false) {
					GiveMessage.errorMessage("Deletion of suggestion successful, but could not remove temporary suggestion file.");
				}
			}
		}
		else {
			GiveMessage.errorMessage("Deletion not successful. Files may be damaged. Check file system.");
		}
		
		MyMenu.updateActions();
	}

	/**
	 * The user can delete an suggestion when audio is open and there is at least one suggestion to the current file.
	 */
	@Override
	public void update() {
		if(CurAudio.audioOpen() && SuggestionDisplay.getNumSuggestions() > 0) {
			setEnabled(true);
		}
		else {
			setEnabled(false);
		}
	}
}
