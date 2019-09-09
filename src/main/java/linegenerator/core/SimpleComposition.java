package linegenerator.core;

import java.util.ArrayList;
import java.util.HashMap;

import linegenerator.core.exceptions.CompositionDefinitionException;
import linegenerator.core.exceptions.FalseGridException;

public class SimpleComposition extends AbstractComposition {

    private ArrayList<String> m_Calls;
    private ArrayList<Integer> m_PlainLeadCounts;

    public SimpleComposition(final HashMap<String, Method> pr_MethodLibrary, final int pr_Changes) {
        super(pr_MethodLibrary, pr_Changes);
        m_Calls = new ArrayList<String>();
        m_PlainLeadCounts = new ArrayList<Integer>();
    }
    
    public void addRows(final String pr_ShortHand) throws CompositionDefinitionException {
        
        int plainLeads = 0;
        for (char call : pr_ShortHand.toCharArray()) {
            if (call == 'p') {
                plainLeads++;
            } else {
                addCall(String.valueOf(call));
                addPlainLeadCount(plainLeads + 1);
                addMethodChange(null);
                plainLeads = 0;
            }
        }
    
        if (plainLeads > 0) {
            addCall(LeadType.PLAIN.toString());
            addPlainLeadCount(plainLeads);
            addMethodChange(null);
        }
    }

    @Override
    public int getNumRows() {
        return m_Calls.size();
    }

    public void addCall(final String pr_Call) {
        m_Calls.add(pr_Call);
    }

    public String getCall(final int pr_Row) {
        return m_Calls.get(pr_Row);
    }

    public void addPlainLeadCount(final Integer pr_Count) {
        m_PlainLeadCounts.add(pr_Count);
    }

    public int getPlainLeadCount(final int pr_Row) {
        return m_PlainLeadCounts.get(pr_Row);
    }
    
    @Override
    public int getNumHeaders() {
        return 1;
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
        
        final ArrayList<LeadType> calls = new ArrayList<LeadType>();
        final ArrayList<Integer> plainLeadCounts = new ArrayList<Integer>();
        final ArrayList<Method> methodChanges = new ArrayList<Method>();
        
        applyFootnotes(calls, plainLeadCounts, methodChanges);

        Grid grid = new Grid(m_FirstMethod);
        int startOffset = 0;
        if (m_FirstMethod.getStartOffset() > 0) {
            grid.add(m_FirstMethod.getLeadNotation(LeadType.PLAIN, 0), LeadType.PLAIN, m_FirstMethod, m_FirstMethod.getStartOffset());
            m_FirstChange = grid.getLastChange();
        } else if (!m_FirstMethod.getStage().getFirstChange().contains(m_FirstChange)) {
            grid = new Grid(m_FirstChange, m_FirstMethod);
            finishFinalCourse(m_FirstMethod, 0, grid, -1, false);
            startOffset = (grid.size() - 1) % m_FirstMethod.getLeadNotation(LeadType.PLAIN, 0).size();
            grid = new Grid(m_FirstMethod);
        }
        
        Notation leadNotation = null;
        Method method = m_FirstMethod;
        int notationIndex = m_FirstMethod.getPlaceNotation().length - 1;
        String courseEnd;
        boolean addCourseEnd = true;
        for (int i = 0; i < calls.size(); i++) {
            for (int j = 0; j < plainLeadCounts.get(i) - 1; j++) {
                leadNotation = method.getLeadNotation(LeadType.PLAIN, notationIndex);
                notationIndex = (notationIndex + 1) % method.getPlaceNotation().length;
                grid.add(leadNotation, LeadType.PLAIN, method, startOffset);
                startOffset = 0;
            }
            
            if (m_OverriddenCalls.containsKey(calls.get(i))) {
                leadNotation = method.createLeadNotation(m_OverriddenCalls.get(calls.get(i)));
            } else {
                leadNotation = method.getLeadNotation(calls.get(i), notationIndex);
            }
            
            if (leadNotation == null) {
            	throw new CompositionDefinitionException("Call is undefined: " + calls.get(i));
            }
            
            notationIndex = (notationIndex + 1) % method.getPlaceNotation().length;
            courseEnd = grid.add(leadNotation, calls.get(i), method, startOffset);
            startOffset = 0;
            addCourseEnd = i < m_Calls.size();
            if (addCourseEnd) {
                m_CourseEnds.add(courseEnd);
                m_CourseEndsComplete.add(true);
            }
            method = methodChanges.get(i);
        }
        
        String roundsChange = m_FirstMethod.getStage().getFirstChange();
        
        if (m_PadPlainLeads) {
	        if (!grid.containsSufficientRounds()) {
	            int numExtraLeads = finishFinalCourse(method, notationIndex, grid, 0, false);
	            if (grid.containsSufficientRounds() && (numExtraLeads > 0)) {
	                m_Calls.add(LeadType.PLAIN.toString());
	                m_PlainLeadCounts.add(numExtraLeads);
	            }
	        } else if (!grid.getLastChange().contains(roundsChange)) {
	            for (int i = grid.size() - 1; i >= 0; i--) {
	                if (!grid.getRow(i).contains(roundsChange)) {
	                    grid.removeRow(i);
	                }
	                if (grid.getLastChange().contains(roundsChange)) {
	                    break;
	                }
	            }
	            if (addCourseEnd) {
	                m_CourseEndsComplete.set(m_CourseEndsComplete.size() - 1, false);
	            }
	        }
        }
        
        m_Changes = grid.size() - 1;
        simplifyCourseEnds(method);
        
        m_IsTrue = grid.isTrue();
        
        return grid;
    }
    
    protected void applyFootnotes(final ArrayList<LeadType> pu_Calls,
                                  final ArrayList<Integer> pu_PlainLeadCounts,
                                  final ArrayList<Method> pu_MethodChanges)
    {            
        for (int i = 1; i <= getNumParts(); i++) {
            for (int j = 0; j < m_Calls.size(); j++) {
                String call = performSubstitutions(m_Calls.get(j), i);
                String extraCalls = "";
                if (call != null && call.length() > 1) {
                    extraCalls = call.substring(1);
                    call = "" + call.charAt(0);
                }
                pu_Calls.add(LeadType.getLeadType(call));
                pu_PlainLeadCounts.add(m_PlainLeadCounts.get(j));
                if (j < m_MethodChanges.size()) {
                    pu_MethodChanges.add(m_MethodChanges.get(j));
                }
                for (char c : extraCalls.toCharArray()) {
                    pu_Calls.add(LeadType.getLeadType(String.valueOf(c)));
                    pu_PlainLeadCounts.add(1);
                    if (j < m_MethodChanges.size()) {
                        pu_MethodChanges.add(pu_MethodChanges.get(pu_MethodChanges.size() - 1));
                    }                    
                }
            }
        }
    }
    
    @Override
    public String getHeaderAsString() {
        StringBuilder returnString = new StringBuilder();
        if (m_FirstChange != null) {
            returnString.append("\t");
            returnString.append(m_FirstChange);
        }
        if (m_IsSpliced) {
            returnString.append("\t");
            returnString.append(getMethodLabel(m_FirstMethod));
        } else {
            returnString.append("\t");
        }
        returnString.append("\n");
        return returnString.toString();
    }

    @Override
    public String getRowAsString(final int pr_Row) {
        StringBuilder returnString = new StringBuilder();
        if (m_Calls.get(pr_Row) != null) {
            returnString.append(m_Calls.get(pr_Row).toString());
        }
        if (pr_Row < m_CourseEnds.size()) {
            returnString.append("\t");
            if (!m_CourseEndsComplete.get(pr_Row)) {
                returnString.append("(");
            }
            returnString.append(m_CourseEnds.get(pr_Row));
            if (!m_CourseEndsComplete.get(pr_Row)) {
                returnString.append(")");
            }
        }
        if (m_IsSpliced) {
            if (pr_Row < m_MethodChanges.size()) {
                returnString.append("\t");
                returnString.append(getMethodLabel(m_MethodChanges.get(pr_Row)));
            }
        } else {
            returnString.append("\t");
            returnString.append(m_PlainLeadCounts.get(pr_Row));
        }
        returnString.append("\n");
        return returnString.toString();
    }
    
    @Override
    public String getCompositionAsString() {
        StringBuilder returnString = new StringBuilder();
        for (int i = 0; i < m_Calls.size(); i++) {
            returnString.append(getRowAsString(i));
        }
        return returnString.toString();
    }
    
}
