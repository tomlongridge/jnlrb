package linegenerator.core;

import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;

import linegenerator.core.exceptions.CompositionDefinitionException;
import linegenerator.core.exceptions.FalseGridException;

public class CompositionWriter extends AbstractCompositionVisitor {
    
    private AbstractComposition[] m_Compositions;
    private int m_CompositionPointer;
    private AbstractComposition m_CurrentComposition;
    private int m_CurrentCompositionRow;
    private boolean m_IsCompositionValid;
    
    private String m_CurrentLine;
    private int m_CurrentLineNumber;
    private StringBuilder m_CurrentOutput;
    private StringBuilder m_CompositionOutput;
    private int m_CurrentChanges;
    
    private boolean m_HeadersFound;
    private int m_LastCompositionLineIndex;
    
    public CompositionWriter(final Method[] pr_MethodLibrary, final AbstractComposition[] pr_Compositions) {
        super(pr_MethodLibrary);
        m_Compositions = pr_Compositions;
        m_CompositionPointer = 0;
        m_CompositionOutput = new StringBuilder();
        m_CurrentOutput = new StringBuilder();
        m_CurrentChanges = 0;
    }

    @Override
    public void lineStarted(final int pr_LineNumber, final String pr_Line) {
        m_CurrentLineNumber = pr_LineNumber;
        m_CurrentLine = pr_Line;
        
        m_CompositionOutput.append(m_CurrentOutput);
        m_CurrentOutput = new StringBuilder();
    }
    
    @Override
    public void changesFound(int pr_Changes) {
        m_CurrentChanges = pr_Changes;
    }

    @Override
    public void compositionStarted(final CompositionType pr_Type) {
        
        m_CurrentComposition = m_Compositions[m_CompositionPointer++];
        
        try {
            m_IsCompositionValid = m_CurrentComposition.isTrue();
        } catch (FalseGridException e) {
            // Error already reported - just output original text
            m_IsCompositionValid = false;
        } catch (CompositionDefinitionException e) {
            // Error already reported - just output original text
            m_IsCompositionValid = false;
        }
            
        int calculatedChanges = m_CurrentComposition.getChanges();
        if (m_CurrentChanges > 0) {
            if (m_IsCompositionValid && (m_CurrentChanges != calculatedChanges)) {
                writeError(
                        MessageFormat.format("Incorrect number of changes found: {0} calculated, {1} found.",
                                             calculatedChanges,
                                             m_CurrentChanges));
                m_IsCompositionValid = false;
            }
        } else if (m_IsCompositionValid) {
            m_CurrentOutput.append(calculatedChanges);
            m_CurrentOutput.append("\n");
        }
        
        m_HeadersFound = false;
    }

    @Override
    public void headersFound(final String pr_FirstChange, final Method pr_FirstMethod)
    {
        if (m_IsCompositionValid) {
            if (pr_FirstChange == null) {
                m_CurrentOutput.append("\t");
                m_CurrentOutput.append(m_CurrentComposition.getFirstChange());
            } else {
                if (!AbstractComposition.doCourseEndsMatch(pr_FirstChange, m_CurrentComposition.getFirstChange())) {
                    writeError(
                            MessageFormat.format("Incorrect first change found: {0} calculated, {1} found.",
                                                 pr_FirstChange,
                                                 m_CurrentComposition.getFirstChange()));
                }
            }
        }
        if (m_CurrentLine.charAt(0) != '\t') {
            m_CurrentOutput.append("\t");
        }
        m_CurrentOutput.append(m_CurrentLine);
        m_CurrentOutput.append("\n");
        m_HeadersFound = true;
        
    }

    @Override
    public void headersFound(final String[] pr_Headers, final String pr_FirstChange)
    {

        if (m_IsCompositionValid) {
            m_CurrentOutput.append(m_CurrentLine.toUpperCase());
            if ((pr_FirstChange == null)) {
                m_CurrentOutput.append("\t");
                m_CurrentOutput.append(m_CurrentComposition.getFirstChange());
            } else {
                if (!AbstractComposition.doCourseEndsMatch(pr_FirstChange, m_CurrentComposition.getFirstChange())) {
                    writeError(
                            MessageFormat.format("Incorrect first change found: {0} calculated, {1} found.",
                                                 pr_FirstChange,
                                                 m_CurrentComposition.getFirstChange()));
                }
            }
        } else {
            m_CurrentOutput.append(m_CurrentLine);
        }
        
        m_CurrentOutput.append("\n");
        m_HeadersFound = true;
    }
    
    @Override
    public void compositionRowFound(final int pr_Row, 
                                    final String pr_Call,
                                    final String pr_MethodChangeLabel,
                                    final Method pr_MethodChange,
                                    final String pr_CourseEnd, 
                                    final boolean pr_IsCompleteCourseEnd,
                                    final int pr_PlainLeadCount) {
        
        m_CurrentCompositionRow = pr_Row;
        
        if (m_IsCompositionValid) {
            
            if (!m_HeadersFound) {
                m_CurrentOutput.append("\t");
                m_CurrentOutput.append(m_CurrentComposition.getFirstChange());
                m_CurrentOutput.append("\n");
                m_HeadersFound = true;
            }

            final String calculatedCourseEnd = m_CurrentComposition.getCourseEnd(pr_Row);
            final boolean calculatedIsCompleteCourseEnd = m_CurrentComposition.isCourseEndComplete(pr_Row);

            if (pr_CourseEnd != null) {
             
                if (!AbstractComposition.doCourseEndsMatch(calculatedCourseEnd, pr_CourseEnd)) {
                    writeError(
                            MessageFormat.format("Incorrect course end found: {0} calculated, {1} found.",
                                                 calculatedCourseEnd,
                                                 pr_CourseEnd));
                    m_IsCompositionValid = false;
                } else {
                    if (pr_IsCompleteCourseEnd != calculatedIsCompleteCourseEnd) {
                        writeError(
                                MessageFormat.format("Unexpected incomplete course end found: {0} calculated, {1} found.",
                                                     calculatedIsCompleteCourseEnd ? "Complete" : "Incomplete",
                                                     pr_IsCompleteCourseEnd ? "Complete" : "Incomplete"));
                        m_IsCompositionValid = false;
                    }
                }
                
                m_CurrentOutput.append(m_CurrentLine);
                
            } else {
                
                if (pr_Call != null) {
                    m_CurrentOutput.append(pr_Call);
                }
                m_CurrentOutput.append("\t");
                if (!calculatedIsCompleteCourseEnd) {
                    m_CurrentOutput.append("(");
                }
                m_CurrentOutput.append(calculatedCourseEnd);
                if (!calculatedIsCompleteCourseEnd) {
                    m_CurrentOutput.append(")");
                }
                m_CurrentOutput.append("\t");
                if (m_CurrentComposition.isSpliced()) {
                    if (pr_MethodChangeLabel != null) {
                        if (!calculatedIsCompleteCourseEnd) {
                            m_CurrentOutput.append("(");
                        }
                        m_CurrentOutput.append(pr_MethodChangeLabel);
                        if (!calculatedIsCompleteCourseEnd) {
                            m_CurrentOutput.append(")");
                        }
                    }
                } else {
                    m_CurrentOutput.append(pr_PlainLeadCount);
                }
            }
            
        } else {
            m_CurrentOutput.append(m_CurrentLine);
        }
        
        m_CurrentOutput.append("\n");
        
        m_LastCompositionLineIndex = m_CompositionOutput.length() + m_CurrentOutput.length();
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

        m_CurrentCompositionRow = pr_Row;
        
        if (m_IsCompositionValid) {
            
            if (!m_HeadersFound) {
                m_CurrentOutput.append("\t");
                m_CurrentOutput.append(m_CurrentComposition.getFirstChange());
                m_CurrentOutput.append("\n");
                m_HeadersFound = true;
            }
            
            final String calculatedCourseEnd = m_CurrentComposition.getCourseEnd(pr_Row);
            final boolean calculatedIsCompleteCourseEnd = m_CurrentComposition.isCourseEndComplete(pr_Row);

            if (pr_CourseEnd != null) {

                if (!AbstractComposition.doCourseEndsMatch(calculatedCourseEnd, pr_CourseEnd)) {
                    writeError(
                            MessageFormat.format("Incorrect course end found: {0} calculated, {1} found.",
                                                 calculatedCourseEnd,
                                                 pr_CourseEnd));
                    m_IsCompositionValid = false;
                } else if (pr_IsCompleteCourseEnd != calculatedIsCompleteCourseEnd) {
                    writeError(
                            MessageFormat.format("Unexpected incomplete course end found: {0} calculated, {1} found.",
                                                 calculatedIsCompleteCourseEnd ? "Complete" : "Incomplete",
                                                 pr_IsCompleteCourseEnd ? "Complete" : "Incomplete"));
                    m_IsCompositionValid = false;
                }
                
                m_CurrentOutput.append(m_CurrentLine);
                
            } else {

                if (pr_Part != null) {
                    m_CurrentOutput.append(pr_Part);
                    for (int i = 1; i < m_CurrentComposition.getNumHeaders(); i++) {
                        m_CurrentOutput.append("\t");
                    }
                    m_CurrentOutput.append("\t");
                } else if (pr_Calls != null){
                    for (String call : pr_Calls) {
                        if (call != null) {
                            m_CurrentOutput.append(call);
                        }
                        m_CurrentOutput.append("\t");
                    }
                } else {
                    m_CurrentOutput.append("\t");
                }

                if (!pr_MethodChangeLabels.isEmpty()) {
                    for (int i = 0; i < pr_MethodChangeLabels.size(); i++) {
                        if (!calculatedIsCompleteCourseEnd && (i == pr_MethodChangeLabels.size() - 1)) {
                            m_CurrentOutput.append("(");
                        }
                        m_CurrentOutput.append(pr_MethodChangeLabels.get(i));
                        if (!calculatedIsCompleteCourseEnd && (i == pr_MethodChangeLabels.size() - 1)) {
                            m_CurrentOutput.append(")");
                        }
                    }
                    m_CurrentOutput.append("\t");
                }
                if (!calculatedIsCompleteCourseEnd) {
                    m_CurrentOutput.append("(");
                }
                m_CurrentOutput.append(calculatedCourseEnd);
                if (!calculatedIsCompleteCourseEnd) {
                    m_CurrentOutput.append(")");
                }
                
                if (pr_NumLeads != -1) {
                    m_CurrentOutput.append("\t[");
                    m_CurrentOutput.append(pr_NumLeads);
                    m_CurrentOutput.append("]");
                }
                
                for (String label : pr_PartLabels) {
                    m_CurrentOutput.append("\t:");
                    m_CurrentOutput.append(label);
                }
            }
            
        } else {
            m_CurrentOutput.append(m_CurrentLine);
        }
        
        m_CurrentOutput.append("\n");
        m_LastCompositionLineIndex = m_CompositionOutput.length() + m_CurrentOutput.length();
    }
    
    public void compositionRowFound(final String pr_ShortHand)
    {
        if (!m_HeadersFound) {
            m_CurrentOutput.append(m_CurrentComposition.getHeaderAsString());
            m_HeadersFound = true;
        }
        
        if (m_IsCompositionValid) {
            m_CurrentOutput.append(m_CurrentComposition.getCompositionAsString());
        } else {
            m_CurrentOutput.append(m_CurrentLine);
            m_CurrentOutput.append("\n");
        }
        
        m_CurrentCompositionRow = m_CurrentComposition.getNumRows() - 1;
        m_LastCompositionLineIndex = m_CompositionOutput.length() + m_CurrentOutput.length();
    }

    public void foundFootnote(final String pr_Footnote)
    {
        // Do nothing
    }

    @Override
    public void lineFinished() {
        if (m_CurrentOutput.length() == 0) {
            m_CompositionOutput.append(m_CurrentLine);
            m_CompositionOutput.append("\n");
        }
    }

    @Override
    public void compositionFinished() {
        
        m_CompositionOutput.append(m_CurrentOutput);
        
        for (int i = m_CurrentCompositionRow + 1; i < m_CurrentComposition.getNumRows(); i++) {
            m_CompositionOutput.insert(m_LastCompositionLineIndex, m_CurrentComposition.getRowAsString(i));
        }
        
        m_CurrentOutput = new StringBuilder("\n");
        m_CurrentChanges = 0;
    }

    @Override
    public void stop() {

        m_CompositionOutput.append(m_CurrentOutput);
        
        try {
            FileWriter writer = new FileWriter(m_OutputFile);
            writer.write(m_CompositionOutput.toString().trim());
            writer.close();
        } catch (IOException e) {
            System.out.println("Unable to write to composition file: " + e.getMessage());
        }
        
    }
    
    private void writeError(final String pr_Message) 
    {
        System.err.println(pr_Message);
        System.err.println("Composition Number: " + m_CompositionPointer +
                           ", Line Number: " + m_CurrentLineNumber);
    }

}
