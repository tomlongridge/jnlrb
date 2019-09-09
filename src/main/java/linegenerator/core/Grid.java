package linegenerator.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import linegenerator.core.exceptions.DoesNotEndInRoundsException;
import linegenerator.core.exceptions.FalseGridException;
import linegenerator.core.exceptions.RepeatedChangeException;

/**
 * Represents the a grid of numbers generated for a method.
 * 
 * @author Tom
 */
public class Grid extends ArrayList<String> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Stores where the lead ends are in grid. */
	protected ArrayList<Boolean> m_LeadEnds;
    
    /** Stores where the labels are in grid. */
	protected ArrayList<Boolean> m_Labels;
    
	protected ArrayList<LeadType> m_Calls;
	protected ArrayList<Method> m_Methods;
	
	private Stage m_Stage;
	
	private String m_FirstChange;
	
	/**
	 * Constructor.
	 */
	public Grid(final Method pr_FirstMethod) {
	    this(pr_FirstMethod.getStage().getFirstChange(), pr_FirstMethod);        
    }
    
    /**
     * Constructor.
     */
    public Grid(final String pr_FirstChange, final Method pr_FirstMethod) {
        
        m_LeadEnds = new ArrayList<Boolean>();
        m_Labels = new ArrayList<Boolean>();
        m_Calls = new ArrayList<LeadType>();
        m_Methods = new ArrayList<Method>();

        m_Stage = pr_FirstMethod.getStage();
        m_FirstChange = pr_FirstChange;
        
        StringBuilder firstChange = new StringBuilder();
        String label;
        for (int i = 1; i <= m_Stage.getBells(); i++) {
            label = Stage.getLabelAtPosition(i);
            if (!pr_FirstChange.contains(label)) {
                firstChange.append(label);
            } else {
                break;
            }
        }
        
        firstChange.append(pr_FirstChange);
        
        for (int i = firstChange.length() + 1; i <= m_Stage.getBells(); i++) {
            firstChange.append(Stage.getLabelAtPosition(i));
        }
        
        m_FirstChange = firstChange.toString();
        
        add(m_FirstChange, pr_FirstMethod, false, true);
        
    }
    
    public String add(final Notation pr_Notation, final LeadType pr_LeadType, final Method pr_Method) 
    {
        return add(pr_Notation, pr_LeadType, pr_Method, 0, pr_Notation.size() - 1);
    }
    
    public String add(final Notation pr_Notation, final LeadType pr_LeadType, final Method pr_Method, final int pr_Start)
    {
        return add(pr_Notation, pr_LeadType, pr_Method, pr_Start, pr_Notation.size() - 1);
    }
	
	public String add(final Notation pr_Notation, final LeadType pr_LeadType, final Method pr_Method, final int pr_Start, final int pr_End)
	{
        String currentChange = getLastChange();
        for (int i = pr_Start; i <= pr_End; i++) {
            currentChange = cross(currentChange, pr_Notation.get(i), m_Stage);
            add(currentChange, i == pr_End ? pr_LeadType : LeadType.PLAIN, pr_Method, i == pr_Notation.size() - 1, size() % pr_Notation.size() == 0);
        }
        
        return currentChange;
	}
    
    /**
     * Performs the cross operation on the specified change.
     * @param pr_Change The change to operate on
     * @param pr_Places The places to keep the same
     * @return The new change
     */
    private String cross(final String pr_Change, final String pr_Places, final Stage pr_Stage) {

        /* 
         * Calculate the external places:
         * - If the first place in the row is even, add a place at the start
         * - For even bell methods, if the last place in the row is odd, add a place at the end
         * - For odd bell methods, if the last place is even, add a place at the end
         */
        StringBuffer places = null;
        if (pr_Places != null) {
            places = new StringBuffer(pr_Places);
            int firstPlace = Stage.getPositionOfLabel(places.charAt(0));
            int lastPlace = Stage.getPositionOfLabel(places.charAt(places.length() - 1));
            if ((firstPlace % 2) == 0) {
                // Add external place at the beginning
                places.insert(0, "1");
            } else if ((((pr_Stage.getBells() % 2) == 0) && ((lastPlace % 2) == 1)) 
                       || (((pr_Stage.getBells() % 2) == 1) && ((lastPlace % 2) == 0))) {
                // Add external place at the end
                places.append(pr_Stage.getBells());
            }
        }
        
        /*
         * Perform the cross using the places
         */
        StringBuffer newChange = new StringBuffer();
        for (int i = 1; i <= pr_Change.length(); i++) {
            
            if ((places != null) && places.indexOf(Stage.getLabelAtPosition(i)) > -1) {
                newChange.append(pr_Change.charAt(i - 1));
            } else {
                if (i < pr_Change.length()) {
                    newChange.append(pr_Change.charAt(i));
                }
                newChange.append(pr_Change.charAt(i - 1));
                if (i < pr_Change.length()) {
                    i++;
                }
            }
        }
        
        return newChange.toString();
        
    }

	/**
	 * Adds a row to the grid.
	 * @param pr_Row the row to add
	 * @param pr_LeadEnd whether the row is a lead end
	 */
	public final String add(final String pr_Row, final Method pr_Method, final boolean pr_LeadEnd, final boolean pr_Label) {
		return add(pr_Row, LeadType.PLAIN, pr_Method, pr_LeadEnd, pr_Label);
	}

	/**
	 * Adds a row to the grid.
	 * @param pr_Row the row to add
	 * @param pr_LeadEnd whether the row is a lead end
	 */
	public final String add(final String pr_Row, final LeadType pr_LeadType, final Method pr_Method, final boolean pr_LeadEnd, final boolean pr_Label) {
	    m_Labels.add(pr_Label);
		add(pr_Row);
		m_LeadEnds.add(pr_LeadEnd);
		m_Calls.add(pr_LeadType);
		m_Methods.add(pr_Method);
		return getLastChange();
	}
	
	public final String add(final Grid pr_Grid)
	{
	    m_Labels.addAll(pr_Grid.m_Labels.subList(1, pr_Grid.m_Labels.size()));
	    addAll(pr_Grid.subList(1, pr_Grid.size()));
	    m_LeadEnds.addAll(pr_Grid.m_LeadEnds.subList(1, pr_Grid.m_LeadEnds.size()));
	    m_Calls.addAll(pr_Grid.m_Calls.subList(1, pr_Grid.m_Calls.size()));
	    m_Methods.addAll(pr_Grid.m_Methods.subList(1, pr_Grid.m_Methods.size()));
	    return getLastChange();
	}
    
    /**
     * Gets the specified row from the grid.
     * @param pr_Index the row to return
     * @return the row
     */
    public final String getRow(final int pr_Index) {
        return get(pr_Index);
    }

    public void removeRow(final int pr_Index) {
        remove(pr_Index);
        m_Labels.remove(pr_Index);
        m_LeadEnds.remove(pr_Index);
    }
    
    /**
     * Determines whether the selected row is a lead end.
     * @param pr_Index the row to check
     * @return whether it is a lead end
     */
    public final boolean isLeadEnd(final int pr_Index) {
        return m_LeadEnds.get(pr_Index);
    }
    
    /**
     * Determines whether the selected row is labelled.
     * @param pr_Index the row to check
     * @return whether it is a lead end
     */
    public final boolean isLabel(final int pr_Index) {
        return m_Labels.get(pr_Index);
    }
	
    public String getFirstLeadEnd() {
        for (int i = 0; i < size(); i++) {
            if (m_LeadEnds.get(i)) {
                return get(i);
            }
        }
        return get(size() - 1);
    }
    
    public String getLastLeadEnd() {
        for (int i = size() - 1; i >= 0; i--) {
            if (m_LeadEnds.get(i)) {
                return get(i);
            }
        }
        return get(0);
    }
	
	public HashMap<String, String> getTransitions()
	{
	    final HashMap<String, String> transitions = new HashMap<String, String>();
	    final String leadHead = getFirstLeadEnd();
        for (int j = 1; j <= leadHead.length(); j++) {
            String bell = Stage.getLabelAtPosition(j);
            transitions.put(bell, Stage.getLabelAtPosition(leadHead.indexOf(bell) + 1));
        }
	    return transitions;
	}

	public String getLastChange() {
	    if (size() == 0) {
	        return null;
	    }
	    return get(size() - 1);
	}
	
	public boolean isTrue() throws FalseGridException {
	    
	    int numExtents = (int) Math.ceil((float)(size() - 1) / (float)getStage().getMaxChanges());
	    
	    ArrayList<String> sortedGrid = new ArrayList<String>(this);
	    String lastChange = sortedGrid.remove(sortedGrid.size() - 1); // Remove the last element to check later
	            
        if (!get(0).equals(lastChange)) {
            throw new DoesNotEndInRoundsException(lastChange, this);
        }
	    
	    Collections.sort(sortedGrid);
	    
	    outer: for (int i = 0; i < sortedGrid.size(); ) {
	        inner: for (int j = 1; j <= numExtents + 1; j++) {
    	        if (i + j < sortedGrid.size()) {
    	            if (sortedGrid.get(i).equals(sortedGrid.get(i + j))) {
    	                if (j > numExtents - 1) {
    	                    throw new RepeatedChangeException(sortedGrid.get(i), this);
    	                }
    	            } else {
    	                i += j;
    	                break inner;
    	            }
    	        } else {
    	            break outer;
    	        }
	        }
	    }
	    
	    return true;
	}

	@Override
	public String toString() {
	    StringBuffer grid = new StringBuffer();
	    Method lastMethod = null;
	    StringBuffer leadDivider = new StringBuffer();
	    for (int i = 0; i < m_Stage.getBells(); i++) {
	    	leadDivider.append("-");
	    }
	    for (int i = 0; i < size(); i++) {
	    	if (m_Calls.get(i) != LeadType.PLAIN) {
	    		grid.append(m_Calls.get(i));
	    	} else {
	    		grid.append(" ");
	    	}
	        grid.append(" " + this.get(i));
	        if (i < m_Methods.size() - 1 && !m_Methods.get(i+1).equals(lastMethod)) {
	        	lastMethod = m_Methods.get(i+1);
	        	grid.append(" " + lastMethod);
	        }
	        grid.append("\n");
	        if (m_Labels.get(i)) {
	        	grid.append("  ");
	        	grid.append(leadDivider);
	        	grid.append("\n");
	        }
	    }
	    return grid.toString();
	}
	
	public Stage getStage()
	{
	    return Stage.getStage("" + get(0).length());
	}

    public boolean containsRounds()
    {
        return (size() > 0) &&
               subList(1, size()).contains(Stage.getFirstChange(get(0).length()));
    }

    public boolean containsSufficientRounds()
    {
        final String firstChange = m_Stage.getFirstChange();
        int numRounds = 0;
        for (int i = 1; i < size(); i++) {
            if (get(i).equals(firstChange)) {
                numRounds++;
            }
        }
        
        return numRounds >= 1 && numRounds <= (size()) / (m_Stage.getMaxChanges()) + 1;
        
//        if (size() - 1 < m_Stage.getMaxChanges()) {
//            return numRounds == 1;
//        } else {
//            return (size() - 1) / m_Stage.getMaxChanges() <= numRounds;
//        }
    }
    
    public boolean endsInRounds()
    {
        return getLastChange().equals(Stage.getFirstChange(get(0).length()));
    }
    
    public int getTenorPosition()
    {
		return getLastLeadEnd().indexOf(Stage.getLabelAtPosition(getStage().getBells())) + 1;
    }
    
}
