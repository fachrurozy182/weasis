/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.scribejava.core.oauth.OAuth20Service
 */
package org.weasis.core.api.net.auth;

import com.github.scribejava.core.oauth.OAuth20Service;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Optional;
import org.weasis.core.api.net.auth.AsyncCallbackServerHandler;

public interface AcceptCallbackHandler
extends CompletionHandler<AsynchronousSocketChannel, AsyncCallbackServerHandler> {
    public Optional<String> code();

    public void code(String var1);

    public OAuth20Service service();
}

