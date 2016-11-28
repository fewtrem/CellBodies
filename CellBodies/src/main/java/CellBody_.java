
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.OpenDialog;
import ij.io.Opener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import ij.plugin.frame.PlugInFrame;
import java.awt.Color;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
public class CellBody_ extends PlugInFrame implements MouseListener{
	// Constants:
	private static final long serialVersionUID = 1L;
	// For max projection thresholds:
	public static final int THRESHOLD = 15;
	// Channel constants:
	public static final String[] CHANNELS = {"Red","Green"};
	public static final int[] CHANNELINTS = {16,8};
	public static final int[] CHANNELXS = {0xff0000,0x00ff00};
	// The store:
	ArrayList<ListBox<CellBodyROI>> overallStore;
	// The list box
	ListBox<CellBodyROI> ROIList;
	// files we will use:
	ArrayList<String> filesToOpen;
	// store current stuff:
	// current file in list int:
	int curFile = 0;
	// current channel in that file:
	int curChannel;
	// current raw imageopened:
	ImagePlus curImage;
	// tools:
	Overlay projImageOverlay;
	Overlay mainImageOverlay;
	OvalRoi lastROI;
	ImagePlus theViewer;
	ImagePlus thisProjection;
	JPanel listHolder;
	String thisFilePath;
	// 
	boolean doOne;
	
	// Constructor:
	public CellBody_(){
		// instantiate:
		super("Cell Body Plugin");
		overallStore = new ArrayList<ListBox<CellBodyROI>>();
		this.ROIList = null;
		this.projImageOverlay = new Overlay();
		this.mainImageOverlay = new Overlay();
		theViewer = new ImagePlus();
		ImageListener newListner = new ImageListener(){
			public void imageUpdated(ImagePlus thisIm){
				updateMainOverlay();
			}
			public void imageClosed(ImagePlus thisIm){}
			public void imageOpened(ImagePlus thisIm){}
		};
		ImagePlus.addImageListener(newListner);
		thisProjection = new ImagePlus();
		doGUI();
	}
	
	//GUI:
	public void doGUI(){
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
	    Button curButton = new Button("Next");
	    curButton.addActionListener (new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
		    	nextImage();
	    	}
	    });
	    add(curButton);
	    curButton = new Button("Delete");
	    curButton.addActionListener (new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
		    	deleteROI();
	    	}
	    });
	    add(curButton);
	    curButton = new Button("Add extra ROI");
	    curButton.addActionListener (new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
		    	addWithoutCheck();
	    	}
	    });
	    add(curButton);
	    curButton = new Button("Save");
	    curButton.addActionListener (new ActionListener(){
	    	public void actionPerformed(ActionEvent e){
		    	saveToFile();
	    	}
	    });
	    add(curButton);
	    listHolder = new JPanel();
	    add(listHolder);
		pack();
		GUI.center(this);
		this.setVisible(true);
	}
	
	// The run routine:
	public void run(String arg) {
		// open a list of images to look at.
	    // Tool window allows someone to see z projection and imagename...
	    // Need a next button
		// need to be able to remove drawn ovals, so use a listbox.
		// get the current image
		//OpenDialog whatIsFile = new OpenDialog("Select the xml document","");
		//String dir = whatIsFile.getDirectory();
		//String fN = whatIsFile.getFileName();
		//TODO:
		String dir = "/home/s1144899/Desktop/DICE/PhD/Python Projects/CellBodies";
		String fN = "a.txt";
		if (dir!=null&&fN!=null){
			File toGet = new File(dir,fN);
			String pathToOpen = toGet.toString();
			filesToOpen = new ArrayList<String>();
			try {
				BufferedReader br = new BufferedReader(new FileReader(pathToOpen));
			    String line;
			    while ((line = br.readLine()) != null) {
			       // process the line.
			    	filesToOpen.add(line);
			    }
			    br.close();
			}
			catch (Exception e){
				IJ.log("Error: "+e.getMessage());
			}
			// default oldROI
			this.lastROI = null;
			curFile = -1;
			curChannel = 1;
			
			doOne = true;
			if (filesToOpen.size()==0) doOne=false;
			loadImage();
			theViewer.getCanvas().addMouseListener(this);
		}
		return;
	}
	
	// Update the overlay for the main image:
	public void updateMainOverlay(){
		// clear it:
		this.mainImageOverlay.clear();
		// add relevant ROIs:
		for (CellBodyROI thisCBROI : ROIList){
			int theDif = thisCBROI.thisSlice-theViewer.getCurrentSlice();
			if (theDif<3&&theDif>-3){
				mainImageOverlay.setFillColor(new Color(255,100+(theDif*50),100-(theDif*50),50));
				this.mainImageOverlay.add(thisCBROI.thisROI);
			}
		}
	}
	
	// When a ROI is added, check to see if it is the same as the last one or not...
	public void checkROIAdd(){
		Roi roi = this.theViewer.getRoi();
		if (roi != null){
		    if (roi.getType() == Roi.OVAL){
			    OvalRoi thisOval = (OvalRoi)roi;
		    	if (thisOval != lastROI){
		    		addROI(thisOval);
		    	}
		    }
		}
    }
	
	// Add ROI:
	public void addROI(OvalRoi thisOval){
		int curSlice = theViewer.getCurrentSlice();
		if (thisOval != null){
			if (thisOval.getBounds().getWidth() != 0.0 && thisOval.getBounds().getHeight()!=0.0){
				IJ.log("Adding "+thisOval.getBounds().toString());
				ROIList.add(new CellBodyROI(thisOval,projImageOverlay,curSlice));
			}
		}
		theViewer.updateAndDraw();
		thisProjection.repaintWindow();
		lastROI = thisOval;	
	}
		
	// Add ROI button:
	public void addWithoutCheck(){
		Roi roi = this.theViewer.getRoi();
		if (roi != null){
		    if (roi.getType() == Roi.OVAL){
			    OvalRoi thisOval = (OvalRoi)roi;
		    	addROI(thisOval);
		    }
			else{
				IJ.log("Need an oval ROI first...");
			}
		}
		else{
			IJ.log("Need an oval ROI first...");
		}
	}
	
	// Delete button:
	public void deleteROI(){
		ROIList.remove(ROIList.getAWTList().getSelectedIndex()).delete();
		theViewer.updateAndDraw();
		thisProjection.repaintWindow();
	}
	
	// Next button:
	// when the image is done, save the oval co-ords in a text file for import into python later...
	public void nextImage(){
		//TODO: Add save in here and wipe list and close other images:
		loadImage();
		
	}
	
	// Save progress so far:
	public void saveToFile(){ 
		JFileChooser fileChooser = new JFileChooser();
		if (fileChooser.showSaveDialog(CellBody_.this) == JFileChooser.APPROVE_OPTION) {
		  File file = fileChooser.getSelectedFile();
		  // save to file (copy in XML)
		}
	}

	
	// Add current button:

	
	public void loadImage(){
		Opener opener = new Opener();
		if (curChannel==0){
			curChannel+=1;
		}
		else{
			curChannel=0;
			curFile+=1;
			if (curFile<this.filesToOpen.size()){
				thisFilePath = this.filesToOpen.get(curFile);
				this.curImage = opener.openImage(thisFilePath);				
			}
			else{
				IJ.showMessage("All images done");
				theViewer.hide();
				thisProjection.hide();
				doOne = false;
			}
		}
		if (doOne==true){
			ROIList = new ListBox<CellBodyROI>(thisFilePath,CHANNELS[curChannel]);
			overallStore.add(ROIList);
			int thisChannel=CHANNELINTS[curChannel];
			int concat = CHANNELXS[curChannel];
			ImageStack thisStack = curImage.getStack();
			ImageStack theStack = new ImageStack(thisStack.getWidth(),thisStack.getHeight(),thisStack.getSize());
			ColorProcessor meanProc = new ColorProcessor(thisStack.getWidth(), thisStack.getHeight());
			//ImagePlus thisProjection = new ImagePlus("Projection",cp.createProcessor(thisStack.getWidth(), thisStack.getHeight()));
			//ImageProcessor meanProc = thisProjection.getProcessor();
			double multFactor = (1.0/(double)thisStack.getSize());
			int[] total = new int[thisStack.getWidth()*thisStack.getHeight()];
			int[] freq = new int[thisStack.getWidth()*thisStack.getHeight()];
			for(int a = 0;a<thisStack.getSize();a++){
				int[] toAdd = (int[])thisStack.getPixels(a+1);
				int[] toSee = new int[thisStack.getWidth()*thisStack.getHeight()];
				for (int i = 0; i < toAdd.length; ++i) {
					int thisAdd = (int)(toAdd[i] & concat)>>thisChannel;
					if (thisAdd>=THRESHOLD){
						total[i] += thisAdd*a;
						freq[i] += thisAdd;
					}
					toSee[i] = thisAdd;
				}
				theStack.setPixels(toSee,a+1);
			}
			for (int i = 0; i < total.length; ++i) {
				meanProc.setColor(new Color(0,0,0));
				if(freq[i]>0){
					double factor = (((double)total[i]/(double)freq[i])*multFactor);
					int newRGB = Color.HSBtoRGB((float)factor,1.0f,0.5f);
					//IJ.log(Integer.toString(factor));
					//meanProc.set(i, (newRGB[0]<<16)+(newRGB[1]<<8)+(newRGB[0]));
					meanProc.set(i,newRGB);
				}
			}
			thisProjection.setProcessor(meanProc);
			theViewer.setStack(filesToOpen.get(curFile),theStack);
			theViewer.setOverlay(this.mainImageOverlay);
			thisProjection.setOverlay(this.projImageOverlay);
			listHolder.removeAll();
		    listHolder.add(ROIList.getAWTList());
			//TODO:
			addROI(new OvalRoi(0,0,300,300));
			addROI(new OvalRoi(100,300,10,150));
			theViewer.show();
			thisProjection.show();
			this.pack();
		}
	}
	
    public void mousePressed(MouseEvent e) { //checkROIAdd(); 
    	
    } 
    
    	
    public void mouseDragged(MouseEvent e) { //checkROIAdd(); 
    	
    }
    
    public void mouseClicked(MouseEvent e) { //checkROIAdd(); 
    
    }
    
    public void keyPressed(KeyEvent e) { //checkROIAdd(); 
    
    }

	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		
	}
	public void close(){
		if (theViewer.isVisible()==true){
			this.theViewer.getCanvas().removeMouseListener(this);
			this.mainImageOverlay.clear();
			
		}
		super.close();
	}
}
