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

import java.awt.event.ActionEvent;

import util.GiveMessage;

import components.MyFrame;
import components.suggestions.Suggestion;
import components.suggestions.SuggestionDisplay;
import components.suggestions.SuggestionTable;

import control.CurAudio;
import edu.upenn.psych.memory.precisionplayer.PrecisionPlayer;

public class JumpToSuggestionAction extends IdentifiedSingleAction {


	public JumpToSuggestionAction() {
	}

	@Override	
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		Suggestion targetAnn = SuggestionTable.popSelectedSuggestion();
		if(targetAnn == null) {
			System.err.println("selection is invalid, can't jump to Suggestion");
		}
		else {
			long curFrame = CurAudio.getMaster().millisToFrames(targetAnn.getTime());
			if(curFrame < 0 || curFrame > CurAudio.getMaster().durationInFrames() - 1) {
				GiveMessage.errorMessage("The suggestion I am jumpting to isn't in range.\nPlease check suggestion file for errors."); 
				return;
			}
			CurAudio.setAudioProgressAndUpdateActions(curFrame);
			CurAudio.getPlayer().queuePlayAt(curFrame);
		}
		MyFrame.getInstance().requestFocusInWindow();
	}

	@Override
	public void update() {
		if(CurAudio.audioOpen()) {
			if(CurAudio.getPlayer().getStatus() == PrecisionPlayer.Status.PLAYING) {
				setEnabled(false);
			}
			else {
				if(SuggestionDisplay.getNumSuggestions() > 0) {
					setEnabled(true);
				}
				else {
					setEnabled(false);
				}
			}
		}
		else {
			setEnabled(false);
		}
	}
}
