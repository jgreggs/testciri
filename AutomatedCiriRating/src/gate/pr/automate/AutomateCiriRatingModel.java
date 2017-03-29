/**
 * 
 */
package gate.pr.automate;

import gate.creole.AbstractLanguageAnalyser;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;

/**
 * @author Josh
 *
 */
public class AutomateCiriRatingModel extends AbstractLanguageAnalyser{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4096500249724623328L;

	  /**
	   * Number of sentences to be sent to Alchemy API to process in one batch
	   * Default is 10 sentences.
	   */
	  private Integer numberOfSentencesInBatch = 10;

	  /**
	   * This is the number of sentences that are used as sentences in context both
	   * on left and right side and not annotated when sent as part of a batch
	   * unless there's no more sentence to be considered as part of the context.
	   */
	  private Integer numberOfSentencesInContext = 5;
	  
	/**
	   * Annotation set that contains the segment annotation and the annotations to
	   * be copied.
	   */
	  private String inputASName;
	  
	  /**
	   * Annotation set that contains the segment annotation and the annotations to
	   * set as output.
	   */
	  private String outputASName;
	  
	  private boolean debug = false;
	  // Exit gracefully if exception caught on init()
	  private boolean gracefulExit;
	  
	/**
	 * @return the numberOfSentencesInBatch
	 */
	public Integer getNumberOfSentencesInBatch() {
		return numberOfSentencesInBatch;
	}

	/**
	 * @param numberOfSentencesInBatch the numberOfSentencesInBatch to set
	 */
	public void setNumberOfSentencesInBatch(Integer numberOfSentencesInBatch) {
		this.numberOfSentencesInBatch = numberOfSentencesInBatch;
	}

	/**
	 * @return the numberOfSentencesInContext
	 */
	public Integer getNumberOfSentencesInContext() {
		return numberOfSentencesInContext;
	}

	/**
	 * @param numberOfSentencesInContext the numberOfSentencesInContext to set
	 */
	public void setNumberOfSentencesInContext(Integer numberOfSentencesInContext) {
		this.numberOfSentencesInContext = numberOfSentencesInContext;
	}

	/**
	 * @return the inputASName
	 */
	public String getInputASName() {
		return inputASName;
	}

	  /**
	   * Annotation set to use for obtaining segment annotations and the annotations
	   * to copy into the composite document.
	   * 
	   * @param inputASName the inputASName to set
	   */
	  @CreoleParameter(comment="The name of the input annotation set.")
	  @Optional
	  @RunTime
	public void setInputASName(String inputASName) {
		this.inputASName = inputASName;
	}
	/**
	 * @return the outputASName
	 */
	public String getOutputASName() {
		return outputASName;
	}

	  /**
	   * Annotation set to use for obtaining segment annotations and the annotations
	   * to set.
	   * 
	   * @param outputASName the outputASName to set
	   */
	  @CreoleParameter(comment="The name of the output annotation set.")
	  @Optional
	  @RunTime
	public void setOutputASName(String outputASName) {
		this.outputASName = outputASName;
	}
	/**
	 * @return the debug
	 */
	public boolean isDebug() {
		return debug;
	}
	/**
	 * @param debug the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	/**
	 * @return the gracefulExit
	 */
	public boolean isGracefulExit() {
		return gracefulExit;
	}
	/**
	 * @param gracefulExit the gracefulExit to set
	 */
	public void setGracefulExit(boolean gracefulExit) {
		this.gracefulExit = gracefulExit;
	}
	  
}
