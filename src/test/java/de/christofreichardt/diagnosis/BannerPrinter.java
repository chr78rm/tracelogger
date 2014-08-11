/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis;

/**
 *
 * @author Developer
 */
public class BannerPrinter {
  public void start(String testName, Class<?> clazz) {
    System.out.println();
    System.out.printf("-- TEST --%n");
    System.out.printf("-- TEST --> %s.%s%n", clazz.getSimpleName(), testName);
    System.out.printf("-- TEST --%n");
  }
}
