/*
 * TracePrintStream.java
 */
package de.christofreichardt.diagnosis.io;

import de.christofreichardt.diagnosis.AbstractThreadMap;
import de.christofreichardt.diagnosis.ThreadLocalMap;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * This is the main implementation of an indentable PrintStream. This stream uses a thread map to retrieve the current stack size
 * and computes the extend of the indentation accordingly.
 *
 * @author Christof Reichardt
 */
public class TracePrintStream extends IndentablePrintStream {

    /** indicates the maximum number of indentations */
    public final static int MAX_INDENT_NUMBER = 100;
    /** number of spaces that should used per indentation */
    public final static int INDENT_CHAR_NUMBER = 2;
    /** string array that contains the indent strings */
    final protected static String[] INDENT_STRING;

    static {
        INDENT_STRING = new String[MAX_INDENT_NUMBER];
        for (int i = 0; i < MAX_INDENT_NUMBER; i++) {
            char[] spaces = new char[i * INDENT_CHAR_NUMBER];
            Arrays.fill(spaces, ' ');
            INDENT_STRING[i] = new String(spaces);
        }
    }

    /** provides access to the tracing contexts indexed by thread objects */
    final protected AbstractThreadMap threadMap;

    /**
     * Creates a new instance by passing a {@link NullOutputStream} to the base class.
     */
    public TracePrintStream() {
        super(new NullOutputStream());
        this.threadMap = new ThreadLocalMap();
    }

    /**
     * Creates a new instance of TracePrintStream by passing the given OutputStream to the underlying PrintStream.
     * The threadMap will be needed to determine the current stack size and hence the indentation level.
     *
     * @param out       the underlying OutputStream
     * @param threadMap to compute the indentation level
     */
    public TracePrintStream(OutputStream out, AbstractThreadMap threadMap) {
        super(out);
        this.threadMap = threadMap;
    }

    @Override
    public IndentablePrintStream printIndent(String s) {
        printIndentString();
        print(s);

        return this;
    }

    @Override
    public IndentablePrintStream printIndentln(String s) {
        printIndentString();
        println(s);

        return this;
    }

    @Override
    public IndentablePrintStream printIndentString() {
        int level = this.threadMap.getCurrentStackSize();

        if (level < 0) {
            System.err.println("ERROR: Trace stream unlocked but no stack!"); // TODO: rethink this
        } else if (level >= 0 && level < MAX_INDENT_NUMBER) {
            print(INDENT_STRING[level]);
        } else {
            print(INDENT_STRING[MAX_INDENT_NUMBER - 1]);
        }

        return this;
    }

    @Override
    public Indentable printfIndentln(String format, Object... args) {
        printIndentString();
        printf(format, args);
        println();

        return this;
    }

}
