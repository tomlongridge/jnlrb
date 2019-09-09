package linegenerator.core;

import java.util.HashMap;

import linegenerator.core.exceptions.InvalidPlaceNotationException;

/**
 * Represents a Method.
 * 
 * @author Tom
 */
public class Method implements Comparable<Method> {
    
    /** Character used to denote symmetric place notation. */
    public static final char PLACE_NOTATION_SYMMETRIC = '&';
    
    /** Character used to denote non-symmetric place notation. */
    public static final char PLACE_NOTATION_NONSYMETRIC = '+';

    /** Regular expression to validate place notation. */
    public static final String PLACE_NOTATION_REGEXP = "([\\dETA-ZXx\\.]+[\\,]?)+";

    /** Regular expression to validate place notation for a method. */
    public static final String METHOD_PLACE_NOTATION_REGEXP = "([\\&\\+][\\dETA-ZXx\\.]+[\\,]?)+";

	/** The method name. */
	private String m_Name;

    /** The type of method that this is. */
    private MethodType m_MethodType;
    
    /** The number of bells in the method. */
    private Stage m_Stage;
    
    /** The place notation string used to construct the method. */
    private String[] m_PlaceNotation;
    
    /** The labels for the place notation. */
    private String[] m_NotationLabels;
    
    /** The place notation string for the lead end. */
    private String[] m_LeadEnd;
    
    /** The place notation string for a bob lead end. */
    private String[] m_BobLeadEnd;
    
    /** The place notation string for a single lead end. */
    private String[] m_SingleLeadEnd;
    
    /** The place notation used to construct the method. */
    private Notation[] m_PlainLeadNotation;
    
    /** The place notation at a bob lead end. */
    private Notation[] m_BobLeadNotation;
    
    /** The place notation at a single lead end. */
    private Notation[] m_SingleLeadNotation;

	/** The bell at which to start the line. */
    private int m_StartBell;

    /** The location to start the line within the place notation. */
    private int m_StartOffset;
    
    private HashMap<String,String> m_CallingPositionAmendments;
    
	/**
	 * Constructor.
	 * @param pr_Name The name of the method
	 * @param pr_MethodType The type of method
	 * @param pr_Stage The number of bells in the method
	 * @param pr_PlaceNotation The place notation used to construct the method
	 * @param pr_LeadEndNotation The place notation at the lead end
	 * @param pr_BobLeadEndNotation The place notation at a bob lead end
	 * @param pr_SingleLeadEndNotation The place notation at a single lead end
	 * @param pr_StartBell The bell at which to start the line
	 * @param pr_StartOffset The location to start the line within the place notation
	 * @throws InvalidPlaceNotationException 
	 */
	public Method(final String pr_Name,
	              final MethodType pr_MethodType,
	              final Stage pr_Stage,
                  final String pr_PlaceNotation,
                  final String pr_LeadEndNotation,
                  final String pr_BobLeadEndNotation,
                  final String pr_SingleLeadEndNotation,
                  final int pr_StartBell,
                  final int pr_StartOffset) throws InvalidPlaceNotationException {
	    setName(pr_Name);
	    setMethodType(pr_MethodType);
	    setStage(pr_Stage);
	    setPlaceNotation(pr_PlaceNotation, pr_LeadEndNotation, pr_BobLeadEndNotation, pr_SingleLeadEndNotation);
	    setStartBell(pr_StartBell);
	    setStartOffset(pr_StartOffset);
	    m_CallingPositionAmendments = new HashMap<String, String>();
	}

	/**
	 * @return the method name
	 */
	public final String getName() {
		return m_Name;
	}

	/**
	 * @param pr_Name the method name to set
	 */
	public final void setName(final String pr_Name) {
	    m_Name = pr_Name;
	}

	/**
	 * @return the number of bells
	 */
	public final Stage getStage() {
		return m_Stage;
	}

	/**
	 * Sets the stage of the method.
	 * @param pr_Stage the number of bells to set
	 */
	public final void setStage(final Stage pr_Stage) {
		m_Stage = pr_Stage;
	}
	
	/**
	 * @return the method type
	 */
	public final MethodType getMethodType() {
		return m_MethodType;
	}
	
	/**
	 * Sets the method type.
	 * @param pr_MethodType the method type
	 */
	public final void setMethodType(final MethodType pr_MethodType) {
		m_MethodType = pr_MethodType;
	}
    
    /**
     * @return the place notation array
     */
    public final String[] getPlaceNotation() {
        return m_PlaceNotation;
    }
    
    /**
     * @return the place notation label array
     */
    public final String[] getNotationLabel() {
        return m_NotationLabels;
    }

    /**
     * @return the lead end place notation
     */
    public String[] getLeadEnd() {
        return m_LeadEnd;
    }

    /**
     * @return the bob lead end place notation
     */
    public String[] getBobLeadEnd() {
        return m_BobLeadEnd;
    }

    /**
     * @return the single lead end place notation
     */
    public String[] getSingleLeadEnd() {
        return m_SingleLeadEnd;
    }
    
    public Notation getLeadNotation(final LeadType pr_LeadType, final int pr_NotationIndex)
    {
        switch (pr_LeadType) {
        case BOB:
            if (m_BobLeadNotation == null) {
                m_BobLeadNotation = createLeadNotation(m_BobLeadEnd);
            }
            return m_BobLeadNotation != null ? m_BobLeadNotation[pr_NotationIndex] : null;
        case SINGLE:
            if (m_SingleLeadNotation == null) {
                m_SingleLeadNotation = createLeadNotation(m_SingleLeadEnd);
            }
            return m_SingleLeadNotation != null ? m_SingleLeadNotation[pr_NotationIndex] : null;
        default:
            if (m_PlainLeadNotation == null) {
                m_PlainLeadNotation = createLeadNotation(m_LeadEnd);
            }
            return m_PlainLeadNotation != null ? m_PlainLeadNotation[pr_NotationIndex] : null;
        }
    }

    /**
     * Parses a string containing the place notation of the method.
     * 
     * @param pr_Notation the place notation string used to construct the method
     * @param pr_LeadEnd the place notation string for the lead end
     */
    public final void setPlaceNotation(final String pr_Notation,
                                       final String pr_PlainLeadEnd,
                                       final String pr_BobLeadEnd,
                                       final String pr_SingleLeadEnd) throws InvalidPlaceNotationException {
       
        m_PlaceNotation = pr_Notation.split(",");
        m_NotationLabels = new String[m_PlaceNotation.length];
        for (int i = 0; i < m_PlaceNotation.length; i++) {
            if (m_PlaceNotation[i].contains("=")) {
                String[] notationParts = m_PlaceNotation[i].split("=");
                m_NotationLabels[i] = notationParts[0];
                m_PlaceNotation[i] = notationParts[1];
            } else {
                m_NotationLabels[i] = null;
            }
        }
        
        m_LeadEnd = pr_PlainLeadEnd.split(",");
        if (pr_BobLeadEnd != null) {
            m_BobLeadEnd = pr_BobLeadEnd.split(",");
            if (m_PlaceNotation.length != m_BobLeadEnd.length) {
                throw new InvalidPlaceNotationException("The number of place notations and bob lead end notations are not the same.");
            }
        } else {
            m_BobLeadEnd = null;
        }
        if (pr_SingleLeadEnd != null) {
            m_SingleLeadEnd = pr_SingleLeadEnd.split(",");
            if (m_PlaceNotation.length != m_SingleLeadEnd.length) {
                throw new InvalidPlaceNotationException("The number of place notations and single lead end notations are not the same.");
            }
        } else {
            m_SingleLeadEnd = null;
        }
        
        m_PlainLeadNotation = null;
        m_BobLeadNotation = null;
        m_SingleLeadNotation = null;
    }
    
    public final Notation createLeadNotation(final String pr_LeadEnd) {
        String[] leadEnds = new String[m_LeadEnd.length];
        for (int i = 0; i < m_LeadEnd.length; i++) {
            leadEnds[i] = pr_LeadEnd;
        }
        return createLeadNotation(leadEnds)[0];
    }

    /**
     * Parses a string containing the place notation of the method.
     * 
     * @param pr_Notation the place notation string used to construct the method
     * @param pr_LeadEnd the place notation string for the lead end
     */
    private final Notation[] createLeadNotation(final String[] pr_LeadEnds) {
        
        if (pr_LeadEnds == null) {
            return null;
        }
        
        final Notation[] leads = new Notation[m_PlaceNotation.length];
        try {
            for (int i = 0; i < m_PlaceNotation.length; i++) {
                Notation notation = new Notation(m_PlaceNotation[i]);
                Notation leadEndNotation = new Notation(pr_LeadEnds[i]);
                notation.add(leadEndNotation.remove(leadEndNotation.size() - 1));
                if (!leadEndNotation.isEmpty()) {
                    int pointer = notation.size() - 1;
                    while (leadEndNotation.size() > 0) {
                        pointer--;
                        notation.set(pointer, leadEndNotation.remove(leadEndNotation.size() - 1));
                    }
                }
                leads[i] = notation;
            }
        } catch (InvalidPlaceNotationException e) {
            // Do nothing - notation already validated
        }

        return leads;
    }

    /**
     * @param m_StartBell the bell at which to start the line
     */
    public void setStartBell(int pr_StartBell) {
        m_StartBell = pr_StartBell;
    }

    /**
     * 
     * @return the bell at which to start the line
     */
    public int getStartBell() {
        if (m_StartBell > 0) {
            return m_StartBell;
        } else {
            final String endNotation = m_PlainLeadNotation[0].get(m_PlainLeadNotation[0].size() - 1);
            if (endNotation == null) {
                return 1;
            } else {
                return Stage.getPositionOfLabel(endNotation.charAt(endNotation.length() - 1));
            }
        }
    }

    /**
     * @param pr_Offset the location to start the line within the place notation
     */
    public void setStartOffset(int pr_Offset) {
        m_StartOffset = pr_Offset;
    }

    /**
     * 
     * @return the location to start the line within the place notation
     */
    public int getStartOffset() {
        return m_StartOffset;
    }
    
    public void addCallingPositionAmendment(final String pr_Normal, final String pr_Amended)
    {
        m_CallingPositionAmendments.put(pr_Normal, pr_Amended);
    }
    
    public String getAmendedCallingPosition(final String pr_Normal)
    {
        if (m_CallingPositionAmendments.containsKey(pr_Normal)) {
            return m_CallingPositionAmendments.get(pr_Normal);
        } else {
            return pr_Normal;
        }
    }
    
    public String getFileIdentifier() {
        return getStage().getLabel() + "_" +
               getMethodType().getCode() + "_" +
               getName().replaceAll("[\\s\\']*", "");
    }
    
	@Override
	public String toString() {
	    return m_Name + " " + 
	           (m_MethodType.shouldDisplayNameInMethod() ? m_MethodType.toString() + " " : "") + 
	           m_Stage;
	}
	
	@Override
	public boolean equals(final Object pr_Object) {
	    
	    if (pr_Object == null) {
	        return false;
	    } else if (!(pr_Object instanceof Method)) {
	        return false;
	    } else {
	        Method method = (Method) pr_Object;
	        return method.m_Name.equals(m_Name) && 
	               method.m_Stage.equals(m_Stage) &&
	               method.m_MethodType.equals(m_MethodType);
	    }
	    
	}
	
	public String getDescription()
	{
	    final StringBuilder description = new StringBuilder();
        String leadHead;
        Grid lead;
        HashMap<String, String> plainTransitions;
        HashMap<String, String> bobTransitions;
        HashMap<String, String> singleTransitions;
        
        for (int i = 0; i < getPlaceNotation().length; i++) {
        
            if (getNotationLabel()[i] != null) {
                description.append(getNotationLabel()[i] + ": ");
            }
            description.append(getPlaceNotation()[i]);
            description.append(" plh" + '\u00a0');
            description.append(getLeadEnd()[i]);
            description.append(" (");
            lead = new Grid(this);
            lead.add(getLeadNotation(LeadType.PLAIN, i), LeadType.PLAIN, this);
            leadHead = lead.getFirstLeadEnd();
            if (leadHead.charAt(0) == '1') {
                leadHead = leadHead.substring(1);
            }
            description.append(leadHead);
            description.append(")");
            plainTransitions = lead.getTransitions();
            if (getBobLeadEnd() != null) {
                description.append(", blh" + '\u00a0');
                description.append(getBobLeadEnd()[i]);
                description.append(" (");
                lead = new Grid(this);
                lead.add(getLeadNotation(LeadType.BOB, i), LeadType.BOB, this);
                leadHead = lead.getFirstLeadEnd();
                if (leadHead.charAt(0) == '1') {
                    leadHead = leadHead.substring(1);
                }
                description.append(leadHead);
                description.append(")");
                bobTransitions = lead.getTransitions();
                for (int j = 1; j <= getStage().getBells(); j++) {
                    String bell = Stage.getLabelAtPosition(j);
                    if (!plainTransitions.get(bell).equals(bobTransitions.get(bell))) {
                        description.append(" " + bell + ":" + bobTransitions.get(bell));
                    }
                }
            }
            if (getSingleLeadEnd() != null) {
                description.append(", slh" + '\u00a0');
                description.append(getSingleLeadEnd()[i]);
                description.append(" (");
                lead = new Grid(this);
                lead.add(getLeadNotation(LeadType.SINGLE, i), LeadType.SINGLE, this);
                leadHead = lead.getFirstLeadEnd();
                if (leadHead.charAt(0) == '1') {
                    leadHead = leadHead.substring(1);
                }
                description.append(leadHead);
                description.append(")");
                singleTransitions = lead.getTransitions();
                for (int j = 1; j <= getStage().getBells(); j++) {
                    String bell = Stage.getLabelAtPosition(j);
                    if (!plainTransitions.get(bell).equals(singleTransitions.get(bell))) {
                        description.append(" " + bell + ":" + singleTransitions.get(bell));
                    }
                }
            }
        }
        
        return description.toString();
	}

	@Override
	public int compareTo(Method other) {
		
		int cmp = m_Stage.compareTo(other.getStage());
		if (cmp != 0) {
			return cmp;
		}
		
		cmp = m_MethodType.compareTo(other.getMethodType());
		if (cmp != 0) {
			return cmp;
		}
		
		cmp = m_Name.compareTo(other.getName());
		if (cmp != 0) {
			return cmp;
		}
		
		return 0;
		
	}
}
