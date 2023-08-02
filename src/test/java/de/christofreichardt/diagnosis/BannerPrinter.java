/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * @author Developer
 */
public class BannerPrinter {
    public void start(String testName, Class<?> clazz) {
        System.out.println();
        System.out.printf("-- TEST --%n");
        System.out.printf("-- TEST --> %s:%s.%s%n", Thread.currentThread().getName(), clazz.getSimpleName(), testName);
        System.out.printf("-- TEST --%n");
    }

    public void startUnit(Class<?> clazz) {
        String header =
                "| " +
                String.format("Starting '%s' at '%s'", clazz.getSimpleName(), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) +
                " |";
        char[] cells = new char[header.length() - 2];
        Arrays.fill(cells, '-');
        String border =
                '+' +
                String.valueOf(cells) +
                '+';
        System.out.printf("%n%s%n", border);
        System.out.printf("%s%n", header);
        System.out.printf("%s%n", border);
    }
}
