package io.mosip.print.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.websub.api.annotation.PreAuthenticateContentAndVerifyIntent;
import io.mosip.print.exception.RegPrintAppException;
import io.mosip.print.model.CredentialStatusEvent;
import io.mosip.print.model.EventModel;
import io.mosip.print.model.MOSIPMessage;
import io.mosip.print.service.PrintService;

@RestController
@RequestMapping(value = "/print")
public class Print {

	@Value("${mosip.event.secret}")
	private String secret;

	/** The printservice. */
	@Autowired
	private PrintService<Map<String, byte[]>> printService;


	@PostMapping(value = "/enqueue", consumes = "application/json")
	@PreAuthenticateContentAndVerifyIntent(secret = "Kslk30SNF2AChs2", callback = "/print/enqueue", topic = "http://mosip.io/print/pdf")
	public void printPost(@RequestBody MOSIPMessage message) {
		System.out.println(message.getTopic());
		// TODO: Validate the MOSIPmessage
		// TODO:Call the print service with the map that we received from MOSIPMessage
		// printService.print()
	}

	@PostMapping(path = "/statusPrint", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> handlePublishEvent(@RequestParam String topic,
			@RequestBody CredentialStatusEvent credentialStatusEvent) {

		printService.publishEvent(topic, credentialStatusEvent);
		return new ResponseEntity<>("successfully published", HttpStatus.ACCEPTED);
	}

	/**
	 * Gets the file.
	 *
	 * @param printRequest the print request DTO
	 * @param token        the token
	 * @param errors       the errors
	 * @param printRequest the print request DTO
	 * @return the file
	 * @throws IOException
	 * @throws RegPrintAppException the reg print app exception
	 */
	@PostMapping(path = "/callback/notifyPrint", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthenticateContentAndVerifyIntent(secret = "test", callback = "/v1/print/print/callback/notifyPrint", topic = "792112/CREDENTIAL_ISSUED")
	public ResponseEntity<Object> handleSubscribeEvent(@RequestBody EventModel eventModel) throws IOException {
		String credential = eventModel.getEvent().getData().get("credential").toString();
		byte[] str1 = CryptoUtil.decodeBase64(credential);
		String decodedCrdential = new String(str1, Charset.forName("UTF-8"));
		JSONObject jsonObject = new JSONObject(decodedCrdential);
		Map proofMap = new HashMap<String, String>();
		proofMap = (Map) eventModel.getEvent().getData().get("proof");
		String sign = proofMap.get("signature").toString();
		byte[] pdfbytes = printService.getDocuments(decodedCrdential, getSignature(sign, credential), "UIN", false)
				.get("uinPdf");
		InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfbytes));

		File pdfFile = new File(
				"/media/lenovo/872da60f-3c16-4cfb-b900-0f63cbe7f3a9/opt/projects/mosip/MajorBug/print/uin.pdf");
		OutputStream os = new FileOutputStream(pdfFile);
		os.write(pdfbytes);
		os.close();
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
				.header("Content-Disposition", "attachment; filename=\"" + "4957694814" + ".pdf\"")
				.body((Object) resource);

	}

	private String getSignature(String sign, String crdential) {
		String signHeader = sign.split("\\.")[0];
		String signData = sign.split("\\.")[2];
		String signature = signHeader + "." + crdential + "." + signData;
		return signature;
	}

}
