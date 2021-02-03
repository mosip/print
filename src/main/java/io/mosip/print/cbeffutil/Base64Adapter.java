/**
 * 
 */
package io.mosip.print.cbeffutil;

/**
 * @author Ramadurai Pandian
 * 
 * An Adaptor class to bye-pass the JAXB default Base64 encoding/decoding 
 * and to use kernel cryptoutil for Base64 conversion.
 *
 */
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.codec.binary.Base64;

public class Base64Adapter extends XmlAdapter<String, byte[]> {

	/**
	 * @param data biometrics image data
	 * @return base64 decoded data after unmarshalling
	 */
	@Override
	public byte[] unmarshal(String data) throws Exception {
		return Base64.decodeBase64(data);
	}

	/**
	 * @param data biometrics image data
	 * @return base64 encoded data after marshalling
	 */
	@Override
	public String marshal(byte[] data) throws Exception {
		return Base64.encodeBase64String(data);
	}

}