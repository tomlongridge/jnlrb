package linegenerator.core;

import java.util.ArrayList;

import linegenerator.core.exceptions.CompositionDefinitionException;

public class CompositionReader extends AbstractCompositionVisitor {
    
    private ArrayList<AbstractComposition> m_Compositions;
    private AbstractComposition m_CurrentComposition;
    private int m_CurrentChanges;
    
    public CompositionReader(final Method[] pr_MethodLibrary) {

        super(pr_MethodLibrary);

        m_Compositions = new ArrayList<AbstractComposition>();
        m_CurrentComposition = null;
        m_CurrentChanges = 0;
    }
    
    @Override
    public void changesFound(int pr_Changes) {
        m_CurrentChanges = pr_Changes;
    }

    @Override
    public void compositionStarted(final CompositionType pr_Type) {
        
        if (pr_Type == CompositionType.TABLE) {
            m_CurrentComposition = new StandardTableComposition(m_MethodTable, m_CurrentChanges);
        } else if (pr_Type == CompositionType.ROW_COUNT_TABLE) {
            m_CurrentComposition = new LeadCountTableComposition(m_MethodTable, m_CurrentChanges);
        } else if (pr_Type == CompositionType.SIMPLE) {
            m_CurrentComposition = new SimpleComposition(m_MethodTable, m_CurrentChanges);
        }
        
        if (m_CurrentComposition != null) {
            m_CurrentComposition.setFirstMethod(m_LastMethod);
        }
        
    }

    @Override
    public void compositionFinished() {
        m_Compositions.add(m_CurrentComposition);
        m_CurrentComposition = null;
        m_CurrentChanges = 0;
    }

    @Override
    public void headersFound(final String pr_FirstChange, final Method pr_FirstMethod) {
        m_CurrentComposition.setFirstChange(pr_FirstChange);
        ((SimpleComposition) m_CurrentComposition).setFirstMethod(pr_FirstMethod);
    }

    @Override
    public void headersFound(final String[] pr_Headers, final String pr_FirstChange) {
        ((AbstractTableComposition) m_CurrentComposition).setHeaders(pr_Headers);
        m_CurrentComposition.setFirstChange(pr_FirstChange);
    }

    @Override
    public void compositionRowFound(final int pr_Row,
                                    final String pr_Call, 
                                    final String pr_MethodChangeLabel, 
                                    final Method pr_MethodChange, 
                                    final String pr_CourseEnd,
                                    final boolean pr_IsCompleteCourseEnd,
                                    final int pr_PlainLeadCount) {

        if (m_CurrentComposition.getFirstMethod() == null) {
            m_CurrentComposition.setFirstMethod(pr_MethodChange);
        }
        
        ((SimpleComposition) m_CurrentComposition).addCall(pr_Call);
        ((SimpleComposition) m_CurrentComposition).addPlainLeadCount(pr_PlainLeadCount);
        for (int i = 0; i < pr_PlainLeadCount; i++) {
            m_CurrentComposition.addMethodChange(pr_MethodChange);
        }
        m_CurrentComposition.addCourseEnd(pr_CourseEnd, pr_IsCompleteCourseEnd);
    }

    @Override
    public void compositionRowFound(final int pr_Row,
                                    final String[] pr_Calls, 
                                    final String pr_Part, 
                                    final ArrayList<String> pr_MethodChangeLabels, 
                                    final ArrayList<Method> pr_MethodChanges, 
                                    final String pr_CourseEnd,
                                    final String[] pr_PartLabels,
                                    final int pr_NumLeads,
                                    final boolean pr_IsCompleteCourseEnd) {

        if (m_CurrentComposition.getFirstMethod() == null) {
            m_CurrentComposition.setFirstMethod(pr_MethodChanges.get(0));
        }
        
        TableCompositionRow row;
        Method[] methods = pr_MethodChanges.toArray(new Method[pr_MethodChanges.size()]);
        if (pr_Part == null) {
            row = new TableCompositionRow(pr_Calls,
                                          methods, 
                                          pr_NumLeads,
                                          pr_PartLabels,
                                          true,
                                          false);
        } else if (LabelledPartCompositionRow.isLabelledPart(pr_Part)) {
            row = new LabelledPartCompositionRow(pr_Part,
                                                 methods,
                                                 pr_PartLabels,
                                                 true);
        } else if (FootnoteReferenceCompositionRow.isFootnoteReference(pr_Part)) {
            row = new FootnoteReferenceCompositionRow(pr_Part,
                                                      methods,
                                                      pr_NumLeads,
                                                      pr_PartLabels,
                                                      true);
        } else {
            throw new IllegalArgumentException("Labelled row not recognised as either a footnote or part.");
        }
        ((AbstractTableComposition) m_CurrentComposition).addRow(row);
        m_CurrentComposition.addCourseEnd(pr_CourseEnd, pr_IsCompleteCourseEnd);
        
    }
    
    public void compositionRowFound(final String pr_ShortHand) throws CompositionDefinitionException
    {
        if (m_CurrentComposition == null) {
            if (pr_ShortHand.matches("[p\\-s]+")) {
                m_CurrentComposition = new SimpleComposition(m_MethodTable, 0);
            } else {
                m_CurrentComposition = new StandardTableComposition(m_MethodTable, 0);
            }            
        }
        
        m_CurrentComposition.setFirstMethod(m_LastMethod);
        m_CurrentComposition.addRows(pr_ShortHand);
    }

    public void foundFootnote(final String pr_Footnote)
    {
        m_CurrentComposition.addFootnote(pr_Footnote);
    }

    @Override
    public void lineStarted(final int pr_LineNumber, final String pr_Line) {
        // Do nothing
    }

    @Override
    public void lineFinished() {
        // Do nothing
    }
    
    @Override
    public void stop() {
        // Do nothing
    }
    
    public ArrayList<AbstractComposition> getCompositions() 
    {
        return m_Compositions;
    }

}
