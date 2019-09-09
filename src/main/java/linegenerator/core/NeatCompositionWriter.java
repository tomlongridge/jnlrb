package linegenerator.core;

import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;

import linegenerator.core.exceptions.CompositionDefinitionException;

public class NeatCompositionWriter extends AbstractCompositionVisitor {

    private StringBuilder m_OutputText;
    private String m_CurrentOutput;
    private boolean m_Overwrite;
    private boolean m_InIgnoreSection;
    
    public NeatCompositionWriter(final Method[] pr_MethodLibrary,
                                 final boolean pr_Overwrite) {
        super(pr_MethodLibrary);
        m_Overwrite = pr_Overwrite;
        m_OutputText = new StringBuilder();
        m_InIgnoreSection = false;
    }
    
    @Override
    public void methodFound(Method pr_Method, String pr_Label) {
        super.methodFound(pr_Method, pr_Label);
        m_CurrentOutput = pr_Method.toString() + "\n" + pr_Method.getDescription();
    }

    @Override
    public void lineStarted(int pr_LineNumber, String pr_Line) {
        if (pr_Line.startsWith("#")) {
            if (!m_InIgnoreSection) {
                int i;
                for (i = 1; i < pr_Line.length(); i++) {
                    if (pr_Line.charAt(i) != ' ') {
                        break;
                    }
                }
                m_CurrentOutput = pr_Line.substring(i);
            }
            if (pr_Line.endsWith("!")) {
                m_InIgnoreSection = !m_InIgnoreSection;
                m_CurrentOutput = "";
            }
        } else {
            m_CurrentOutput = pr_Line;            
        }
    }
    
    @Override
    public void changesFound(int pr_Changes) {
    }

    @Override
    public void compositionStarted(CompositionType pr_Type) {
    }

    @Override
    public void headersFound(String pr_FirstChange, Method pr_FirstMethod) {
    }

    @Override
    public void headersFound(String[] pr_Headers, String pr_FirstChange) {
        m_CurrentOutput = "\t" + m_CurrentOutput;
    }

    @Override
    public void compositionRowFound(String pr_ShortHand)
            throws CompositionDefinitionException {
    }

    @Override
    public void compositionRowFound(int pr_Row, String pr_Call,
            String pr_MethodChangeLabel, Method pr_MethodChange,
            String pr_CourseEnd, boolean pr_IsCompleteCourseEnd,
            int pr_PlainLeadCount) {
    }

    @Override
    public void compositionRowFound(int pr_Row, String[] pr_Calls,
            String pr_Part, ArrayList<String> pr_MethodChangeLabels,
            ArrayList<Method> pr_MethodChanges, String pr_CourseEnd,
            String[] pr_PartLabels, int pr_NumLeads,
            boolean pr_IsCompleteCourseEnd) {
        m_CurrentOutput = "\t" + m_CurrentOutput;
    }

    @Override
    public void foundFootnote(String pr_Footnote) {
    }

    @Override
    public void lineFinished() {
        if (!m_InIgnoreSection && !m_CurrentOutput.equals("")) {
            m_OutputText.append(m_CurrentOutput);
            m_OutputText.append("\n");
        }
    }

    @Override
    public void compositionFinished() {
    }

    @Override
    public void stop() {
        try {
            final FileWriter writer = new FileWriter(m_OutputFile, !m_Overwrite);
            writer.write(m_OutputText.toString());
            writer.close();
        } catch (IOException e) {
            System.err.println(MessageFormat.format("Unable to output composition file for {0}: {1}", m_OutputFile.getAbsolutePath(), e.getMessage()));
        }
    }


}
