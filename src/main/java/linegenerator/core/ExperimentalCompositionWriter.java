package linegenerator.core;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import linegenerator.core.exceptions.CompositionDefinitionException;
import linegenerator.core.exceptions.FalseGridException;

public class ExperimentalCompositionWriter extends AbstractCompositionVisitor {
    
    private AbstractComposition[] m_Compositions;
    private int m_CompositionPointer;
    private AbstractComposition m_CurrentComposition;
    private boolean m_IsCompositionValid;
    
    private String m_CurrentLine;
    private StringBuilder m_CurrentOutput;
    
    public ExperimentalCompositionWriter(final Method[] pr_MethodLibrary,
                                         final AbstractComposition[] pr_Compositions) {
        super(pr_MethodLibrary);
        m_Compositions = pr_Compositions;
        m_CompositionPointer = 0;
        m_CurrentOutput = new StringBuilder();
        m_IsCompositionValid = true;
    }

    @Override
    public void lineStarted(final int pr_LineNumber, final String pr_Line) {
        m_CurrentLine = pr_Line;
    }
    
    @Override
    public void changesFound(int pr_Changes) {
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
            
    }

    @Override
    public void headersFound(final String pr_FirstChange, final Method pr_FirstMethod)
    {
    }

    @Override
    public void headersFound(final String[] pr_Headers, final String pr_FirstChange)
    {
    }
    
    @Override
    public void compositionRowFound(final int pr_Row, 
                                    final String pr_Call,
                                    final String pr_MethodChangeLabel,
                                    final Method pr_MethodChange,
                                    final String pr_CourseEnd, 
                                    final boolean pr_IsCompleteCourseEnd,
                                    final int pr_PlainLeadCount) {
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
    }
    
    public void compositionRowFound(final String pr_ShortHand)
    {
    }

    public void foundFootnote(final String pr_Footnote)
    {
        // Do nothing
    }

    @Override
    public void lineFinished() {
        if (m_IsCompositionValid) {
            m_CurrentOutput.append(m_CurrentLine);
            m_CurrentOutput.append("\n");
        }
    }

    @Override
    public void compositionFinished() {
        if (m_IsCompositionValid) {
            m_CurrentOutput.append("\n");
        }
    }

    @Override
    public void stop() {

        try {
            FileWriter writer = new FileWriter(m_OutputFile);
            writer.write(m_CurrentOutput.toString().trim());
            writer.close();
        } catch (IOException e) {
            System.out.println("Unable to write to composition file: " + e.getMessage());
        }
        
    }
}
