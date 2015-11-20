package org.alfresco.museum.ucm;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Anonymous user should be configured before any attempt to login it is made.
 * Ideally this should be done just once upon Spring context initialization.
 * Unfortunately such services as personService can't be invoked at this early
 * stage. They were designed to be invoked during HTTP request handling only. As
 * a workaround any web script which needs to login anonymous user should call
 * this webscript. It is also the only valid way to get user credentials.
 */
public class UCMAnonymousUser extends DeclarativeWebScript {
	public static final String USERNAME_PROERTY_NAME = "username";
	public static final String PASSWORD_PROERTY_NAME = "password";

	private static final String USERNAME = "visitor";
	private static final String PASSWORD = "visitor";

	private AuthorityService authorityService;
	private PersonService personService;
	private PermissionService permissionService;
	private MutableAuthenticationService authenticationService;

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status) {
		configureAnonymousUser();
		HashMap<String, Object> model = new HashMap<String, Object>();
		model.put(USERNAME_PROERTY_NAME, USERNAME);
		model.put(PASSWORD_PROERTY_NAME, PASSWORD);
		return model;
	};

	/**
	 * See <a href=
	 * "https://forums.alfresco.com/forum/general/non-technical-alfresco-discussion/creating-users-using-java-api-10032008-1248"
	 * >discussion thread</a>
	 */
	private void configureAnonymousUser() {
		if (!this.getPersonService().personExists(USERNAME)) {
			// create the node to represent the Person
			NodeRef anonymousUserRef = this.getPersonService().getPerson(USERNAME);

			// ensure that user can access their own Person object
			this.getPermissionService().setPermission(anonymousUserRef, USERNAME,
					this.getPermissionService().getAllPermission(), true);

			//TODO: explicitly add user to group PermissionService.GUEST_AUTHORITY or AuthorityType.EVERYONE.name() ?
			//this.getAuthorityService().addAuthority("Everyone", USERNAME);

			// create the ACEGI Authentication instance for the new user
			this.getAuthenticationService().createAuthentication(USERNAME, PASSWORD.toCharArray());
		}
	}

	public AuthorityService getAuthorityService() {
		return authorityService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public PersonService getPersonService() {
		return personService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public PermissionService getPermissionService() {
		return permissionService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public MutableAuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public void setAuthenticationService(MutableAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
}
