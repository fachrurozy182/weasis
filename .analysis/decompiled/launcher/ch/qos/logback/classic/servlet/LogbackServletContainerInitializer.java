/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.servlet.ServletContainerInitializer
 *  jakarta.servlet.ServletContext
 *  jakarta.servlet.ServletException
 */
package ch.qos.logback.classic.servlet;

import ch.qos.logback.classic.servlet.LogbackServletContextListener;
import ch.qos.logback.classic.util.StatusViaSLF4JLoggerFactory;
import ch.qos.logback.core.util.OptionHelper;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import java.util.EventListener;
import java.util.Set;

public class LogbackServletContainerInitializer
implements ServletContainerInitializer {
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        if (this.isDisabledByConfiguration(ctx)) {
            StatusViaSLF4JLoggerFactory.addInfo("Due to deployment instructions will NOT register an instance of " + String.valueOf(LogbackServletContextListener.class) + " to the current web-app", this);
            return;
        }
        StatusViaSLF4JLoggerFactory.addInfo("Adding an instance of  " + String.valueOf(LogbackServletContextListener.class) + " to the current web-app", this);
        LogbackServletContextListener lscl = new LogbackServletContextListener();
        ctx.addListener((EventListener)((Object)lscl));
    }

    boolean isDisabledByConfiguration(ServletContext ctx) {
        String disableAttributeStr = null;
        String disableAttribute = ctx.getInitParameter("logbackDisableServletContainerInitializer");
        if (disableAttribute instanceof String) {
            disableAttributeStr = disableAttribute;
        }
        if (OptionHelper.isNullOrEmptyOrAllSpaces(disableAttributeStr)) {
            disableAttributeStr = OptionHelper.getSystemProperty("logbackDisableServletContainerInitializer");
        }
        if (OptionHelper.isNullOrEmptyOrAllSpaces(disableAttributeStr)) {
            disableAttributeStr = OptionHelper.getEnv("logbackDisableServletContainerInitializer");
        }
        return Boolean.parseBoolean(disableAttributeStr);
    }
}

