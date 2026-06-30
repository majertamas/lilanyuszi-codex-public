package com.lilanyuszi.app.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constant {

    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String BEARER = "Bearer";
    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";
    public static final String BEARER_PLUS_EMPTY = BEARER + SPACE;
    public static final String LAX = "Lax";
    public static final String SLASH = "/";
    public static final String AUTHORIZATION = "Authorization";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String JWT_CLAIM_EMAIL = "email";
    public static final String JWT_CLAIM_ROLE = "role";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String API_AUTH_PATH = "/api/auth";
    public static final String API_SHOPPING_LIST_PATH = "/api/shopping-lists";
    public static final String API_LIST_PATH = "/api/lists";
    public static final String API_SHARED_ACCESS_ALIAS_PATH = "/api/shared-access-aliases";
    public static final String AUTH_REFRESH_PATH = "/refresh";
    public static final String AUTH_LOGOUT_PATH = "/logout";
    public static final String API_AUTH_REFRESH_PATH = API_AUTH_PATH + AUTH_REFRESH_PATH;
    public static final String API_AUTH_LOGOUT_PATH = API_AUTH_PATH + AUTH_LOGOUT_PATH;
    public static final String API_AUTH_PATTERN = API_AUTH_PATH + "/**";
    public static final String OAUTH2_PATH_PREFIX = "/oauth2/";
    public static final String OAUTH2_PATTERN = "/oauth2/**";
    public static final String SHOPPING_LIST_NOT_FOUND = "Shopping list not found";
    public static final String SHOPPING_LIST_ACCESS_DENIED = "Shopping list access denied";
    public static final String SHOPPING_LIST_NAME_REQUIRED = "Shopping list name is required";
    public static final String SHARED_ACCESS_EXISTS = "List already exists";
    public static final String SHARED_ACCESS_NOT_FOUND = "Shared access not found";
    public static final String SHARED_ACCESS_ID_REQUIRED = "Shared access id is required";
    public static final String SHARED_ACCESS_ALIAS_ACCESS_DENIED = "Shared access alias access denied";
    public static final String SHARED_ACCESS_ALIAS_REQUIRED = "Shared access alias is required";
    public static final long ACCESS_TOKEN_EXPIRATION_MINUTES = 15;
    public static final long REFRESH_TOKEN_EXPIRATION_DAYS = 30;
}
