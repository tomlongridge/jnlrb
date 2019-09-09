package linegenerator.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LabelledPartCompositionRow extends TableCompositionRow {

    public final static Pattern LABELLED_PART_REGEX = Pattern.compile("([1-9])?([A-Z])");
    
    private String m_Part;
    private int m_Repetitions;
    
    public LabelledPartCompositionRow(final String pr_Part,
                                      final int pr_Repetitions,
                                      final Method[] pr_Methods,
                                      final String[] pr_Labels,
                                      boolean pr_DisplayCourseEnds) {
        super(null, pr_Methods, -1, pr_Labels, pr_DisplayCourseEnds, false);
        m_Part = pr_Part;
        m_Repetitions = pr_Repetitions;
    }
    
    public LabelledPartCompositionRow(final String pr_Part,
                                      final Method[] pr_Methods,
                                      final String[] pr_Labels,
                                      boolean pr_DisplayCourseEnds) {
        
        super(null, pr_Methods, -1, pr_Labels, pr_DisplayCourseEnds, false);
        Matcher matcher = LABELLED_PART_REGEX.matcher(pr_Part);
        if (matcher.matches()) {
            if (matcher.group(1) != null) {
                m_Repetitions = Integer.parseInt(matcher.group(1));
            } else {
                m_Repetitions = 1;
            }
            m_Part = matcher.group(2);
        } else {
            throw new IllegalArgumentException("Created a labelled composition with invalid syntax.");
        }
        
    }
    
    public String getPart() {
        return m_Part;
    }
    
    public int getRepetitions() {
        return m_Repetitions;
    }
    
    @Override
    public int size() {
        return 1;
    }
    
    @Override
    protected TableCompositionRow clone() {
        return new LabelledPartCompositionRow(m_Part, m_Repetitions, m_Methods, m_Labels, m_DisplayCourseEnd);
    }
    
    @Override
    public String toString() {
        if (m_Repetitions == 1) {
            return m_Part;
        } else {
            return m_Repetitions + m_Part;
        }
    }
    
    public static boolean isLabelledPart(final String pr_Part) {
        return LABELLED_PART_REGEX.matcher(pr_Part).matches();
    }

}
