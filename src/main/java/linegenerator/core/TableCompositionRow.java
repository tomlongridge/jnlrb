package linegenerator.core;


public class TableCompositionRow {
    
    private String[] m_Calls;
    protected Method[] m_Methods;
    protected int m_NumLeads;
    protected String[] m_Labels;
    protected boolean m_DisplayCourseEnd;
    protected boolean m_IsExpandedRow;
    
    public TableCompositionRow(final String[] pr_Calls,
                               final Method[] pr_Methods,
                               final int pr_NumLeads,
                               final String[] pr_Labels,
                               final boolean pr_DisplayCourseEnds,
                               final boolean pr_IsExpandedRow) {
        m_Calls = pr_Calls;
        m_Methods = pr_Methods;
        m_NumLeads = pr_NumLeads;
        m_Labels = pr_Labels;
        m_DisplayCourseEnd = pr_DisplayCourseEnds;
        m_IsExpandedRow = pr_IsExpandedRow;
    }
    
    public void setCalls(final String[] pr_Calls) {
        m_Calls = pr_Calls;
    }
    
    public String[] getCalls()
    {
        return m_Calls;
    }
    
    public int getNumCalls()
    {
        int numCalls = 0;
        for (String call : m_Calls) {
            if (LeadType.getLeadType(call) != LeadType.PLAIN) {
                numCalls++;
            }
        }
        return numCalls;
    }
    
    public Method[] getMethods() {
        return m_Methods;
    }
    
    public int getNumLeads() {
        return m_NumLeads;
    }
    
    public String[] getLabels() {
        return m_Labels;
    }

    public void setLabels(final String[] pr_Labels) {
        m_Labels = pr_Labels;
    }
    
    public boolean isInPart(final String pr_Label)
    {
        for (String label : m_Labels) {
            if (label.equals(pr_Label)) {
                return true;
            }
        }
        return false;
    }
    
    public int size() 
    {
        if (m_Calls != null) {
            return m_Calls.length;
        } else {
            return 0;
        }
    }
    
    public boolean containsCall()
    {
        for (String call : m_Calls) {
            if (LeadType.getLeadType(call) != LeadType.PLAIN) {
                return true;
            }
        }
        return false;
    }
    
    public boolean shouldDisplayCourseEnd() {
        return m_DisplayCourseEnd;
    }
    
    public void setDisplayCourseEnd(final boolean pr_DisplayCourseEnd) {
        m_DisplayCourseEnd = pr_DisplayCourseEnd;
    }
    
    public boolean isExpandedRow() {
        return m_IsExpandedRow;
    }
    
    public void setExpandedRow(final boolean pr_IsExpandedRow) {
        m_IsExpandedRow = pr_IsExpandedRow;
    }
    
    protected TableCompositionRow clone() {
        return new TableCompositionRow(m_Calls, m_Methods, m_NumLeads, m_Labels, m_DisplayCourseEnd, m_IsExpandedRow);
    }
    
    public void insertColumn(final int pr_Index) {
        String[] calls = new String[m_Calls.length + 1];
        for (int i = 0; i < calls.length; i++) {
            if (i <= pr_Index) {
                calls[i] = m_Calls[i];
            } else {
                calls[i] = m_Calls[i - 1];
            }
        }
        m_Calls = calls;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String call : m_Calls) {
            if (call != null) {
                sb.append(call);
            }
            sb.append("\t");
        }
        if (m_NumLeads != -1) {
            sb.append(m_NumLeads);
            sb.append("\t");
        }
        return sb.toString();
    }
}
