package io.mosip.print.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.print.exception.PlatformErrorMessages;

/**
 * The Class IdentityNotFoundException.
 */
public class IdentityNotFoundException extends BaseUncheckedException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Instantiates a new identity not found exception.
	 */
	public IdentityNotFoundException() {
		super();
	}
	
	/**
	 * Instantiates a new identity not found exception.
	 *
	 * @param errorMessage the error message
	 */
	public IdentityNotFoundException(String errorMessage) {
		super(PlatformErrorMessages.RPR_PIS_IDENTITY_NOT_FOUND.getCode()+ EMPTY_SPACE, errorMessage);
	}

	/**
	 * Instantiates a new identity not found exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public IdentityNotFoundException(String message, Throwable cause) {
		super(PlatformErrorMessages.RPR_PIS_IDENTITY_NOT_FOUND.getCode() + EMPTY_SPACE, message, cause);
	}

}
