package de.christofreichardt.util;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class responsible for searching for expressions of the form ${property} within strings and replacing them with the value of the corresponding property from the
 * given <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Properties.html">Properties</a> - if there is one.
 *
 * @author Christof Reichardt
 */
public class PropertyExpression {
    static final int MAX_DEPTH = 5;
    final Map<Object, Object> properties;
    final Pattern pattern = Pattern.compile("\\$\\{[a-zA-Z0-9.]+}");

    /**
     * Constructor expects some <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Properties.html">Properties</a>.
     * @param properties the given name-value pairs
     */
    public PropertyExpression(Properties properties) {
        this.properties = Collections.unmodifiableMap(properties);
    }

    /**
     * Replaces expressions of the form ${property} within the given String by replacing ${property} with the corresponding property value.
     *
     * @param expression the to be examined String
     * @param recursive if true the replacement is recursively executed until no expressions of the form ${property} can be found anymore
     * @return the substituted String
     */
    public String replace(String expression, final boolean recursive) {
        return replace(expression, recursive, 0);
    }

    /**
     * Replaces expressions of the form ${property} within the given String by replacing ${property} with the corresponding property value (no recursion)
     * .
     * @param expression the to be examined String
     * @return the substituted String
     */
    public String replace(String expression) {
        return replace(expression, false, 0);
    }

    private String replace(String expression, final boolean recursive, int depth) {
        if (depth > MAX_DEPTH) {
            throw new RuntimeException("Too much recursion.");
        }
        String result;
        Matcher matcher = this.pattern.matcher(expression);
        if (matcher.matches()) {
            String key = expression.substring(2, expression.length() - 1);
            if (this.properties.containsKey(key)) {
                expression = (String) this.properties.get(key);
                if (recursive) {
                    result = replace(expression, true, ++depth);
                } else {
                    result = expression;
                }
            } else {
                throw new NoSuchElementException(key);
            }
        } else {
            int pos = 0;
            StringBuilder stringBuilder = new StringBuilder();
            boolean found = false;
            while (matcher.find()) {
                found = true;
                stringBuilder.append(expression, pos, matcher.start());
                String key = expression.substring(matcher.start() + 2, matcher.end() - 1);
                if (this.properties.containsKey(key)) {
                    stringBuilder.append(this.properties.get(key));
                    pos = matcher.end();
                } else {
                    throw new NoSuchElementException(key);
                }
            }
            stringBuilder.append(expression, pos, expression.length());
            if (found && recursive) {
                result = replace(stringBuilder.toString(), true, ++depth);
            } else {
                result = stringBuilder.toString();
            }
        }

        return result;
    }
}
