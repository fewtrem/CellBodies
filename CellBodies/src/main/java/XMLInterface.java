import ij.IJ;
import ij.io.OpenDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.Element;



public class XMLInterface {
	public static final String OUTPUTENCODING = "UTF-8";
	private static final String ELEMENTNAME = "image";
	public static final String ATT_NAME = "name";
	public static final String ATT_CHANNEL = "channel";
	public static final String ELEM_ROI = "cbRoi";
	public static final String ATT_X = "x";
	public static final String ATT_Y = "y";
	public static final String ATT_WIDTH = "width";
	public static final String ATT_HEIGHT = "height";
	/**
	 * @param args
	 * @return 
	 */
	public static void SaveXML(ArrayList<ListBox<CellBodyROI>> overallStore){
		// Make a new document:
		Document doc = null;
		try{
		OutputStreamWriter errorWriter = new OutputStreamWriter(System.err,
				OUTPUTENCODING);
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		docBuilder.setErrorHandler(new MyErrorHandler (new PrintWriter(errorWriter, true)));
		doc = docBuilder.newDocument();
		}
		catch(Exception e){
			IJ.log("Exception: "+e.getMessage());
		}
		for (ListBox<CellBodyROI> thisList : overallStore){
			Element CBElement = doc.createElement(ELEMENTNAME);
			CBElement.setAttribute(ATT_NAME,thisList.getFileName());
			CBElement.setAttribute(ATT_CHANNEL,thisList.getChannel());
			for (CellBodyROI thisCBR :thisList){
				Element CBRElement = doc.createElement(ELEMENTNAME);
				CBRElement.setAttribute(ATT_X,Integer.toString(thisCBR.getBounds().x));
				CBRElement.setAttribute(ATT_Y,Integer.toString(thisCBR.getBounds().y));
				CBRElement.setAttribute(ATT_WIDTH,Double.toString(thisCBR.getBounds().getWidth()));
				CBRElement.setAttribute(ATT_HEIGHT,Double.toString(thisCBR.getBounds().getHeight()));
			}
			doc.appendChild(CBElement);
		}
		// Save it:
	    try {
	    	JFrame SimpleFileChooser = new JFrame();
	    	JFileChooser chooser = new JFileChooser();
	        int option = chooser.showSaveDialog(SimpleFileChooser);
	        if (option == JFileChooser.APPROVE_OPTION) {
	          if (chooser.getSelectedFile()!=null){
	        	  Transformer tr = TransformerFactory.newInstance().newTransformer();
	  	        tr.setOutputProperty(OutputKeys.INDENT, "yes");
	  	        tr.setOutputProperty(OutputKeys.METHOD, "xml");
	  	        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	  	        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	  	        // send DOM to file
	  	        tr.transform(new DOMSource(doc), 
	  	                             new StreamResult(new FileOutputStream(chooser.getSelectedFile())));
	          }
	        }
	    } catch (TransformerException te) {
	        System.out.println(te.getMessage());
	    } catch (IOException ioe) {
	        System.out.println(ioe.getMessage());
	    }
	}
	private static class MyErrorHandler implements ErrorHandler {
	    private PrintWriter out;

	    MyErrorHandler(PrintWriter out) {
	        this.out = out;
	    }

	    private String getParseExceptionInfo(SAXParseException spe) {
	        String systemId = spe.getSystemId();
	        if (systemId == null) {
	            systemId = "null";
	        }

	        String info = "URI=" + systemId + " Line=" + spe.getLineNumber() +
	                      ": " + spe.getMessage();
	        return info;
	    }

	    public void warning(SAXParseException spe) throws SAXException {
	        out.println("Warning: " + getParseExceptionInfo(spe));
	    }
	        
	    public void error(SAXParseException spe) throws SAXException {
	        String message = "Error: " + getParseExceptionInfo(spe);
	        throw new SAXException(message);
	    }

	    public void fatalError(SAXParseException spe) throws SAXException {
	        String message = "Fatal Error: " + getParseExceptionInfo(spe);
	        throw new SAXException(message);
	    }
	}
}
