package linegenerator.core.exceptions;

import java.util.List;

public class FalseGridException extends Exception {

    /** Version ID. */
    private static final long serialVersionUID = 4381069862971665038L;

    private List<String> m_Grid;
    
    /**
     * Constructor.
     * @param pr_Message The reason why the notation is invalid
     */
    public FalseGridException(final String pr_Message, final List<String> pr_Grid) {
        super(pr_Message);
        setGrid(pr_Grid);
    }

    public List<String> getGrid() {
        return m_Grid;
    }

    public void setGrid(List<String> m_Grid) {
        this.m_Grid = m_Grid;
    }

}
