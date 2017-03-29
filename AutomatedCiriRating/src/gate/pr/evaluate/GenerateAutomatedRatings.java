/**
 * 
 */
package gate.pr.evaluate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;

import gate.AnnotationSet;
import gate.Document;
import gate.DocumentContent;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.corpora.DocumentContentImpl;
import gate.creole.ResourceInstantiationException;
import gate.gui.MainFrame;
import gate.pr.dao.MySQLDBConnection;
import gate.util.Out;

/**
 * @author Josh
 *
 */
public class GenerateAutomatedRatings {
	
	
	//create source document - done
	//create automated document
		//need to finish patterns
		//need to map and store to database
	//option: put all created documents to external file and call later for specific file
	
	public static void main(String[] args) throws Exception {
		
		//GATE setup
		Gate.init(); //prepare library
		
		//show main window
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run(){
				MainFrame.getInstance().setVisible(true);
			}
		});
		
		
		
		getAutomatedRatings();
		//getSourceRatings();
		
   }

	private static void getAutomatedRatings() throws Exception{

		//create source doc
		//create automated doc
		
		//create source anno set
		//create automated anno set
		
		//get source map from database
		MySQLDBConnection dbConnection = new MySQLDBConnection();
		
		List<Map<String, Object>> sourceList = dbConnection.readSourceTable();
			
		List<Map<String, Object>> automatedList = dbConnection.readAutomatedTable();
		
		//get automated map from database
		
		//parse maps
						
		String resourceClassName = "gate.corpora.DocumentImpl";
		
		Document sourceDoc = null;
		Document automatedDoc = null;
		DocumentContent documentContent = null;
		
		ArrayList<Document> docList = new ArrayList<Document>();
		ArrayList<String> docAdded = new ArrayList<String>();
		
		StringBuilder text = new StringBuilder();
		String previous = "";
		
		for(int i = 0; i < automatedList.size(); i++){
			
			TreeMap<String, Object> automatedMap = (TreeMap<String, Object>) automatedList.get(i);
			
			String automatedCountryYear = (String) automatedMap.get("country_year");
			
			for(int j = 0; j < sourceList.size(); j++){
				
				TreeMap<String, Object> sourceMap = (TreeMap<String, Object>) sourceList.get(j);
				
				String sourceCountryYear = (String) sourceMap.get("country_year");
				
				if(automatedCountryYear.equals(sourceCountryYear)){
					
					StringBuilder startText = new StringBuilder();
					
					for(Map.Entry<String, Object> entry : sourceMap.entrySet()){
						
						//String key = StringUtils.substringBefore(entry.getKey(), "_rating");
						String key = entry.getKey().toUpperCase();
						
						if("unctry_rating".equalsIgnoreCase(key))
							key = "UNCOUNTRY_RATING";
						
						startText.append(key + " ");
						String subStr = "";
						
						if("ctry_rating".equalsIgnoreCase(key)){
							
							subStr = StringUtils.substringBefore(startText.toString(), "CTRY_RATING");
							
							String current = (String) entry.getValue();
							
							if(!docAdded.contains(current)){
								docAdded.add(current);
								sourceDoc = (Document) Factory.createResource(resourceClassName);
								sourceDoc.setName("SourceEval_" + current);
								docList.add(sourceDoc);
								
								automatedDoc = (Document) Factory.createResource(resourceClassName);
								automatedDoc.setName("AutomatedEval_" + current);
								docList.add(automatedDoc);
								
								
							}
							
							if((!previous.isEmpty() && !current.isEmpty()) && !current.equals(previous)){
								
								text = new StringBuilder();
								text = text.append(subStr);
								
							}
							
							previous = current;

						}
									
						text.append(key.toUpperCase() + " ");
						
						Map.Entry<String, Object> lastEntry = sourceMap.lastEntry();

						if(lastEntry.getKey().equals(entry.getKey())){
							
							text.append("\n");
						}
						
					}
					
					
				}

				
			}

			documentContent = new DocumentContentImpl(text.toString());
			sourceDoc.setContent(documentContent);
			automatedDoc.setContent(documentContent);
			
		}
		
/*		Out.prln(System.getProperty("user.dir"));
		
		Path path = Paths.get(System.getProperty("user.dir") + "/documents");
		
		if(!Files.exists(path)){
			
			try{
				Files.createDirectories(path);
				
			}catch(IOException e){
				e.printStackTrace();
			}
			
		}*/
		
		//loop to get docs from list

		for(Document document : docList){
			
			AnnotationSet automatedAS = document.getAnnotations("CiriRatings_Source");
			AnnotationSet sourceAS  = document.getAnnotations("CiriRatings_Source");
			FeatureMap params = null;
			String content = document.getContent().toString();
			
			String automatedCountryYear = "";
			
			int automatedCount = 1;
			
			for(int i = 0; i < automatedList.size(); i++){
				
				TreeMap<String, Object> map = (TreeMap<String, Object>) automatedList.get(i);
				
				automatedCountryYear = (String) map.get("country_year");
				
				boolean found = false;
				
				for(Entry<String, Object> entry : map.entrySet()){
					
					String countryName = (String) map.get("ctry_rating");
					String docName = "AutomatedEval_" + countryName;
					
					if(docName.equals(document.getName())){
						
						found = true;
						params = Factory.newFeatureMap();
						params.put(entry.getKey(), entry.getValue());
						
						//String key = StringUtils.substringBefore(entry.getKey(), "_rating").toUpperCase();
						String key = entry.getKey().toUpperCase();
						
						if("unctry_rating".equalsIgnoreCase(key))
							key = "UNCOUNTRY_RATING";
						
						long start = StringUtils.ordinalIndexOf(content, key, automatedCount);
						long length = key.length();
						long end = start + length;
						
						automatedAS.add(start, end, (String)map.get("country_year"), params);
						
					}
					
				}
				
				for(int j = 0; j < sourceList.size(); j++){
					
					TreeMap<String, Object> sourceMap = (TreeMap<String, Object>) sourceList.get(j);
					
					String sourceCountryYear = (String) sourceMap.get("country_year");

					if(automatedCountryYear.equals(sourceCountryYear)){
						
						for(Entry<String, Object> entry : sourceMap.entrySet()){
							
							String countryName = (String) sourceMap.get("ctry_rating");
							String docName = "SourceEval_" + countryName;
							
							if(docName.equals(document.getName())){
								
								found = true;
								params = Factory.newFeatureMap();
								params.put(entry.getKey(), entry.getValue());
						        
								//String key = StringUtils.substringBefore(entry.getKey(), "_rating").toUpperCase();
								String key = entry.getKey().toUpperCase();
								
								if("unctry_rating".equalsIgnoreCase(key))
									key = "UNCOUNTRY_RATING";

								long start = StringUtils.ordinalIndexOf(content, key, automatedCount);
								if(start == -1){
									continue;
								}
									
								long length = key.length();
								long end = start + length;
								Out.prln("start: " + start + " end: " + end + " key " + key);
								sourceAS.add(start, end, (String)map.get("country_year"), params);
								
							}
							
						}
						
					}
					
				}

				if(found)
					automatedCount++;
				
			}		
			
		}
		
		//then use Annotation diff tool after process complete
		
	}
}
