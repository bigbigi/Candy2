package com.permission.manager;

/**
 * Created by big on 2019/1/15.
 */

public interface OnPermission {

    void hasPermission();

    void noPermission(String permission);
}
