package linegenerator.core.exceptions;
/**
 * Thrown to indicate that the a problem occurred during generation of the method grid.
 * @author Tom
 *
 */
public class MethodGenerationException extends Exception {
	
	/** Version ID. */
	private static final long serialVersionUID = -1645045453423711281L;

	/**
	 * Constructor.
	 * @param pr_Message The reason why the notation is invalid
	 */
	public MethodGenerationException(final String pr_Message) {
		super(pr_Message);
	}

}
