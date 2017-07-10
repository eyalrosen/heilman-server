// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
package com.example;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import edu.cmu.ark.*;
import edu.stanford.nlp.trees.*;

public class SimplifierServlet extends HttpServlet {
    ServletContext context;
    boolean verbose = false;
    final String DEFAULT_GLOBAL_PROPERTIES_FILE =
        "/var/lib/tomcat8/webapps/heilman-server/WEB-INF/classes/global.properties";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        context = getServletContext();
        // debug
        String debug = config.getInitParameter("debug");
        GlobalProperties.setDebug(debug != null && debug.equalsIgnoreCase("true"));
        // propertiesFile
        String propertiesFile = config.getInitParameter("propertiesFile");
        if (propertiesFile == null)
            propertiesFile = DEFAULT_GLOBAL_PROPERTIES_FILE;
        GlobalProperties.loadProperties(propertiesFile);
        // verbose
        String verboseStr = config.getInitParameter("verbose");
        verbose = verboseStr != null && verboseStr.equalsIgnoreCase("true");
        GlobalProperties.setComputeFeatures(verbose);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)  
            throws ServletException, IOException {
        String input = req.getParameter("input");
        if (input == null) {
            resp.sendError(500, "Parameter input expected");
            return;
        }
        context.log("input=" + input);

        String output = simplify(input);

        context.log("output=" + output);
        resp.setContentType("text/plain");
        resp.getWriter().println(output);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)  
            throws ServletException, IOException {
        doGet(req, resp);
    }

    public String simplify(String doc) {
        SentenceSimplifier ss = new SentenceSimplifier(); 
        Tree parsed;
        StringBuilder builder = new StringBuilder();

        long startTime = System.currentTimeMillis();

        List<String> sentences = AnalysisUtilities.getSentences(doc);
        //iterate over each segmented sentence and generate questions
        List<Question> outputQuestions = new ArrayList<Question>();

        for (String sentence: sentences){
            parsed = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
            if (GlobalProperties.getDebug())
                System.err.println("input: "+parsed.yield().toString());
            if (GlobalProperties.getDebug())
                System.err.println("parse: "+sentence.toString());
            //if no parse, print the original sentence
            if (parsed.yield().toString().equals(".")){
                if (verbose) System.out.print("\t"+sentence);
                return sentence;
            }

            outputQuestions.clear();
            outputQuestions.addAll(ss.simplify(parsed));
            for(Question q: outputQuestions) {
                builder.append(AnalysisUtilities.getCleanedUpYield(
                            q.getIntermediateTree()) + "\r\n");
                if (verbose)
                    System.out.print("\t"+
                            AnalysisUtilities.getCleanedUpYield(
                                q.getSourceTree()));
                if (verbose) System.out.print("\t"+simplificationFeatureString(q));
            }
        }
        System.err.println("Seconds Elapsed:\t"+
                ((System.currentTimeMillis()-startTime)/1000.0));
        return builder.toString();
    }

    private static String simplificationFeatureString(Question q) {
        String res = "";

        res += q.getFeatureValue("extractedFromAppositive");
        res += "\t" + q.getFeatureValue("extractedFromComplementClause");
        res += "\t" + q.getFeatureValue("extractedFromConjoined");
        res += "\t" + q.getFeatureValue("extractedFromConjoinedNPs");
        res += "\t" + q.getFeatureValue("extractedFromNounParticipial");
        res += "\t" + q.getFeatureValue("extractedFromRelativeClause");
        res += "\t" + q.getFeatureValue("extractedFromSubordinateClause");
        res += "\t" + q.getFeatureValue("extractedFromVerbParticipial");
        //res += "\t" + q.getFeatureValue("extractedFromWithParticipial");
        res += "\t" + q.getFeatureValue("movedLeadingPPs");	
        res += "\t" + q.getFeatureValue("removedAppositives");
        res += "\t" + q.getFeatureValue("removedClauseLevelModifiers");
        res += "\t" + q.getFeatureValue("removedNonRestrRelClausesAndParticipials");
        res += "\t" + q.getFeatureValue("removedParentheticals");
        res += "\t" + q.getFeatureValue("removedVerbalModifiersAfterCommas");
        res += "\t" + q.getFeatureValue("extractedFromLeftMostMainClause");

        return res;
    }
}
