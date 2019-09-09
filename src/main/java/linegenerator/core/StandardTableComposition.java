package linegenerator.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import linegenerator.core.exceptions.CompositionDefinitionException;
import linegenerator.core.exceptions.FalseGridException;

public class StandardTableComposition extends AbstractTableComposition {

    public StandardTableComposition(final HashMap<String, Method> pr_MethodLibrary, final int pr_Changes) {
        super(pr_MethodLibrary, pr_Changes);
        m_Rows = new ArrayList<TableCompositionRow>();
    }
    
    @Override
    public void addRows(final String pr_ShortHand) throws CompositionDefinitionException {
        
        final String shortHand = pr_ShortHand.replaceAll("\\s", "");
        ArrayList<String> headers;
        if (m_Headers == null) {
            headers = new ArrayList<String>();
            for (char c : shortHand.toCharArray()) {
                String callPosition = String.valueOf(c);
                if (!callPosition.matches("[" + LeadType.SINGLE.toString() + "0-9]")) {
                    if (!headers.contains(callPosition.toUpperCase())) {
                        headers.add(callPosition.toUpperCase());
                    }
                }
            }
            m_Headers = headers.toArray(new String[headers.size()]);
        }

        String[] calls = new String[m_Headers.length];
        String headersString = "";
        for (String header : m_Headers) {
            headersString += header;
        }
        StringBuilder lastChar = new StringBuilder();
        String call;
        int callIndex;
        int lastCallIndex = -1;
        outerLoop:
        for (char c : shortHand.toCharArray()) {
            String callPosition = String.valueOf(c);
            if (callPosition.matches("[" + LeadType.SINGLE.toString() + "0-9]") &&
                !headersString.contains(callPosition)) {
                lastChar.append(callPosition);
            } else {
                if (lastChar.length() == 0) {
                    call = LeadType.BOB.toString();
                } else {
                    call = lastChar.toString();
                    lastChar = new StringBuilder();
                }
                for (callIndex = 0; callIndex < m_Headers.length; callIndex++) {
                    if (m_Headers[callIndex].equalsIgnoreCase(callPosition)) {
                        if (callIndex <= lastCallIndex) {
                            addRow(new TableCompositionRow(calls, new Method[] { m_FirstMethod }, -1, new String[0], true, false));
                            calls = new String[m_Headers.length];
                        }
                        calls[callIndex] = call;
                        lastCallIndex = callIndex;
                        continue outerLoop;
                    }
                }
                throw new CompositionDefinitionException("Unrecognised header in short hand composition: " + c);
            }
            
        }
        
        addRow(new TableCompositionRow(calls, new Method[] { m_FirstMethod }, -1, new String[0], true, false));
    }
    
    @Override
    public Grid prove() throws CompositionDefinitionException, FalseGridException {

        m_IsTrue = false;
        m_CourseEnds.clear();
        m_CourseEndsComplete.clear();
        m_Changes = 0;
        if (m_FirstChange == null) {
            m_FirstChange = m_FirstMethod.getStage().getFirstChange();
        }
        
        calculateTransitions();
        
        String lastChange = m_FirstMethod.getStage().getFirstChange();
        final String homePosition = m_FirstMethod.getStage().getLabel();
        String currentPosition = homePosition;
        int notationIndex = 0;
        int startOffset = m_FirstMethod.getStartOffset();
        boolean foundCall;
        String lastChangeOfLastCourse = null;
        Boolean displayLastRowCourseEnd = null;
        int methodPointer = 0;
        
        Grid grid = new Grid(m_FirstChange, m_FirstMethod);

        if (!lastChange.contains(m_FirstChange)) {
            finishFinalCourse(m_FirstMethod, 0, grid, -1, false);
            currentPosition = Stage.getLabelAtPosition(grid.getLastLeadEnd().indexOf(homePosition) + 1);
            startOffset = (grid.size() - 1) % m_FirstMethod.getLeadNotation(LeadType.PLAIN, 0).size();
            grid = new Grid(m_FirstMethod);
        }
        
        final ArrayList<TableCompositionRow> rows = getExpandedNotation(applyFootnotes());
        
        if (rows.size() == 0) {
            throw new CompositionDefinitionException("The composition does not contain any rows.");
        }
        
        final ArrayList<Method> methods = new ArrayList<Method>();
        for (TableCompositionRow row : rows) {
            if (!row.isExpandedRow()) {
                for (Method method : row.getMethods()) {
                    methods.add(method);
                }
            }
        }
        
        for (int rowPtr = 0; rowPtr < rows.size(); rowPtr++) {
            TableCompositionRow row = rows.get(rowPtr);
            for (int callPtr = 0; callPtr < row.size(); callPtr++) {
                if (row.getCalls()[callPtr] == null) {
                    continue;
                }
                LeadType lead = LeadType.getLeadType(row.getCalls()[callPtr]);
                foundCall = false;
                String startPosition = currentPosition;
                do {
                    for (String position : getCallingPosition(methods.get(methodPointer), m_Headers[callPtr], lead)) {
                        
                    	Method currentMethod = methods.get(methodPointer);
                        if ((lead == LeadType.BOB) && m_BobLeadTransitions.get(methods.get(methodPointer)).get(notationIndex).get(currentPosition).equals(position)) {
                            if (m_OverriddenCalls.containsKey(LeadType.BOB)) {
                                grid.add(currentMethod.createLeadNotation(m_OverriddenCalls.get(LeadType.BOB)), LeadType.BOB, currentMethod);
                            } else {
                                grid.add(currentMethod.getLeadNotation(LeadType.BOB, notationIndex), LeadType.BOB, currentMethod, startOffset);
                            }
                            currentPosition = position;
                            foundCall = true;
                        } else if ((lead == LeadType.SINGLE) && m_SingleLeadTransitions.get(methods.get(methodPointer)).get(notationIndex).get(currentPosition).equals(position)) {
                            if (m_OverriddenCalls.containsKey(LeadType.SINGLE)) {
                                grid.add(currentMethod.createLeadNotation(m_OverriddenCalls.get(LeadType.SINGLE)), LeadType.SINGLE, currentMethod);
                            } else {
                                grid.add(currentMethod.getLeadNotation(LeadType.SINGLE, notationIndex), LeadType.SINGLE, currentMethod, startOffset);
                            }
                            currentPosition = position;
                            foundCall = true;
                        } else {
                            grid.add(currentMethod.getLeadNotation(LeadType.PLAIN, notationIndex), LeadType.PLAIN, currentMethod, startOffset);
                            currentPosition = m_PlainLeadTransitions.get(methods.get(methodPointer)).get(notationIndex).get(currentPosition);
                        }
                        
                        lastChange = grid.getLastChange();
                        if (currentPosition.equals(homePosition)) {
                            if ((lastChangeOfLastCourse != null) || 
                                (foundCall && row.shouldDisplayCourseEnd() && (rowPtr < rows.size() - 1 || methodPointer == methods.size() - 1)) ||
                                (foundCall && (displayLastRowCourseEnd == null) && row.shouldDisplayCourseEnd() && (callPtr == row.getNumCalls())) ||
                                ((displayLastRowCourseEnd != null) && (displayLastRowCourseEnd == true))) {
                                addCourseEnd(lastChange, true);
                            } 
                            lastChangeOfLastCourse = null;
                            displayLastRowCourseEnd = null;
                        } else if (foundCall && lastChangeOfLastCourse != null) {
                            // Add incomplete course end if the home position was not reached on the last row and we've reached a call on this row
                            Grid extraChangesToCourseEnd = new Grid(lastChangeOfLastCourse, currentMethod);
                            // todo: check is this works on E+ stages: think it compares E with 11
                            String theoreticalPostition = String.valueOf(lastChangeOfLastCourse.indexOf(Stage.getLabelAtPosition(grid.getStage().getBells())) + 1);
                            while (!theoreticalPostition.equals(homePosition)) {
                                extraChangesToCourseEnd.add(methods.get(methodPointer).getLeadNotation(LeadType.PLAIN, notationIndex), LeadType.PLAIN, currentMethod, startOffset);
                                theoreticalPostition = m_PlainLeadTransitions.get(methods.get(methodPointer)).get(notationIndex).get(theoreticalPostition);
                            }
                            addCourseEnd(extraChangesToCourseEnd.getLastChange(), false);
                            lastChangeOfLastCourse = null;
                            displayLastRowCourseEnd = null;
                        }
                        
                        if (methodPointer < methods.size() - 1) {
                            methodPointer++;
                        }
                        notationIndex = (notationIndex + 1) % methods.get(methodPointer).getPlaceNotation().length;
                        startOffset = 0;
                        
                    }
                    
                } while (!foundCall && !startPosition.equals(currentPosition));
            }
            lastChangeOfLastCourse = null;
            displayLastRowCourseEnd = null;
            if (!currentPosition.equals(homePosition)) {
                displayLastRowCourseEnd = row.shouldDisplayCourseEnd();
                if (displayLastRowCourseEnd) {
                    lastChangeOfLastCourse = grid.getLastChange();
                }
            }
        }
        
        int numLeadsToCourseEnd = 0;
        int localMethodPointer = methodPointer;
        while ((localMethodPointer < methods.size() - 1) || !currentPosition.equals(homePosition)) {
            currentPosition = m_PlainLeadTransitions.get(methods.get(localMethodPointer)).get(notationIndex).get(currentPosition);
            numLeadsToCourseEnd++;
            if (localMethodPointer < methods.size() - 1) {
                localMethodPointer++;
            }
        }

        if (m_PadPlainLeads) {
	        finishFinalCourse(methods.toArray(new Method[methods.size()]),
	                          methodPointer,
	                          notationIndex,
	                          grid,
	                          numLeadsToCourseEnd,
	                          (numLeadsToCourseEnd > 0) && rows.get(rows.size() - 1).shouldDisplayCourseEnd()); // Don't add course end if we've already reached the end
        }
        
        m_Changes = grid.size() - 1;
        simplifyCourseEnds(m_FirstMethod);
        
        m_IsTrue = grid.isTrue();
        
        if (m_CourseEnds.size() != m_Rows.size()) {
            throw new CompositionDefinitionException(MessageFormat.format("The number of course ends ({0}) does not match the number of rows ({1}).", m_CourseEnds.size(), m_Rows.size()));
        }
        
        return grid;
    }
}
