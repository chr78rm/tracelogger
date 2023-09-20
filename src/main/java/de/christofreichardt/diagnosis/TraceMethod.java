/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.diagnosis;

import java.lang.management.ManagementFactory;

/**
 * Outlines a method for tracing.
 *
 * @author Christof Reichardt
 */
public class TraceMethod {

    final private String signature;
    private final Class<?> clazz;
    final private long startTime = System.currentTimeMillis();
    final private long startCpuTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
    private long elapsedTime;
    private long elapsedCpuTime;
    private final String methodName;

    /**
     * Constructs a TraceMethod with the specified signature.
     *
     * @param signature the signature of the TraceMethod
     */
    public TraceMethod(String signature) {
        this.signature = signature;
        this.methodName = signature;
        this.clazz = null;
    }

    /**
     * Constructs a TraceMethod with the specified signature and class.
     *
     * @param signature the signature of the TraceMethod
     * @param clazz     the class of the TraceMethod
     */
    @Deprecated
    public TraceMethod(String signature, Class<?> clazz) {
        this.signature = signature;
        this.methodName = signature;
        this.clazz = clazz;
    }

    /**
     * Constructs a TraceMethod with the specified signature.
     *
     * @param returnType      the return type of the method as string representation
     * @param object          the object that owns the method
     * @param methodSignature the remaining method signature (without return type) inclusive parameter as string representation
     */
    public TraceMethod(String returnType, Object object, String methodSignature) {
        this.clazz = object.getClass();
        this.signature = returnType + " " + this.clazz.getSimpleName() + "[" + System.identityHashCode(object) + "]." + methodSignature;
        this.methodName = methodSignature;
    }

    /**
     * Constructs a TraceMethod with the specified signature.
     *
     * @param returnType      the return type of the method as string representation
     * @param clazz           the class to which that method belong
     * @param methodSignature the remaining method signature (without return type) inclusive parameter as string representation
     */
    public TraceMethod(String returnType, Class<?> clazz, String methodSignature) {
        this.clazz = clazz;
        this.signature = returnType + " " + this.clazz.getSimpleName() + "." + methodSignature;
        this.methodName = methodSignature;
    }

    /**
     * This is the string representation of the method signature. It is composed of the return type, the owning class
     * and the remaining method signature (method name and list of parameters).
     *
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * This is the elapsed time in milliseconds since the construction of the TraceMethod until the moment the TraceMethod
     * is popped from the stack again.
     *
     * @return the elapsedTime
     */
    public long getElapsedTime() {
        return elapsedTime;
    }

    /**
     * This is the elapsed CPU time since the construction of the TraceMethod until the moment the TraceMethod
     * is popped from the stack again. That is only the execution time of the current thread on the CPU will be measured.
     * Suppose the scheduler decides to switch the contect to another thread during the execution of the method. This time doesn't count.
     * Or the method itself passes the actual work to another thread. That doesn't count either.
     *
     * @return the elapsedCpuTime
     */
    public long getElapsedCpuTime() {
        return elapsedCpuTime;
    }

    /**
     * The class owning the method.
     *
     * @return the clazz
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * The method name inclusive list of parameters.
     *
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Stops the elapsed (cpu) time since creation of this TraceMethod instance.
     */
    public void stopTime() {
        this.elapsedTime = System.currentTimeMillis() - this.startTime;
        this.elapsedCpuTime = (ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - this.startCpuTime) / 1000000;
    }
}
