
import ij.gui.*;

import java.awt.*;
public class CellBodyROI {
	OvalRoi thisROI;
	OvalRoi thisROIProj;
	Overlay thisOverlay;
	int thisSlice;
	/**
	 * @param args
	 */
	public CellBodyROI (OvalRoi ovalIn,Overlay overlayIn,int curSlice) {
		this.thisROI = new OvalRoi(ovalIn.getBounds().x,ovalIn.getBounds().y,(int)ovalIn.getBounds().getWidth(),(int)ovalIn.getBounds().getHeight());
		this.thisROIProj = new OvalRoi(ovalIn.getBounds().x,ovalIn.getBounds().y,(int)ovalIn.getBounds().getWidth(),(int)ovalIn.getBounds().getHeight());
		this.thisOverlay = overlayIn;
		this.thisSlice = curSlice;
		// Add a shaded patch to the image.
		overlayIn.setFillColor(new Color(255,0,0));
		overlayIn.add(this.thisROIProj);
	}
	
	public void delete(){
		// remove the shaded patch...
		this.thisOverlay.remove(thisROIProj);
	}
	@Override
	public String toString(){
		return "["+this.thisROI.getBounds().x+","+this.thisROI.getBounds().y+","+thisSlice+"], ["+this.thisROI.getBounds().getWidth()+","+this.thisROI.getBounds().getHeight()+"]";
	}
	public Rectangle getBounds(){
		return thisROI.getBounds();
	}
}
