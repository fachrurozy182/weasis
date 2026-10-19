/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.command;

import java.util.List;

public interface Option {
    public Option setStopOnBadOption(boolean var1);

    public Option setOptionsFirst(boolean var1);

    public Option parse(List<?> var1, boolean var2);

    public Option parse(List<?> var1);

    public Option parse(Object[] var1, boolean var2);

    public Option parse(Object[] var1);

    public boolean isSet(String var1);

    public String get(String var1);

    public List<String> getList(String var1);

    public Object getObject(String var1);

    public List<Object> getObjectList(String var1);

    public int getNumber(String var1);

    public List<String> args();

    public List<Object> argObjects();

    public void usage();

    public IllegalArgumentException usageError(String var1);

    default public boolean isOnlyOneOptionActivate(String ... options) {
        int trueCount = 0;
        for (String name : options) {
            if (!this.isSet(name)) continue;
            ++trueCount;
        }
        return trueCount == 1;
    }
}

