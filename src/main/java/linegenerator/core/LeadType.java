package linegenerator.core;


/**
 * Represents the lead types.
 * 
 * @author Tom
 */
public enum LeadType {
	
	PLAIN(""),
	BOB("-"),
	TWIN_BOB("x"),
	SINGLE("s");
	
	/** The code of the method type. */
	private String m_Code;
	
    /**
     * Constructor.
     * @param pr_Code the code of the method type
     */
    private LeadType(final String pr_Code) {
        m_Code = pr_Code;
    }
    
	/**
	 * @return the number of bells
	 */
	public final String toString() {
		return m_Code;
	}
	
	/**
	 * Converts a string to a lead type enumeration.
	 * @param pr_MethodType the string
	 * @return the method type
	 */
	public static final LeadType getLeadType(final String pr_Code) {
		
	    if ((pr_Code == null) || pr_Code.equals("") || pr_Code.equals("p")) {
	        return PLAIN;
	    } else if (pr_Code.equals("-")) {
			return BOB;
	    } else if (pr_Code.equals("x")) {
	        return TWIN_BOB;
		} else if (pr_Code.equals("s")) {
			return SINGLE;
		}
		
		return null;
		
	}
}
