package linegenerator.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import linegenerator.core.exceptions.CompositionDefinitionException;
import linegenerator.core.exceptions.FalseGridException;

public class LeadCountTableComposition extends AbstractTableComposition {

    private boolean m_IsTwinBobComposition;

    public LeadCountTableComposition(final HashMap<String, Method> pr_MethodLibrary, final int pr_Changes) {
        super(pr_MethodLibrary, pr_Changes);
        m_IsTwinBobComposition = false;
    }
    
    @Override
    public void setHeaders(String[] pr_Headers) {
        super.setHeaders(pr_Headers);
        
        for (String s : m_Headers) {
            if (s.matches("[SHLQ]")) {
                m_IsTwinBobComposition = true;
                break;
            }
        }
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
        int notationIndex = 0;
        int methodRowPointer = 0;
        int methodPointer = 0;
        int defaultNumLeads;

        Grid grid = new Grid(m_FirstMethod);
        final String homePosition;
        int startOffset;
        if (m_FirstMethod.getStartOffset() > 0) {
            grid.add(m_FirstMethod.getLeadNotation(LeadType.PLAIN, notationIndex), LeadType.PLAIN, m_FirstMethod, m_FirstMethod.getStartOffset());
            m_FirstChange = grid.getLastChange();
            Grid fullCourse = new Grid(m_FirstMethod);
            fullCourse.add(m_FirstMethod.getLeadNotation(LeadType.PLAIN, notationIndex), LeadType.PLAIN, m_FirstMethod, m_FirstMethod.getStartOffset());
            notationIndex = (notationIndex + 1) % m_FirstMethod.getPlaceNotation().length;
            finishFinalCourse(m_FirstMethod, notationIndex, fullCourse, 0, false);
            startOffset = 0;
            homePosition = String.valueOf(fullCourse.getRow(fullCourse.size() - m_FirstMethod.getStartOffset() - 1).indexOf(m_FirstMethod.getStage().getLabel()) + 1);
        } else if (!m_FirstMethod.getStage().getFirstChange().contains(m_FirstChange)) {
            grid = new Grid(m_FirstChange, m_FirstMethod);
            finishFinalCourse(m_FirstMethod, 0, grid, 0, false);
            startOffset = (grid.size() - 1) % m_FirstMethod.getLeadNotation(LeadType.PLAIN, 0).size();
            grid = new Grid(m_FirstMethod);
            homePosition = m_FirstMethod.getStage().getLabel();
        } else {
            startOffset = 0;
            homePosition = m_FirstMethod.getStage().getLabel();
        }
        String currentPosition = m_FirstMethod.getStage().getLabel();
        
        String lastHeader = m_Headers[m_Headers.length - 1];
        int maxHeader = 0;
        if (lastHeader.matches("[SHLQ]")) {
            switch (lastHeader.charAt(0)) {
            case 'S': 
                maxHeader = 4;
                break;
            case 'H': 
                maxHeader = 6;
                break;
            case 'L': 
                maxHeader = 8;
                break;
            case 'Q': 
            default:
                maxHeader = 13;
                break;
            }
        } else if (lastHeader.matches("\\d+")) {
            maxHeader = Integer.parseInt(lastHeader);
        }
        
        if (m_FirstMethod.getMethodType() == MethodType.PRINCIPLE) {
            defaultNumLeads = Math.max(m_FirstMethod.getStage().getBells(), maxHeader);
        } else {
            defaultNumLeads = Math.max(m_FirstMethod.getStage().getBells() - 1, maxHeader);
        }

        final ArrayList<TableCompositionRow> rows = getExpandedNotation(applyFootnotes());
        if (rows.size() == 0) {
            throw new CompositionDefinitionException("The composition does not contain any rows.");
        }
        
        final ArrayList<Integer> headers = getSimplifiedHeaders(rows);
        
        int leadCount;
        int headerPointer;
        int nextCall;
        LeadType lead;
        int numLeads;
        TableCompositionRow row = null;
        for (int rowNum = 0; rowNum < rows.size(); rowNum++) {
            row = rows.get(rowNum);
            if (startOffset > 0) {
                leadCount = 0;
            } else {
                leadCount = 1;
            }
            headerPointer = 0;
            while (row.getCalls()[headerPointer] == null) {
                headerPointer++;
            }
            if (row.getNumLeads() != -1) {
                numLeads = row.getNumLeads();
            } else {
                numLeads = defaultNumLeads;
            }
            while (leadCount <= numLeads) {
                lead = LeadType.PLAIN;
                if ((headerPointer > -1) && (startOffset == 0)) {
                    nextCall = headers.get(headerPointer);
                    if (leadCount == nextCall) {
                        lead = LeadType.getLeadType(row.getCalls()[headerPointer]);
                        do {
                            headerPointer++;
                            if (headerPointer == row.getCalls().length) {
                                headerPointer = -1;
                                break;
                            }
                        } while (row.getCalls()[headerPointer] == null);
                    }
                }
                
                Method currentMethod = rows.get(methodRowPointer).getMethods()[methodPointer];
                Grid newRows = new Grid(grid.getLastChange(), currentMethod); 
                if ((lead == LeadType.BOB) || (lead == LeadType.TWIN_BOB)) {
                    if (m_OverriddenCalls.containsKey(LeadType.BOB)) {
                        newRows.add(currentMethod.createLeadNotation(m_OverriddenCalls.get(LeadType.BOB)), LeadType.BOB, currentMethod);
                    } else if (m_OverriddenCalls.containsKey(LeadType.TWIN_BOB)) {
                        newRows.add(currentMethod.createLeadNotation(m_OverriddenCalls.get(LeadType.TWIN_BOB)), LeadType.BOB, currentMethod);
                    } else {
                        newRows.add(currentMethod.getLeadNotation(LeadType.BOB, notationIndex), LeadType.BOB, currentMethod, startOffset);
                    }
                    currentPosition = m_BobLeadTransitions.get(currentMethod).get(notationIndex).get(currentPosition);
                } else if (lead == LeadType.SINGLE) {
                    if (m_OverriddenCalls.containsKey(LeadType.SINGLE)) {
                        newRows.add(currentMethod.createLeadNotation(m_OverriddenCalls.get(LeadType.SINGLE)), LeadType.SINGLE, currentMethod);
                    } else {
                        newRows.add(currentMethod.getLeadNotation(LeadType.SINGLE, notationIndex), LeadType.SINGLE, currentMethod, startOffset);
                    }
                    currentPosition = m_SingleLeadTransitions.get(currentMethod).get(notationIndex).get(currentPosition);
                } else {
                    newRows.add(currentMethod.getLeadNotation(LeadType.PLAIN, notationIndex), LeadType.PLAIN, currentMethod, startOffset);
                    currentPosition = m_PlainLeadTransitions.get(currentMethod).get(notationIndex).get(currentPosition);
                }
                grid.add(newRows);
                
                if (methodPointer < rows.get(methodRowPointer).getMethods().length - 1) {
                    methodPointer++;
                }

                notationIndex = (notationIndex + 1) % rows.get(methodRowPointer).getMethods()[methodPointer].getPlaceNotation().length;
                
                if (startOffset > 0) {
                    startOffset = 0;
                }
                                
                lastChange = grid.getLastChange();
                if (leadCount == numLeads) {
                    // Add course end if not the last row or if it's the last row and ends here (i.e. there are no more changes after the last call)
                    if (row.shouldDisplayCourseEnd() && ((rowNum < rows.size() - 1) || grid.endsInRounds())) {
                        addCourseEnd(lastChange, true);
                    }
                    if (methodRowPointer < rows.size() - 1) {
                        methodRowPointer++;
                    }
                    methodPointer = 0;
                }
                leadCount++;
                
                if ((headerPointer == -1) && (rowNum == rows.size() - 1)) {
                    break;
                }
            }
            
        }
        
        if (m_PadPlainLeads) {
	        if (!grid.containsSufficientRounds()) {
	            
	            int numLeadsTotal = 0;
	            for (ArrayList<HashMap<String, String>> notation : m_PlainLeadTransitions.values()) {
	                for (HashMap<String, String> transitions : notation) {
	                    numLeadsTotal += transitions.size();
	                }
	            }
	     
	            int notationIndexLocal = notationIndex;
	            int numLeadsToCourseEnd = 0;
	            for (numLeadsToCourseEnd = 0; numLeadsToCourseEnd < numLeadsTotal; numLeadsToCourseEnd++) {
	                if (((notationIndexLocal == 0) && currentPosition.equals(homePosition)) ||
	                    numLeadsToCourseEnd > numLeadsTotal) {
	                    break;
	                }
	                currentPosition = m_PlainLeadTransitions.get(rows.get(methodRowPointer).getMethods()[methodPointer]).get(notationIndexLocal).get(currentPosition);
	                notationIndexLocal = (notationIndexLocal + 1) % rows.get(methodRowPointer).getMethods()[methodPointer].getPlaceNotation().length;
	            }
	            
	            finishFinalCourse(rows.get(methodRowPointer).getMethods(),
	                              methodPointer,
	                              notationIndex,
	                              grid,
	                              numLeadsToCourseEnd,
	                              rows.get(rows.size() - 1).shouldDisplayCourseEnd());
	            
	        } else if (!grid.endsInRounds()) {
	            
	            String finalChange = grid.getLastChange();
	            for (int i = grid.size() - 1; i >= 0; i--) {
	                if (!grid.endsInRounds()) {
	                    grid.removeRow(i);
	                }
	                if (grid.endsInRounds()) {
	                    break;
	                }
	            }
	            
	            if (rows.get(rows.size() - 1).shouldDisplayCourseEnd()) {
	                addCourseEnd(finalChange, false);
	            }
	        }
        }
        
        m_Changes = grid.size() - 1;
        simplifyCourseEnds(m_FirstMethod);
        
        m_IsTrue = grid.isTrue();
        
        if (m_CourseEnds.size() != m_Rows.size()) {
            throw new CompositionDefinitionException(MessageFormat.format("The number of course ends ({0}) does not match the number of rows ({1}).", m_CourseEnds.size(), m_Rows.size()));
        }
        
        return grid;
    }
    
    private ArrayList<Integer> getSimplifiedHeaders(final ArrayList<TableCompositionRow> pr_Rows) throws CompositionDefinitionException
    {
        final ArrayList<Integer> headers = new ArrayList<Integer>();
        int insertedColumns = 0;
        
        for (int i = 0; i < m_Headers.length; i++) {
            
            if (m_IsTwinBobComposition) {
                if (m_Headers[i].toUpperCase().equals("S")) {
                    headers.add(3);
                    headers.add(4);
                } else if (m_Headers[i].toUpperCase().equals("H")) {
                    headers.add(5);
                    headers.add(6);
                } else if (m_Headers[i].toUpperCase().equals("L")) {
                    headers.add(7);
                    headers.add(8);
                } else if (m_Headers[i].toUpperCase().equals("Q")) {
                    headers.add(12);
                    headers.add(13);
                } else if (m_Headers[i].equals("1")) {
                    headers.add(13);
                } else if (m_Headers[i].equals("2")) {
                    headers.add(7);
                } else if (m_Headers[i].equals("3")) {
                    headers.add(5);
                } else if (m_Headers[i].equals("4")) {
                    headers.add(3);
                } else if (m_Headers[i].equals("5")) {
                    headers.add(9);
                } else if (m_Headers[i].equals("6")) {
                    headers.add(11);
                }
                if (m_Headers[i].matches("[SHLQ]")) {
                    for (TableCompositionRow row : pr_Rows) {
                        row.insertColumn(i + insertedColumns);
                    }
                    insertedColumns++;
                }
            } else {
                try {
                    headers.add(Integer.parseInt(m_Headers[i]));
                } catch (NumberFormatException e) {
                    throw new CompositionDefinitionException("Unrecognised calling position: " + m_Headers[i]);
                }
            }
            
        }
        return headers;
    }

    @Override
    public void addRows(String pr_ShortHand) throws CompositionDefinitionException {
        throw new UnsupportedOperationException("Short hand notation not supported for lead count tables.");
    }
    
}
