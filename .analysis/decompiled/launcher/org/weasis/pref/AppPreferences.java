/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.pref;

import com.formdev.flatlaf.json.Json;
import com.formdev.flatlaf.json.ParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.launcher.FileUtil;
import org.weasis.launcher.Utils;
import org.weasis.pref.Preference;

public class AppPreferences
extends HashMap<String, Preference> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppPreferences.class);
    private static final String DELIM_START = "${";
    private static final String DELIM_STOP = "}";
    static final String CODE = "code";
    static final String VAL = "value";
    static final String DESC = "description";
    static final String TYPE = "type";
    static final String JAVA_TYPE = "javaType";
    static final String DEFAULT = "defaultValue";
    static final String CATEGORY = "category";

    public AppPreferences() {
        super(50);
    }

    public AppPreferences(Map<String, Preference> m) {
        super(m);
    }

    public boolean readJson(URI uri) {
        Map json;
        try (InputStream is = FileUtil.getAdaptedConnection(uri.toURL(), false).getInputStream();
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);){
            json = (Map)Json.parse(reader);
        }
        catch (ParseException | IOException ex) {
            LOGGER.error("Cannot read json file: {}", (Object)uri, (Object)ex);
            return false;
        }
        List prefList = (List)json.get("weasisPreferences");
        if (prefList == null) {
            LOGGER.error("No weasisPreferences found in json file: {}", (Object)uri);
            return false;
        }
        for (Map map : prefList) {
            String code = (String)map.get(CODE);
            Preference p = new Preference(code, (String)map.get(TYPE), (String)map.get(JAVA_TYPE), (String)map.get(CATEGORY));
            p.setValue((String)map.get(VAL));
            p.setDescription((String)map.get(DESC));
            p.setDefaultValue((String)map.get(DEFAULT));
            this.put(code, p);
        }
        return true;
    }

    public String getValue(String key) {
        return Optional.ofNullable((Preference)this.get(key)).map(Preference::getValue).orElse(null);
    }

    public String getValue(String key, String defaultVal) {
        return Optional.ofNullable((Preference)this.get(key)).map(Preference::getValue).filter(Utils::hasText).orElse(defaultVal);
    }

    public String substVars(String val, String currentKey, Map<String, String> cycleMap) throws IllegalArgumentException {
        int startDelim;
        if (cycleMap == null) {
            cycleMap = new HashMap<String, String>();
        }
        cycleMap.put(currentKey, currentKey);
        if (val == null) {
            return "";
        }
        int stopDelim = -1;
        do {
            if ((stopDelim = ((String)val).indexOf(DELIM_STOP, stopDelim + 1)) < 0) {
                return val;
            }
            startDelim = ((String)val).lastIndexOf(DELIM_START, stopDelim);
            if (startDelim >= 0) continue;
            return val;
        } while (startDelim > stopDelim);
        String variable = ((String)val).substring(startDelim + DELIM_START.length(), stopDelim);
        if (cycleMap.get(variable) != null) {
            throw new IllegalArgumentException("recursive variable reference: " + variable);
        }
        String substValue = this.getValue(variable);
        if (substValue == null) {
            substValue = System.getProperty(variable, "");
        }
        cycleMap.remove(variable);
        val = ((String)val).substring(0, startDelim) + substValue + ((String)val).substring(stopDelim + DELIM_STOP.length());
        val = this.substVars((String)val, currentKey, cycleMap);
        return val;
    }
}

