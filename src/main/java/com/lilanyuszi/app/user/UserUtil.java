package com.lilanyuszi.app.user;

import lombok.experimental.UtilityClass;

import static com.lilanyuszi.app.util.Constant.EMPTY_STRING;

@UtilityClass
public class UserUtil {

    public static String getPicture(User user) {
        if (user == null) {
            return "";
        }
        return user.getPicture() == null ? EMPTY_STRING : user.getPicture();
    }

    public static String getName(User user) {
        if (user == null) {
            return "";
        }
        return user.getName() == null ? EMPTY_STRING : user.getName();
    }

    public static String getNickname(User user) {
        if (user == null) {
            return "";
        }
        return user.getNickName() == null ? EMPTY_STRING : user.getNickName();
    }
}
