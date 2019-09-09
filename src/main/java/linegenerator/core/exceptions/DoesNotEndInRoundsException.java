package linegenerator.core.exceptions;

import java.util.List;

public class DoesNotEndInRoundsException extends FalseGridException {

    /** Version ID. */
    private static final long serialVersionUID = 4381069862971665038L;

    /**
     * Constructor.
     * @param pr_Change The change that is repeated.
     */
    public DoesNotEndInRoundsException(final String pr_Change, final List<String> pr_Grid) {
        super("Does not end in rounds. (Last change " + pr_Change  + " after " + pr_Grid.size() + " changes.)", pr_Grid);
    }

}
