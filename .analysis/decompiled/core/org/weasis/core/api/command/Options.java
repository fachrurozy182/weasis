/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.LangUtil
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.weasis.core.api.command.Option;
import org.weasis.core.util.LangUtil;
import org.weasis.core.util.StringUtil;

public class Options
implements Option {
    public static final String NL = System.getProperty("line.separator", "\n");
    private static final String REGEX = "(?x)\\s*" + "(?:-([^-]))?" + "(?:,?\\s*-(\\w))?" + "(?:,?\\s*--(\\w[\\w-]*)(=\\w+)?)?" + "(?:,?\\s*--(\\w[\\w-]*))?" + ".*?(?:\\(default=(.*)\\))?\\s*";
    private static final int GROUP_SHORT_OPT_1 = 1;
    private static final int GROUP_SHORT_OPT_2 = 2;
    private static final int GROUP_LONG_OPT_1 = 3;
    private static final int GROUP_ARG_1 = 4;
    private static final int GROUP_LONG_OPT_2 = 5;
    private static final int GROUP_DEFAULT = 6;
    private final Pattern parser = Pattern.compile(REGEX);
    private final Pattern uname = Pattern.compile("^Usage:\\s+(\\w+)");
    private final Map<String, Boolean> unmodifiableOptSet;
    private final Map<String, Object> unmodifiableOptArg;
    private final Map<String, Boolean> optSet = new HashMap<String, Boolean>();
    private final Map<String, Object> optArg = new HashMap<String, Object>();
    private final Map<String, String> optName = new HashMap<String, String>();
    private final Map<String, String> optAlias = new HashMap<String, String>();
    private final List<Object> xargs = new ArrayList<Object>();
    private List<String> args = null;
    private static final String UNKNOWN = "unknown_usage_name";
    private String usageName = "unknown_usage_name";
    private int usageIndex = 0;
    private final String[] spec;
    private final String[] gspec;
    private final String[] defArgs;
    private String error = null;
    private boolean optionsFirst = false;
    private boolean stopOnBadOption = false;

    public static void main(String[] args) {
        String[] usage = new String[]{"test - test Options usage", "  text before Usage: is displayed when usage() is called and no error has occurred.", "  so can be used as a simple help message.", "", "Usage: testOptions [OPTION]... PATTERN [FILES]...", "  Output control: arbitrary non-option text can be included.", "  -? --help                show help", "  -c --count=COUNT           show COUNT lines", "  -h --no-filename         suppress the prefixing filename on output", "  -q --quiet, --silent     suppress all normal output", "     --binary-files=TYPE   assume that binary files are TYPE", "                           TYPE is 'binary', 'text', or 'without-match'", "  -I                       equivalent to --binary-files=without-match", "  -d --directories=ACTION  how to handle directories (default=skip)", "                           ACTION is 'read', 'recurse', or 'skip'", "  -D --devices=ACTION      how to handle devices, FIFOs and sockets", "                           ACTION is 'read' or 'skip'", "  -R, -r --recursive       equivalent to --directories=recurse"};
        Option opt = Options.compile(usage).parse(args);
        if (opt.isSet("help")) {
            opt.usage();
            return;
        }
        if (opt.args().isEmpty()) {
            throw opt.usageError("PATTERN not specified");
        }
        System.out.println(opt);
        if (opt.isSet("count")) {
            System.out.println("count = " + opt.getNumber("count"));
        }
        System.out.println("--directories specified: " + opt.isSet("directories"));
        System.out.println("directories=" + opt.get("directories"));
    }

    public static Option compile(String[] optSpec) {
        return new Options(optSpec, null, null);
    }

    public static Option compile(String optSpec) {
        return Options.compile(optSpec.split("\\n"));
    }

    public static Option compile(String[] optSpec, Option gopt) {
        return new Options(optSpec, null, gopt);
    }

    public static Option compile(String[] optSpec, String[] gspec) {
        return new Options(optSpec, gspec, null);
    }

    @Override
    public Option setStopOnBadOption(boolean stopOnBadOption) {
        this.stopOnBadOption = stopOnBadOption;
        return this;
    }

    @Override
    public Option setOptionsFirst(boolean optionsFirst) {
        this.optionsFirst = optionsFirst;
        return this;
    }

    @Override
    public boolean isSet(String name) {
        if (!this.optSet.containsKey(name)) {
            throw new IllegalArgumentException("option not defined in spec: " + name);
        }
        return this.optSet.get(name);
    }

    @Override
    public Object getObject(String name) {
        if (!this.optArg.containsKey(name)) {
            throw new IllegalArgumentException("option not defined with argument: " + name);
        }
        List<Object> list = this.getObjectList(name);
        return list.isEmpty() ? "" : list.get(list.size() - 1);
    }

    @Override
    public List<Object> getObjectList(String name) {
        ArrayList<Object> list;
        Object arg = this.optArg.get(name);
        if (arg == null) {
            throw new IllegalArgumentException("option not defined with argument: " + name);
        }
        if (arg instanceof String) {
            String val = (String)arg;
            list = new ArrayList<Object>();
            if (StringUtil.hasText((CharSequence)val)) {
                list.add(arg);
            }
        } else {
            list = (ArrayList<Object>)arg;
        }
        return list;
    }

    @Override
    public List<String> getList(String name) {
        ArrayList<String> list = new ArrayList<String>();
        for (Object o : this.getObjectList(name)) {
            try {
                list.add((String)o);
            }
            catch (ClassCastException e) {
                throw new IllegalArgumentException("option not String: " + name);
            }
        }
        return list;
    }

    private void addArg(String name, Object value) {
        ArrayList<Object> list;
        Object arg = this.optArg.get(name);
        if (arg instanceof String) {
            list = new ArrayList<Object>();
            this.optArg.put(name, list);
        } else {
            list = (ArrayList<Object>)arg;
        }
        list.add(value);
    }

    @Override
    public String get(String name) {
        try {
            return (String)this.getObject(name);
        }
        catch (ClassCastException e) {
            throw new IllegalArgumentException("option not String: " + name);
        }
    }

    @Override
    public int getNumber(String name) {
        String number = this.get(name);
        try {
            if (number != null) {
                return Integer.parseInt(number);
            }
            return 0;
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("option '" + name + "' not Number: " + number);
        }
    }

    @Override
    public List<Object> argObjects() {
        return this.xargs;
    }

    @Override
    public List<String> args() {
        if (this.args == null) {
            this.args = new ArrayList<String>();
            for (Object arg : this.xargs) {
                this.args.add(arg == null ? "null" : arg.toString());
            }
        }
        return this.args;
    }

    @Override
    public void usage() {
        StringBuilder buf = new StringBuilder();
        int index = 0;
        if (this.error != null) {
            buf.append(this.error);
            buf.append(NL);
            index = this.usageIndex;
        }
        for (int i = index; i < this.spec.length; ++i) {
            buf.append(this.spec[i]);
            buf.append(NL);
        }
        System.err.print(buf);
    }

    @Override
    public IllegalArgumentException usageError(String s) {
        this.error = this.usageName + ": " + s;
        this.usage();
        return new IllegalArgumentException(this.error);
    }

    private Options(String[] spec, String[] gspec, Option opt) {
        this.gspec = gspec;
        Options gopt = (Options)opt;
        if (gspec == null && gopt == null) {
            this.spec = spec;
        } else {
            ArrayList<String> list = new ArrayList<String>();
            list.addAll(Arrays.asList(spec));
            list.addAll(Arrays.asList(gspec != null ? gspec : gopt.gspec));
            this.spec = list.toArray(new String[0]);
        }
        HashMap<String, Boolean> myOptSet = new HashMap<String, Boolean>();
        HashMap<String, Object> myOptArg = new HashMap<String, Object>();
        this.parseSpec(myOptSet, myOptArg);
        if (gopt != null) {
            for (Map.Entry<String, Boolean> entry : gopt.optSet.entrySet()) {
                if (!LangUtil.nullToFalse((Boolean)entry.getValue())) continue;
                myOptSet.put(entry.getKey(), true);
            }
            for (Map.Entry<String, Object> entry : gopt.optArg.entrySet()) {
                if ("".equals(entry.getValue())) continue;
                myOptArg.put(entry.getKey(), entry.getValue());
            }
            gopt.reset();
        }
        this.unmodifiableOptSet = Collections.unmodifiableMap(myOptSet);
        this.unmodifiableOptArg = Collections.unmodifiableMap(myOptArg);
        String defOpts = System.getenv(this.usageName.toUpperCase() + "_OPTS");
        this.defArgs = defOpts != null ? defOpts.split("\\s+") : new String[]{};
    }

    private void parseSpec(Map<String, Boolean> myOptSet, Map<String, Object> myOptArg) {
        int index = 0;
        for (String line : this.spec) {
            Matcher u;
            Matcher m = this.parser.matcher(line);
            if (m.matches()) {
                String opt2;
                String dflt;
                String name;
                String opt = m.group(3);
                String string = name = opt != null ? opt : m.group(1);
                if (name != null) {
                    if (myOptSet.containsKey(name)) {
                        throw new IllegalArgumentException("duplicate option in spec: --" + name);
                    }
                    myOptSet.put(name, false);
                }
                String string2 = dflt = m.group(6) != null ? m.group(6) : "";
                if (m.group(4) != null) {
                    myOptArg.put(opt, dflt);
                }
                if ((opt2 = m.group(5)) != null) {
                    this.optAlias.put(opt2, opt);
                    myOptSet.put(opt2, false);
                    if (m.group(4) != null) {
                        myOptArg.put(opt2, "");
                    }
                }
                for (int i = 0; i < 2; ++i) {
                    String sopt = m.group(i == 0 ? 1 : 2);
                    if (sopt == null) continue;
                    if (this.optName.containsKey(sopt)) {
                        throw new IllegalArgumentException("duplicate option in spec: -" + sopt);
                    }
                    this.optName.put(sopt, name);
                }
            }
            if (Objects.equals(this.usageName, UNKNOWN) && (u = this.uname.matcher(line)).find()) {
                this.usageName = u.group(1);
                this.usageIndex = index;
            }
            ++index;
        }
    }

    private void reset() {
        this.optSet.clear();
        this.optSet.putAll(this.unmodifiableOptSet);
        this.optArg.clear();
        this.optArg.putAll(this.unmodifiableOptArg);
        this.xargs.clear();
        this.args = null;
        this.error = null;
    }

    @Override
    public Option parse(Object[] argv) {
        return this.parse(argv, false);
    }

    @Override
    public Option parse(List<?> argv) {
        return this.parse(argv, false);
    }

    @Override
    public Option parse(Object[] argv, boolean skipArg0) {
        if (null == argv) {
            throw new IllegalArgumentException("argv is null");
        }
        return this.parse(Arrays.asList(argv), skipArg0);
    }

    @Override
    public Option parse(List<?> argv, boolean skipArg0) {
        this.reset();
        ArrayList<String> arguments = new ArrayList<String>(Arrays.asList(this.defArgs));
        for (Object arg : argv) {
            if (skipArg0) {
                skipArg0 = false;
                this.usageName = arg.toString();
                continue;
            }
            arguments.add((String)arg);
        }
        String needArg = null;
        Object needOpt = null;
        boolean endOpt = false;
        block5: for (Object e : arguments) {
            String name;
            String arg;
            String string = arg = e == null ? "null" : e.toString();
            if (endOpt) {
                this.xargs.add(e);
                continue;
            }
            if (needArg != null) {
                this.addArg(needArg, e);
                needArg = null;
                needOpt = null;
                continue;
            }
            if (!arg.startsWith("-") || "-".equals(e)) {
                if (this.optionsFirst) {
                    endOpt = true;
                }
                this.xargs.add(e);
                continue;
            }
            if ("--".equals(arg)) {
                endOpt = true;
                continue;
            }
            if (arg.startsWith("--")) {
                int eq = arg.indexOf(61);
                String value = eq == -1 ? null : arg.substring(eq + 1);
                name = arg.substring(2, eq == -1 ? arg.length() : eq);
                ArrayList<String> names = new ArrayList<String>();
                if (this.optSet.containsKey(name)) {
                    names.add(name);
                } else {
                    for (String k : this.optSet.keySet()) {
                        if (!k.startsWith(name)) continue;
                        names.add(k);
                    }
                }
                switch (names.size()) {
                    case 1: {
                        name = (String)names.get(0);
                        this.optSet.put(name, true);
                        if (this.optArg.containsKey(name)) {
                            if (value != null) {
                                this.addArg(name, value);
                                break;
                            }
                            needArg = name;
                            break;
                        }
                        if (value == null) continue block5;
                        throw this.usageError("option '--" + name + "' doesn't allow an argument");
                    }
                    case 0: {
                        if (this.stopOnBadOption) {
                            endOpt = true;
                            this.xargs.add(e);
                            break;
                        }
                        throw this.usageError("invalid option '--" + name + "'");
                    }
                    default: {
                        throw this.usageError("option '--" + name + "' is ambiguous: " + String.valueOf(names));
                    }
                }
                continue;
            }
            for (int i = 1; i < arg.length(); ++i) {
                String c = String.valueOf(arg.charAt(i));
                if (this.optName.containsKey(c)) {
                    name = this.optName.get(c);
                    this.optSet.put(name, true);
                    if (!this.optArg.containsKey(name)) continue;
                    int k = i + 1;
                    if (k < arg.length()) {
                        this.addArg(name, arg.substring(k));
                        continue block5;
                    }
                    needOpt = c;
                    needArg = name;
                    continue block5;
                }
                if (this.stopOnBadOption) {
                    this.xargs.add("-" + c);
                    endOpt = true;
                    continue;
                }
                throw this.usageError("invalid option '" + c + "'");
            }
        }
        if (needArg != null) {
            String name = needOpt != null ? needOpt : "--" + needArg;
            throw this.usageError("option '" + name + "' requires an argument");
        }
        for (Map.Entry entry : this.optAlias.entrySet()) {
            if (LangUtil.nullToFalse((Boolean)this.optSet.get(entry.getKey()))) {
                this.optSet.put((String)entry.getValue(), true);
                if (this.optArg.containsKey(entry.getKey())) {
                    this.optArg.put((String)entry.getValue(), this.optArg.get(entry.getKey()));
                }
            }
            this.optSet.remove(entry.getKey());
            this.optArg.remove(entry.getKey());
        }
        return this;
    }

    public String toString() {
        return "isSet" + String.valueOf(this.optSet) + "\nArg" + String.valueOf(this.optArg) + "\nargs" + String.valueOf(this.xargs);
    }
}

