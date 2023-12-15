/*
 * Copyright 2014-2023 Christof Reichardt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
