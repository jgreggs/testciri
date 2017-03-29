/*
 *  AutomateCiriRating.java
 *
 * Copyright (c) 2000-2012, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 3, 29 June 2007.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  Josh, 29/3/2017
 *
 * For details on the configuration options, see the user guide:
 * http://gate.ac.uk/cgi-bin/userguide/sec:creole-model:config
 */

package gate.pr.automate;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.SentimentOptions;

import gate.*;
import gate.creole.*;
import gate.creole.metadata.*;
import gate.pr.dao.MySQLDBConnection;
import gate.pr.util.ReportConstants;
import gate.util.*;
import gate.wordnet.WordNetException;


/** 
 * This class is the implementation of the resource AUTOMATEDCIRIRATING.
 */
@CreoleResource(name = "AutomatedCiriRating",
        comment = "Processing Resource to calculate ratings for U.S Country Reports.")
public class AutomateCiriRating  extends AutomateCiriRatingModel
  implements ProcessingResource {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5194359849044535340L;	   

	  private static final String ENDPOINT = "https://gateway.watsonplatform.net/natural-language-understanding/api";
	  private static final String USERNAME= "fc484ee1-1113-4627-9f68-e30c2e0717c4";
	  private static final String PASSWORD = "s7nMoX8DTCcH";
	 
	  NaturalLanguageUnderstanding service;
	  
	  public void initNLU(){
		  
		  service = new NaturalLanguageUnderstanding(NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27);
		  service.setEndPoint(ENDPOINT);
		  service.setUsernameAndPassword(USERNAME, PASSWORD);
		  
	  }
	  	/** 
		 *  Should be called to execute this PR on a document.
		 *  Placed after JAPE Process Resource(s)
	  	 * @throws ExecutionException 
		 */
	  	@Override
	  	public void execute() throws ExecutionException{
	  		
	  		initNLU();
	  		
			AnnotationSet inputAS = getInputASName() == null || getInputASName().trim().length() == 0 ? 
					document.getAnnotations() : document.getAnnotations(getInputASName());
								
			AnnotationSet outputAS = getOutputASName() == null || getOutputASName().trim().length() == 0 ? 
					document.getAnnotations() : document.getAnnotations(getOutputASName());
					
			AnnotationSet sectionHeadingSet = inputAS.get("Ciri_SubSection");
			
			Annotation firstAnn = null;
			Annotation lastAnn = null;
			
			Node start = null;
			Node end = null;
			
			List<Annotation> annList = new ArrayList<>((AnnotationSet) sectionHeadingSet);
			
			Collections.sort(annList, new OffsetComparator());
		
			int found = 0;
			
			for(Iterator<Annotation> iter = annList.iterator(); iter.hasNext();){

				if(start == null){
					firstAnn = (Annotation) iter.next();
				}else{
					firstAnn = lastAnn;
				}
				
				String text = gate.Utils.stringFor(document, firstAnn);
				
				if(text.equalsIgnoreCase(ReportConstants.EXECUTIVE_SUMMARY)){
					found++;

					if(found >= 2){

						break;
						
					}
					
				}
				
				start = firstAnn.getStartNode();
								
				if(iter.hasNext()){
				
					lastAnn = (Annotation) iter.next();
				
					end = lastAnn.getStartNode();
				}
				
				features.put("rule", "extract");
				
				outputAS.add(start, end, "Ciri_ReportSection", features);

			}
			
			TreeMap<String, Object> ratingsMap = new TreeMap<String, Object>();
			
			AnnotationSet newReportSectionSet = outputAS.get("Ciri_ReportSection");
			
			int disapRatingValue = 0;
			int tortRatingValue = 0;
			int polprisRatingValue = 0;
			int killRatingValue = 0;
			int assnRatingValue = 0;
			int formovRatingValue = 0;
			int dommovRatingValue = 0;
			int speechRatingValue = 0;
			int elecsdRatingValue = 0;
			int newrelfreRatingValue = 0;
			int workerRatingValue = 0;
			int weconRatingValue = 0;
			int wopolRatingValue = 0;
			int wesocRatingValue = 0;
			int injudRatingValue = 0;
			
			for(Annotation anno : newReportSectionSet){

				if(anno != null){
					
					String annoContent = gate.Utils.stringFor(document, anno);
					
	  				/*As the annotation iterates check the report sections.
	  				  Report sections need to match those in CIRI.
	  				  Create conditionals for each report section to each method
	  				*/
					
					try {
						
						//Put Executive Summary condition here for all ratings
						
						if(annoContent.contains(ReportConstants.KILL_SUBSECTION)){
							
	  	  					killRatingValue = calculateOccurrenceRatingValue(anno, inputAS, outputAS);
	  	  					ratingsMap.put(ReportConstants.KILL, killRatingValue);
						
						}if(annoContent.contains(ReportConstants.DISAP_SUBSECTION)){

  	  						disapRatingValue = calculateOccurrenceRatingValue(anno, inputAS, outputAS);
	  	  					ratingsMap.put(ReportConstants.DISAP, disapRatingValue);

						}if(annoContent.contains(ReportConstants.TORT_SUBSECTION)){
							
	  	  					tortRatingValue = calculateOccurrenceRatingValue(anno, inputAS, outputAS);
	  	  					ratingsMap.put(ReportConstants.TORT, tortRatingValue);

						}if(annoContent.contains(ReportConstants.POLPRIS_SUBSECTION)){
						
	  	  					polprisRatingValue = calculateOccurrenceRatingValue(anno, inputAS, outputAS);
	  	  					ratingsMap.put(ReportConstants.POLPRIS, polprisRatingValue);
	  	  					
						}if(annoContent.contains(ReportConstants.ASSN_SUBSECTION)){
							
						assnRatingValue = calculateASSNRating(anno, outputAS);
						ratingsMap.put(ReportConstants.ASSN, assnRatingValue);
						
					}if(annoContent.contains(ReportConstants.FORMOV_SUBSECTION)){
						
						formovRatingValue = calculateFORMOVRating(anno, outputAS);
						ratingsMap.put(ReportConstants.FORMOV, formovRatingValue);
						
					}if(annoContent.contains(ReportConstants.DOMMOV_SUBSECTION)){
						Out.prln("dom");
						dommovRatingValue = calculateDOMMOVRating(anno, outputAS);
						ratingsMap.put(ReportConstants.DOMMOV, dommovRatingValue);
						
					}if(annoContent.contains(ReportConstants.SPEECH_SUBSECTION)){
						
						speechRatingValue = calculateSPEECHRating(anno, outputAS);
						ratingsMap.put(ReportConstants.SPEECH, speechRatingValue);
						
					}if(annoContent.contains(ReportConstants.SECTION_3)){
						
						elecsdRatingValue = calculateELECSDRating(anno, outputAS);
						ratingsMap.put(ReportConstants.ELECSD, elecsdRatingValue);
						
					}if(annoContent.contains(ReportConstants.NEWRELFRE_SUBSECTION)){
						
						newrelfreRatingValue = calculateNEWRELFRERating(anno, outputAS);
						ratingsMap.put(ReportConstants.NEWRELFRE, newrelfreRatingValue);
						
					}if(annoContent.contains(ReportConstants.INJUD_SUBSECTION)){
						
						injudRatingValue = calculateIndepJudiciaryRating(anno, outputAS);
						ratingsMap.put(ReportConstants.INJUD, injudRatingValue);
						
					}//if(annoContent.contains(SECTION_6)){
						
						//if(annoContent.contains(WECON_SUBSECTION)){
							weconRatingValue = calculateWomenRightsRating(anno, outputAS);
							ratingsMap.put(ReportConstants.WECON, 1);
						//}
						
						//if(annoContent.contains(WOPOL_SUBSECTION)){
							wopolRatingValue = calculateWomenRightsRating(anno, outputAS);
							ratingsMap.put(ReportConstants.WOPOL, 1);
						//}
					
					//}
					if(annoContent.contains(ReportConstants.SECTION_7)){ 
						
						workerRatingValue = calculateWORKERRating(anno, outputAS);
						ratingsMap.put(ReportConstants.WORKER, workerRatingValue);
						
					}

						
					} catch (ResourceInstantiationException | MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
										
				}
					
			}
			
			Out.prln("storeCIRIRatings****ratingsMap: " + ratingsMap);
			
			try {
				ratingsMap.put("ctry", "Afghanistan");
				ratingsMap.put("year", 1983);
				storeCIRIRatings(ratingsMap);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}					
	  		
	  	}	//end execute

		public String getSubSectionExists(String sentString){

		    String separator_1 = "Country Reports on Human Rights";
		    String separator_2 = "Bureau of Democracy, Human Rights and Labor";
		    
			ArrayList<String> headerList = new ArrayList<String>();
			
			headerList.add(ReportConstants.KILL_SUBSECTION);
			headerList.add(ReportConstants.DISAP_SUBSECTION);
			headerList.add(ReportConstants.TORT_SUBSECTION);
			headerList.add(ReportConstants.POLPRIS_SUBSECTION);

	    	for(String subSection : headerList){
	    		
	    		if(sentString.contains(subSection)){
	    			int subSectionIndex = sentString.indexOf(subSection);
	    			
	    			String replaceSection = sentString.substring(0, subSectionIndex + subSection.length());
	    			sentString = sentString.replace(replaceSection, "");
	    		}
	    		
	    		if(sentString.contains(separator_1) && sentString.contains(separator_2)){
	    		
	    		    int subString_1 = sentString.indexOf(separator_1);
	    		    int subString_2 = sentString.indexOf(separator_2);
	    		    int subLength_2 = separator_2.length();
	    		      
	    		    String replaceSub = sentString.substring(subString_1, subString_2 + subLength_2);
	    		    
	    		    sentString = sentString.replace(replaceSub, "");
	    		}
	    		
			    String regex = "\\b[A-Z]+[\\s]+[0-9]+\\b";
			    Pattern pattern = Pattern.compile(regex);
			      
			    Matcher matches = pattern.matcher(sentString);
			    
	    		if(matches.find()){
	    			
	    			sentString = matches.replaceAll("");
	    		}
	    			
	    	}
	    	
	    	return sentString.trim();
			   
		}
		
		/**
		 * 
		 * Will use IncludeNums and WordNet to get number of occurrences. 
		 * Will also need to include Sentence annotation before IncludeNums for accuracy per sentence.
		 *
		 * @param annotation
		 * @param outputAS
		 * @return
		 * @throws WordNetException 
		 * @throws MalformedURLException 
		 * @throws ResourceInstantiationException 
		 * @throws ExecutionException 
		 */
		public int calculateOccurrenceRatingValue(Annotation annotation, AnnotationSet inputAS, AnnotationSet outputAS) throws MalformedURLException, ResourceInstantiationException, ExecutionException {
			
			int ratingValue = 0;
			int numberValue = 0;
			boolean numeric = false;
			
			double sentimentScore = 0;
			boolean sentenceGreaterThanOne = false;
			
			String sentenceContent = "";
			String sentString = null;
			List<Integer> numberValueList = new ArrayList<Integer>();
			
			Node startAnno = annotation.getStartNode();
			Node endAnno = annotation.getEndNode();
			
			AnnotationSet sentenceSet = outputAS.get("Sentence", startAnno.getOffset(), endAnno.getOffset());
			
			List<Annotation> sentenceList = new ArrayList<Annotation>(Utils.inDocumentOrder(sentenceSet));
									
			int sentenceSize = sentenceList.size();

			AnnotationSet occuranceSet = outputAS.get("IncludeNums", startAnno.getOffset(), endAnno.getOffset());

			for(Annotation annoOccurance : occuranceSet){
				
				String numberString = gate.Utils.stringFor(document, annoOccurance);
				
				numeric = StringUtils.isNumeric(numberString);
				
				if(numeric){
					
					numberValue = Integer.parseInt(numberString);

					numberValueList.add(numberValue);
		
				}
				
			}	
						
			for(int i = 0; i < sentenceSize; i += getNumberOfSentencesInBatch()){
												
				String sentenceContentSet = gate.Utils.stringFor(document, sentenceSet);

				String reports_No = "no reports";
				sentenceContentSet = getSubSectionExists(sentenceContentSet);

				int first = sentenceContent.indexOf(".");
				
				int sentenceContentLength = sentenceContentSet.length();
				if(sentenceContentLength > 2 && sentenceSize == 1){
					
					if(sentenceContentSet.contains(reports_No)){
						ratingValue = 2;
						break;
					}
					
				}else if(sentenceContentSet.length() > 2 && (first >= 0) && (first - sentenceContentSet.lastIndexOf(".")) == 0){
					
					if(sentenceContentSet.contains(reports_No)){
						ratingValue = 2;
						break;
					}
					
				}
				
				int endIndex = i + getNumberOfSentencesInBatch() - 1;
				
				if(endIndex >= sentenceList.size()) 
					endIndex = sentenceList.size() - 1;

				// we add numberOfSentencesInContext in left and right context if
				// they are available
				int contextStartIndex = i - getNumberOfSentencesInContext();

				if(contextStartIndex < 0) 
					contextStartIndex = 0;
				
		      	int contextEndIndex = endIndex + getNumberOfSentencesInContext();
		      	if(contextEndIndex >= sentenceList.size()) {
		      		contextEndIndex = sentenceList.size() - 1;
		      	}
		      
		      	// obtain the string to be annotated
		      	sentString = Utils.stringFor(document, Utils.start(sentenceList.get(contextStartIndex)), Utils.end(sentenceList.get(contextEndIndex)));
		      
			    sentString = getSubSectionExists(sentString);
			    
			    //Out.prln("SentString: \n" + sentString);
			    
			    AnalysisResults results = getSentiment(sentString);
			    
			    Out.prln("************************" + results.getSentiment().getDocument().getScore());
			    
/*				String result = processString(sentString.toString());
				
				int docSentimentStart = result.indexOf("<docSentiment>");
		
				if(docSentimentStart > -1){
					
					int scoreStartIndex = result.lastIndexOf("<score>");
					int scoreEndIndex = result.indexOf("</score>");
					
					if(scoreStartIndex > -1){
						
						sentimentScore = Double.parseDouble(result.substring(scoreStartIndex + 8, scoreEndIndex));
						//Out.prln(sentimentScore);
					}else{
						ratingValue = 2;
						break;
					}

				}*/
			    
			    sentenceGreaterThanOne = true;

			}
			
			if(sentenceGreaterThanOne){
			    
			    String reports_No = "no reports";
			    
				String reports_Occasional = "occasion";
				String reports_However = "however";
				String reports_But = "but";
				
				String reports_yes = "were reports";
				
				String reports_some = "were some reports";
				
				String reports_widespread = "widespread";
				String reports_systematic = "systematic";
				String reports_numerous = "numerous";
				String reports_credible = "credible reports";
				
				if(sentString.contains(reports_credible)){
					ratingValue = 1;
					if(sentString.contains("government")){
						ratingValue = 0;
					}
					
				}else if(sentString.contains(reports_widespread) || sentString.contains(reports_systematic)){
					//Out.prln("wide_1");
					ratingValue = 0;
				}else if(sentString.contains(reports_numerous)){
					ratingValue = 1;
				}else if(sentString.contains(reports_yes) || sentString.contains(reports_some)){
					
					if(numeric){
						
						Collections.sort(numberValueList);
						
						int freqValue = 50;
						int lowValue = 0;
												
						for(int value : numberValueList){
							
							if(value < 50 && value > 0){
								ratingValue = 1;
							}else if(value >= 50){
								ratingValue = 0;
							}
						}
						
					}else if(!sentString.contains("government")){
						//Out.prln("wide_2");
						ratingValue = 2;
					}else{
						ratingValue = 1;
					}
						
				}else if(sentString.contains(reports_No) && (sentimentScore < 0.52)){
					//Out.prln("2 value");
					ratingValue = 2;
					
					if(sentString.contains("some") && sentString.contains("reports")){
						//Out.prln("1 value");
						ratingValue = 1;
					}
				}else if(sentString.contains(reports_Occasional) || sentString.contains(reports_But) || sentString.contains(reports_However)){
					ratingValue = 1;
				}else if(!sentString.contains(reports_No) && sentimentScore < 0.52){
					ratingValue = 1;
				}else if(sentimentScore > 0.55){
					//Out.prln("wide_3");
					ratingValue = 1;
				}
			    
			}
			
			return ratingValue;	
			
		}
		
		public int calculateSPEECHRating(Annotation annotation, AnnotationSet outputAS) throws ExecutionException{
		
			int ratingValue = 0;

			double sentimentScore = 0;
			boolean sentenceGreaterThanOne = false;
			
			String sentenceContent = "";
			String sentString = null;
			
			Node startAnno = annotation.getStartNode();
			Node endAnno = annotation.getEndNode();
			
			AnnotationSet sentenceSet = outputAS.get("Sentence", startAnno.getOffset(), endAnno.getOffset());
			
			List<Annotation> sentenceList = new ArrayList<Annotation>(Utils.inDocumentOrder(sentenceSet));
									
			int sentenceSize = sentenceList.size();
						
			for(int i = 0; i < sentenceSize; i += getNumberOfSentencesInBatch()){
														
				int endIndex = i + getNumberOfSentencesInBatch() - 1;
				
				if(endIndex >= sentenceList.size()) 
					endIndex = sentenceList.size() - 1;

				// we add numberOfSentencesInContext in left and right context if
				// they are available
				int contextStartIndex = i - getNumberOfSentencesInContext();

				if(contextStartIndex < 0) 
					contextStartIndex = 0;
				
		      	int contextEndIndex = endIndex + getNumberOfSentencesInContext();
		      	if(contextEndIndex >= sentenceList.size()) {
		      		contextEndIndex = sentenceList.size() - 1;
		      	}
		      
		      	// obtain the string to be annotated
		      	sentString = Utils.stringFor(document, Utils.start(sentenceList.get(contextStartIndex)), Utils.end(sentenceList.get(contextEndIndex)));
		      
			    sentString = getSubSectionExists(sentString);
			    
			    //Out.prln("SentString: \n" + sentString);
		
			    AnalysisResults results = getSentiment(sentString);
			    
			    Out.prln("************************" + results.getSentiment().getDocument().getScore());
		
/*				String result = processString(sentString.toString());
				
				int docSentimentStart = result.indexOf("<docSentiment>");
		
				if(docSentimentStart > -1){
					
					int scoreStartIndex = result.lastIndexOf("<score>");
					int scoreEndIndex = result.indexOf("</score>");
					
					if(scoreStartIndex > -1){
						
						sentimentScore = Double.parseDouble(result.substring(scoreStartIndex + 8, scoreEndIndex));
						//Out.prln(sentimentScore);
					}else{
						ratingValue = 2;
						break;
					}

				}*/
			    
			    sentenceGreaterThanOne = true;

			}
			
			if(sentenceGreaterThanOne){
			    
				String self_censorship = "self-censorship";
				String widespread = "widespread";
				String restricted = "restricted";
				String limited = "may limit";
				String limited_speech = "limit free speech";
				String prohibit = "law prohibits";
				String electoral_prohibit = "electoral law prohibits";
				String generally_respected = "respected";
				String government = "government";
				String no_gov_restrictions = "no government restrictions";
				String public_speech = "public speech";
				String generally_not_respected = "did not respect";
				String control = "control";
				String gov_restrictions = "government restrict";
				
				if(sentString.contains(government)){
					
					if(sentString.contains(no_gov_restrictions) 
							|| sentString.contains(generally_respected)){
						
						ratingValue = 2;
						
						if(sentString.contains(limited) 
								|| sentString.contains(limited_speech) 
								|| (sentString.contains(prohibit) && sentString.contains(public_speech))){
							ratingValue = 1;
						}
						
						
					}else if(sentString.contains(generally_not_respected)){
						ratingValue = 1;
						
						if(sentString.contains(gov_restrictions)){
							ratingValue = 0;
						}
					}
					
				}else if(sentString.contains(widespread) || sentString.contains(self_censorship)){
					ratingValue = 0;
				}
					
			}
				
			return ratingValue;	
			
		}
		
		public int calculateASSNRating(Annotation annotation, AnnotationSet outputAS) throws ExecutionException{
			
			int ratingValue = 0;

			double sentimentScore = 0;
			boolean sentenceGreaterThanOne = false;
			
			String sentenceContent = "";
			String sentString = null;
			
			Node startAnno = annotation.getStartNode();
			Node endAnno = annotation.getEndNode();
			
			AnnotationSet sentenceSet = outputAS.get("Sentence", startAnno.getOffset(), endAnno.getOffset());
			
			List<Annotation> sentenceList = new ArrayList<Annotation>(Utils.inDocumentOrder(sentenceSet));
									
			int sentenceSize = sentenceList.size();
						
			for(int i = 0; i < sentenceSize; i += getNumberOfSentencesInBatch()){
														
				int endIndex = i + getNumberOfSentencesInBatch() - 1;
				
				if(endIndex >= sentenceList.size()) 
					endIndex = sentenceList.size() - 1;

				// we add numberOfSentencesInContext in left and right context if
				// they are available
				int contextStartIndex = i - getNumberOfSentencesInContext();

				if(contextStartIndex < 0) 
					contextStartIndex = 0;
				
		      	int contextEndIndex = endIndex + getNumberOfSentencesInContext();
		      	if(contextEndIndex >= sentenceList.size()) {
		      		contextEndIndex = sentenceList.size() - 1;
		      	}
		      
		      	// obtain the string to be annotated
		      	sentString = Utils.stringFor(document, Utils.start(sentenceList.get(contextStartIndex)), Utils.end(sentenceList.get(contextEndIndex)));
		      
			    sentString = getSubSectionExists(sentString);
			    
			    //Out.prln("SentString: \n" + sentString);
			    
			    AnalysisResults results = getSentiment(sentString);
			    
			    Out.prln("************************" + results.getSentiment().getDocument().getScore());
		
/*				String result = processString(sentString.toString());
				
				int docSentimentStart = result.indexOf("<docSentiment>");
		
				if(docSentimentStart > -1){
					
					int scoreStartIndex = result.lastIndexOf("<score>");
					int scoreEndIndex = result.indexOf("</score>");
					
					if(scoreStartIndex > -1){
						
						sentimentScore = Double.parseDouble(result.substring(scoreStartIndex + 8, scoreEndIndex));
						//Out.prln(sentimentScore);
					}else{
						ratingValue = 2;
						break;
					}

				}*/
			    
			    sentenceGreaterThanOne = true;

			}
			
			if(sentenceGreaterThanOne){
			    
				String restricted = "restricted";
				String law_restricts = "law restricts";
				String limited = "limited";
				String generally_respected = "respected";
				String government = "government";
				String no_gov_restrictions = "no government restrictions";
				String generally_not_respected = "did not respect";
				String gov_restrictions = "government restrict";
				String severe_restrict = "severely restrict";
				
				if(sentString.contains(government)){
					
					if(sentString.contains(no_gov_restrictions) 
							|| sentString.contains(generally_respected)){
						
						ratingValue = 2;
						
						if(sentString.contains(law_restricts) || sentString.contains(limited)){
							ratingValue = 1;
						}
						
						
					}else if(sentString.contains(generally_not_respected)){
						ratingValue = 1;
						
						if(sentString.contains(gov_restrictions) || sentString.contains(severe_restrict)){
							ratingValue = 0;
						}
					}
					
				}
				
			}
		
			return ratingValue;	
			
		}
 	  	
		private int calculateELECSDRating(Annotation annotation, AnnotationSet outputAS) throws ExecutionException{

			int ratingValue = 0;

			double sentimentScore = 0;
			boolean sentenceGreaterThanOne = false;
			
			String sentenceContent = "";
			String sentString = null;
			
			Node startAnno = annotation.getStartNode();
			Node endAnno = annotation.getEndNode();
			
			AnnotationSet sentenceSet = outputAS.get("Sentence", startAnno.getOffset(), endAnno.getOffset());
			
			List<Annotation> sentenceList = new ArrayList<Annotation>(Utils.inDocumentOrder(sentenceSet));
									
			int sentenceSize = sentenceList.size();
						
			for(int i = 0; i < sentenceSize; i += getNumberOfSentencesInBatch()){
														
				int endIndex = i + getNumberOfSentencesInBatch() - 1;
				
				if(endIndex >= sentenceList.size()) 
					endIndex = sentenceList.size() - 1;

				// we add numberOfSentencesInContext in left and right context if
				// they are available
				int contextStartIndex = i - getNumberOfSentencesInContext();

				if(contextStartIndex < 0) 
					contextStartIndex = 0;
				
		      	int contextEndIndex = endIndex + getNumberOfSentencesInContext();
		      	if(contextEndIndex >= sentenceList.size()) {
		      		contextEndIndex = sentenceList.size() - 1;
		      	}
		      
		      	// obtain the string to be annotated
		      	sentString = Utils.stringFor(document, Utils.start(sentenceList.get(contextStartIndex)), Utils.end(sentenceList.get(contextEndIndex)));
		      
			    sentString = getSubSectionExists(sentString);
			    
			    //Out.prln("SentString: \n" + sentString);
			    
	/*			String result = processString(sentString.toString());
				
				int docSentimentStart = result.indexOf("<docSentiment>");
		
				if(docSentimentStart > -1){
					
					int scoreStartIndex = result.lastIndexOf("<score>");
					int scoreEndIndex = result.indexOf("</score>");
					
					if(scoreStartIndex > -1){
						
						sentimentScore = Double.parseDouble(result.substring(scoreStartIndex + 8, scoreEndIndex));
						Out.prln(sentimentScore);
					}else{
						ratingValue = 2;
						break;
					}

				}*/
			    
			    sentenceGreaterThanOne = true;

			}
			
			if(sentenceGreaterThanOne){
			    
				String limited = "limit";
				String no_right = "do not have the right";
				String restricted = "restricted";
				String government = "government";
				String no_gov_restrictions = "no government restrictions";
				String gov_restrictions = "government restrict";
				String cannot_choose = "cannot freely";
				String could_not_choose = "could not freely";
				String not_provide = "does not provide";
				
				if(sentString.contains(government)){
					
					if(sentString.contains(no_gov_restrictions)){
						
						ratingValue = 2;
						
						if(sentString.contains(limited) ){
							ratingValue = 1;
						}
						
						if(sentString.contains(no_right)){
							ratingValue = 0;
						}
						
						
					}else if(sentString.contains(gov_restrictions) || sentString.contains(restricted)){
						ratingValue = 0;
						
					} else if(sentString.contains(cannot_choose) || sentString.contains(could_not_choose) || sentString.contains(not_provide)){
						ratingValue = 0;
					}
					
				}
				
			}
		
			return ratingValue;	
		}

		private int calculateNEWRELFRERating(Annotation annotation, AnnotationSet outputAS) throws ExecutionException{

			int ratingValue = 0;

			double sentimentScore = 0;
			boolean sentenceGreaterThanOne = false;
			
			String sentenceContent = "";
			String sentString = null;
			
			Node startAnno = annotation.getStartNode();
			Node endAnno = annotation.getEndNode();
			
			AnnotationSet sentenceSet = outputAS.get("Sentence", startAnno.getOffset(), endAnno.getOffset());
			
			List<Annotation> sentenceList = new ArrayList<Annotation>(Utils.inDocumentOrder(sentenceSet));
									
			int sentenceSize = sentenceList.size();
						
			for(int i = 0; i < sentenceSize; i += getNumberOfSentencesInBatch()){
														
				int endIndex = i + getNumberOfSentencesInBatch() - 1;
				
				if(endIndex >= sentenceList.size()) 
					endIndex = sentenceList.size() - 1;

				// we add numberOfSentencesInContext in left and right context if
				// they are available
				int contextStartIndex = i - getNumberOfSentencesInContext();

				if(contextStartIndex < 0) 
					contextStartIndex = 0;
				
		      	int contextEndIndex = endIndex + getNumberOfSentencesInContext();
		      	if(contextEndIndex >= sentenceList.size()) {
		      		contextEndIndex = sentenceList.size() - 1;
		      	}
		      
		      	// obtain the string to be annotated
		      	sentString = Utils.stringFor(document, Utils.start(sentenceList.get(contextStartIndex)), Utils.end(sentenceList.get(contextEndIndex)));
		      
			    sentString = getSubSectionExists(sentString);
			    
			    //Out.prln("SentString: \n" + sentString);
			    
	/*			String result = processString(sentString.toString());
				
				int docSentimentStart = result.indexOf("<docSentiment>");
		
				if(docSentimentStart > -1){
					
					int scoreStartIndex = result.lastIndexOf("<score>");
					int scoreEndIndex = result.indexOf("</score>");
					
					if(scoreStartIndex > -1){
						
						sentimentScore = Double.parseDouble(result.substring(scoreStartIndex + 8, scoreEndIndex));
						Out.prln(sentimentScore);
					}else{
						ratingValue = 2;
						break;
					}

				}*/
			    
			    sentenceGreaterThanOne = true;

			}
			
			if(sentenceGreaterThanOne){
			    
				String restricted = "restricted";
				String limited = "may limit";
				String limited_speech = "limit free speech";
				String prohibit = "law prohibits";
				String electoral_prohibit = "electoral law prohibits";
				String generally_respected = "respected";
				String government = "government";
				String no_gov_restrictions = "no government restrictions";
				String public_speech = "public speech";
				String generally_not_respected = "did not respect";
				String control = "control";
				String gov_restrictions = "government restrict";
				
				if(sentString.contains(government)){
					
					if(sentString.contains(no_gov_restrictions) 
							|| sentString.contains(generally_respected)){
						
						ratingValue = 2;
						
						if(sentString.contains(limited) 
								|| sentString.contains(limited_speech) 
								|| (sentString.contains(prohibit) && sentString.contains(public_speech))){
							ratingValue = 1;
						}
						
						
					}else if(sentString.contains(generally_not_respected)){
						ratingValue = 1;
						
						if(sentString.contains(gov_restrictions)){
							ratingValue = 0;
						}
					}
					
				}
				
			}
		
			return ratingValue;	
		}

		private int calculateWORKERRating(Annotation annotation, AnnotationSet outputAS) throws ExecutionException{

			int ratingValue = 0;

			double sentimentScore = 0;
			boolean sentenceGreaterThanOne = false;
			
			String sentenceContent = "";
			String sentString = null;
			
			Node startAnno = annotation.getStartNode();
			Node endAnno = annotation.getEndNode();
			
			AnnotationSet sentenceSet = outputAS.get("Sentence", startAnno.getOffset(), endAnno.getOffset());
			
			List<Annotation> sentenceList = new ArrayList<Annotation>(Utils.inDocumentOrder(sentenceSet));
									
			int sentenceSize = sentenceList.size();
						
			for(int i = 0; i < sentenceSize; i += getNumberOfSentencesInBatch()){
														
				int endIndex = i + getNumberOfSentencesInBatch() - 1;
				
				if(endIndex >= sentenceList.size()) 
					endIndex = sentenceList.size() - 1;

				// we add numberOfSentencesInContext in left and right context if
				// they are available
				int contextStartIndex = i - getNumberOfSentencesInContext();

				if(contextStartIndex < 0) 
					contextStartIndex = 0;
				
		      	int contextEndIndex = endIndex + getNumberOfSentencesInContext();
		      	if(contextEndIndex >= sentenceList.size()) {
		      		contextEndIndex = sentenceList.size() - 1;
		      	}
		      
		      	// obtain the string to be annotated
		      	sentString = Utils.stringFor(document, Utils.start(sentenceList.get(contextStartIndex)), Utils.end(sentenceList.get(contextEndIndex)));
		      
			    sentString = getSubSectionExists(sentString);
			    
			    //Out.prln("SentString: \n" + sentString);
			    
	/*			String result = processString(sentString.toString());
				
				int docSentimentStart = result.indexOf("<docSentiment>");
		
				if(docSentimentStart > -1){
					
					int scoreStartIndex = result.lastIndexOf("<score>");
					int scoreEndIndex = result.indexOf("</score>");
					
					if(scoreStartIndex > -1){
						
						sentimentScore = Double.parseDouble(result.substring(scoreStartIndex + 8, scoreEndIndex));
						Out.prln(sentimentScore);
					}else{
						ratingValue = 2;
						break;
					}

				}*/
			    
			    sentenceGreaterThanOne = true;

			}
			
			if(sentenceGreaterThanOne){
			    
				String restricted = "restricted";
				String not_allow = "not allow";
				String generally_respected = "respected";
				String government = "government";
				String no_gov_restrictions = "no government restrictions";
				String generally_not_respected = "did not respect";
				String gov_restrictions = "government restrict";
				String gov_allow = "government allow";
				
				if(sentString.contains(not_allow)){
					ratingValue = 1;
				} else if(sentString.contains(government)){
					
					if(sentString.contains(no_gov_restrictions) 
							|| sentString.contains(generally_respected)){
						
						ratingValue = 2;
						

						
					}else if(sentString.contains(generally_not_respected)){
						ratingValue = 1;
						
						if(sentString.contains(gov_restrictions)){
							ratingValue = 0;
						}
					}
					
				}
				
			}
		
			return ratingValue;	
		}

		private int calculateDOMMOVRating(Annotation annotation, AnnotationSet outputAS) throws ExecutionException{

			int ratingValue = 0;

			double sentimentScore = 0;
			boolean sentenceGreaterThanOne = false;
			
			String sentenceContent = "";
			String sentString = null;
			
			Node startAnno = annotation.getStartNode();
			Node endAnno = annotation.getEndNode();
			
			AnnotationSet sentenceSet = outputAS.get("Sentence", startAnno.getOffset(), endAnno.getOffset());
			
			List<Annotation> sentenceList = new ArrayList<Annotation>(Utils.inDocumentOrder(sentenceSet));
									
			int sentenceSize = sentenceList.size();
						
			for(int i = 0; i < sentenceSize; i += getNumberOfSentencesInBatch()){
														
				int endIndex = i + getNumberOfSentencesInBatch() - 1;
				
				if(endIndex >= sentenceList.size()) 
					endIndex = sentenceList.size() - 1;

				// we add numberOfSentencesInContext in left and right context if
				// they are available
				int contextStartIndex = i - getNumberOfSentencesInContext();

				if(contextStartIndex < 0) 
					contextStartIndex = 0;
				
		      	int contextEndIndex = endIndex + getNumberOfSentencesInContext();
		      	if(contextEndIndex >= sentenceList.size()) {
		      		contextEndIndex = sentenceList.size() - 1;
		      	}
		      
		      	// obtain the string to be annotated
		      	sentString = Utils.stringFor(document, Utils.start(sentenceList.get(contextStartIndex)), Utils.end(sentenceList.get(contextEndIndex)));
		      
			    sentString = getSubSectionExists(sentString);
			    
			    //Out.prln("SentString: \n" + sentString);
			    
	/*			String result = processString(sentString.toString());
				
				int docSentimentStart = result.indexOf("<docSentiment>");
		
				if(docSentimentStart > -1){
					
					int scoreStartIndex = result.lastIndexOf("<score>");
					int scoreEndIndex = result.indexOf("</score>");
					
					if(scoreStartIndex > -1){
						
						sentimentScore = Double.parseDouble(result.substring(scoreStartIndex + 8, scoreEndIndex));
						Out.prln(sentimentScore);
					}else{
						ratingValue = 2;
						break;
					}

				}*/
			    
			    sentenceGreaterThanOne = true;

			}
			
			if(sentenceGreaterThanOne){
			    
				String restricted = "restricted";
				String limited = "limit";
				String generally_respected = "respected";
				String government = "government";
				String no_gov_restrictions = "no government restrictions";
				String generally_not_respected = "did not respect";
				String gov_restrictions = "government restrict";
				String some_restrict = "some restrict";
				
				if(sentString.contains(government)){
					
					if(sentString.contains(no_gov_restrictions) 
							|| sentString.contains(generally_respected)){
						
						ratingValue = 2;
						
						if(sentString.contains(limited) || sentString.contains(some_restrict)){
							ratingValue = 1;
						}
						
						
					}else if(sentString.contains(generally_not_respected)){
						ratingValue = 1;
						
						if(sentString.contains(gov_restrictions)){
							ratingValue = 0;
						}
					}
					
				}else{
					ratingValue = 0;
				}
				
			}
		
			return ratingValue;	
		}

		private int calculateFORMOVRating(Annotation annotation, AnnotationSet outputAS) throws ExecutionException{
			
			int ratingValue = 0;

			double sentimentScore = 0;
			boolean sentenceGreaterThanOne = false;
			
			String sentenceContent = "";
			String sentString = null;
			
			Node startAnno = annotation.getStartNode();
			Node endAnno = annotation.getEndNode();
			
			AnnotationSet sentenceSet = outputAS.get("Sentence", startAnno.getOffset(), endAnno.getOffset());
			
			List<Annotation> sentenceList = new ArrayList<Annotation>(Utils.inDocumentOrder(sentenceSet));
									
			int sentenceSize = sentenceList.size();
						
			for(int i = 0; i < sentenceSize; i += getNumberOfSentencesInBatch()){
														
				int endIndex = i + getNumberOfSentencesInBatch() - 1;
				
				if(endIndex >= sentenceList.size()) 
					endIndex = sentenceList.size() - 1;

				// we add numberOfSentencesInContext in left and right context if
				// they are available
				int contextStartIndex = i - getNumberOfSentencesInContext();

				if(contextStartIndex < 0) 
					contextStartIndex = 0;
				
		      	int contextEndIndex = endIndex + getNumberOfSentencesInContext();
		      	if(contextEndIndex >= sentenceList.size()) {
		      		contextEndIndex = sentenceList.size() - 1;
		      	}
		      
		      	// obtain the string to be annotated
		      	sentString = Utils.stringFor(document, Utils.start(sentenceList.get(contextStartIndex)), Utils.end(sentenceList.get(contextEndIndex)));
		      
			    sentString = getSubSectionExists(sentString);
			    
			    //Out.prln("SentString: \n" + sentString);
			    
	/*			String result = processString(sentString.toString());
				
				int docSentimentStart = result.indexOf("<docSentiment>");
		
				if(docSentimentStart > -1){
					
					int scoreStartIndex = result.lastIndexOf("<score>");
					int scoreEndIndex = result.indexOf("</score>");
					
					if(scoreStartIndex > -1){
						
						sentimentScore = Double.parseDouble(result.substring(scoreStartIndex + 8, scoreEndIndex));
						Out.prln(sentimentScore);
					}else{
						ratingValue = 2;
						break;
					}

				}*/
			    
			    sentenceGreaterThanOne = true;

			}
			
			if(sentenceGreaterThanOne){
			    
				String restricted = "restricted";
				String limited = "limit";
				String generally_respected = "respected";
				String government = "government";
				String no_gov_restrictions = "no government restrictions";
				String generally_not_respected = "did not respect";
				String gov_restrictions = "government restrict";
				String some_restrict = "some restrict";
				
				if(sentString.contains(government)){
					
					if(sentString.contains(no_gov_restrictions) 
							|| sentString.contains(generally_respected)){
						
						ratingValue = 2;
						
						if(sentString.contains(limited) || sentString.contains(some_restrict)){
							ratingValue = 1;
						}
						
						
					}else if(sentString.contains(generally_not_respected)){
						ratingValue = 1;
						
						if(sentString.contains(gov_restrictions)){
							ratingValue = 0;
						}
					}
					
				}else{
					ratingValue = 0;
				}
				
			}
		
			return ratingValue;	
		}  	  	
  	  	
		public int calculateIndepJudiciaryRating(Annotation annotation, AnnotationSet outputAS){
			
			int ratingValue = 0;

			double sentimentScore = 0;
			boolean sentenceGreaterThanOne = false;
			
			String sentenceContent = "";
			String sentString = null;
			
			Node startAnno = annotation.getStartNode();
			Node endAnno = annotation.getEndNode();
			
			AnnotationSet sentenceSet = outputAS.get("Sentence", startAnno.getOffset(), endAnno.getOffset());
			
			List<Annotation> sentenceList = new ArrayList<Annotation>(Utils.inDocumentOrder(sentenceSet));
									
			int sentenceSize = sentenceList.size();
						
			for(int i = 0; i < sentenceSize; i += getNumberOfSentencesInBatch()){
														
				int endIndex = i + getNumberOfSentencesInBatch() - 1;
				
				if(endIndex >= sentenceList.size()) 
					endIndex = sentenceList.size() - 1;

				// we add numberOfSentencesInContext in left and right context if
				// they are available
				int contextStartIndex = i - getNumberOfSentencesInContext();

				if(contextStartIndex < 0) 
					contextStartIndex = 0;
				
		      	int contextEndIndex = endIndex + getNumberOfSentencesInContext();
		      	if(contextEndIndex >= sentenceList.size()) {
		      		contextEndIndex = sentenceList.size() - 1;
		      	}
		      
		      	// obtain the string to be annotated
		      	sentString = Utils.stringFor(document, Utils.start(sentenceList.get(contextStartIndex)), Utils.end(sentenceList.get(contextEndIndex)));
		      
			    sentString = getSubSectionExists(sentString);
			    
			    //Out.prln("SentString: \n" + sentString);
			    
	/*			String result = processString(sentString.toString());
				
				int docSentimentStart = result.indexOf("<docSentiment>");
		
				if(docSentimentStart > -1){
					
					int scoreStartIndex = result.lastIndexOf("<score>");
					int scoreEndIndex = result.indexOf("</score>");
					
					if(scoreStartIndex > -1){
						
						sentimentScore = Double.parseDouble(result.substring(scoreStartIndex + 8, scoreEndIndex));
						Out.prln(sentimentScore);
					}else{
						ratingValue = 2;
						break;
					}

				}*/
			    
			    sentenceGreaterThanOne = true;

			}
			
			if(sentenceGreaterThanOne){
			    
				
				String restricted = "restricted";
				String limited = "may limit";
				String limited_speech = "limit free speech";
				String prohibit = "law prohibits";
				String electoral_prohibit = "electoral law prohibits";
				String generally_respected = "respected";
				String government = "government";
				String no_gov_restrictions = "no government restrictions";
				String public_speech = "public speech";
				String generally_not_respected = "did not respect";
				String control = "control";
				String gov_restrictions = "government restrict";
				
				if(sentString.contains(government)){
					
					if(sentString.contains(no_gov_restrictions) 
							|| sentString.contains(generally_respected)){
						
						ratingValue = 2;
						
						if(sentString.contains(limited) 
								|| sentString.contains(limited_speech) 
								|| (sentString.contains(prohibit) && sentString.contains(public_speech))){
							ratingValue = 1;
						}
						
						
					}else if(sentString.contains(generally_not_respected)){
						ratingValue = 1;
						
						if(sentString.contains(gov_restrictions)){
							ratingValue = 0;
						}
					}
					
				}
				
			}
		
			return ratingValue;	
				
		}
		
		public int calculateWomenRightsRating(Annotation annotation, AnnotationSet outputAS){
		
			int ratingValue = 0;

			double sentimentScore = 0;
			boolean sentenceGreaterThanOne = false;
			
			String sentenceContent = "";
			String sentString = null;
			
			Node startAnno = annotation.getStartNode();
			Node endAnno = annotation.getEndNode();
			
			AnnotationSet sentenceSet = outputAS.get("Sentence", startAnno.getOffset(), endAnno.getOffset());
			
			List<Annotation> sentenceList = new ArrayList<Annotation>(Utils.inDocumentOrder(sentenceSet));
									
			int sentenceSize = sentenceList.size();
						
			for(int i = 0; i < sentenceSize; i += getNumberOfSentencesInBatch()){
														
				int endIndex = i + getNumberOfSentencesInBatch() - 1;
				
				if(endIndex >= sentenceList.size()) 
					endIndex = sentenceList.size() - 1;

				// we add numberOfSentencesInContext in left and right context if
				// they are available
				int contextStartIndex = i - getNumberOfSentencesInContext();

				if(contextStartIndex < 0) 
					contextStartIndex = 0;
				
		      	int contextEndIndex = endIndex + getNumberOfSentencesInContext();
		      	if(contextEndIndex >= sentenceList.size()) {
		      		contextEndIndex = sentenceList.size() - 1;
		      	}
		      
		      	// obtain the string to be annotated
		      	sentString = Utils.stringFor(document, Utils.start(sentenceList.get(contextStartIndex)), Utils.end(sentenceList.get(contextEndIndex)));
		      
			    sentString = getSubSectionExists(sentString);
			    
			    //Out.prln("SentString: \n" + sentString);
			    
	/*			String result = processString(sentString.toString());
				
				int docSentimentStart = result.indexOf("<docSentiment>");
		
				if(docSentimentStart > -1){
					
					int scoreStartIndex = result.lastIndexOf("<score>");
					int scoreEndIndex = result.indexOf("</score>");
					
					if(scoreStartIndex > -1){
						
						sentimentScore = Double.parseDouble(result.substring(scoreStartIndex + 8, scoreEndIndex));
						Out.prln(sentimentScore);
					}else{
						ratingValue = 2;
						break;
					}

				}*/
			    
			    sentenceGreaterThanOne = true;

			}
			
			if(sentenceGreaterThanOne){
			    
				String restricted = "restricted";
				String limited = "may limit";
				String limited_speech = "limit free speech";
				String prohibit = "law prohibits";
				String electoral_prohibit = "electoral law prohibits";
				String generally_respected = "respected";
				String government = "government";
				String no_gov_restrictions = "no government restrictions";
				String public_speech = "public speech";
				String generally_not_respected = "did not respect";
				String control = "control";
				String gov_restrictions = "government restrict";
				
				if(sentString.contains(government)){
					
					if(sentString.contains(no_gov_restrictions) 
							|| sentString.contains(generally_respected)){
						
						ratingValue = 2;
						
						if(sentString.contains(limited) 
								|| sentString.contains(limited_speech) 
								|| (sentString.contains(prohibit) && sentString.contains(public_speech))){
							ratingValue = 1;
						}
						
						
					}else if(sentString.contains(generally_not_respected)){
						ratingValue = 1;
						
						if(sentString.contains(gov_restrictions)){
							ratingValue = 0;
						}
					}
					
				}
				
			}
		
			return ratingValue;	
			
		}
	  	
		private AnalysisResults getSentiment(String text){
			
			SentimentOptions options = new SentimentOptions.Builder().document(true).build();
			Features features = new Features.Builder().sentiment(options).build();
			AnalyzeOptions parameters = new AnalyzeOptions.Builder().text(text).features(features).returnAnalyzedText(true).build();
			AnalysisResults results = service.analyze(parameters).execute();
			
			return results;
		}
	  	/**
	  	 * Store automated ciri ratings
	  	 * @param ratingsMap
	  	 * @throws Exception
	  	 */
		private void storeCIRIRatings(TreeMap<String, Object> ratingsMap) throws Exception {
			
			MySQLDBConnection dbConnection = new MySQLDBConnection();
			
			dbConnection.createAutomatedTable(ratingsMap, "Afghanistan_1983");
			
		}	//end storeCIRIRatings
	  	
} // class AutomateCiriRating
