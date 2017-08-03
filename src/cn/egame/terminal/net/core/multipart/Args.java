package cn.egame.terminal.net.core.multipart;

import java.util.Collection;

import android.text.TextUtils;

public class Args {
    public Args() {
    }

    public static void check(boolean expression, String message) {
        if(!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void check(boolean expression, String message, Object... args) {
        if(!expression) {
            throw new IllegalArgumentException(String.format(message, args));
        }
    }

    public static <T> T notNull(T argument, String name) {
        if(argument == null) {
            throw new IllegalArgumentException(name + " may not be null");
        } else {
            return argument;
        }
    }

    public static <T extends CharSequence> T notEmpty(T argument, String name) {
        if(argument == null) {
            throw new IllegalArgumentException(name + " may not be null");
        } else if(TextUtils.isEmpty(argument)) {
            throw new IllegalArgumentException(name + " may not be empty");
        } else {
            return argument;
        }
    }

    public static <T extends CharSequence> T notBlank(T argument, String name) {
        if(argument == null) {
            throw new IllegalArgumentException(name + " may not be null");
        } else if(TextUtils.isEmpty(argument)) {
            throw new IllegalArgumentException(name + " may not be blank");
        } else {
            return argument;
        }
    }

    public static <E, T extends Collection<E>> T notEmpty(T argument, String name) {
        if(argument == null) {
            throw new IllegalArgumentException(name + " may not be null");
        } else if(argument.isEmpty()) {
            throw new IllegalArgumentException(name + " may not be empty");
        } else {
            return argument;
        }
    }

    public static int positive(int n, String name) {
        if(n <= 0) {
            throw new IllegalArgumentException(name + " may not be negative or zero");
        } else {
            return n;
        }
    }

    public static long positive(long n, String name) {
        if(n <= 0L) {
            throw new IllegalArgumentException(name + " may not be negative or zero");
        } else {
            return n;
        }
    }

    public static int notNegative(int n, String name) {
        if(n < 0) {
            throw new IllegalArgumentException(name + " may not be negative");
        } else {
            return n;
        }
    }

    public static long notNegative(long n, String name) {
        if(n < 0L) {
            throw new IllegalArgumentException(name + " may not be negative");
        } else {
            return n;
        }
    }
}
