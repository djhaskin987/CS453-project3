/*
 * Created on Aug 26, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author rajiv
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

import java.io.*;
import java.util.StringTokenizer;
import java.util.HashSet;
//import utils.DataBase;

public class StopWords {

	static HashSet<String> StopWords = new HashSet<String>();
	static HashSet<String> validWords = new HashSet<String>();

	public StopWords(String StopwordsFileName){
		try {
			// Read the unorder file in
			BufferedReader in =
                new BufferedReader(new FileReader(StopwordsFileName));
			StringBuffer str = new StringBuffer();
			String nextLine = "";
			while ((nextLine = in.readLine()) != null)
				str.append(nextLine+"\n");
			in.close();
			//save it to a bin tree.
			StringTokenizer st = new StringTokenizer(str.toString());
			while(st.hasMoreTokens()){
				nextLine = st.nextToken();
				if(nextLine.matches("[a-zA-Z'.]*"))
					StopWords.add(nextLine.trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public HashSet<String> getStopWords(){return StopWords;}
	public void setStopWords(HashSet<String> words){StopWords=words;}
	public boolean contains(String word){
		return StopWords.contains(word.toLowerCase().trim());
	}
}
