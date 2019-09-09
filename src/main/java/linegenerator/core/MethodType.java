package linegenerator.core;


/**
 * Represents the types of methods.
 * 
 * @author Tom
 */
public enum MethodType {
	
	/** Alliance methods. */
	ALLIANCE("A", "Alliance"),
	/** Little Alliance methods. */
	LITTLEALLIANCE("LA", "Little Alliance"),
	/** Delight methods. */
	DELIGHT("D", "Delight"),
	/** Hybrid methods. */
	HYBRID("H", "Hybrid"),
	/** Differential methods. */
	DIFFERENTIAL("I", "Differential"),
	/** Principles. */
	PRINCIPLE("O", "Principle", false),
	/** Bob methods. */
	BOB("P", "Bob", false),
	/** Place methods. */
	PLACE("L", "Place"),
	/** Surprise methods. */
	SURPRISE("S", "Surprise"),
	/** Little Surprise methods. */
	LITTLESURPRISE("LS", "Little Surprise"),
	/** Slow Course methods. */
	SLOWCOURSE("SC", "Slow Course"),
	/** Treble Bob methods. */
	TREBLEBOB("T", "Treble Bob"),
	/** Treble Place methods. */
	TREBLEPLACE("TP", "Treble Place");
	
	/** The code of the method type. */
	private String m_Code;
	
	/** The name of the method type. */
	private String m_Name;
	
	/** Whether the name of the method type is shown in the method. */
	private boolean m_DisplayNameInMethod;
    
    /**
     * Constructor.
     * @param pr_Code the code of the method type
     * @param pr_Name the name of the method type
     */
    private MethodType(final String pr_Code,
                       final String pr_Name) {
        m_Code = pr_Code;
        m_Name = pr_Name;
        m_DisplayNameInMethod = true;
    }
    
    /**
     * Constructor.
     * @param pr_Code the code of the method type
     * @param pr_Name the name of the method type
     * @param pr_DisplayNameInMethod whether the name of the method type is shown in the method
     */
    private MethodType(final String pr_Code,
                       final String pr_Name,
                       final boolean pr_DisplayNameInMethod) {
        m_Code = pr_Code;
        m_Name = pr_Name;
        m_DisplayNameInMethod = pr_DisplayNameInMethod;
    }

	/**
	 * @return the number of bells
	 */
	public final String getCode() {
		return m_Code;
	}
	
	/**
	 * @return whether the name of the method type is shown in the method
	 */
	public boolean shouldDisplayNameInMethod() {
        return m_DisplayNameInMethod;
    }
	
	/** {@inheritDoc} */
	@Override
	public final String toString() {
		return m_Name;		
	}
	
	/**
	 * Converts a string to a method type enumeration.
	 * @param pr_MethodType the string
	 * @return the method type
	 */
	public static final MethodType getMethodType(final String pr_MethodType) {
		
		if (pr_MethodType.length() >= 2) {
			if (pr_MethodType.substring(0, 2).equals("TP")) {
				return TREBLEPLACE;
			} else if (pr_MethodType.substring(0, 2).equals("LS")) {
				return LITTLESURPRISE;
			} else if (pr_MethodType.substring(0, 2).equals("LA")) {
				return LITTLEALLIANCE;
			} else if (pr_MethodType.substring(0, 2).equals("SC")) {
				return SLOWCOURSE;
			}
		}
		
		if (pr_MethodType.substring(0, 1).equals("A")) {
			return ALLIANCE;
		} else if (pr_MethodType.substring(0, 1).equals("D")) {
			return DELIGHT;
		} else if (pr_MethodType.substring(0, 1).equals("H")) {
			return HYBRID;
		} else if (pr_MethodType.substring(0, 1).equals("I")) {
			return DIFFERENTIAL;
		} else if (pr_MethodType.substring(0, 1).equals("O")) {
			return PRINCIPLE;
		} else if (pr_MethodType.substring(0, 1).equals("P")) {
			return BOB;
		} else if (pr_MethodType.substring(0, 1).equals("L")) {
			return PLACE;
		} else if (pr_MethodType.substring(0, 1).equals("S")) {
			return SURPRISE;
		} else if (pr_MethodType.substring(0, 1).equals("T")) {
			return TREBLEBOB;
		} else {
			return null;
		}
		
	}
}
