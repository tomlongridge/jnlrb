package linegenerator.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;

import linegenerator.core.exceptions.CompositionDefinitionException;

public abstract class AbstractTableComposition extends AbstractComposition {

    protected String[] m_Headers;
    protected ArrayList<TableCompositionRow> m_Rows;
    
    protected HashMap<Method, ArrayList<HashMap<String, String>>> m_PlainLeadTransitions = new HashMap<Method, ArrayList<HashMap<String, String>>>();
    protected HashMap<Method, ArrayList<HashMap<String, String>>> m_BobLeadTransitions = new HashMap<Method, ArrayList<HashMap<String, String>>>();
    protected HashMap<Method, ArrayList<HashMap<String, String>>> m_SingleLeadTransitions = new HashMap<Method, ArrayList<HashMap<String, String>>>();
    
    public AbstractTableComposition(final HashMap<String, Method> pr_MethodLibrary, final int pr_Changes) {
        super(pr_MethodLibrary, pr_Changes);
        m_Rows = new ArrayList<TableCompositionRow>();
    }
    
    public void setHeaders(final String[] pr_Headers) {
        for (int i = 0; i < pr_Headers.length; i++) {
            pr_Headers[i] = pr_Headers[i].toUpperCase();
        }
        m_Headers = pr_Headers;
        m_IsTrue = null;
    }

    public String[] getHeaders() {
        return m_Headers;
    }

    @Override
    public int getNumRows() {
        return m_Rows.size();
    }
    
    public void addRow(final TableCompositionRow pr_Row) {
        
        if (m_Rows.size() == 0) {
            m_FirstMethod = pr_Row.getMethods()[0];
        }
        
        m_Rows.add(pr_Row);
        m_IsTrue = null;
    }

    public String[] getCalls(final int pr_Row) {
        return m_Rows.get(pr_Row).getCalls();
    }
    
    @Override
    public int getNumHeaders() {
        return m_Headers.length;
    }
    
    protected void calculateTransitions()
    {
        for (TableCompositionRow row : m_Rows) {
            
            for (Method method : row.getMethods()) {
            
                if (!m_PlainLeadTransitions.containsKey(method)) {
                    
                    Grid grid;
                    ArrayList<HashMap<String, String>> plainTransitions = new ArrayList<HashMap<String,String>>();
                    ArrayList<HashMap<String, String>> bobTransitions = new ArrayList<HashMap<String,String>>();
                    ArrayList<HashMap<String, String>> singleTransitions = new ArrayList<HashMap<String,String>>();
                    
                    for (int i = 0; i < method.getPlaceNotation().length; i++) {
                        grid = new Grid(method);
                        grid.add(method.getLeadNotation(LeadType.PLAIN, i), LeadType.PLAIN, method, 0);
                        plainTransitions.add(grid.getTransitions());
                        if (method.getBobLeadEnd() != null) {
                            grid = new Grid(method);
                            if (m_OverriddenCalls.containsKey(LeadType.BOB)) {
                                grid.add(method.createLeadNotation(m_OverriddenCalls.get(LeadType.BOB)), LeadType.BOB, method);
                            } else {
                                grid.add(method.getLeadNotation(LeadType.BOB, i), LeadType.BOB, method, 0);
                            }
                            grid.add(method.getLeadNotation(LeadType.BOB, i), LeadType.BOB, method, 0);
                            bobTransitions.add(grid.getTransitions());
                        }
                        if (method.getSingleLeadEnd() != null) {
                            grid = new Grid(method);
                            if (m_OverriddenCalls.containsKey(LeadType.SINGLE)) {
                                grid.add(method.createLeadNotation(m_OverriddenCalls.get(LeadType.SINGLE)), LeadType.SINGLE, method);
                            } else {
                                grid.add(method.getLeadNotation(LeadType.SINGLE, i), LeadType.SINGLE, method, 0);
                            }
                            grid.add(method.getLeadNotation(LeadType.SINGLE, i), LeadType.SINGLE, method, 0);
                            singleTransitions.add(grid.getTransitions());
                        }
                    }
                    
                    m_PlainLeadTransitions.put(method, plainTransitions);
                    m_BobLeadTransitions.put(method, bobTransitions);
                    m_SingleLeadTransitions.put(method, singleTransitions);
                }
            }
        }
    }
    
    protected ArrayList<TableCompositionRow> applyFootnotes()
    {
        final ArrayList<TableCompositionRow> rows = new ArrayList<TableCompositionRow>();

        for (int i = 1; i <= getNumParts(); i++) {
            for (int j = 0; j < m_Rows.size(); j++) {
                String[] row = new String[m_Rows.get(j).size()];
                for (int k = 0; k < row.length; k++) {
                    String[] calls = m_Rows.get(j).getCalls();
                    if (calls != null) {
                        row[k] = calls[k];
                        if (getGlobalSubstitutions().containsKey(row[k])) {
                            row[k] = getGlobalSubstitutions().get(row[k]);
                        }
                        if (getSubstitutions(i).containsKey(row[k])) {
                            row[k] = getSubstitutions(i).get(row[k]);
                        }
                    }
                }
                TableCompositionRow newRow = m_Rows.get(j).clone();
                // Replace any extra characters not substituted out by footnotes
                for (int k = 0; k < row.length; k++) {
                    if (row[k] != null) {
                        row[k] = row[k].replaceAll("[\\(\\)\\*]", "");
                    }
                }
                newRow.setCalls(row);
                newRow.setDisplayCourseEnd(i == 1 && newRow.shouldDisplayCourseEnd());
                rows.add(newRow);
            }
        }
        
        return rows;
    }
    
    private static boolean containsLabelReferences (final ArrayList<TableCompositionRow> pr_Rows)
    {
        for (TableCompositionRow row : pr_Rows) {
            if (row instanceof LabelledPartCompositionRow) {
                return true;
            }
        }
        return false;
    }
    
    private ArrayList<TableCompositionRow> expandParts(final ArrayList<TableCompositionRow> pr_Rows)
    {
        ArrayList<TableCompositionRow> currentRows = pr_Rows;
        boolean shouldDisplayCourseEnds = true;
        while (AbstractTableComposition.containsLabelReferences(currentRows)) {
        
            final HashMap<String,Integer> allLabels = new HashMap<String, Integer>();
            for (int i = 0; i < currentRows.size(); i++) {
                for (String label : currentRows.get(i).getLabels()) {
                    if (!allLabels.containsKey(label)) {
                        allLabels.put(label, i);
                    }
                }
            }
            
            ArrayList<TableCompositionRow> newRows = new ArrayList<TableCompositionRow>();
            LabelledPartCompositionRow partRow;
            TableCompositionRow newRow;
            for (int i = 0; i < currentRows.size(); i++) {
                if (currentRows.get(i) instanceof LabelledPartCompositionRow) {
                    partRow = (LabelledPartCompositionRow) currentRows.get(i);
                    if (allLabels.containsKey(partRow.getPart())) {
                        for (int j = 0; j < partRow.getRepetitions(); j++) {
                            for (int k = allLabels.get(partRow.getPart()); k < currentRows.size(); k++) {
                                if (currentRows.get(k).isInPart(partRow.getPart())) {
                                    newRow = currentRows.get(k).clone();
                                    newRow.setLabels(new String[0]);
                                    newRow.setDisplayCourseEnd(false);
                                    newRows.add(newRow);
                                } else {
                                    break;
                                }
                            }
                        }
                        newRows.get(newRows.size() - 1).setDisplayCourseEnd(shouldDisplayCourseEnds && i < (currentRows.size() / getNumParts()));
                    } else {
                        newRows.add(currentRows.get(i));
                    }
                } else {
                    newRows.add(currentRows.get(i));
                }
            }
            
            currentRows = newRows;
            shouldDisplayCourseEnds = false; // Only display course ends of inner rows if nested
        }
        
        return currentRows;
    }

    private ArrayList<TableCompositionRow> expandFootnoteReferences(final ArrayList<TableCompositionRow> pr_Rows) throws CompositionDefinitionException
    {
        ArrayList<TableCompositionRow> newRows = new ArrayList<TableCompositionRow>();
        
        final HashMap<String,String[]> allLabels = new HashMap<String, String[]>();
        Matcher matcher;
        for (String footnote : m_Footnotes) {
            matcher = FootnoteReferenceCompositionRow.FOOTNOTE_REGEX.matcher(footnote);
            if (matcher.matches()) {
                allLabels.put(matcher.group(1), matcher.group(2).split(","));
            }
        }
        
        ArrayList<String> headers = new ArrayList<String>(Arrays.asList(m_Headers));
        
        for (int i = 0; i < pr_Rows.size(); i++) {
            if (pr_Rows.get(i) instanceof FootnoteReferenceCompositionRow) {
                String label = ((FootnoteReferenceCompositionRow) pr_Rows.get(i)).getFootnoteLabel();
                if (allLabels.containsKey(label)) {
                    for (String call : allLabels.get(label)) {
                        if (call.startsWith("s")) {
                            call = call.substring(1);
                        }
                        if (!headers.contains(call)) {
                            int headerLead = Integer.parseInt(call);
                            for (int j = 0; j < headers.size(); j++) {
                                if (headers.get(j).matches("\\d+")) {
                                    if (Integer.parseInt(headers.get(j)) > headerLead) {
                                        headers.add(j, call);
                                        break;
                                    }
                                }
                            }
                        }
                        if (!headers.contains(call)) {
                            headers.add(call);
                        }
                    }
                }
            }
        }

        String[] calls;
        TableCompositionRow newRow;
        for (int i = 0; i < pr_Rows.size(); i++) {
            calls = new String[headers.size()];
            if (pr_Rows.get(i) instanceof FootnoteReferenceCompositionRow) {
                String label = ((FootnoteReferenceCompositionRow) pr_Rows.get(i)).getFootnoteLabel();
                if (!allLabels.containsKey(label)) {
                    throw new CompositionDefinitionException("No footnote reference found for part \"" + label + "\"");
                }
                String[] newCalls = allLabels.get(label);
                for (int j = 0; j < newCalls.length; j++) {
                    if (newCalls[j].startsWith("s")) {
                        calls[headers.indexOf(newCalls[j].substring(1))] = "s";
                    } else {
                        calls[headers.indexOf(newCalls[j])] = "-";
                    }
                }
                int numLeads;
                if (pr_Rows.get(i).getNumLeads() > -1) {
                    numLeads = pr_Rows.get(i).getNumLeads();
                } else {
                    numLeads = Integer.parseInt(newCalls[newCalls.length - 1].replace("s", ""));
                }
                newRow = new TableCompositionRow(calls,
                                                 pr_Rows.get(i).getMethods(),
                                                 numLeads,
                                                 pr_Rows.get(i).getLabels(),
                                                 pr_Rows.get(i).shouldDisplayCourseEnd(),
                                                 pr_Rows.get(i).isExpandedRow());
            } else {
                for (int j = 0; j < pr_Rows.get(i).getCalls().length; j++) {
                    if (pr_Rows.get(i).getCalls()[j] != null) {
                        calls[headers.indexOf(m_Headers[j])] = pr_Rows.get(i).getCalls()[j];
                    }
                }
                newRow = pr_Rows.get(i).clone();
                newRow.setCalls(calls);
            }
            newRows.add(newRow);
        }
        
        m_Headers = headers.toArray(new String[headers.size()]);
        return newRows;
    }
    
    protected ArrayList<TableCompositionRow> getExpandedNotation(final ArrayList<TableCompositionRow> pr_Rows) throws CompositionDefinitionException
    {
        ArrayList<TableCompositionRow> rows = expandParts(expandFootnoteReferences(pr_Rows));

        ArrayList<TableCompositionRow> newRows = new ArrayList<TableCompositionRow>();
        String[] currentRow;
        TableCompositionRow newRow;
        for (int i = 0; i < rows.size(); i++) {
            
            String[] calls = rows.get(i).getCalls();
            currentRow = new String[rows.get(i).size()];
            
            for (int j = 0; j < currentRow.length; j++) {
                
                String call = calls[j];
                
                if (call == null) {
                    currentRow[j] = null;
                    continue;
                }
                
                // Convert number to bob notation
                if (call.matches("[\\d]+")) {
                    int numberOfBobs = Integer.parseInt(call);
                    call = "";
                    for (int k = 0; k < numberOfBobs; k++) {
                        call += "-";
                    }
                }

                char[] subcalls = call.toCharArray();
                
                for (int k = 0; k < subcalls.length; k++) {

                    if (k > 0) {
                        for (int l = j + 1; l < currentRow.length; l++) {
                            currentRow[l] = null;
                        }
                        newRow = rows.get(i).clone();
                        newRow.setCalls(currentRow);
                        newRow.setDisplayCourseEnd(false);
                        newRow.setExpandedRow(true);
                        newRows.add(newRow);
                        currentRow = new String[rows.get(i).size()];
                        for (int l = 0; l < j; l++) {
                            currentRow[l] = null;
                        }
                    }
                    currentRow[j] = String.valueOf(subcalls[k]);
                    
                }
                
            }
            
            newRow = rows.get(i).clone();
            newRow.setCalls(currentRow);
            newRows.add(newRow);
            
        }
        
        return newRows;
    }
    
    public String[] getCallingPosition(final Method pr_Method,
                                       final String pr_Position,
                                       final LeadType pr_Call) throws CompositionDefinitionException
    {
        ArrayList<Integer> positions = new ArrayList<Integer>();
        String amendedPosition = pr_Method.getAmendedCallingPosition(pr_Position);
        if (amendedPosition.toUpperCase().equals("H")) {
            positions.add(pr_Method.getStage().getBells());
        } else if (amendedPosition.toUpperCase().equals("W")) {
            positions.add(pr_Method.getStage().getBells() - 1);
        } else if (amendedPosition.toUpperCase().equals("M")) {
            positions.add(pr_Method.getStage().getBells() - 2);
        } else if (amendedPosition.toUpperCase().equals("B")) {
            Grid plainCourse = new Grid(pr_Method);
            plainCourse.add(pr_Method.getLeadNotation(pr_Call, 0), LeadType.PLAIN, pr_Method);
            String leadEnd = plainCourse.getRow(plainCourse.size() - 2).substring(1, 2);
            positions.add(plainCourse.getLastChange().indexOf(leadEnd) + 1);
        } else if (amendedPosition.toUpperCase().equals("I")) {
            positions.add(2);
        } else if (amendedPosition.toUpperCase().equals("O")) {
            positions.add(3);
        } else if (amendedPosition.matches("[0-9ETA-Z]")) {
            positions.add(Stage.getPositionOfLabel(amendedPosition.charAt(0)));
        }
        
        String[] labels = new String[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            if ((positions.get(i) < 0) ||  (positions.get(i) > pr_Method.getStage().getBells())) {
                throw new CompositionDefinitionException("Unrecognised calling position: " + pr_Position);
            }
            labels[i] = Stage.getLabelAtPosition(positions.get(i));
        }
        
        return labels;
    }

    public String getCourseEnd(final int pr_Row) {
        int courseEndIndex = 0;
        for (TableCompositionRow row : m_Rows) {
            if (row.shouldDisplayCourseEnd()) {
                if (courseEndIndex == pr_Row) {
                    break;
                }
                courseEndIndex++;
            }
        }
        if (courseEndIndex < m_CourseEnds.size()) {
            return m_CourseEnds.get(courseEndIndex);
        } else {
            return "";
        }
        
    }
    
    @Override
    public String getHeaderAsString() {
        StringBuilder returnString = new StringBuilder();
        if (m_Headers != null) {
            for (String s : m_Headers) {
                returnString.append(s.toUpperCase());
                returnString.append("\t");
            }
        }
        if (m_FirstChange != null) {
            returnString.append(m_FirstChange);
        }
        returnString.append("\n");
        return returnString.toString();
    }

    @Override
    public String getRowAsString(final int pr_Row) {
        StringBuilder returnString = new StringBuilder();
        returnString.append(m_Rows.get(pr_Row));
        if (pr_Row < m_CourseEnds.size() && m_CourseEnds.get(pr_Row) != null) {
            if (!m_CourseEndsComplete.get(pr_Row)) {
                returnString.append("(");
            }
            returnString.append(m_CourseEnds.get(pr_Row));
            if (!m_CourseEndsComplete.get(pr_Row)) {
                returnString.append(")");
            }
        }
        returnString.append("\n");
        return returnString.toString();
    }
    
    @Override
    public String getCompositionAsString() {
        StringBuilder returnString = new StringBuilder();
        if (m_Rows != null) {
            for (int i = 0; i < m_Rows.size(); i++) {
                returnString.append(getRowAsString(i));
            }
        }
        return returnString.toString();
    }

}
