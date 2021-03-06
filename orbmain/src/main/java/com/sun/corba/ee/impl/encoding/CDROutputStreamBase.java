/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * 
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 * 
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.corba.ee.impl.encoding;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.Any;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;

/**
 * Describes CDROutputObject delegates and provides some
 * implementation.  Non-default constructors are avoided in
 * the delegation to separate instantiation from initialization,
 * so we use init methods.
 */
abstract class CDROutputStreamBase extends java.io.OutputStream
{
    protected CDROutputObject parent;

    // Required by parent CDROutputObject
    public void setParent(CDROutputObject parent) {
        this.parent = parent;
    }

    // See EncapsOutputStream, the only one that uses the
    // non-pooled ByteBuffers, for additional info.
    protected abstract void init(org.omg.CORBA.ORB orb,
                                 BufferManagerWrite bufferManager,
                                 byte streamFormatVersion,
                                 boolean usePooledByteBuffers);

    public abstract void write_boolean(boolean value);

    public abstract void write_char(char value);
    public abstract void write_wchar(char value);
    public abstract void write_octet(byte value);
    public abstract void write_short(short value);
    public abstract void write_ushort(short value);
    public abstract void write_long(int value);
    public abstract void write_ulong(int value);
    public abstract void write_longlong(long value);
    public abstract void write_ulonglong(long value);
    public abstract void write_float(float value);
    public abstract void write_double(double value);
    public abstract void write_string(String value);
    public abstract void write_wstring(String value);
    public abstract void write_boolean_array(boolean[] value, int offset, int length);
    public abstract void write_char_array(char[] value, int offset, int length);
    public abstract void write_wchar_array(char[] value, int offset, int length);
    public abstract void write_octet_array(byte[] value, int offset, int length);
    public abstract void write_short_array(short[] value, int offset, int length);
    public abstract void write_ushort_array(short[] value, int offset, int length);
    public abstract void write_long_array(int[] value, int offset, int length);
    public abstract void write_ulong_array(int[] value, int offset, int length);
    public abstract void write_longlong_array(long[] value, int offset, int length);
    public abstract void write_ulonglong_array(long[] value, int offset, int length);
    public abstract void write_float_array(float[] value, int offset, int length);
    public abstract void write_double_array(double[] value, int offset, int length);
    public abstract void write_Object(org.omg.CORBA.Object value);
    public abstract void write_TypeCode(TypeCode value);
    public abstract void write_any(Any value);
    @SuppressWarnings({"deprecation"})
    public abstract void write_Principal(org.omg.CORBA.Principal value);
    public void write(int b) throws java.io.IOException {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    public abstract void write_fixed(java.math.BigDecimal value);
    public void write_Context(org.omg.CORBA.Context ctx,
                              org.omg.CORBA.ContextList contexts) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    public abstract org.omg.CORBA.ORB orb();

    // org.omg.CORBA_2_3.portable.OutputStream
    public abstract void write_value(java.io.Serializable value);

    public abstract void write_value(java.io.Serializable value, java.lang.Class clz);
    public abstract void write_value(java.io.Serializable value, String repository_id);
    public abstract void write_value(java.io.Serializable value,
                                     org.omg.CORBA.portable.BoxedValueHelper factory);
    public abstract void write_abstract_interface(java.lang.Object obj);
    // java.io.OutputStream

    // com.sun.corba.ee.impl.encoding.MarshalOutputStream
    public abstract void start_block();

    public abstract void end_block();
    public abstract void putEndian();
    public abstract void writeTo(java.io.OutputStream s)
        throws IOException;
    public abstract byte[] toByteArray();
    protected abstract byte[] toByteArray(int start);

    // org.omg.CORBA.DataOutputStream
    public abstract void write_Abstract (java.lang.Object value);

    public abstract void write_Value (java.io.Serializable value);
    public abstract void write_any_array(org.omg.CORBA.Any[] seq, int offset, int length);
    // org.omg.CORBA.portable.ValueBase
    public abstract String[] _truncatable_ids();

    // Needed by request and reply messages for GIOP versions >= 1.2 only.
    abstract void setHeaderPadding(boolean headerPadding);

    // Required by IIOPOutputStream and other subclasses
    public abstract int getSize();

    public abstract int getIndex();

    public abstract void setIndex(int value);
    //     public abstract void close() throws IOException;
//     public abstract void flush() throws IOException;
//     public abstract void write(byte b[], int off, int len) throws IOException;
//     public abstract void write(byte b[]) throws IOException;

    abstract void dereferenceBuffer();
    public abstract ByteBuffer getByteBuffer();

    public abstract BufferManagerWrite getBufferManager();

    public abstract void write_fixed(java.math.BigDecimal bigDecimal, short digits, short scale);
    public abstract void writeOctetSequenceTo(org.omg.CORBA.portable.OutputStream s);

    public abstract GIOPVersion getGIOPVersion();

    public abstract void writeIndirection(int tag, int posIndirectedTo);

    abstract void freeInternalCaches();

    abstract void alignOnBoundary(int octetBoundary);

    // org.omg.CORBA.portable.ValueOutputStream

    public abstract void start_value(String rep_id);

    public abstract void end_value();
}

