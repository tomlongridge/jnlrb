package linegenerator.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import linegenerator.core.exceptions.CompositionDefinitionException;
import linegenerator.core.exceptions.DoesNotEndInRoundsException;
import linegenerator.core.exceptions.FalseGridException;

public abstract class AbstractComposition {

    protected HashMap<String, Method> m_MethodTable;
    protected int m_Changes;
    protected String m_FirstChange;
    protected boolean m_IsSpliced;
    protected Method m_FirstMethod;
    protected ArrayList<Method> m_MethodChanges;
    protected ArrayList<String> m_CourseEnds;
    protected ArrayList<Boolean> m_CourseEndsComplete;
    protected ArrayList<String> m_Footnotes;
    protected Boolean m_IsTrue;
    protected HashMap<String, String> m_GlobalSubstitutions;
    protected HashMap<LeadType,String> m_OverriddenCalls;
    protected ArrayList<HashMap<String, String>> m_Substitutions;
    protected boolean m_PadPlainLeads;
    
    public AbstractComposition(final HashMap<String, Method> pr_MethodLibrary, final int pr_Changes) {
        m_MethodTable = pr_MethodLibrary;
        m_Changes = pr_Changes;
        m_MethodChanges = new ArrayList<Method>();
        m_CourseEnds = new ArrayList<String>();
        m_CourseEndsComplete = new ArrayList<Boolean>();
        m_Footnotes = new ArrayList<String>();
        m_Substitutions = new ArrayList<HashMap<String,String>>();
        m_GlobalSubstitutions = new HashMap<String, String>();
        m_OverriddenCalls = new HashMap<LeadType, String>();
        m_Substitutions.add(new HashMap<String, String>());
        m_IsTrue = null;
        m_PadPlainLeads = true;
    }

    public void setChanges(final int pr_Changes) {
        m_Changes = pr_Changes;
        m_IsTrue = null;
    }

    public int getChanges() {
        return m_Changes;
    }

    public void setFirstMethod(final Method pr_Method) {
        m_FirstMethod = pr_Method;
        m_IsTrue = null;
    }

    public Method getFirstMethod() {
        return m_FirstMethod;
    }

    public void setFirstChange(final String pr_FirstChange) {
        m_FirstChange = pr_FirstChange;
        m_IsTrue = null;
    }

    public String getFirstChange() {
        return m_FirstChange;
    }
    
    public abstract int getNumHeaders();

    public void addMethodChange(final Method pr_Method) {
        
        final Method methodToAdd;
        if (pr_Method == null) {
            methodToAdd = m_FirstMethod;
        } else {
            methodToAdd = pr_Method;
        }
        if (m_MethodChanges.size() == 0) {
            m_IsSpliced = !methodToAdd.equals(m_FirstMethod);
        } else if (!m_IsSpliced && !methodToAdd.equals(m_MethodChanges.get(m_MethodChanges.size() - 1))) {
            m_IsSpliced = true;
        }
        
        m_MethodChanges.add(methodToAdd);
        m_IsTrue = null;
        
    }

    public Method getMethodChange(final int pr_Row) {
        return m_MethodChanges.get(pr_Row);
    }

    public boolean isSpliced() {
        return m_IsSpliced;
    }

    public void addCourseEnd(final String pr_CourseEnd, final boolean pr_IsComplete) {
        m_CourseEnds.add(pr_CourseEnd);
        m_CourseEndsComplete.add(pr_IsComplete);
    }

    public String getCourseEnd(final int pr_Row) {
        return m_CourseEnds.get(pr_Row);
    }

    public boolean isCourseEndComplete(final int pr_Row) {
        if (pr_Row > -1 && pr_Row < m_CourseEndsComplete.size()) {
            return m_CourseEndsComplete.get(pr_Row);
        } else {
            return false;
        }
    }
    
    public abstract int getNumRows();
    
    public boolean isTrue() throws CompositionDefinitionException, FalseGridException
    {
        if (m_IsTrue == null) {
            prove();
        }
        return m_IsTrue;
    }
    
    public abstract Grid prove() throws CompositionDefinitionException, FalseGridException;

    protected void simplifyCourseEnds(final Method pr_Method) {
        
        final ArrayList<String> allCourseEnds = new ArrayList<String>();
        allCourseEnds.add(m_FirstChange);
        allCourseEnds.addAll(m_CourseEnds);
        
        // Calculate the maximum length of course end used (these might vary depending on what is written in the file)
        int maxLength = 0;
        for (int i = 0; i < allCourseEnds.size(); i++) {
            if (allCourseEnds.get(i).length() > maxLength) {
                maxLength = allCourseEnds.get(i).length();
            }
        }
        
        // Expand all course ends to maximum length to allow comparisons
        boolean prefix;
        for (int i = 0; i < allCourseEnds.size(); i++) {
            prefix = true;
            if (allCourseEnds.get(i).length() < maxLength) {
                StringBuilder prefixString = new StringBuilder();
                StringBuilder suffixString = new StringBuilder();
                for (int j = 1; j <= maxLength; j++) {
                    if (prefix) {
                        if (!allCourseEnds.get(i).contains(Stage.getLabelAtPosition(j))) {
                            prefixString.append(Stage.getLabelAtPosition(j));
                        } else {
                            prefix = false;
                        }
                    } else {
                        if (!allCourseEnds.get(i).contains(Stage.getLabelAtPosition(j))) {
                            suffixString.append(Stage.getLabelAtPosition(j));
                        }
                    }
                }
                allCourseEnds.set(i, prefixString.append(allCourseEnds.get(i)).append(suffixString).toString());
            }
        }
        
        // Calculate which positions are fixed
        final ArrayList<Boolean> fixedBells = new ArrayList<Boolean>();
        for (int i = 0; i < maxLength; i++) {
            fixedBells.add(true);
        }
        for (int i = 0; i < allCourseEnds.size(); i++) {
            for (int j = 0; j < allCourseEnds.get(i).length(); j++) {
                if ((i < allCourseEnds.size() - 1) &&
                    fixedBells.get(j) && 
                    (allCourseEnds.get(i).charAt(j) != allCourseEnds.get(i + 1).charAt(j))) {
                    fixedBells.set(j, false);
                }
            }
        }
        
        final int minimumCourseEndLength;
        if (pr_Method.getStage().getBells() > Stage.DOUBLES.getBells()) {
            minimumCourseEndLength = 5;
        } else if (pr_Method.getMethodType() == MethodType.PRINCIPLE) {
            minimumCourseEndLength = pr_Method.getStage().getBells();
        } else {
            minimumCourseEndLength = pr_Method.getStage().getBells() - 1;
        }        
        
        int affectedStart;
        if (pr_Method.getMethodType() == MethodType.PRINCIPLE) {
            affectedStart = 0;
        } else {
            affectedStart = 1;
        }
        
        int affectedEnd;
        for (affectedEnd = fixedBells.size() - 1; affectedEnd - affectedStart >= minimumCourseEndLength; affectedEnd--) {
            if (!fixedBells.get(affectedEnd)) {
                break;
            }
        }
        
        // Apply substring to all course ends
        m_FirstChange = allCourseEnds.get(0).substring(affectedStart, affectedEnd + 1);
        for (int i = 1; i < allCourseEnds.size(); i++) {
            m_CourseEnds.set(i - 1, allCourseEnds.get(i).substring(affectedStart, affectedEnd + 1));
        }
        
    }
    
    public String getFootnote(final int pr_Footnote) {
        return m_Footnotes.get(pr_Footnote);
    }
    
    public void addFootnote(final String pr_Footnote) {
        
        m_Footnotes.add(pr_Footnote);
        m_IsTrue = null;
        int numParts = m_Substitutions.size();

        Matcher matcher = AbstractCompositionVisitor.FOOTNOTE_XPART_REGEX.matcher(pr_Footnote);
        if (matcher.matches()) {
            numParts = Integer.parseInt(matcher.group(1));
            HashMap<String, String> firstPart = m_Substitutions.get(0);
            m_Substitutions.clear();
            for (int i = 0; i < numParts; i++) {
                m_Substitutions.add(new HashMap<String, String>(firstPart));
            }
            return;
        }
        
        matcher = AbstractCompositionVisitor.FOOTNOTE_SUBSTITUTIONS_REGEX.matcher(pr_Footnote);
        if (matcher.matches()) {
            String pattern = matcher.group(1);
            String substitution = matcher.group(2);
            String partsString = matcher.group(3);
            if (partsString == null) {
                m_GlobalSubstitutions.put(pattern, substitution);
            } else {
                String[] parts = partsString.trim().split("( )?(\\,|and) ");
                for (String part : parts) {
                    int partValue = Integer.parseInt(part);
                    if (partValue > 0 && partValue <= numParts) {
                        m_Substitutions.get(partValue - 1).put(pattern, substitution);
                    }
                }
                for (int i = 0; i < m_Substitutions.size(); i++) {
                    if (!m_Substitutions.get(i).containsKey(pattern)) {
                        m_Substitutions.get(i).put(pattern, pattern.replace("*", ""));
                    }
                }
            }
            return;
        }
        
        matcher = AbstractCompositionVisitor.FOOTNOTE_CALL_OVERRIDE_REGEX.matcher(pr_Footnote);
        if (matcher.matches()) {
            m_OverriddenCalls.put(LeadType.getLeadType(matcher.group(1)), matcher.group(2));
        }
        
        matcher = AbstractCompositionVisitor.FOOTNOTE_SELECTIVE_PARTS_REGEX.matcher(pr_Footnote);
        if (matcher.matches()) {
            boolean omit = matcher.group(1) != null;
            String pattern = matcher.group(2);
            if (matcher.group(3) != null) {
                String[] parts = matcher.group(3).trim().split("( )?(\\,|and) ");
                for (String part : parts) {
                    int partValue = Integer.parseInt(part);
                    if (partValue > 0 && partValue <= numParts) {
                        if (omit) {
                            m_Substitutions.get(partValue - 1).put(pattern, "");
                        } else {
                            m_Substitutions.get(partValue - 1).put(pattern, pattern.replace("*", ""));
                        }
                    }
                }
                for (int i = 0; i < m_Substitutions.size(); i++) {
                    if (!m_Substitutions.get(i).containsKey(pattern)) {
                        if (omit) {
                            m_Substitutions.get(i).put(pattern, pattern.replace("*", ""));
                        } else {
                            m_Substitutions.get(i).put(pattern, "");
                        }
                    }
                }
            } else {
                if (omit) {
                    m_GlobalSubstitutions.put(pattern, "");
                } else {
                    m_GlobalSubstitutions.put(pattern, pattern.replace("*", ""));
                }
            }
            return;
        }
        
    }
    
    public int getNumParts()
    {
        return m_Substitutions.size();
    }

    protected String performSubstitutions(final String pr_Call, final int pr_Part)
    {
        if (pr_Part >= 1 && pr_Part <= m_Substitutions.size()) {
            if (m_Substitutions.get(pr_Part - 1).containsKey(pr_Call)) {
                return m_Substitutions.get(pr_Part - 1).get(pr_Call);
            }            
        }
        if (m_GlobalSubstitutions.containsKey(pr_Call)) {
            return m_GlobalSubstitutions.get(pr_Call);
        }
        if (pr_Call != null) {
            return pr_Call.replaceAll("\\*", "");
        }
        return pr_Call;
    }
    
    protected HashMap<String, String> getGlobalSubstitutions()
    {
        return m_GlobalSubstitutions;
    }
    protected HashMap<String, String> getSubstitutions(final int pr_Part)
    {
        return m_Substitutions.get(pr_Part - 1);
    }

    protected int finishFinalCourse(final Method pr_Methods,
                                    final int pr_NotationIndex,
                                    final Grid pu_Grid,
                                    final int pr_NumLeadsToCourseEnd,
                                    final boolean pr_AddCourseEnds) throws DoesNotEndInRoundsException {
        return finishFinalCourse(new Method[] { pr_Methods }, 0, pr_NotationIndex, pu_Grid, pr_NumLeadsToCourseEnd, pr_AddCourseEnds);
    }

    /**
     * Completes the supplied grid with plain leads until rounds is reached or a complete plain course as run.
     * 
     * Add a lead at a time using method list supplied...
     * 
     * ... if it doesn't contain rounds: add the lead to the grid
     * ... if it contains rounds: add the preceding rows to the grid
     * ... if lead end is course end (i.e. in home position):
     *     ... if rounds has already been found: add incomplete course
     *     ... otherwise: add complete course
     *     
     * The complete/incomplete flag is a little complicated because it's classified as complete if you don't
     * hit the home position but the course end is the same as the previously added one, so we check whether
     * the last course end added matches the final course end.
     * 
     * Also, it seems to be that for Spliced the above rule does apply if there are still methods to be rung
     * as the course end could change by changing the method.
     * 
     * @param pr_Methods
     * @param pr_MethodIndex
     * @param pr_NotationIndex
     * @param pu_Grid
     * @param pr_NumLeadsToCourseEnd
     * @param pr_AddCourseEnds
     * @return the number of leads added
     * @throws DoesNotEndInRoundsException 
     */
    protected int finishFinalCourse(final Method[] pr_Methods,
                                    final int pr_MethodIndex,
                                    final int pr_NotationIndex,
                                    final Grid pu_Grid,
                                    final int pr_NumLeadsToCourseEnd,
                                    final boolean pr_AddCourseEnds) throws DoesNotEndInRoundsException {

    	if (pu_Grid.endsInRounds() && pu_Grid.containsSufficientRounds()) {
    		return 0;
    	}

    	Method method = pr_Methods[pr_MethodIndex];
    	final String firstChange = method.getStage().getFirstChange();
    	final int homePosition = method.getStage().getBells();
    	final int maxNumLeads = pr_NumLeadsToCourseEnd + 
    							(method.getStage().getBells() * method.getPlaceNotation().length);
    	final boolean methodsRemaining = pr_MethodIndex < pr_Methods.length - 1;
    	final String lastLeadEndAdded = pu_Grid.getLastLeadEnd();
    	
    	Grid newLead;
    	boolean roundsFound = false;
    	boolean courseEndFound = false;
    	int notationIndex = pr_NotationIndex;
    	Grid grid = pu_Grid;
    	int lead;
		
    	for (lead = 0; lead < maxNumLeads; lead++) {
    		
    		method = pr_Methods[Integer.min(pr_MethodIndex + lead, pr_Methods.length - 1)];
    		newLead = new Grid(grid.getLastChange(), method);
    		newLead.add(method.getLeadNotation(LeadType.PLAIN, notationIndex), LeadType.PLAIN, method, 0);
    		notationIndex = (notationIndex + 1) % method.getPlaceNotation().length;
    		
    		if (!newLead.containsRounds()) {
    			grid.add(newLead);
    		} else {
    			for (int row = 1; row < newLead.size(); row++) {
    				grid.add(newLead.getRow(row), method, newLead.isLeadEnd(row), newLead.isLabel(row));
    				if (newLead.getRow(row).contains(firstChange)) {
    					grid = new Grid(newLead.getRow(row), method);
    					roundsFound = true;
    				}
    			}
    		}
    		
			if ((grid.getTenorPosition() == homePosition
    			&& (notationIndex == method.getPlaceNotation().length - 1))) { // In multi-notation methods, need to check it's a lead end
    			if (pr_AddCourseEnds && !courseEndFound) {
    				addCourseEnd(grid.getLastLeadEnd(),
    							 !roundsFound || 			// It's complete if the change is hit before finding rounds
    							 grid.endsInRounds() ||     // ... or if the course ends in rounds (i.e. rounds just found)
    							 (lastLeadEndAdded.equals(grid.getLastLeadEnd()) && !methodsRemaining));
    				                                        // ... or course end is the same as the last added lead end
    														// ... but not in spliced if there are methods remaining
    			}
    			courseEndFound = true;
    		}
    		
    		if (roundsFound && (courseEndFound || !pr_AddCourseEnds)) {
    			return lead;
    		}
    		
    	}
    	
    	// Throw exception if we got to the end without getting the course ends (if we were expecting to)
    	if (pr_AddCourseEnds) {
    		throw new DoesNotEndInRoundsException(pu_Grid.getLastChange(), pu_Grid);
    	}
    	
    	return lead;
    }
        
//        if (pu_Grid.size() > 1 && pu_Grid.endsInRounds()) {
//            return 0;
//        }
//
//
//        Method firstMethod = pr_Methods[pr_MethodIndex];
//        int maxNumLeads = pr_NumLeadsToCourseEnd + 
//        		firstMethod.getStage().getBells() * firstMethod.getPlaceNotation().length;
////        int maxNumLeads;
////        if (pr_NumLeadsToCourseEnd > 0) {
////            maxNumLeads = pr_NumLeadsToCourseEnd;
////        } else {
////            maxNumLeads = firstMethod.getStage().getBells() * firstMethod.getPlaceNotation().length;
////        }
//        
//        Grid newLead;
//        Grid currentGrid = pu_Grid;
//        String firstChange = firstMethod.getStage().getFirstChange();
//        String homePosition = firstMethod.getStage().getLabel();
//        int notationIndex = pr_NotationIndex;
//        int numLeads = 0;
//        int numLeadsAdded = 0;
//        int methodPointer = pr_MethodIndex;
//        boolean addedMethodOffsetChanges = false;
//        boolean foundCourseEnd = false;
//        String lookAheadCourseEnd = null;
//        do {
//            numLeads++;
//            newLead = new Grid(currentGrid.getLastChange(), pr_Methods[methodPointer]);
//            newLead.add(pr_Methods[methodPointer].getLeadNotation(LeadType.PLAIN, notationIndex), LeadType.PLAIN, pr_Methods[methodPointer], 0);
//            notationIndex = (notationIndex + 1) % pr_Methods[methodPointer].getPlaceNotation().length;
//            if (!newLead.endsInRounds() && newLead.containsRounds()) {
//                for (int i = 1; i < newLead.size(); i++) {
//                    currentGrid.add(newLead.getRow(i), pr_Methods[methodPointer], newLead.isLeadEnd(i), newLead.isLabel(i));
//                    if (newLead.getRow(i).contains(firstChange)) {
//                        numLeadsAdded = numLeads - 1;
//                        currentGrid = new Grid(newLead.getRow(i), pr_Methods[methodPointer]);
////                        if (pu_Grid.containsSufficientRounds() && foundCourseEnd) {
////                        	break;
////                        }
//                    }
//                }
//                if (homePosition.equals(String.valueOf(currentGrid.getLastLeadEnd().indexOf(Stage.getLabelAtPosition(currentGrid.getStage().getBells())) + 1))) {
////                	if (pr_AddCourseEnds) {
////                		addCourseEnd(currentGrid.getLastLeadEnd(), newLead.endsInRounds());
////                	}
////                	foundCourseEnd = true;
//                	lookAheadCourseEnd = currentGrid.getLastLeadEnd();
//                }
//            } else {
//                currentGrid.add(newLead);
//                if (homePosition.equals(String.valueOf(currentGrid.getLastLeadEnd().indexOf(Stage.getLabelAtPosition(currentGrid.getStage().getBells())) + 1))) {
//                    if (pr_AddCourseEnds) {
//                    	addCourseEnd(currentGrid.getLastLeadEnd(), newLead.endsInRounds());
//                    }
//                    foundCourseEnd = true;
//                }
//                if (!addedMethodOffsetChanges && (pr_Methods[methodPointer].getStartOffset() > 0) && (numLeads == maxNumLeads)) {
//                    maxNumLeads++;
//                    addedMethodOffsetChanges = true;
//                }
//            }
//
//            if (methodPointer < pr_Methods.length - 1) {
//                methodPointer++;
//            }
//            
//            if (pu_Grid.containsSufficientRounds() && foundCourseEnd) {
//            	break;
//            }
//        }
//        while (numLeads < maxNumLeads);
//        
//        if (lookAheadCourseEnd != null) {
//        	if (pr_AddCourseEnds) {
//        		addCourseEnd(lookAheadCourseEnd, false);
//        	}
//        	foundCourseEnd = true;
//        }
//        
//        if (!pu_Grid.containsSufficientRounds()) {
//            // Try one more lead to see if it comes round in the next few blows
//            newLead = new Grid(currentGrid.getLastChange(), pr_Methods[methodPointer]);
//            newLead.add(pr_Methods[methodPointer].getLeadNotation(LeadType.PLAIN, notationIndex), LeadType.PLAIN, pr_Methods[methodPointer], 0);
//            notationIndex = (notationIndex + 1) % pr_Methods[methodPointer].getPlaceNotation().length;
//            if (newLead.containsRounds()) {
//                for (int i = 1; i < newLead.size(); i++) {
//                    currentGrid.add(newLead.getRow(i), pr_Methods[methodPointer], newLead.isLeadEnd(i), newLead.isLabel(i));
//                    if (newLead.getRow(i).contains(firstChange)) {
//                        numLeads++;
//                        if (pr_AddCourseEnds) {
//                            addCourseEnd(currentGrid.getLastLeadEnd(), true);
//                        }
//                        break;
//                    }
//                }
//            }
//        } else if (pr_AddCourseEnds && !foundCourseEnd) {
//            addCourseEnd(currentGrid.getLastChange(), currentGrid.getLastChange().equals(firstChange) || currentGrid.getLastChange().equals(pu_Grid.getLastLeadEnd()));
//        }
//        
//        return numLeadsAdded;
//    }
    
    public String getMethodLabel(final Method pr_Method)
    {
        for (Entry<String, Method> entry : m_MethodTable.entrySet()) {
            if (entry.getValue().equals(pr_Method)) {
                return entry.getKey();
            }
        }
        return "?";
    }
    
    public abstract void addRows(final String pr_ShortHand) throws CompositionDefinitionException;

    public abstract String getHeaderAsString();
    public abstract String getRowAsString(final int pr_Row);
    public abstract String getCompositionAsString();
    
    @Override
    public String toString() {
        final StringBuilder returnString = new StringBuilder();
        if (m_Changes > 0) {
            returnString.append(m_Changes);
            returnString.append("\n");
        }
        returnString.append(getHeaderAsString());
        returnString.append(getCompositionAsString());
        for (String footnote : m_Footnotes) {
            returnString.append(footnote);
        }
        return returnString.toString();
    }
    
    public static boolean doCourseEndsMatch(final String pr_String1, final String pr_String2)
    {
        String string1 = pr_String1;
        String string2 = pr_String2;
        if (pr_String2.length() > pr_String1.length()) {
            string1 = pr_String1 + pr_String2.substring(pr_String1.length());
        } else if (pr_String2.length() < pr_String1.length()) {
            string2 = pr_String2 + pr_String1.substring(pr_String2.length());
        }
        
        return string1.equals(string2) && AbstractComposition.isValidCourseEnd(string1);
        
    }

    public static boolean isValidCourseEnd(final String pr_CourseEnd)
    {
        if (!pr_CourseEnd.matches("[\\dET]+")) {
            return false;
        }
                
        char[] bells = pr_CourseEnd.toCharArray().clone();
        int[] bellNos = new int[bells.length];
        for (int i = 0; i < bells.length; i++) {
            if (Character.isDigit(bells[i])) {
                bellNos[i] = Integer.parseInt("" + bells[i]);
                if (bellNos[i] == 0) {
                    bellNos[i] = 10;
                }
            } else if (bells[i] == 'E') {
                bellNos[i] = 11;
            } else if (bells[i] == 'T') {
                bellNos[i] = 12;
            }
        }
        
        Arrays.sort(bellNos);
        
        for (int i = 0; i < bellNos.length; i++) {
            if ((i < bellNos.length - 1) && (bellNos[i] + 1 != bellNos[i + 1])) {
                return false;
            }
        }
        
        return true;
    }

	public void setPadPlainLeads(boolean m_PadPlainLeads) {
		this.m_PadPlainLeads = m_PadPlainLeads;
	}
}
