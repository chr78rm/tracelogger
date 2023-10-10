package de.christofreichardt.util;

import de.christofreichardt.diagnosis.BannerPrinter;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PropertyExpressionUnit5 implements WithAssertions {
    final private BannerPrinter bannerPrinter = new BannerPrinter();

    @BeforeAll
    void printHeader() {
        this.bannerPrinter.startUnit(getClass());
    }

    @Test
    void replaceSimpleExpression() {
        this.bannerPrinter.start("replaceSimpleExpression", getClass());

        Properties properties = new Properties();
        String logDir = "D:\\app-server\\wildfly-28.0.1.Final\\standalone\\log";
        properties.setProperty("jboss.server.log.dir", logDir);
        PropertyExpression propertyExpression = new PropertyExpression(properties);
        String result = propertyExpression.replace("${jboss.server.log.dir}");
        assertThat(result).isEqualTo(logDir);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "---${de.christofreichardt.util.1}---${de.christofreichardt.util.2}---",
            "---${de.christofreichardt.util.1}---${de.christofreichardt.util.2}",
            "${de.christofreichardt.util.1}---${de.christofreichardt.util.2}---",
            "---${de.christofreichardt.util.1}---${de.christofreichardt.util.2}---${de.christofreichardt.util.3}---",
            "---value-1---value-2---"
    })
    void replaceExpressions(String expression) {
        this.bannerPrinter.start("replaceExpressions", getClass());
        System.out.printf("expression = %s%n", expression);

        Map<String, String> expectedResults = Map.of(
                "---${de.christofreichardt.util.1}---${de.christofreichardt.util.2}---", "---value-1---value-2---",
                "---${de.christofreichardt.util.1}---${de.christofreichardt.util.2}", "---value-1---value-2",
                "${de.christofreichardt.util.1}---${de.christofreichardt.util.2}---", "value-1---value-2---",
                "---${de.christofreichardt.util.1}---${de.christofreichardt.util.2}---${de.christofreichardt.util.3}---", "---value-1---value-2---value-3---",
                "---value-1---value-2---", "---value-1---value-2---"
        );

        Properties properties = new Properties();
        properties.setProperty("de.christofreichardt.util.1", "value-1");
        properties.setProperty("de.christofreichardt.util.2", "value-2");
        properties.setProperty("de.christofreichardt.util.3", "value-3");
        PropertyExpression propertyExpression = new PropertyExpression(properties);
        String result = propertyExpression.replace(expression);
        System.out.printf("result = %s%n", result);
        assertThat(result).isEqualTo(expectedResults.get(expression));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "${de.christofreichardt.util.1}",
            "${de.christofreichardt.util.3}",
    })
    void replaceRecursively(String expression) {
        this.bannerPrinter.start("replaceRecursively", getClass());
        System.out.printf("expression = %s%n", expression);

        Map<String, String> expectedResults = Map.of(
                "${de.christofreichardt.util.1}", "---value-2---",
                "${de.christofreichardt.util.3}", "value-2"
        );

        Properties properties = new Properties();
        properties.setProperty("de.christofreichardt.util.1", "---${de.christofreichardt.util.2}---");
        properties.setProperty("de.christofreichardt.util.2", "value-2");
        properties.setProperty("de.christofreichardt.util.3", "${de.christofreichardt.util.2}");
        PropertyExpression propertyExpression = new PropertyExpression(properties);
        String result = propertyExpression.replace(expression, true);
        System.out.printf("result = %s%n", result);
        assertThat(result).isEqualTo(expectedResults.get(expression));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "---${de.christofreichardt.util.1}---${de.christofreichardt.util.2}---",
            "${de.christofreichardt.util.3}",
    })
    void noSuchProperty(String expression) {
        this.bannerPrinter.start("noSuchProperty", getClass());
        System.out.printf("expression = %s%n", expression);

        Properties properties = new Properties();
        properties.setProperty("de.christofreichardt.util.1", "---value-1---");
        properties.setProperty("de.christofreichardt.util.3", "---${de.christofreichardt.util.2}---");
        PropertyExpression propertyExpression = new PropertyExpression(properties);
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> propertyExpression.replace(expression, true));
    }

    @Test
    void endlessRecurring() {
        this.bannerPrinter.start("endlessRecurring", getClass());

        Properties properties = new Properties();
        properties.setProperty("de.christofreichardt.util.1", "---${de.christofreichardt.util.1}---");
        properties.setProperty("de.christofreichardt.util.2", "---value-2---");
        PropertyExpression propertyExpression = new PropertyExpression(properties);
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> propertyExpression.replace("${de.christofreichardt.util.1}---${de.christofreichardt.util.2}---", true))
                .withMessage("Too much recursion.");
    }
}
