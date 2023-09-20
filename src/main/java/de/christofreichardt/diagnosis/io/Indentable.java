/*
 * Indentable.java
 */
package de.christofreichardt.diagnosis.io;

/**
 * This interface defines the methods {@link IndentablePrintStream}s must implement.
 *
 * @author Christof Reichardt
 */
public interface Indentable {

    /**
     * Prints an indented string.
     *
     * @param s the string to be printed
     * @return the indentable stream itself
     */
    Indentable printIndent(String s);

    /**
     * Prints an indented string together with a line feed.
     *
     * @param s the string to be printed
     * @return the indentable stream itself
     */
    Indentable printIndentln(String s);

    /**
     * A convenience method to print an indented string onto the stream using the specified format string and the given arguments.
     *
     * @param format the format string
     * @param args   the to be printed arguments
     * @return the indentable stream itself
     */
    Indentable printfIndentln(String format, Object... args);

    /**
     * Prints only the current indentation.
     *
     * @return the indentable stream itself
     */
    Indentable printIndentString();
}
