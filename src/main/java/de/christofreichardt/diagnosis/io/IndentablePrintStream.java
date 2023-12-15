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
    abstract public IndentablePrintStream printfIndentln(String format, Object... args);

    /**
     * Acquires a lock to prevent concurrent access to the {@code IndentablePrintStream}. This is useful if multiple threads are
     * writing to the {@code IndentablePrintStream}.
     */
    abstract public void lock();

    /**
     * Unlocks the lock.
     */
    abstract public void unlock();

    /**
     * The given {@code Runnable} will be executed when the lock could be acquired.
     * @param runnable should contain print statements
     */
    abstract public void runWithLock(Runnable runnable);
}
