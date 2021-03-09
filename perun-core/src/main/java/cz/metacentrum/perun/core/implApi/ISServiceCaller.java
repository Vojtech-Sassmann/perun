package cz.metacentrum.perun.core.implApi;

import java.io.IOException;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public interface ISServiceCaller {
	String IS_ERROR_STATUS = "ERR";
	String IS_OK_STATUS = "OK";

	ISResponseData call(String requestBody, int requestId) throws IOException;
}
