package io.mosip.print.util;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.print.constant.ApiName;
import io.mosip.print.constant.LoggerFileConstant;
import io.mosip.print.constant.MappingJsonConstants;
import io.mosip.print.dto.ErrorDTO;
import io.mosip.print.dto.VidResponseDTO;
import io.mosip.print.exception.ApisResourceAccessException;
import io.mosip.print.exception.IdRepoAppException;
import io.mosip.print.exception.PacketManagerException;
import io.mosip.print.exception.ParsingException;
import io.mosip.print.exception.PlatformErrorMessages;
import io.mosip.print.exception.VidCreationException;
import io.mosip.print.idrepo.dto.IdResponseDTO1;
import io.mosip.print.logger.PrintLogger;
import io.mosip.print.service.PrintRestClientService;
import lombok.Data;

/**
 * The Class Utilities.
 *
 * @author Girish Yarru
 */
@Component

/**
 * Instantiates a new utilities.
 */
@Data
public class Utilities {
	/** The reg proc logger. */
	private static Logger printLogger = PrintLogger.getLogger(Utilities.class);
	private static final String sourceStr = "source";

	/** The Constant UIN. */
	private static final String UIN = "UIN";

	/** The Constant FILE_SEPARATOR. */
	public static final String FILE_SEPARATOR = "\\";

	/** The Constant RE_PROCESSING. */
	private static final String RE_PROCESSING = "re-processing";

	/** The Constant HANDLER. */
	private static final String HANDLER = "handler";

	/** The Constant NEW_PACKET. */
	private static final String NEW_PACKET = "New-packet";

	@Value("${IDSchema.Version}")
	private String idschemaVersion;

	@Autowired
	private ObjectMapper objMapper;


	/** The rest client service. */
	@Autowired
	private PrintRestClientService<Object> restClientService;

	@Value("${provider.packetreader.mosip}")
	private String provider;

	/** The config server file storage URL. */
	@Value("${config.server.file.storage.uri}")
	private String configServerFileStorageURL;

	/** The get reg processor identity json. */
	@Value("${registration.processor.identityjson}")
	private String getRegProcessorIdentityJson;

	/** The get reg processor demographic identity. */
	@Value("${registration.processor.demographic.identity}")
	private String getRegProcessorDemographicIdentity;

	/** The get reg processor applicant type. */
	@Value("${registration.processor.applicant.type}")
	private String getRegProcessorApplicantType;

	/** The dob format. */
	@Value("${registration.processor.applicant.dob.format}")
	private String dobFormat;
	
	/** The registration processor abis json. */
	@Value("${registration.processor.print.textfile}")
	private String registrationProcessorPrintTextFile;

	/** The id repo update. */
	@Value("${registration.processor.id.repo.update}")
	private String idRepoUpdate;

	/** The vid version. */
	@Value("${registration.processor.id.repo.vidVersion}")
	private String vidVersion;

	@Value("${packet.default.source}")
	private String defaultSource;
	/** The packet info dao. */

	@Autowired
	private PacketManagerService packetManagerService;

	private static final String VALUE = "value";

	private String mappingJsonString = null;

	/**
	 * Gets the json.
	 *
	 * @param configServerFileStorageURL
	 *            the config server file storage URL
	 * @param uri
	 *            the uri
	 * @return the json
	 */
	public static String getJson(String configServerFileStorageURL, String uri) {
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.getForObject(configServerFileStorageURL + uri, String.class);
	}

	/**
	 * get applicant age by registration id. Checks the id json if dob or age
	 * present, if yes returns age if both dob or age are not present then retrieves
	 * age from id repo
	 *
	 * @param id
	 *            the registration id
	 * @return the applicant age
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ApisResourceAccessException
	 *             the packet decryption failure exception
	 * @throws RegistrationProcessorCheckedException
	 */
	public int getApplicantAge(String id, String source, String process) throws IOException,
			ApisResourceAccessException, JsonProcessingException, PacketManagerException {
		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
				id, "Utilities::getApplicantAge()::entry");

		JSONObject regProcessorIdentityJson = getRegistrationProcessorMappingJson();
		String ageKey = JsonUtil.getJSONValue(JsonUtil.getJSONObject(regProcessorIdentityJson, MappingJsonConstants.AGE), VALUE);
		String dobKey = JsonUtil.getJSONValue(JsonUtil.getJSONObject(regProcessorIdentityJson, MappingJsonConstants.DOB), VALUE);


		String applicantDob = packetManagerService.getField(id, dobKey, source, process);
	    String applicantAge = packetManagerService.getField(id, ageKey, source, process);
		if (applicantDob != null) {
			return calculateAge(applicantDob);
		} else if (applicantAge != null) {
			printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
					id, "Utilities::getApplicantAge()::exit when applicantAge is not null");
			return Integer.valueOf(applicantAge);

		} else {

			String uin = getUIn(id, source, process);
			JSONObject identityJSONOject = retrieveIdrepoJson(uin);
			String idRepoApplicantDob = JsonUtil.getJSONValue(identityJSONOject, dobKey);
			if (idRepoApplicantDob != null) {
				printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
						id, "Utilities::getApplicantAge()::exit when ID REPO applicantDob is not null");
				return calculateAge(idRepoApplicantDob);
			}
			Integer idRepoApplicantAge = JsonUtil.getJSONValue(identityJSONOject, ageKey);
			printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
					id, "Utilities::getApplicantAge()::exit when ID REPO applicantAge is not null");
			return idRepoApplicantAge != null ? idRepoApplicantAge : 0;

		}

	}

	public String getDefaultSource() {
		String[] strs = provider.split(",");
		List<String> strList = Arrays.asList(strs);
		Optional<String> optional = strList.stream().filter(s -> s.contains(sourceStr)).findAny();
		String source = optional.isPresent() ? optional.get().replace(sourceStr + ":", "") : null;
		return source;
	}

	/**
	 * retrieving identity json ffrom id repo by UIN.
	 *
	 * @param uin
	 *            the uin
	 * @return the JSON object
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws IdRepoAppException
	 *             the id repo app exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public JSONObject retrieveIdrepoJson(String uin) throws ApisResourceAccessException, IdRepoAppException, IOException {

		if (uin != null) {
			printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
					"Utilities::retrieveIdrepoJson()::entry");
			List<String> pathSegments = new ArrayList<>();
			pathSegments.add(uin);
			IdResponseDTO1 idResponseDto;

			idResponseDto = (IdResponseDTO1) restClientService.getApi(ApiName.IDREPOGETIDBYUIN, pathSegments, "", "",
					IdResponseDTO1.class);
			if (idResponseDto == null) {
				printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
						"Utilities::retrieveIdrepoJson()::exit idResponseDto is null");
				return null;
			}
			if (!idResponseDto.getErrors().isEmpty()) {
				List<ErrorDTO> error = idResponseDto.getErrors();
				printLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
						"Utilities::retrieveIdrepoJson():: error with error message " + error.get(0).getMessage());
				throw new IdRepoAppException(error.get(0).getMessage());
			}
			String response = objMapper.writeValueAsString(idResponseDto.getResponse().getIdentity());
			printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
					"Utilities::retrieveIdrepoJson():: IDREPOGETIDBYUIN GET service call ended Successfully");
			try {
				return (JSONObject) new JSONParser().parse(response);
			} catch (org.json.simple.parser.ParseException e) {
				printLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
						ExceptionUtils.getStackTrace(e));
				throw new IdRepoAppException("Error while parsing string to JSONObject",e);
			}


		}
		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
				"Utilities::retrieveIdrepoJson()::exit UIN is null");
		return null;
	}


	/**
	 * Gets registration processor mapping json from config and maps to
	 * RegistrationProcessorIdentity java class.
	 *
	 * @return the registration processor identity json
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public JSONObject getRegistrationProcessorMappingJson() throws IOException {
		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"Utilities::getRegistrationProcessorMappingJson()::entry");

		mappingJsonString = (mappingJsonString != null && !mappingJsonString.isEmpty()) ?
				mappingJsonString : Utilities.getJson(configServerFileStorageURL, getRegProcessorIdentityJson);
		ObjectMapper mapIdentityJsonStringToObject = new ObjectMapper();
		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"Utilities::getRegistrationProcessorMappingJson()::exit");
		return JsonUtil.getJSONObject(mapIdentityJsonStringToObject.readValue(mappingJsonString, JSONObject.class), MappingJsonConstants.IDENTITY);

	}

	public String getMappingJsonValue(String key) throws IOException {
		JSONObject jsonObject = getRegistrationProcessorMappingJson();
		Object obj = jsonObject.get(key);
		if (obj instanceof LinkedHashMap) {
			LinkedHashMap hm = (LinkedHashMap) obj;
			return hm.get("value") != null ? hm.get("value").toString() : null;
		}
		return jsonObject.get(key) != null ? jsonObject.get(key).toString() : null;

	}

	/**
	 * Get UIN from identity json (used only for update/res update/activate/de
	 * activate packets).
	 *
	 * @param id
	 *            the registration id
	 * @return the u in
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws RegistrationProcessorCheckedException
	 */
	public String getUIn(String id, String source, String process) throws IOException, ApisResourceAccessException, PacketManagerException, JsonProcessingException {
		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
				id, "Utilities::getUIn()::entry");
		String uinKey = JsonUtil.getJSONValue(JsonUtil.getJSONObject(getRegistrationProcessorMappingJson(), MappingJsonConstants.UIN), VALUE);
		String UIN = packetManagerService.getField(id, uinKey, source, process);

		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
				id, "Utilities::getUIn()::exit");

		return UIN;

	}


	/**
	 * Gets the latest transaction id.
	 *
	 * @param registrationId
	 *            the registration id
	 * @return the latest transaction id
	 */
	/*
	 * public String getLatestTransactionId(String registrationId) {
	 * printLogger.debug(LoggerFileConstant.SESSIONID.toString(),
	 * LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
	 * "Utilities::getLatestTransactionId()::entry"); RegistrationStatusEntity
	 * entity = registrationStatusDao.findById(registrationId);
	 * printLogger.debug(LoggerFileConstant.SESSIONID.toString(),
	 * LoggerFileConstant.REGISTRATIONID.toString(), registrationId,
	 * "Utilities::getLatestTransactionId()::exit"); return entity != null ?
	 * entity.getLatestRegistrationTransactionId() : null;
	 * 
	 * }
	 */

	/**
	 * retrieve UIN from IDRepo by registration id.
	 *
	 * @param regId
	 *            the reg id
	 * @return the JSON object
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws IdRepoAppException
	 *             the id repo app exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public JSONObject retrieveUIN(String regId) throws ApisResourceAccessException, IdRepoAppException, IOException {
		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
				regId, "Utilities::retrieveUIN()::entry");

		if (regId != null) {
			List<String> pathSegments = new ArrayList<>();
			pathSegments.add(regId);
			IdResponseDTO1 idResponseDto;
			printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					regId, "Utilities::retrieveUIN():: RETRIEVEIDENTITYFROMRID GET service call Started");

			idResponseDto = (IdResponseDTO1) restClientService.getApi(ApiName.RETRIEVEIDENTITYFROMRID, pathSegments, "",
					"", IdResponseDTO1.class);
			printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
					"Utilities::retrieveUIN():: RETRIEVEIDENTITYFROMRID GET service call ended successfully");

			if (!idResponseDto.getErrors().isEmpty()) {
				printLogger.error(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), regId,
						"Utilities::retrieveUIN():: error with error message "
								+ PlatformErrorMessages.RPR_PVM_INVALID_UIN.getMessage() + " "
								+ idResponseDto.getErrors().toString());
				throw new IdRepoAppException(
						PlatformErrorMessages.RPR_PVM_INVALID_UIN.getMessage() + idResponseDto.getErrors().toString());
			}
			String response = objMapper.writeValueAsString(idResponseDto.getResponse().getIdentity());
			try {
				return (JSONObject) new JSONParser().parse(response);
			} catch (org.json.simple.parser.ParseException e) {
				printLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
						ExceptionUtils.getStackTrace(e));
				throw new IdRepoAppException("Error while parsing string to JSONObject",e);
			}

		}
		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"Utilities::retrieveUIN()::exit regId is null");

		return null;
	}

	/**
	 * Calculate age.
	 *
	 * @param applicantDob
	 *            the applicant dob
	 * @return the int
	 */
	private int calculateAge(String applicantDob) {
		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"Utilities::calculateAge():: entry");

		DateFormat sdf = new SimpleDateFormat(dobFormat);
		Date birthDate = null;
		try {
			birthDate = sdf.parse(applicantDob);

		} catch (ParseException e) {
			printLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", "Utilities::calculateAge():: error with error message "
							+ PlatformErrorMessages.RPR_SYS_PARSING_DATE_EXCEPTION.getMessage());
			throw new ParsingException(PlatformErrorMessages.RPR_SYS_PARSING_DATE_EXCEPTION.getCode(), e);
		}
		LocalDate ld = new java.sql.Date(birthDate.getTime()).toLocalDate();
		Period p = Period.between(ld, LocalDate.now());
		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"Utilities::calculateAge():: exit");

		return p.getYears();

	}



	/**
	 * Gets the uin by vid.
	 *
	 * @param vid
	 *            the vid
	 * @return the uin by vid
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws VidCreationException
	 *             the vid creation exception
	 */
	@SuppressWarnings("unchecked")
	public String getUinByVid(String vid) throws ApisResourceAccessException, VidCreationException {
		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"Utilities::getUinByVid():: entry");
		List<String> pathSegments = new ArrayList<>();
		pathSegments.add(vid);
		String uin = null;
		VidResponseDTO response;
		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"Stage::methodname():: RETRIEVEIUINBYVID GET service call Started");

		response = (VidResponseDTO) restClientService.getApi(ApiName.GETUINBYVID, pathSegments, "", "",
				VidResponseDTO.class);
		printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
				"Utilities::getUinByVid():: RETRIEVEIUINBYVID GET service call ended successfully");

		if (!response.getErrors().isEmpty()) {
			throw new VidCreationException(PlatformErrorMessages.RPR_PGS_VID_EXCEPTION.getMessage(),
					"VID creation exception");

		} else {
			uin = response.getResponse().getUin();
		}
		return uin;
	}



	/**
	 * Retrieve idrepo json status.
	 *
	 * @param uin
	 *            the uin
	 * @return the string
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 * @throws IdRepoAppException
	 *             the id repo app exception
	 */
	public String retrieveIdrepoJsonStatus(String uin) throws ApisResourceAccessException, IdRepoAppException {
		String response = null;
		if (uin != null) {
			printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
					"Utilities::retrieveIdrepoJson()::entry");
			List<String> pathSegments = new ArrayList<>();
			pathSegments.add(uin);
			IdResponseDTO1 idResponseDto;

			idResponseDto = (IdResponseDTO1) restClientService.getApi(ApiName.IDREPOGETIDBYUIN, pathSegments, "", "",
					IdResponseDTO1.class);
			if (idResponseDto == null) {
				printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
						"Utilities::retrieveIdrepoJson()::exit idResponseDto is null");
				return null;
			}
			if (!idResponseDto.getErrors().isEmpty()) {
				List<ErrorDTO> error = idResponseDto.getErrors();
				printLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
						"Utilities::retrieveIdrepoJson():: error with error message " + error.get(0).getMessage());
				throw new IdRepoAppException(error.get(0).getMessage());
			}

			response = idResponseDto.getResponse().getStatus();

			printLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
					"Utilities::retrieveIdrepoJson():: IDREPOGETIDBYUIN GET service call ended Successfully");
		}

		return response;
	}

	private void addSchemaVersion(JSONObject identityObject) throws IOException {

		JSONObject regProcessorIdentityJson = getRegistrationProcessorMappingJson();
		String schemaVersion = JsonUtil.getJSONValue(
				JsonUtil.getJSONObject(regProcessorIdentityJson, MappingJsonConstants.IDSCHEMA_VERSION),
				MappingJsonConstants.VALUE);

		identityObject.put(schemaVersion, Float.valueOf(idschemaVersion));

	}

}
