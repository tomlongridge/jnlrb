package linegenerator.core.exceptions;

import linegenerator.core.Grid;

public class RepeatedChangeException extends FalseGridException {

    /** Version ID. */
    private static final long serialVersionUID = 4381069862971665038L;

    /**
     * Constructor.
     * @param pr_Change The change that is repeated.
     */
    public RepeatedChangeException(final String pr_Change, final Grid pr_Grid) {
        super("Repeated change found at change " + (pr_Grid.lastIndexOf(pr_Change) - 1)+ 
        	  " of " + pr_Grid.size() + ": " + pr_Change, pr_Grid);
    }

}
