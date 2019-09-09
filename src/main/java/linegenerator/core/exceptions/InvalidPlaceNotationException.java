package linegenerator.core.exceptions;
/**
 * Thrown to indicate that the specified method definition file is not valid.
 * @author Tom
 *
 */
public class InvalidPlaceNotationException extends Exception {

	/** Version ID. */
	private static final long serialVersionUID = 4381069862971665038L;

	/**
	 * Constructor.
	 * @param pr_Message The reason why the notation is invalid
	 */
	public InvalidPlaceNotationException(final String pr_Message) {
		super(pr_Message);
	}

}
