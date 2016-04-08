package main;

/**
 * Represents illegal file formatting.
 */
public class IlegalFileFormatException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 845832610467589755L;

	/**
	 * The error message.
	 */
	private final String error;
	
	/**
	 * Makes a new IlegalFileFormatException with the specified error message.
	 *
	 * @param string
	 *            the error message.
	 */
	public IlegalFileFormatException(String string) {

		error = string;
	}
	
	@Override
	public String getMessage() {
	
		return error;
	}
	
}
