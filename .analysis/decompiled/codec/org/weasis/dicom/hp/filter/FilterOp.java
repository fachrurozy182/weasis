/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.filter;

import java.util.Collection;
import org.weasis.dicom.hp.filter.HPFilter;
import org.weasis.dicom.macro.Code;

public class FilterOp {
    public static final HPFilter MEMBER_OF = new MemberOf();
    public static final HPFilter NOT_MEMBER_OF = new NotMemberOf();
    public static final HPFilter RANGE_INCL = new RangeIncl();
    public static final HPFilter RANGE_EXCL = new RangeExcl();
    public static final HPFilter GREATER_OR_EQUAL = new GreaterOrEqual();
    public static final HPFilter LESS_OR_EQUAL = new LessOrEqual();
    public static final HPFilter GREATER_THAN = new GreaterThan();
    public static final HPFilter LESS_THAN = new LessThan();

    private FilterOp() {
    }

    public static HPFilter valueOf(String codeString) {
        return switch (codeString) {
            case "MEMBER_OF" -> MEMBER_OF;
            case "NOT_MEMBER_OF" -> NOT_MEMBER_OF;
            case "RANGE_INCL" -> RANGE_INCL;
            case "RANGE_EXCL" -> RANGE_EXCL;
            case "GREATER_OR_EQUAL" -> GREATER_OR_EQUAL;
            case "LESS_OR_EQUAL" -> LESS_OR_EQUAL;
            case "GREATER_THAN" -> GREATER_THAN;
            case "LESS_THAN" -> LESS_THAN;
            default -> throw new IllegalArgumentException("codeString: " + codeString);
        };
    }

    static boolean inRange(int value, int[] params) {
        return value >= params[0] && value <= params[1];
    }

    static boolean inRange(int value, long[] params) {
        long l = (long)value & 0xFFFFFFFFL;
        return l >= params[0] && l <= params[1];
    }

    static boolean inRange(float value, float[] params) {
        return value >= params[0] && value <= params[1];
    }

    static boolean inRange(double value, double[] params) {
        return value >= params[0] && value <= params[1];
    }

    static int compare(int value, int[] params) {
        return Integer.compare(value, params[0]);
    }

    static int compare(int value, long[] params) {
        long l = (long)value & 0xFFFFFFFFL;
        return Long.compare(l, params[0]);
    }

    static int compare(float value, float[] params) {
        return Float.compare(value, params[0]);
    }

    static int compare(double value, double[] params) {
        return Double.compare(value, params[0]);
    }

    static class MemberOf
    extends HPFilter {
        public MemberOf() {
            super("MEMBER_OF", 0);
        }

        @Override
        public boolean op(String[] values, int valueNumber, String[] params) {
            if (valueNumber != 0) {
                return MemberOf.memberOf(values[valueNumber - 1], params);
            }
            for (String value : values) {
                if (!MemberOf.memberOf(value, params)) continue;
                return true;
            }
            return false;
        }

        @Override
        public boolean op(int[] values, int valueNumber, int[] params) {
            if (valueNumber != 0) {
                return MemberOf.memberOf(values[valueNumber - 1], params);
            }
            for (int value : values) {
                if (!MemberOf.memberOf(value, params)) continue;
                return true;
            }
            return false;
        }

        @Override
        public boolean op(int[] values, int valueNumber, long[] params) {
            if (valueNumber != 0) {
                return MemberOf.memberOf(values[valueNumber - 1], params);
            }
            for (int value : values) {
                if (!MemberOf.memberOf(value, params)) continue;
                return true;
            }
            return false;
        }

        @Override
        public boolean op(float[] values, int valueNumber, float[] params) {
            if (valueNumber != 0) {
                return MemberOf.memberOf(values[valueNumber - 1], params);
            }
            for (float value : values) {
                if (!MemberOf.memberOf(value, params)) continue;
                return true;
            }
            return false;
        }

        @Override
        public boolean op(double[] values, int valueNumber, double[] params) {
            if (valueNumber != 0) {
                return MemberOf.memberOf(values[valueNumber - 1], params);
            }
            for (double value : values) {
                if (!MemberOf.memberOf(value, params)) continue;
                return true;
            }
            return false;
        }

        @Override
        public boolean op(Collection<Code> values, Collection<Code> params) {
            for (Code value : values) {
                if (!MemberOf.memberOf(value, params)) continue;
                return true;
            }
            return false;
        }
    }

    static class NotMemberOf
    extends HPFilter {
        public NotMemberOf() {
            super("NOT_MEMBER_OF", 0);
        }

        @Override
        public boolean op(String[] values, int valueNumber, String[] params) {
            if (valueNumber != 0) {
                return !NotMemberOf.memberOf(values[valueNumber - 1], params);
            }
            for (String value : values) {
                if (!NotMemberOf.memberOf(value, params)) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(int[] values, int valueNumber, int[] params) {
            if (valueNumber != 0) {
                return !NotMemberOf.memberOf(values[valueNumber - 1], params);
            }
            for (int value : values) {
                if (!NotMemberOf.memberOf(value, params)) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(int[] values, int valueNumber, long[] params) {
            if (valueNumber != 0) {
                return !NotMemberOf.memberOf(values[valueNumber - 1], params);
            }
            for (int value : values) {
                if (!NotMemberOf.memberOf(value, params)) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(float[] values, int valueNumber, float[] params) {
            if (valueNumber != 0) {
                return !NotMemberOf.memberOf(values[valueNumber - 1], params);
            }
            for (float value : values) {
                if (!NotMemberOf.memberOf(value, params)) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(double[] values, int valueNumber, double[] params) {
            if (valueNumber != 0) {
                return !NotMemberOf.memberOf(values[valueNumber - 1], params);
            }
            for (double value : values) {
                if (!NotMemberOf.memberOf(value, params)) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(Collection<Code> values, Collection<Code> params) {
            for (Code value : values) {
                if (!NotMemberOf.memberOf(value, params)) continue;
                return false;
            }
            return true;
        }
    }

    static class RangeIncl
    extends HPFilter {
        public RangeIncl() {
            super("RANGE_INCL", 2);
        }

        @Override
        public boolean op(int[] values, int valueNumber, int[] params) {
            if (valueNumber != 0) {
                return FilterOp.inRange(values[valueNumber - 1], params);
            }
            for (int value : values) {
                if (FilterOp.inRange(value, params)) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(int[] values, int valueNumber, long[] params) {
            if (valueNumber != 0) {
                return FilterOp.inRange(values[valueNumber - 1], params);
            }
            for (int value : values) {
                if (FilterOp.inRange(value, params)) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(float[] values, int valueNumber, float[] params) {
            if (valueNumber != 0) {
                return FilterOp.inRange(values[valueNumber - 1], params);
            }
            for (float value : values) {
                if (FilterOp.inRange(value, params)) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(double[] values, int valueNumber, double[] params) {
            if (valueNumber != 0) {
                return FilterOp.inRange(values[valueNumber - 1], params);
            }
            for (double value : values) {
                if (FilterOp.inRange(value, params)) continue;
                return false;
            }
            return true;
        }
    }

    static class RangeExcl
    extends HPFilter {
        public RangeExcl() {
            super("RANGE_EXCL", 2);
        }

        @Override
        public boolean op(int[] values, int valueNumber, int[] params) {
            if (valueNumber != 0) {
                return !FilterOp.inRange(values[valueNumber - 1], params);
            }
            for (int value : values) {
                if (!FilterOp.inRange(value, params)) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(int[] values, int valueNumber, long[] params) {
            if (valueNumber != 0) {
                return !FilterOp.inRange(values[valueNumber - 1], params);
            }
            for (int value : values) {
                if (!FilterOp.inRange(value, params)) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(float[] values, int valueNumber, float[] params) {
            if (valueNumber != 0) {
                return !FilterOp.inRange(values[valueNumber - 1], params);
            }
            for (float value : values) {
                if (!FilterOp.inRange(value, params)) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(double[] values, int valueNumber, double[] params) {
            if (valueNumber != 0) {
                return !FilterOp.inRange(values[valueNumber - 1], params);
            }
            for (double value : values) {
                if (!FilterOp.inRange(value, params)) continue;
                return false;
            }
            return true;
        }
    }

    static class GreaterOrEqual
    extends HPFilter {
        public GreaterOrEqual() {
            super("GREATER_OR_EQUAL", 1);
        }

        @Override
        public boolean op(int[] values, int valueNumber, int[] params) {
            if (valueNumber != 0) {
                return FilterOp.compare(values[valueNumber - 1], params) >= 0;
            }
            for (int value : values) {
                if (FilterOp.compare(value, params) >= 0) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(int[] values, int valueNumber, long[] params) {
            if (valueNumber != 0) {
                return FilterOp.compare(values[valueNumber - 1], params) >= 0;
            }
            for (int value : values) {
                if (FilterOp.compare(value, params) >= 0) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(float[] values, int valueNumber, float[] params) {
            if (valueNumber != 0) {
                return FilterOp.compare(values[valueNumber - 1], params) >= 0;
            }
            for (float value : values) {
                if (FilterOp.compare(value, params) >= 0) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(double[] values, int valueNumber, double[] params) {
            if (valueNumber != 0) {
                return FilterOp.compare(values[valueNumber - 1], params) >= 0;
            }
            for (double value : values) {
                if (FilterOp.compare(value, params) >= 0) continue;
                return false;
            }
            return true;
        }
    }

    static class LessOrEqual
    extends HPFilter {
        public LessOrEqual() {
            super("LESS_OR_EQUAL", 1);
        }

        @Override
        public boolean op(int[] values, int valueNumber, int[] params) {
            if (valueNumber != 0) {
                return FilterOp.compare(values[valueNumber - 1], params) <= 0;
            }
            for (int value : values) {
                if (FilterOp.compare(value, params) <= 0) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(int[] values, int valueNumber, long[] params) {
            if (valueNumber != 0) {
                return FilterOp.compare(values[valueNumber - 1], params) <= 0;
            }
            for (int value : values) {
                if (FilterOp.compare(value, params) <= 0) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(float[] values, int valueNumber, float[] params) {
            if (valueNumber != 0) {
                return FilterOp.compare(values[valueNumber - 1], params) <= 0;
            }
            for (float value : values) {
                if (FilterOp.compare(value, params) <= 0) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(double[] values, int valueNumber, double[] params) {
            if (valueNumber != 0) {
                return FilterOp.compare(values[valueNumber - 1], params) <= 0;
            }
            for (double value : values) {
                if (FilterOp.compare(value, params) <= 0) continue;
                return false;
            }
            return true;
        }
    }

    static class GreaterThan
    extends HPFilter {
        public GreaterThan() {
            super("GREATER_THAN", 1);
        }

        @Override
        public boolean op(int[] values, int valueNumber, int[] params) {
            if (valueNumber != 0) {
                return FilterOp.compare(values[valueNumber - 1], params) > 0;
            }
            for (int value : values) {
                if (FilterOp.compare(value, params) > 0) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(int[] values, int valueNumber, long[] params) {
            if (valueNumber != 0) {
                return FilterOp.compare(values[valueNumber - 1], params) > 0;
            }
            for (int value : values) {
                if (FilterOp.compare(value, params) > 0) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(float[] values, int valueNumber, float[] params) {
            if (valueNumber != 0) {
                return FilterOp.compare(values[valueNumber - 1], params) > 0;
            }
            for (float value : values) {
                if (FilterOp.compare(value, params) > 0) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(double[] values, int valueNumber, double[] params) {
            if (valueNumber != 0) {
                return FilterOp.compare(values[valueNumber - 1], params) > 0;
            }
            for (double value : values) {
                if (FilterOp.compare(value, params) > 0) continue;
                return false;
            }
            return true;
        }
    }

    static class LessThan
    extends HPFilter {
        public LessThan() {
            super("LESS_THAN", 1);
        }

        @Override
        public boolean op(int[] values, int valueNumber, int[] params) {
            if (valueNumber != 0) {
                return FilterOp.compare(values[valueNumber - 1], params) < 0;
            }
            for (int value : values) {
                if (FilterOp.compare(value, params) < 0) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(int[] values, int valueNumber, long[] params) {
            if (valueNumber != 0) {
                return FilterOp.compare(values[valueNumber - 1], params) < 0;
            }
            for (int value : values) {
                if (FilterOp.compare(value, params) < 0) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(float[] values, int valueNumber, float[] params) {
            if (valueNumber != 0) {
                return FilterOp.compare(values[valueNumber - 1], params) < 0;
            }
            for (float value : values) {
                if (FilterOp.compare(value, params) < 0) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean op(double[] values, int valueNumber, double[] params) {
            if (valueNumber != 0) {
                return FilterOp.compare(values[valueNumber - 1], params) < 0;
            }
            for (double value : values) {
                if (FilterOp.compare(value, params) < 0) continue;
                return false;
            }
            return true;
        }
    }
}

