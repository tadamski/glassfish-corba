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

package com.sun.corba.ee.impl.servicecontext;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.servicecontext.ServiceContextBase ;
import com.sun.corba.ee.spi.servicecontext.MaxStreamFormatVersionServiceContext ;

import com.sun.corba.ee.impl.misc.ORBUtility;

public class MaxStreamFormatVersionServiceContextImpl extends ServiceContextBase 
    implements MaxStreamFormatVersionServiceContext
{
    private byte maxStreamFormatVersion;

    // The singleton uses the maximum version indicated by our
    // ValueHandler.
    public static final MaxStreamFormatVersionServiceContext singleton
        = new MaxStreamFormatVersionServiceContextImpl();

    private MaxStreamFormatVersionServiceContextImpl() 
    {
        maxStreamFormatVersion = ORBUtility.getMaxStreamFormatVersion();
    }

    public MaxStreamFormatVersionServiceContextImpl(byte maxStreamFormatVersion) 
    {
        this.maxStreamFormatVersion = maxStreamFormatVersion;
    }

    public MaxStreamFormatVersionServiceContextImpl(InputStream is, GIOPVersion gv) 
    {
        super(is) ;

        maxStreamFormatVersion = is.read_octet();
    }

    public int getId() 
    { 
        return SERVICE_CONTEXT_ID; 
    }

    public void writeData(OutputStream os) 
    {
        os.write_octet(maxStreamFormatVersion);
    }
    
    public byte getMaximumStreamFormatVersion()
    {
        return maxStreamFormatVersion;
    }

    public String toString() 
    {
        return "MaxStreamFormatVersionServiceContextImpl[" 
            + maxStreamFormatVersion + "]";
    }
}
    



