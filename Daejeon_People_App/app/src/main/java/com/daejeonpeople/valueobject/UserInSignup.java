package com.daejeonpeople.valueobject;

/**
 * Created by dsm2016 on 2017-07-04.
 */

public class UserInSignup {
    public static String email;
    public static boolean emailCertified;

    public static String name;
    public static boolean nameChecked;

    public static String id;
    public static boolean idChecked;

    public static String password;
    public static boolean passwordConfirmed;

    public static void initializeAll() {
        nameChecked = false;
        idChecked = false;
        passwordConfirmed = false;
    }
}
