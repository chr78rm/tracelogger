/*
 * TestClass.java
 *
 * Created on 26. April 2006, 11:44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.christofreichardt.diagnosis;

import java.util.Random;

/**
 * Shows the normal use of an injected tracer together with two error cases.
 *
 * @author Christof Reichardt
 */
public class TestClass {

    /**
     * the number of loops to perform
     */
    static public final int NUMBER_OF_LOOPS = 1000;

    private AbstractTracer tracer = null;
    private boolean emptyStackCorruption = false;
    private boolean stackOverFlowCorruption = false;
    private boolean staticCall = false;

    /**
     * Constructor expects a tracer instance.
     *
     * @param tracer the tracer to use
     */
    public TestClass(AbstractTracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Indicates if an empty stack corruption should be tested. On an average every hundreth call delivers true and as a result
     * a call to the entry()-method will be omitted. Subsequently an empty stack corruption occurs during a call of the wayout()- method.
     *
     * @return true if a call to the entry()-method should be omitted.
     */
    public boolean isEmptyStackCorruption() {
        boolean flag = false;

        if (this.emptyStackCorruption == true) {
            Random random = new Random();
            if (random.nextInt(100) == 25) {
                flag = true;
            }
        }

        return flag;
    }

    /**
     * Enables the empty stack corruption test case.
     *
     * @param emptyStackCorruption true enables the test case
     */
    public void setEmptyStackCorruption(boolean emptyStackCorruption) {
        this.emptyStackCorruption = emptyStackCorruption;
    }

    /**
     * Indicates if a stack overflow should be provoked. On an average every tenth call delivers true and as a result
     * a call to a wayout()-method will be omitted. Subsequently the stack size overflows during a call of the entry()-method.
     *
     * @return true if a call to the wayout()-method should be omitted.
     */
    public boolean isStackOverFlowCorruption() {
        boolean flag = false;

        if (this.stackOverFlowCorruption == true) {
            Random random = new Random();
            if (random.nextInt(100) >= 90) {
                flag = true;
            }
        }

        return flag;
    }

    /**
     * Enables the stack overflow test case.
     *
     * @param stackOverFlowCorruption true enables the test case
     */
    public void setStackOverFlowCorruption(boolean stackOverFlowCorruption) {
        this.stackOverFlowCorruption = stackOverFlowCorruption;
    }

    /**
     * Indicates that a static call should be made.
     *
     * @param staticCall the staticCall to set
     */
    public void setStaticCall(boolean staticCall) {
        this.staticCall = staticCall;
    }

    /*
     * element functions
     */

    /**
     * First level test method.
     */
    public void performTests() {
        this.tracer.entry("void", this, "performTests()");

        try {
            this.tracer.out().lock();
            try {
                this.tracer.out().printfIndentln("%s is performing tests ...", Thread.currentThread().getName());
                this.tracer.out().printfIndentln("%s: this.numberOfLoops = %d", Thread.currentThread().getName(), TestClass.NUMBER_OF_LOOPS);
            } finally {
                this.tracer.out().unlock();
            }

            for (int i = 0; i < TestClass.NUMBER_OF_LOOPS; i++) {
                try {
                    Integer index = Integer.valueOf(i);
                    this.tracer.out().runWithLock(() -> this.tracer.out().printfIndentln("%s: i = %d", Thread.currentThread().getName(), index));

                    firstTestMethod(i);
                } catch (java.lang.Exception ex) {
                    this.tracer.logException(LogLevel.WARNING, ex, this.getClass(), "performTests()");
                }
            }

            this.tracer.logMessage(LogLevel.WARNING, "Nur zum testen.", this.getClass(), "performTests()");
        } finally {
            this.tracer.wayout();
        }
    }

    private void firstTestMethod(int i) throws java.lang.Exception {
        this.tracer.entry("void", this, "firstTestMethod(int i)");

        try {
            this.tracer.out().runWithLock(() -> this.tracer.out().printfIndentln("%s is within first test method.", Thread.currentThread().getName()));

            secondTestMethod(i);
            thirdTestMethod(i, false);
        } finally {
            this.tracer.wayout();
        }
    }

    private void secondTestMethod(int i) throws java.lang.Exception {
        if (!isEmptyStackCorruption()) {
            this.tracer.entry("void", this, "secondTestMethod(int i)");
        }

        try {
            this.tracer.out().runWithLock(() -> this.tracer.out().printfIndentln("%s is within second test method.", Thread.currentThread().getName()));

            thirdTestMethod(i, true);
            if (this.staticCall) {
                staticCall(i, this.tracer);
            }
        } finally {
            if (!isStackOverFlowCorruption()) {
                this.tracer.wayout();
            }
        }
    }

    private void thirdTestMethod(int i, boolean flag) throws java.lang.Exception {
        this.tracer.entry("void", this, "thirdTestMethod(int i)");

        try {
            this.tracer.out().runWithLock(() -> this.tracer.out().printfIndentln("%s is within third test method.", Thread.currentThread().getName()));

            if (flag) {
                fourthTestMethod(i);
            }
            if (i % 10 == 0) {
                throw new java.lang.Exception(String.format("%d: Eine Exception zu Testzwecken.", i));
            }
        } finally {
            this.tracer.wayout();
        }
    }

    private void fourthTestMethod(int i) {
        this.tracer.entry("void", this, "fourthTestMethod(int i)");

        try {
            this.tracer.out().runWithLock(() -> this.tracer.out().printfIndentln("%s is within fourth test method.", Thread.currentThread().getName()));
        } finally {
            this.tracer.wayout();
        }
    }

    private static void staticCall(int i, AbstractTracer tracer) throws java.lang.Exception {
        tracer.entry("void", TestClass.class, "staticCall(int i, AbstractTracer tracer)");

        try {
            tracer.out().runWithLock(() -> tracer.out().printfIndentln("%s is within the static call.", Thread.currentThread().getName()));
        } finally {
            tracer.wayout();
        }
    }
}
