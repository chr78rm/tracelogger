/*
 * Created on 16.07.2003
 * Revised on 17.07.2007
 *
 */
package de.christofreichardt.diagnosis.io;

import java.io.PrintStream;
import java.io.OutputStream;

/**
 * The base class of all indentable print streams.
 *
 * @author Christof Reichardt
 */
public abstract class IndentablePrintStream extends PrintStream implements Indentable {

    /**
     * This constructor passes the given OutputStream to the underlying PrintStream implementation.
     *
     * @param out the desired OutputStream
     */
    public IndentablePrintStream(OutputStream out) {
        super(out);
    }

    /*
     * Implementation of Indentable interface
     */
    @Override
    abstract public IndentablePrintStream printIndent(String s);

    @Override
    abstract public IndentablePrintStream printIndentln(String s);

    @Override
    abstract public IndentablePrintStream printIndentString();

    @Override
    abstract public Indentable printfIndentln(String format, Object... args);
}
