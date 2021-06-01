package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MuadmPasswordManagerModule extends GenericPasswordManagerModule {

	private final static Logger log = LoggerFactory.getLogger(MuadmPasswordManagerModule.class);

	@Override
	public void checkLoginFormat(PerunSession sess, String login) throws InvalidLoginException {

		((PerunBl)sess.getPerun()).getModulesUtilsBl().checkLoginNamespaceRegex("mu-adm", login, GenericPasswordManagerModule.defaultLoginPattern);

		if (!((PerunBl)sess.getPerun()).getModulesUtilsBl().isUserLoginPermitted("mu-adm", login)) {
			log.warn("Login '{}' is not allowed in {} namespace by configuration.", login, "mu-adm");
			throw new InvalidLoginException("Login '"+login+"' is not allowed in 'mu' namespace by configuration.");
		}

	}

	@Override
	public void checkPasswordStrength(PerunSession sess, String login, String password) throws PasswordStrengthException {
		if (StringUtils.isBlank(password)) {
			log.warn("Password for {}:{} cannot be empty.", "mu-adm", login);
			throw new PasswordStrengthException("Password for " + "mu-adm" + ":" + login + " cannot be empty.");
		}
	}
}
