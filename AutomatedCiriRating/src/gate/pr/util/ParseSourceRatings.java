/**
 * 
 */
package gate.pr.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;

import gate.pr.dao.MySQLDBConnection;
import gate.util.Out;

/**
 * @author Josh
 *
 */
public class ParseSourceRatings {

	//CSV file header
    private static final String [] FILE_HEADER_MAPPING = {"CTRY","YEAR","CIRI","COW","POLITY","UNCTRY","UNREG","UNSUBREG","PHYSINT","DISAP"
    		,"KILL","POLPRIS","TORT","OLD_EMPINX","NEW_EMPINX",
    	"ASSN","FORMOV","DOMMOV","OLD_MOVE","SPEECH","ELECSD","OLD_RELFRE","NEW_RELFRE","WORKER","WECON","WOPOL","WOSOC","INJUD"};
	
/*    private static final String [] FILE_HEADER_MAPPING = {"CTRY","YEAR","PHYSINT","DISAP","KILL","POLPRIS","TORT","NEW_EMPINX",
    	"ASSN","FORMOV","DOMMOV","SPEECH","ELECSD","NEW_RELFRE","WORKER","WECON","WOPOL","INJUD"};
*/
    //Country Attributes
    private static final String COUNTRY_CTRY = "CTRY";
    private static final String COUNTRY_YEAR = "YEAR";
    private static final String COUNTRY_CIRI = "CIRI";
    private static final String COUNTRY_COW = "COW";
    private static final String COUNTRY_POLITY = "POLITY";
    private static final String COUNTRY_UNCTRY = "UNCTRY";
    private static final String COUNTRY_UNREG = "UNREG";
    private static final String COUNTRY_UNSUBREG = "UNSUBREG";
    private static final String COUNTRY_PHYSINT = "PHYSINT";
    private static final String COUNTRY_DISAP = "DISAP";
    private static final String COUNTRY_KILL = "KILL";
    private static final String COUNTRY_POLPRIS = "POLPRIS";
    private static final String COUNTRY_TORT = "TORT";
    private static final String COUNTRY_OLD_EMPINX = "OLD_EMPINX";
    private static final String COUNTRY_NEW_EMPINX = "NEW_EMPINX";
    private static final String COUNTRY_ASSN = "ASSN";
    private static final String COUNTRY_FORMOV = "FORMOV";
    private static final String COUNTRY_DOMMOV = "DOMMOV";
    private static final String COUNTRY_OLD_MOVE = "OLD_MOVE";
    private static final String COUNTRY_SPEECH = "SPEECH";
    private static final String COUNTRY_ELECSD = "ELECSD";
    private static final String COUNTRY_OLD_RELFRE = "OLD_RELFRE";
    private static final String COUNTRY_NEW_RELFRE = "NEW_RELFRE";
    private static final String COUNTRY_WORKER = "WORKER";
    private static final String COUNTRY_WECON = "WECON";
    private static final String COUNTRY_WOPOL = "WOPOL";
    private static final String COUNTRY_WOSOC = "WOSOC";
    private static final String COUNTRY_INJUD = "INJUD";
    
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		FileReader fileReader = null;
		
		String fileName = "C:/Users/Josh/Desktop/Thesis2015/CIRI Data 1981_2011 2014.04.14.csv";

		CSVParser csvFileParser = null;
		
		try{	
			
        	MySQLDBConnection dbConnection = new MySQLDBConnection();
        	
			//Create the CSVFormat object with the header mapping
	        CSVFormat csvFileFormat = CSVFormat.RFC4180.withHeader(FILE_HEADER_MAPPING);
	        		
	        //initialize FileReader object
	        fileReader = new FileReader(fileName);  
	        
	        csvFileParser = new CSVParser(fileReader, csvFileFormat);
	        
	        //Get a list of CSV file records
	        List<CSVRecord> csvRecords = csvFileParser.getRecords();
	        
	        //Read the CSV file records starting from the second record to skip the header
			        
	        for(int i = 1; i < csvRecords.size(); i++){
	        	
	        	CSVRecord record = (CSVRecord) csvRecords.get(i);
	        		        
	        	String country = record.get(COUNTRY_CTRY);
	        	int year = Integer.parseInt(record.get(COUNTRY_YEAR));
	        	String countryYearId = country + "_" + year;
	        	
	        	Map<String, Object> countryMap = new TreeMap<String, Object>();

	        	for(int j = 0; j < FILE_HEADER_MAPPING.length; j++){
	        		
		        	countryMap.put(FILE_HEADER_MAPPING[j], record.get(FILE_HEADER_MAPPING[j]));
		        			        	
	        	}
	        	
	        	dbConnection.createSourceTable(countryMap, countryYearId);
	        	
	        	if(i == 2){
	        		break;
	        	}

	        }
	        	
	        	
        }catch (Exception e) {
        	System.out.println("Error in CsvFileReader !!!");
            e.printStackTrace();           
        } finally {
        	
        	try {
                fileReader.close();
                csvFileParser.close();
            } catch (IOException e) {
            	System.out.println("Error while closing fileReader/csvFileParser !!!");
                e.printStackTrace();
            }
        	
        }
	
	}

}
