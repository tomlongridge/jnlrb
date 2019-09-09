package linegenerator.core;

import java.util.regex.Pattern;

public class FootnoteReferenceCompositionRow extends TableCompositionRow {

    public final static Pattern FOOTNOTE_REFERENCE_REGEX = Pattern.compile("[a-rt-wyz]");
    public final static Pattern FOOTNOTE_REGEX = Pattern.compile("([a-rt-z])[ ]?\\=[ ]?([0-9,s]+)\\.?");
    
    private String m_FootnoteLabel;
    
    public FootnoteReferenceCompositionRow(final String pr_FootnoteLabel,
                                           final Method[] pr_Methods,
                                           final int pr_NumLeads,
                                           final String[] pr_Labels,
                                           boolean pr_DisplayCourseEnds) {
        
        super(null, pr_Methods, pr_NumLeads, pr_Labels, pr_DisplayCourseEnds, false);
        m_FootnoteLabel = pr_FootnoteLabel;
        
    }
    
    public String getFootnoteLabel() {
        return m_FootnoteLabel;
    }
    
    @Override
    public int size() {
        return 1;
    }
    
    @Override
    protected TableCompositionRow clone() {
        return new FootnoteReferenceCompositionRow(m_FootnoteLabel, m_Methods, m_NumLeads, m_Labels, m_DisplayCourseEnd);
    }
    
    @Override
    public String toString() {
        return m_FootnoteLabel;
    }
    
    public static boolean isFootnoteReference(final String pr_FootnoteLabel) {
        return FOOTNOTE_REFERENCE_REGEX.matcher(pr_FootnoteLabel).matches();
    }
    
    public static boolean isFootnote(final String pr_Footnote) {
        return FOOTNOTE_REGEX.matcher(pr_Footnote).matches();
    }

}
