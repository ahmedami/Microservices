package com.ibra.services.trakingfile.utils;

public interface DefaultKeys
{
	static final String SESSION = "session";
	static final String TIMESTAMP = "timestamp";
	static final String NEW_VISIT = "new_visit";
	static final String USER_AGENT = "user_agent";
	static final String IP = "ip";
	static final String REFERER = "referer";
	static final String URL = "url";
	static final String ELAPSED = "elapsed";
	static final String PAGE_TYPE = "page_type";
	static final String DO_NOT_TRACK = "doNotTrack";

	static final String LAST_VISIT = "last_visit"; // not used anymore, since 2012-05-16 (johannes)
	static final String LAST_SESSION = "last_session"; // not used anymore, since 2012-05-16 (johannes)

	static final String LIFETIME = "lifetime"; // introduced on 2012-05-16 (johannes)
	static final String LIFETIME_FIRST_VISIT = "lifetime_1st_visit"; // introduced on 2012-05-16 (johannes)

	static final String IS_WHITELABELED = "is_whitelabel";

	static final String TRACKING_COOKIE_NAME = "vmTracking";
	static final String LIFETIME_COOKIE_NAME = "vmLife"; // (johannes) changed name to "reset" life time tracking
	static final String AFFILINET_LANDING_COOKIE = "vmAf";
	static final String HIDE_WELCOME_TEASER_COOKIE_NAME = "vmHideWelcomeTeaser";

	static final int SESSION_LENGTH_IN_SECONDS = 60 * 30; // 30 MIN
	static final long SESSION_LENGTH_IN_MILLIS = 1000L * SESSION_LENGTH_IN_SECONDS; // 30 MIN

	static final int LIFETIME_COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 365 * 5; // 5 YEARS
	static final char LIFETIME_COOKIE_VALUE_DELIMITER = '-';
	static final String LIFETIME_COOKIE_ZERO_LOGINS_FLAG = "Z";

	static final String SHOW_POPUPS = "vmPopups";
	static final String SHOW_REDIRECTION_HINT = "vmRedirectionHint";

	static final String VM_COLLAPSE = "vmCollapse";

}
