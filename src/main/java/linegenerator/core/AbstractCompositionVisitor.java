package linegenerator.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import linegenerator.core.exceptions.CompositionDefinitionException;

public abstract class AbstractCompositionVisitor {

    private enum State
    {
        IN_COMPOSITION_LIST,
        IN_COMPOSITION_HEADER,
        IN_COMPOSITION,
        IN_FOOTNOTES;
    }
    
    public enum CompositionType
    {
        SIMPLE,
        ROW_COUNT_TABLE,
        TABLE,
        SHORT_HAND;
    }

    private static final String SHORT_HAND_PREFIX = "$";
    private static final String COMMENT_PREFIX = "#";
    private static final String SUBSTITUTION_CHARS = "[\\*\\+]";
    
    private static final String COURSE_END_PATTERN = "[\\(]?[\\dETA-Z]{3,}[\\)]?";
    private static final String METHOD_LABEL_PATTERN = "(?:[A-Za-z]{1,2})?";
    private static final String METHOD_LIST_PATTERN = "[\\(]?[A-Z][a-z]?\\*?[\\)]?";
    
    private static final Pattern METHOD_LINE_REGEX = Pattern.compile("\\[(?:(" + METHOD_LABEL_PATTERN + "?)\\=)?([\\w\\s\\'\\)\\(\\.]+)\\,[\\s]*([\\d]+)\\]");
    private static final Pattern SIMPLE_HEADER_REGEX = Pattern.compile("(?:\\t(" + COURSE_END_PATTERN + "))?\\t*((?:" + METHOD_LIST_PATTERN + ")+)?");
    private static final Pattern SIMPLE_ROW_REGEX = Pattern.compile("([\\-s]" + SUBSTITUTION_CHARS + "?)?(?:\\t(" + COURSE_END_PATTERN + "))?\\t*([0-9]{1,2}|(?:" + METHOD_LIST_PATTERN + ")+)?");
    private static final Pattern TABLE_ROW_REGEX = Pattern.compile("((?:(?:[\\(]?(?:[1-9\\-sx]|ss)?[" + SUBSTITUTION_CHARS + "\\)]?(?:\\t+|$))+(?:[\\(]?(?:[1-9\\-sx]|ss)[" + SUBSTITUTION_CHARS + "\\)]?(?:\\t+|$))?)|[1-9]?[A-Z](?:\\t+|$)|[a-rt-z](?:\\t+|$))(?:((?:" + METHOD_LIST_PATTERN + ")+)(?:\\t+|$))?(?:(" + COURSE_END_PATTERN + ")(?:\\t+|$))?(?:\\[([0-9]+)\\](?:\\t|$))?((?:\\:[A-Z](?:\\t+|$))*)?");
    private static final Pattern CHANGES_REGEX = Pattern.compile("(\\d+)( \\([\\d\\w\\-]+\\))?");
    
    public static final Pattern FOOTNOTE_XPART_REGEX = Pattern.compile("([0-9]+) (P|p)art[.]?");
    public static final Pattern FOOTNOTE_SUBSTITUTIONS_REGEX = Pattern.compile("([s\\-0-9ETA-Z]" + SUBSTITUTION_CHARS + ")\\s*=\\s*([\\-s]+)(?: in part[s]? ([0-9]{1,2}(?:(?:\\,| and) [0-9]{1,2})*)(?: only)?)?.*");
    public static final Pattern FOOTNOTE_CALL_OVERRIDE_REGEX = Pattern.compile("([s\\-])\\s*=\\s*([0-9\\.]+).*");
    public static final Pattern FOOTNOTE_SELECTIVE_PARTS_REGEX = Pattern.compile("(Omit )?([-s0-9]*" + SUBSTITUTION_CHARS + "?)(?: in part[s]? ([0-9]{1,2}(?:(?:\\,| and) [0-9]{1,2})*)(?: only)?)?.*");    
    protected File m_InputFile;
    protected File m_OutputFile;
    protected Method[] m_MethodLibrary;
    protected HashMap<String, Method> m_MethodTable;
    protected Method m_LastMethod;
    
    public AbstractCompositionVisitor(final Method[] pr_MethodLibrary) {
        m_MethodLibrary = pr_MethodLibrary;
        m_MethodTable = new HashMap<String, Method>();
        m_LastMethod = null;
    }
    
    public void visit(final File pr_InputFile) throws CompositionDefinitionException, IOException
    {
        visit(pr_InputFile, pr_InputFile);
    }
        
    public void visit(final File pr_InputFile, final File pr_OutputFile) throws CompositionDefinitionException, IOException
    {
        m_InputFile = pr_InputFile;
        m_OutputFile = pr_OutputFile;
        
        final BufferedReader reader = new BufferedReader(new FileReader(m_InputFile));
        
        String line;
        State currentState = State.IN_COMPOSITION_LIST;
        String courseEnd;
        boolean isCompleteCourseEnd;
        int compositionRow = 0;
        Matcher matcher;
        int lineNumber = 0;
        String firstChange = null;
        int plainLeadCount;
        String methodLabel;
        int numLeads;
        String[] headers = null;
        CompositionType compositionType = CompositionType.SIMPLE;
        String[] partLabels;
        ArrayList<String> compositionPartLabels = null;
        String labelledPart;
        String[] tableCalls;
        boolean foundMethod;
        
        while ((line = reader.readLine()) != null) {
            
            lineNumber++;
            
            switch(currentState) {
            
            case IN_COMPOSITION_LIST:

                if (line.equals("") || line.startsWith(COMMENT_PREFIX)) {
                    lineStarted(lineNumber, line);
                    lineFinished();
                    break;
                }
                
                matcher = METHOD_LINE_REGEX.matcher(line);
                if (matcher.matches()) {
                    
                    lineStarted(lineNumber, line);
                    Stage foundStage = Stage.getStage(matcher.group(matcher.groupCount()));
                    foundMethod = false;
                    for (Method m : m_MethodLibrary) {
                        if (m.getStage() == foundStage) {
                            String foundName = matcher.group(matcher.groupCount() - 1);
                            if (m.getMethodType().shouldDisplayNameInMethod()) {
                                if (!foundName.endsWith(m.getMethodType().toString())) {
                                    continue;
                                } else {
                                    foundName = foundName.substring(0, foundName.indexOf(m.getMethodType().toString()));
                                }
                            }
                            if (foundName.trim().equals(m.getName())) {
                                if (matcher.groupCount() == 3) {
                                    methodFound(m, matcher.group(1));
                                } else {
                                    methodFound(m, null);
                                }
                                m_LastMethod = m;
                                foundMethod = true;
                                break;
                            }
                        }
                    }
                    if (!foundMethod) {
                        throw new CompositionDefinitionException("Unrecognised method reference: ", line, lineNumber);
                    }
                    lineFinished();
                    break;
                    
                } else {
                    
                    if (m_MethodTable.isEmpty()) {
                        throw new CompositionDefinitionException("No method definitions found.", lineNumber);
                    }
                
                    currentState = State.IN_COMPOSITION_HEADER;
                    
                    matcher = CHANGES_REGEX.matcher(line);
                    if (matcher.matches()) {
                        lineStarted(lineNumber, line);
                        changesFound(Integer.parseInt(matcher.group(1)));
                        lineFinished();
                        break;
                    }
                
                }
                
            case IN_COMPOSITION_HEADER:

                if (line.startsWith(COMMENT_PREFIX)) {
                    lineStarted(lineNumber, line);
                    lineFinished();
                    break;
                } else if (line.equals("")) {
                    throw new CompositionDefinitionException("Unexpected blank line in composition.", lineNumber);
                }
                
                compositionRow = 0;
                if (line.startsWith(SHORT_HAND_PREFIX)) {
                    compositionType = CompositionType.SHORT_HAND;
                } else if ((LeadType.getLeadType(line.substring(0, 1).trim()) != null) ||
                           ((line.length() == 1) && (line.matches("[A-Z]")))) {
                    compositionType = CompositionType.SIMPLE;
                } else {
                    if (line.matches("[0-9ETSLQ\t][0-9ETSLQH\t]*")) {
                        // Numbers and Stedman calls only in header
                        compositionType = CompositionType.ROW_COUNT_TABLE;
                    } else {
                        compositionType = CompositionType.TABLE;                        
                    }
                }
                
                compositionStarted(compositionType);
                currentState = State.IN_COMPOSITION;
                compositionPartLabels = new ArrayList<String>();

                if ((compositionType == CompositionType.TABLE) ||
                    (compositionType == CompositionType.ROW_COUNT_TABLE)) {
                    
                    lineStarted(lineNumber, line);

                    headers = line.split("\\t");
                    if (headers[headers.length - 1].matches("\\(?([\\dETA-Z]{3,})\\)?")) {
                        firstChange = headers[headers.length - 1].replaceAll("[\\(\\)]", "");
                        headers = Arrays.copyOfRange(headers, 0, headers.length - 1);
                    } else {
                        firstChange = null;
                    }
                    
                    headersFound(headers, firstChange);
                    
                    lineFinished();
                    break;
                    
                } else if (compositionType == CompositionType.SIMPLE) {
                    
                    matcher = SIMPLE_HEADER_REGEX.matcher(line);
                    if (matcher.matches()) {

                        if (matcher.groupCount() == 2) {
                            firstChange = matcher.group(1).replaceAll("[\\(\\)]", "");
                        } else {
                            firstChange = null;
                        }
                        
                        foundMethod = false;
                        String lastColumn = matcher.group(matcher.groupCount());
                        if ((lastColumn != null) && lastColumn.matches("[A-Za-z]{1,2}\\*?")) {
                            lastColumn = lastColumn.replaceAll("\\*","");
                            if (m_MethodTable.containsKey(lastColumn)) {
                                m_LastMethod = m_MethodTable.get(lastColumn);
                                foundMethod = true;
                            } else {
                                throw new CompositionDefinitionException("The method label \"" + lastColumn + "\" is undefined.", lineNumber);
                            }
                        }
                        
                        if (firstChange != null || foundMethod) {
                            lineStarted(lineNumber, line);
                            headersFound(firstChange, m_LastMethod);
                            lineFinished();
                            break;
                        }
                    }
                }
                    
            case IN_COMPOSITION:
                
                if (line.startsWith(COMMENT_PREFIX)) {
                    lineStarted(lineNumber, line);
                    lineFinished();
                    break;
                } else if (line.equals("")) {
                    if (compositionRow == 0) {
                        throw new CompositionDefinitionException("No rows found in composition.", lineNumber);
                    }
                    compositionFinished();
                    currentState = State.IN_COMPOSITION_LIST;
                    break;
                }
                
                if (line.startsWith(SHORT_HAND_PREFIX)) {
                    compositionType = CompositionType.SHORT_HAND;
                }

                if ((compositionType == CompositionType.TABLE) ||
                    (compositionType == CompositionType.ROW_COUNT_TABLE)) {

                    lineStarted(lineNumber, line);
                    
                    matcher = TABLE_ROW_REGEX.matcher(line);
                    if (matcher.matches()) {
                
                        ArrayList<String> methodLabels = new ArrayList<String>();
                        ArrayList<Method> methods = new ArrayList<Method>();

                        if (matcher.group(2) != null) {
                            
                            String methodsString = matcher.group(2).replaceAll("[\\(|\\)]", "");
                            methodLabel = null;
                            for (char m : methodsString.toCharArray()) {
                                
                                if (Character.isUpperCase(m)) {
                                    if (methodLabel != null) {
                                        if (m_MethodTable.containsKey(methodLabel)) {
                                            methodLabels.add(methodLabel);
                                            methods.add(m_MethodTable.get(methodLabel));
                                        } else {
                                            throw new CompositionDefinitionException("The method label \"" + methodLabel + "\" is undefined.", lineNumber);
                                        }
                                    }
                                    methodLabel = String.valueOf(m);
                                } else if ((m != '*') && (methodLabel != null)) {
                                    methodLabel += String.valueOf(m);
                                }
                            }
                            if (methodLabel != null) {
                                if (m_MethodTable.containsKey(methodLabel)) {
                                    methodLabels.add(methodLabel);
                                    methods.add(m_MethodTable.get(methodLabel));
                                } else {
                                    throw new CompositionDefinitionException("The method label \"" + methodLabel + "\" is undefined.", lineNumber);
                                }
                            }
                            
                            Pattern pat = Pattern.compile("([A-Z][a-z]?)+");
                            Matcher methodsMatcher = pat.matcher(methodsString);
                            if (methodsMatcher.matches()) {
                                for (int i = 1; i < methodsMatcher.groupCount(); i++) {
                                    if (m_MethodTable.containsKey(methodsMatcher.group(i))) {
                                        methodLabels.add(methodsMatcher.group(i));
                                        methods.add(m_MethodTable.get(methodsMatcher.group(i)));
                                        methodLabel = null;
                                    } else {
                                        throw new CompositionDefinitionException("The method label \"" + methodsMatcher.group(i) + "\" is undefined.", lineNumber);
                                    }
                                }
                            }
                            
                        } else {
                            methods.add(m_LastMethod);
                        }

                        String[] calls = matcher.group(1).split("\\t");
                        if (calls.length == 0) {
                            tableCalls = null;
                            labelledPart = null;
                        } else if ((calls.length == 1) && 
                            LabelledPartCompositionRow.isLabelledPart(calls[0]) ||
                            FootnoteReferenceCompositionRow.isFootnoteReference(calls[0])) {
                            tableCalls = null;
                            labelledPart = calls[0];
                        } else {
                            tableCalls = new String[headers.length];
                            labelledPart = null;
                            for (int i = 0; i < tableCalls.length; i++) {
                                if ((i < calls.length) && !calls[i].equals("")) {
                                    tableCalls[i] = calls[i];
                                } else {
                                    tableCalls[i] = null;
                                }
                            }
                        }
                        
                        courseEnd = null;
                        isCompleteCourseEnd = true;
                        if (matcher.group(3) != null) {
                            courseEnd = matcher.group(3);
                            if (courseEnd.startsWith("(")) {
                                isCompleteCourseEnd = false;
                                courseEnd = courseEnd.substring(1, courseEnd.length() - 1);
                            }
                        }
                        
                        if (matcher.group(4) != null) {
                            numLeads = Integer.parseInt(matcher.group(4));
                        } else {
                            numLeads = -1;
                        }
                        
                        if (!matcher.group(5).equals("")) {
                            partLabels = matcher.group(5).replaceAll("\t", "").substring(1).split(":");
                            compositionPartLabels.addAll(Arrays.asList(partLabels));
                        } else {
                            partLabels = new String[0];
                        }
                        
                        compositionRowFound(compositionRow++,
                                            tableCalls,
                                            labelledPart,
                                            methodLabels,
                                            methods, 
                                            courseEnd,
                                            partLabels,
                                            numLeads,
                                            isCompleteCourseEnd);
                        
                        lineFinished();
                        break;
                    }
                
                } else if (compositionType == CompositionType.SIMPLE) {

                    lineStarted(lineNumber, line);
                
                    matcher = SIMPLE_ROW_REGEX.matcher(line);
                    if (matcher.matches()) {
                        
                        courseEnd = null;
                        isCompleteCourseEnd = true;
                        if (matcher.group(2) != null) {
                            courseEnd = matcher.group(2);
                            if (courseEnd.startsWith("(")) {
                                isCompleteCourseEnd = false;
                                courseEnd = courseEnd.substring(1, courseEnd.length() - 1);
                            }
                        }
    
                        plainLeadCount = 1;
                        methodLabel = null;
    
                        if (matcher.group(3) != null) {
                            if (matcher.group(3).matches("\\d+")) {
                                plainLeadCount = Integer.parseInt(matcher.group(3));
                            } else if (matcher.group(3).matches("[A-Za-z]{1,2}")) {
                                methodLabel = matcher.group(3).replaceAll("\\*","");
                                if (m_MethodTable.containsKey(methodLabel)) {
                                    m_LastMethod = m_MethodTable.get(methodLabel);
                                } else {
                                    throw new CompositionDefinitionException("The method label \"" + matcher.group(3) + "\" is undefined.", lineNumber);
                                }
                            } else {
                                throw new CompositionDefinitionException("The plain lead count column contains an unexpected character: " + matcher.group(3), lineNumber);
                            }
                        }
                            
                        compositionRowFound(compositionRow++,
                                            matcher.group(1),
                                            methodLabel,
                                            m_LastMethod,
                                            courseEnd,
                                            isCompleteCourseEnd,
                                            plainLeadCount);
                        
                        lineFinished();
                        break;
                    
                    }
                } else if (compositionType == CompositionType.SHORT_HAND) {
                    
                    lineStarted(lineNumber, line);
                    compositionRow = 1;
                    compositionRowFound(line.substring(1));
                    lineFinished();
                    currentState = State.IN_FOOTNOTES;
                    break;
                    
                } else {
                    throw new CompositionDefinitionException("Unsupported composition type found: " + compositionType, lineNumber);
                }
                
                currentState = State.IN_FOOTNOTES;
                    
            case IN_FOOTNOTES:
                
                if (line.startsWith(COMMENT_PREFIX)) {
                    
                    lineStarted(lineNumber, line);
                    lineFinished();
                    break;
                    
                } else if (line.equals("")) {
                    
                    if (compositionRow == 0) {
                        throw new CompositionDefinitionException("No rows found in composition.", lineNumber);
                    }
                    compositionFinished();
                    currentState = State.IN_COMPOSITION_LIST;
                    break;
                    
                } else {

                    lineStarted(lineNumber, line);
                    
                    // Check whether the line looks like a footnote
                    // (The FOOTNOTE_SELECTIVE_PARTS_REGEX is too inclusive, so checking for key words in this only.)
                    if (line.matches(FOOTNOTE_XPART_REGEX.pattern()) ||
                        line.matches(FOOTNOTE_SUBSTITUTIONS_REGEX.pattern()) || 
                        line.matches(FOOTNOTE_CALL_OVERRIDE_REGEX.pattern()) ||
                        (line.contains("Omit") || line.contains("part") || line.contains("and") || line.contains("only")) ||
                        FootnoteReferenceCompositionRow.isFootnote(line)) {
                        foundFootnote(line);
                    } else {
                        throw new CompositionDefinitionException(MessageFormat.format("Unrecognised footnote found \"{0}\".", line), lineNumber);
                    }
                    
                    lineFinished();
                    break;
                }
            }
        }
        
        if ((currentState == State.IN_COMPOSITION) || currentState == State.IN_FOOTNOTES) {
            compositionFinished();
        }
        
        reader.close();
        stop();
        
    }
    
    public void methodFound(final Method pr_Method, final String pr_Label)
    {
        m_MethodTable.put(pr_Label, pr_Method);
    }
    
    public abstract void lineStarted(final int pr_LineNumber, final String pr_Line);

    public abstract void changesFound(final int pr_Changes);
    
    public abstract void compositionStarted(final CompositionType pr_Type);

    public abstract void headersFound(final String pr_FirstChange, final Method pr_FirstMethod);

    public abstract void headersFound(final String[] pr_Headers, final String pr_FirstChange);
    
    public abstract void compositionRowFound(final String pr_ShortHand) throws CompositionDefinitionException;

    public abstract void compositionRowFound(final int pr_Row,
                                             final String pr_Call,
                                             final String pr_MethodChangeLabel,
                                             final Method pr_MethodChange,
                                             final String pr_CourseEnd,
                                             final boolean pr_IsCompleteCourseEnd,
                                             final int pr_PlainLeadCount);

    public abstract void compositionRowFound(final int pr_Row,
                                             final String[] pr_Calls,
                                             final String pr_Part,
                                             final ArrayList<String> pr_MethodChangeLabels,
                                             final ArrayList<Method> pr_MethodChanges,
                                             final String pr_CourseEnd,
                                             final String[] pr_PartLabels,
                                             final int pr_NumLeads,
                                             final boolean pr_IsCompleteCourseEnd);

    public abstract void foundFootnote(final String pr_Footnote);
    
    public abstract void lineFinished();
    
    public abstract void compositionFinished();
    
    public abstract void stop();
}
