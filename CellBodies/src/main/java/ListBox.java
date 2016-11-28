import java.util.ArrayList;
import java.awt.*;

import javax.swing.JPanel;

public class ListBox<T> extends ArrayList<T> {
	/**
	 * 
	 */
	private String channel;
	private String fileName;
	
	private static final long serialVersionUID = 1L;
	List awtList;
	/**
	 * @param args
	 */
	public ListBox(String fileName,String channel){
		super();
		this.channel = channel;
		this.fileName = fileName;
		awtList = new List(10);
	}
	public T remove(int index){
		this.awtList.remove(index);
		//this.awtList.update(g);
		return super.remove(index);	
	}
	
	public void clear(){
		awtList.removeAll();
		super.clear();
	}

	public boolean add(T inOne){
		boolean done = super.add(inOne);
		if (done == true){
			this.awtList.add(inOne.toString());
		}
		//this.awtList.update(g);
		return done;
		
	}
	
	public List getAWTList(){
		return awtList;
	}
	
	public String getChannel(){
		return this.channel;
	}
	
	public String getFileName(){
		return this.fileName;
	}
}
