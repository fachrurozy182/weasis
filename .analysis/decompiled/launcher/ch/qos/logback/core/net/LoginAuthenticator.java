/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.mail.Authenticator
 *  jakarta.mail.PasswordAuthentication
 */
package ch.qos.logback.core.net;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

public class LoginAuthenticator
extends Authenticator {
    String username;
    String password;

    LoginAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(this.username, this.password);
    }
}

