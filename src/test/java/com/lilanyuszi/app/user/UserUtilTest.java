package com.lilanyuszi.app.user;

import org.junit.jupiter.api.Test;

import static com.lilanyuszi.app.util.Constant.EMPTY_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserUtilTest {

    @Test
    void returnsEmptyStringsWhenUserIsNull() {
        assertEquals(EMPTY_STRING, UserUtil.getPicture(null));
        assertEquals(EMPTY_STRING, UserUtil.getName(null));
        assertEquals(EMPTY_STRING, UserUtil.getNickname(null));
    }

    @Test
    void returnsEmptyStringsWhenUserFieldsAreNull() {
        User user = new User();

        assertEquals(EMPTY_STRING, UserUtil.getPicture(user));
        assertEquals(EMPTY_STRING, UserUtil.getName(user));
        assertEquals(EMPTY_STRING, UserUtil.getNickname(user));
    }

    @Test
    void returnsUserFieldsWhenTheyArePresent() {
        User user = new User();
        user.setPicture("https://example.com/avatar.png");
        user.setName("Jane User");
        user.setNickName("jane");

        assertEquals("https://example.com/avatar.png", UserUtil.getPicture(user));
        assertEquals("Jane User", UserUtil.getName(user));
        assertEquals("jane", UserUtil.getNickname(user));
    }
}
