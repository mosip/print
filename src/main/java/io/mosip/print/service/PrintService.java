package io.mosip.print.service;

import io.mosip.print.model.CredentialStatusEvent;

public interface PrintService<T> {
    
    /*public String print(Map map){
        //TODO: implement the logic to print the card here

        return "";
    }*/
	public void publishEvent(String topic,CredentialStatusEvent credentialStatusEvent);
	
	
	/**
	 * Gets the documents.
	 *
	 * @param type
	 *            the type
	 * @param idValue
	 *            the id value
	 * @param cardType
	 *            the card type
	 * @param isPasswordProtected
	 *            the is password protected
	 * @return the documents
	 */
	public T getDocuments(String credentialSubject, String sign, String cardType, boolean isPasswordProtected);

	// Map<String, byte[]> getDocuments(String credentialSubject, String sign,
	// String cardType,
	// boolean isPasswordProtected);
	
}