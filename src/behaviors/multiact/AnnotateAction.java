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

package behaviors.multiact;

import info.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import util.GiveMessage;
import util.OSPath;

import info.UserPrefs;

import behaviors.UpdatingAction;
import behaviors.singleact.DeleteSelectedAnnotationAction;

import components.MyFrame;
import components.MyMenu;
import components.annotations.Annotation;
import components.annotations.AnnotationDisplay;
import components.annotations.AnnotationFileParser;
import components.waveform.WaveformDisplay;
import components.wordpool.WordpoolDisplay;
import components.wordpool.WordpoolWord;

import control.CurAudio;
import edu.upenn.psych.memory.precisionplayer.PrecisionPlayer;

/**
 * Commits a user's annotation, updating the annotation file and program window as appropriate.
 * 
 * @author Yuvi Masory
 */
public class AnnotateAction extends IdentifiedMultiAction {

	public static enum Mode {INTRUSION, REGULAR};
	
	private Mode mode;

	/**
	 * Create an <code>Action</code> corresponding to an intrusion or a normal annotation.
	 * 
	 * @param isIntrusion Whether the annotations committed by this <code>Action</code> are intrusions
	 */
	public AnnotateAction(Mode mode) {
		super(mode);
		this.mode = mode;
	}
	
	private static String obfuscate(String in) {
		byte[] inb = in.getBytes();
		StringBuffer buff = new StringBuffer();
		for(byte b: inb) {
			buff.append(b + " ");
		}
		return buff.toString();
	}
	
	public static File getOutputFile() {
		String curFileName = CurAudio.getCurrentAudioFileAbsolutePath();
		File oFile = new File(OSPath.basename(curFileName) + "." + Constants.temporaryAnnotationFileExtension);
		return oFile;
	}

	/**
	 * Performs the <code>AnnotationAction</code> by appending the word in the text field to the temporary annotations file.
	 * 
	 * @param e The <code>ActionEvent</code> provided by the trigger.
	 */
	@Override	
	public void actionPerformed(ActionEvent e){
		super.actionPerformed(e);
		//do nothing if no audio file is open
		if(CurAudio.audioOpen() == false) { 
			WordpoolDisplay.clearText();
			return;
		}
		
		//retrieve time associated with annotation
		double time = CurAudio.getMaster().framesToMillis(CurAudio.getAudioProgress());
		
		//retrieve text associated with annotation, possibly the intrusion string
		String text = WordpoolDisplay.getFieldText(); 
		if(text.length() == 0) {
			if(mode == Mode.INTRUSION) {
				text = Constants.intrusionSoundString;
			}
			else {
				return;
			}
		}
		
		//find whether the text matches a wordpool entry, so we can find the wordpool number of the annotation text
		WordpoolWord match = WordpoolDisplay.findMatchingWordpooWord(text);
		int shouldbeindex = -1;
		if(match == null) {
			if(mode == Mode.REGULAR) { //words not from the wordpool must be marked as intrusions
				return;
			}
			String wordpoolFilename = UserPrefs.prefs.get(UserPrefs.wordpoolFilename, UserPrefs.wordpoolFilename);
			//System.out.println("Current wordpool file is: " + wordpoolFilename);
			shouldbeindex = WordpoolDisplay.getInstance().getWordpoolWordIndex(text);
			String shouldbeindexstr = Integer.valueOf(shouldbeindex).toString();
			System.out.println(text + " index should be " + shouldbeindexstr);
			//match = new WordpoolWord(text, -1);
			match = new WordpoolWord(text, shouldbeindex);
			ArrayList<WordpoolWord> matchlist = new ArrayList<WordpoolWord>();
			matchlist.add(match);
			WordpoolDisplay.getInstance().addWordpoolWords(matchlist);
			WriteToWpFileLine(wordpoolFilename, text, (shouldbeindex - 1));
		}


		//append the new annotation to the end of the temporary annotation file
		File oFile = getOutputFile();

		if(oFile.exists() == false) {		
			try {
				oFile.createNewFile();
			} 
			catch (IOException e1) {
				e1.printStackTrace();
				GiveMessage.errorMessage("Could not create " + Constants.temporaryAnnotationFileExtension + " file.");
			}		
		}
		if(oFile.exists()) {
			//check for header
			try {
				if(AnnotationFileParser.headerExists(oFile) == false) {

					String annotatorName = MyMenu.getAnnotator();
					if(annotatorName == null) {
						annotatorName = GiveMessage.inputMessage("Please enter your name:");
						if(annotatorName == null || annotatorName.equals("")) {
							GiveMessage.errorMessage("Cannot commit annotation without name.");
							return;
						}
					}
					MyMenu.setAnnotator(annotatorName);

					AnnotationFileParser.prependHeader(oFile, annotatorName);
				}

				Annotation ann = new Annotation(time, match.getNum(), match.getText());

				//check if we are annotating the same position as an existing annotation, if so delete
				new DeleteSelectedAnnotationAction().actionPerformed(
						new ActionEvent(WordpoolDisplay.getInstance(), ActionEvent.ACTION_PERFORMED, null, System.currentTimeMillis(), 0));
				WaveformDisplay.getInstance().repaint();
				
				//file may no longer exist after deletion
				if(oFile.exists() == false) {
					if(oFile.createNewFile()) {
						String annotatorName = GiveMessage.inputMessage("Please enter your name:");
						if(annotatorName == null || annotatorName.equals("")) {
							GiveMessage.errorMessage("Cannot commit annotation without name.");
							return;
						}
						if(AnnotationFileParser.headerExists(oFile) == false) {
							AnnotationFileParser.prependHeader(oFile, annotatorName);
						}
					}
					else {
						throw new IOException("Could not re-create file.");
					}	
				}
				
				
				//add a new annotation object, and clear the field
				AnnotationFileParser.appendAnnotation(ann, oFile);
				if(shouldbeindex != -1){
					AnnotationDisplay.getInstance().addNewWPAnn(ann, shouldbeindex);

					//Write the newly updated annotation row numbers to the annotation file
					
					String annotatorName = MyMenu.getAnnotator();
					String annotationFilename = OSPath.basename(CurAudio.getCurrentAudioFileAbsolutePath()) + "." + Constants.temporaryAnnotationFileExtension;
					File annotationFile = new File(annotationFilename);
					File tmpFile = new File(oFile.getAbsolutePath() + "." + Constants.deletionTempFileExtension);
					BufferedWriter fw = new BufferedWriter(new FileWriter(tmpFile));
					
					Annotation [] annList = AnnotationDisplay.getInstance().getAnnotationsInOrder();
					int annListSize = annList.length;
					//System.out.println("Number of annotations: " + Integer.valueOf(annListSize).toString());
					for(Annotation currentAnn : annList){
						String currline = AnnotationFileParser.makeLine(currentAnn) + "\n";
						fw.write(currline);
					}
					//br.close();
		
					fw.close();	
					if(annotationFile.delete() == false) {
						GiveMessage.errorMessage("could not delete old file");
					}
					if(tmpFile.renameTo(annotationFile) == false) {
						GiveMessage.errorMessage("could not rename temp deletion file to normal temp file");
					}
					AnnotationFileParser.prependHeader(annotationFile, annotatorName);
				}
				else{
					AnnotationDisplay.getInstance().addAnnotation(ann);
				}
				WordpoolDisplay.clearText();
			} 
			catch (IOException e1) {
				e1.printStackTrace();
				GiveMessage.errorMessage("Error comitting annotation! Check files for damage.");
			}
		}
		
	    //return focus to the frame after annotation, for the sake of action key bindings
	    MyFrame.getInstance().requestFocusInWindow();
	    MyMenu.updateActions();
	}

	public static void AppendToWpFile(String wpFilename, String wpWord){
		try {
			System.out.println("Attempting to append " + wpWord + " to file " + wpFilename);
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(wpFilename, true)));
			out.println("");
			out.print(wpWord);
			out.close();
			System.out.println("Finished appending");
		} 
		catch (IOException e2) {
			e2.printStackTrace();
			GiveMessage.errorMessage("Error appending to wordpool file! Check files for damage.");
		}
	}

	public static void WriteToWpFileLine(String wpFilename, String wpWord, int wpLine){
		try {
			Path path = Paths.get(wpFilename);
			List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
			
			if(wpLine < lines.size()){
				lines.add(wpLine, wpWord);
			}
			else{
				lines.add(wpWord);
			}
			Files.write(path, lines, StandardCharsets.UTF_8);
		} 
		catch (IOException e2) {
			e2.printStackTrace();
			GiveMessage.errorMessage("Error appending to wordpool file! Check files for damage.");
		}
	}
	
	public static void writeSpans() {
		if(UpdatingAction.getStamps().size() > 0) {
			ArrayList<ArrayList<Long>> spans = new ArrayList<ArrayList<Long>>();
			
			Long[] stamps = UpdatingAction.getStamps().toArray(new Long[] {});
			Arrays.sort(stamps);

			long start = 0L;
			long end = 0L;
			for(long stamp: stamps) {
				if(stamp - end > Constants.timeout) {
					if(start > 0 && end > start) {
						ArrayList<Long> nSpan = new ArrayList<Long>();
						nSpan.add(start);
						nSpan.add(end);
						spans.add(nSpan);
					}
					start = stamp;
					end = stamp;
				}
				else {
					end = stamp;
				}
			}
			if(start > 0 && end > start) {
				ArrayList<Long> nSpan = new ArrayList<Long>();
				nSpan.add(start);
				nSpan.add(end);
				spans.add(nSpan);
			}
			
			UpdatingAction.getStamps().clear();
			
			for(ArrayList<Long> span: spans) {
				String toWrite = "Span: " + span.get(0) + "-" + span.get(1);
				try {
					AnnotationFileParser.addField(getOutputFile(), obfuscate(toWrite));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	

	/**
	 * <code>AnnotateActions</code> are enabled anytime audio is open and not playing.
	 */
	@Override
	public void update() {
		if(CurAudio.audioOpen()) {
			if(CurAudio.getPlayer().getStatus() == PrecisionPlayer.Status.PLAYING) {
				setEnabled(false);
			}
			else {
				setEnabled(true);
			}
		}
		else {
			setEnabled(false);
			WordpoolDisplay.clearText();
		}
	}
}
