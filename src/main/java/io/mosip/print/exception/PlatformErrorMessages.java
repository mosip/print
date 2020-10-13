
package io.mosip.print.exception;

// TODO: Auto-generated Javadoc
/**
 * The Enum RPRPlatformErrorMessages.
 *
 * @author M1047487
 */
public enum PlatformErrorMessages {


	// Printing stage exceptions
	RPR_PRT_PDF_NOT_GENERATED(PlatformConstants.PRT_PRINT_PREFIX + "001", "Error while generating PDF for UIN Card"),
	/** The rpr rgs json parsing exception. */
	RPR_RGS_JSON_PARSING_EXCEPTION(PlatformConstants.PRT_PRINT_PREFIX + "017", "JSON Parsing Failed"),
	/** The invalid input parameter. */
	RPR_PGS_INVALID_INPUT_PARAMETER(PlatformConstants.PRT_PRINT_PREFIX + "011", "Invalid Input Parameter - %s"),
	/** The rpr rgs json mapping exception. */
	RPR_RGS_JSON_MAPPING_EXCEPTION(PlatformConstants.PRT_PRINT_PREFIX + "016", "JSON Mapping Failed"),
	RPR_PRT_PDF_SIGNATURE_EXCEPTION(PlatformConstants.PRT_PRINT_PREFIX + "024", "PDF Signature error"),
	/** The rpr pvm invalid uin. */
	RPR_PVM_INVALID_UIN(PlatformConstants.PRT_PRINT_PREFIX + "012", "Invalid UIN"),
	/** The rpr rct unknown resource exception. */
	RPR_RCT_UNKNOWN_RESOURCE_EXCEPTION(PlatformConstants.PRT_PRINT_PREFIX + "001", "Unknown resource provided"),
	/** The rpr utl digital sign exception. */
	RPR_UTL_DIGITAL_SIGN_EXCEPTION(PlatformConstants.PRT_PRINT_PREFIX + "003", "Failed to generate digital signature"),
	/** The rpr utl biometric tag match. */
	RPR_UTL_BIOMETRIC_TAG_MATCH(PlatformConstants.PRT_PRINT_PREFIX + "001", "Both Files have same biometrics"),
	/** The rpr prt uin not found in database. */
	RPR_PRT_UIN_NOT_FOUND_IN_DATABASE(PlatformConstants.PRT_PRINT_PREFIX + "002", "UIN not found in database"),
	/** The rpr bdd abis abort. */
	RPR_BDD_ABIS_ABORT(PlatformConstants.PRT_PRINT_PREFIX + "002",
			"ABIS for the Reference ID and Request ID was Abort"),
	/** The rpr tem processing failure. */
	RPR_TEM_PROCESSING_FAILURE(PlatformConstants.PRT_PRINT_PREFIX + "002", "The Processing of Template Failed "),
	RPR_SYS_JSON_PARSING_EXCEPTION(PlatformConstants.PRT_PRINT_PREFIX + "009", "Error while parsing Json"),
	RPR_AUT_INVALID_TOKEN(PlatformConstants.PRT_PRINT_PREFIX + "01", "Invalid Token Present"),
	/** The rpr cmb unsupported encoding. */
	RPR_CMB_UNSUPPORTED_ENCODING(PlatformConstants.PRT_PRINT_PREFIX + "002", "Unsupported Failure"),
	/** The rpr sys no such field exception. */
	RPR_SYS_NO_SUCH_FIELD_EXCEPTION(PlatformConstants.PRT_PRINT_PREFIX + "008", "Could not find the field"),

	/** The rpr sys instantiation exception. */
	RPR_SYS_INSTANTIATION_EXCEPTION(PlatformConstants.PRT_PRINT_PREFIX + "007",
			"Error while creating object of JsonValue class"),

	RPR_PIS_IDENTITY_NOT_FOUND(PlatformConstants.PRT_PRINT_PREFIX + "002",
			"Unable to Find Identity Field in ID JSON"),
	/** Access denied for the token present. */
	RPR_AUT_ACCESS_DENIED(PlatformConstants.PRT_PRINT_PREFIX + "02", "Access Denied For Role - %s");
	

	/** The error message. */
	private final String errorMessage; 

	/** The error code. */
	private final String errorCode;

	/**
	 * Instantiates a new platform error messages.
	 *
	 * @param errorCode
	 *            the error code
	 * @param errorMsg
	 *            the error msg
	 */
	private PlatformErrorMessages(String errorCode, String errorMsg) {
		this.errorCode = errorCode;
		this.errorMessage = errorMsg;
	}

	/**
	 * Gets the error message.
	 *
	 * @return the error message
	 */
	public String getMessage() {
		return this.errorMessage;
	}

	/**
	 * Gets the error code.
	 *
	 * @return the error code
	 */
	public String getCode() {
		return this.errorCode;
	}

}