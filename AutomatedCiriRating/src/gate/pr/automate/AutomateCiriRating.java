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

import gate.*;
import gate.creole.*;
import gate.creole.metadata.*;
import gate.util.*;


/** 
 * This class is the implementation of the resource AUTOMATEDCIRIRATING.
 */
@CreoleResource(name = "AutomatedCiriRating",
        comment = "Processing Resource to calculate ratings for U.S Country Reports.")
public class AutomateCiriRating  extends AbstractLanguageAnalyser
  implements ProcessingResource {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5194359849044535340L;


} // class AutomateCiriRating