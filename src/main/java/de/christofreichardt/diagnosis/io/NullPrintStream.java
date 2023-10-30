package de.christofreichardt.diagnosis.io;

import java.io.OutputStream;
import java.util.Locale;

/**
 * The tracer classes return such a NullPrintStream when the current state and the configuration demands that no output should be written. This is equivalent to a redirection to /dev/null.
 *
 * @author Christof Reichardt
 */
final public class NullPrintStream extends IndentablePrintStream {

    public NullPrintStream() {
        super(new NullOutputStream());
    }

    @Override
    public IndentablePrintStream printIndent(String s) {
        return this;
    }

    @Override
    public IndentablePrintStream printIndentln(String s) {
        return this;
    }

    @Override
    public IndentablePrintStream printIndentString() {
        return this;
    }

    @Override
    public IndentablePrintStream printfIndentln(String format, Object... args) {
        return this;
    }

    @Override
    public IndentablePrintStream printfIndentlnWithLock(String format, Object... args) {
        return this;
    }

    @Override
    public void lock() {
    }

    @Override
    public void unlock() {
    }

    @Override
    public void runWithLock(Runnable runnable) {
    }

    /**
     * Pseudo append()-method.
     *
     * @param csq won't be evaluated
     * @return this indentable print stream
     */
    @Override
    public IndentablePrintStream append(CharSequence csq) {
        return this;
    }

    /**
     * Pseudo append()-method.
     *
     * @param csq   won't be evaluated
     * @param start won't be evaluated
     * @param end   won't be evaluated
     * @return this indentable print stream
     */
    @Override
    public IndentablePrintStream append(CharSequence csq, int start, int end) {
        return this;
    }

    /**
     * Pseudo append()-method.
     *
     * @param c won't be evaluated
     * @return this indentable print stream
     */
    @Override
    public IndentablePrintStream append(char c) {
        return this;
    }

    /**
     * Pseudo clearError()-method.
     */
    @Override
    protected void clearError() {
    }

    /**
     * Indicates always no error.
     *
     * @return always false
     */
    @Override
    public boolean checkError() {
        return false;
    }

    /**
     * Pseudo format()-method.
     *
     * @param l      won't be evaluated
     * @param format won't be evaluated
     * @param args   won't be evaluated
     * @return this indentable print stream
     */
    @Override
    public IndentablePrintStream format(Locale l, String format, Object... args) {
        return this;
    }

    /**
     * Pseudo format()-method.
     *
     * @param format won't be evaluated
     * @param args   won't be evaluated
     * @return this indentable print stream
     */
    @Override
    public IndentablePrintStream format(String format, Object... args) {
        return this;
    }

    /**
     * Pseudo print()-method.
     *
     * @param s won't be evaluated
     */
    @Override
    public void print(char[] s) {
    }

    /**
     * Pseudo print()-method.
     *
     * @param obj won't be evaluated
     */
    @Override
    public void print(Object obj) {
    }

    /**
     * Pseudo print()-method.
     *
     * @param c won't be evaluated
     */
    @Override
    public void print(char c) {
    }

    /**
     * Pseudo print()-method.
     *
     * @param i won't be evaluated
     */
    @Override
    public void print(int i) {
    }

    /**
     * Pseudo print()-method.
     *
     * @param s won't be evaluated
     */
    @Override
    public void print(String s) {
    }

    /**
     * Pseudo print()-method.
     *
     * @param b won't be evaluated
     */
    @Override
    public void print(boolean b) {
    }

    /**
     * Pseudo print()-method.
     *
     * @param l won't be evaluated
     */
    @Override
    public void print(long l) {
    }

    /**
     * Pseudo print()-method.
     *
     * @param d won't be evaluated
     */
    @Override
    public void print(double d) {
    }

    /**
     * Pseudo print()-method.
     *
     * @param f won't be evaluated
     */
    @Override
    public void print(float f) {
    }

    /**
     * Pseudo printf()-method.
     *
     * @param l      won't be evaluated
     * @param format won't be evaluated
     * @param args   won't be evaluated
     * @return this indentable print stream
     */
    @Override
    public IndentablePrintStream printf(Locale l, String format, Object... args) {
        return this;
    }

    /**
     * Pseudo printf()-method.
     *
     * @param format won't be evaluated
     * @param args   won't be evaluated
     * @return this indentable print stream
     */
    @Override
    public IndentablePrintStream printf(String format, Object... args) {
        return this;
    }

    /**
     * Pseudo println()-method.
     *
     * @param x won't be evaluated
     */
    @Override
    public void println(char[] x) {
    }

    /**
     * Pseudo println()-method.
     */
    @Override
    public void println() {
    }

    /**
     * Pseudo println()-method.
     *
     * @param x won't be evaluated
     */
    @Override
    public void println(Object x) {
    }

    /**
     * Pseudo println()-method.
     *
     * @param x won't be evaluated
     */
    @Override
    public void println(char x) {
    }

    /**
     * Pseudo println()-method.
     *
     * @param x won't be evaluated
     */
    @Override
    public void println(int x) {
    }

    /**
     * Pseudo println()-method.
     *
     * @param x won't be evaluated
     */
    @Override
    public void println(String x) {
    }

    /**
     * Pseudo println()-method.
     *
     * @param x won't be evaluated
     */
    @Override
    public void println(boolean x) {
    }

    /**
     * Pseudo println()-method.
     *
     * @param x won't be evaluated
     */
    @Override
    public void println(long x) {
    }

    /**
     * Pseudo println()-method.
     *
     * @param x won't be evaluated
     */
    @Override
    public void println(double x) {
    }

    /**
     * Pseudo println()-method.
     *
     * @param x won't be evaluated
     */
    @Override
    public void println(float x) {
    }

    /**
     * Pseudo setError()-method.
     */
    @Override
    protected void setError() {
    }

    /**
     * Pseudo write()-method.
     *
     * @param b won't be evaluated
     */
    @Override
    public void write(int b) {
    }

    /**
     * Pseudo write()-method.
     *
     * @param buf won't be evaluated
     * @param off won't be evaluated
     * @param len won't be evaluated
     */
    @Override
    public void write(byte[] buf, int off, int len) {
    }

    /**
     * Pseudo write()-method.
     *
     * @param b won't be evaluated
     */
    @Override
    public void write(byte[] b) {
    }

    /**
     * Pseudo flush()-method.
     */
    @Override
    public void flush() {
    }

    /**
     * Closes the underlying IndentablePrintStream.
     */
    @Override
    public void close() {
        super.close();
    }
}
