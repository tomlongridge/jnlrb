package linegenerator.core;


/**
 * Represents the number of bells in a method.
 * 
 * @author Tom
 */
public enum Stage implements Comparable<Stage> {
	
	/** 1 bell methods. */
	UNUS(1),
	/** 2 bell methods. */
	MICROMUS(2),
	/** 3 bell methods. */
	SINGLES(3),
	/** 4 bell methods. */
	MINIMUS(4),
	/** 5 bell methods. */
	DOUBLES(5),
	/** 6 bell methods. */
	MINOR(6),
	/** 7 bell methods. */
	TRIPLES(7),
	/** 8 bell methods. */
	MAJOR(8),
	/** 9 bell methods. */
	CATERS(9),
	/** 10 bell methods. */
	ROYAL(10),
	/** 11 bell methods. */
	CINQUES(11),
	/** 12 bell methods. */
	MAXIMUS(12),
	/** 13 bell methods. */
	SEXTUPLES(13),
	/** 14 bell methods. */
	FOURTEEN(14),
	/** 15 bell methods. */
	SEPTUPLES(15),
	/** 16 bell methods. */
	SIXTEEN(16);
	
	private final static String LABELS = "1234567890ETABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
	/** Number of bells represented by the enumeration. */
	private int m_Bells;
	
	/**
	 * Constructor.
	 * @param pr_Bells the number of bells represented by the enumeration
	 */
	private Stage(final int pr_Bells) {
		m_Bells = pr_Bells;
	}

    /**
     * @return the number of bells
     */
    public final int getBells() {
        return m_Bells;
    }

    /**
     * @return the number of bells
     */
    public final String getLabel() {
        return Stage.getLabelAtPosition(m_Bells);
    }
    
    public static boolean isLabel(final char pr_Label) {
        return LABELS.contains("" + pr_Label);
    }
    
    public static int getPositionOfLabel(final char pr_Label) {
        return LABELS.indexOf(pr_Label) + 1;
    }
    
    public static String getLabelAtPosition(final int pr_Position) {
        return "" + LABELS.charAt(pr_Position - 1);
    }
    
    /**
     * @return the first change on this stage
     */
    public final String getFirstChange() {
        return getFirstChange(m_Bells);
    }
    
    /**
     * @return the first change on this stage
     */
    public final static String getFirstChange(int pr_Bells) {
        StringBuffer change = new StringBuffer();
        for (int i = 0; i < pr_Bells; i++) {
            change.append(LABELS.charAt(i));
            
        }
        return change.toString();
    }
	
	/**
	 * @return the factorial of the stage
	 */
	public final int getMaxChanges() {
		return factorial(m_Bells);
	}
	
	/** {@inheritDoc} */
	@Override
	public final String toString() {
		
		switch (m_Bells) {
		case 1: return "Unus";
		case 2: return "Micromus";
		case 3: return "Singles";
		case 4: return "Minimus";
		case 5: return "Doubles";
		case 6: return "Minor";
		case 7: return "Triples";
		case 8: return "Major";
		case 9: return "Caters";
		case 10: return "Royal";
		case 11: return "Cinques";
		case 12: return "Maximus";
		case 13: return "Sextuples";
		case 14: return "Fourteen";
		case 15: return "Septuples";
		case 16: return "Sixteen";
		default: return m_Bells + " In";
		}
		
	}
	
	/**
	 * Calculates the factorial of the specified number.
	 * @param pr_N the number to calculate the factorial of
	 * @return the factorial of the specified number
	 */
	private int factorial(final int pr_N) {
		if (pr_N == 1) {
			return 1;
		} else {
			return pr_N * factorial(pr_N - 1);
		}
	}
	
	/**
	 * Converts a string to a stage enumeration.
	 * @param pr_Stage the string
	 * @return the stage
	 */
	public static final Stage getStage(final String pr_Stage) {
		
		switch (Integer.parseInt(pr_Stage)) {
		case 1: return UNUS;
		case 2: return MICROMUS;
		case 3: return SINGLES;
		case 4: return MINIMUS;
		case 5: return DOUBLES;
		case 6: return MINOR;
		case 7: return TRIPLES;
		case 8: return MAJOR;
		case 9: return CATERS;
		case 10: return ROYAL;
		case 11: return CINQUES;
		case 12: return MAXIMUS;
		case 13: return SEXTUPLES;
		case 14: return FOURTEEN;
		case 15: return SEPTUPLES;
		case 16: return SIXTEEN;
		default: return null;
		}		
	}
	
}
