package com.sigiv.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilitario dev: genera hash BCrypt de un password.
 * Uso: mvn exec:java -Dexec.mainClass=com.sigiv.util.HashGen -Dexec.args="admin123"
 */
public class HashGen {
    public static void main(String[] args) {
        String pwd = args.length > 0 ? args[0] : "admin123";
        System.out.println(BCrypt.hashpw(pwd, BCrypt.gensalt(10)));
    }
}
