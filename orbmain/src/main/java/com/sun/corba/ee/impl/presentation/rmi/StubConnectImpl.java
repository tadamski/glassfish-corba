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

package com.sun.corba.ee.impl.presentation.rmi ;

import java.rmi.RemoteException;

import javax.rmi.CORBA.Tie;

import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.BAD_INV_ORDER;

import org.omg.CORBA.portable.ObjectImpl;
import org.omg.CORBA.portable.Delegate;

import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;

import com.sun.corba.ee.impl.util.Utility;

import com.sun.corba.ee.impl.ior.StubIORImpl ;

import com.sun.corba.ee.spi.logging.UtilSystemException ;

import com.sun.corba.ee.impl.corba.CORBAObjectImpl ;

public abstract class StubConnectImpl 
{
    private static UtilSystemException wrapper = 
        UtilSystemException.self ;

    /** Connect the stub to the orb if necessary.  
    * @param ior The StubIORImpl for this stub (may be null)
    * @param proxy The externally visible stub seen by the user (may be the same as stub)
    * @param stub The stub implementation that extends ObjectImpl
    * @param orb The ORB to which we connect the stub.
    */
    public static StubIORImpl connect( StubIORImpl ior, org.omg.CORBA.Object proxy, 
        org.omg.CORBA.portable.ObjectImpl stub, ORB orb ) throws RemoteException 
    {
        Delegate del = null ;

        try {
            try {
                del = StubAdapter.getDelegate( stub );
                
                if (del.orb(stub) != orb) 
                    throw wrapper.connectWrongOrb() ;
            } catch (org.omg.CORBA.BAD_OPERATION err) {    
                if (ior == null) {
                    // No IOR, can we get a Tie for this stub?
                    Tie tie = (javax.rmi.CORBA.Tie) Utility.getAndForgetTie(proxy);
                    if (tie == null) 
                        throw wrapper.connectNoTie() ;

                    // Is the tie already connected?  If it is, check that it's 
                    // connected to the same ORB, otherwise connect it.
                    ORB existingOrb = orb ;
                    try {
                        existingOrb = tie.orb();
                    } catch (BAD_OPERATION exc) { 
                        // Thrown when tie is an ObjectImpl and its delegate is not set.
                        tie.orb(orb);
                    } catch (BAD_INV_ORDER exc) { 
                        // Thrown when tie is a Servant and its delegate is not set.
                        tie.orb(orb);
                    }

                    if (existingOrb != orb) 
                        throw wrapper.connectTieWrongOrb() ;
                        
                    // Get the delegate for the stub from the tie.
                    del = StubAdapter.getDelegate( tie ) ;
                    ObjectImpl objref = new CORBAObjectImpl() ;
                    objref._set_delegate( del ) ;
                    ior = new StubIORImpl( objref ) ;
                } else {
                    // ior is initialized, so convert ior to an object, extract
                    // the delegate, and set it on ourself
                    del = ior.getDelegate( orb ) ;
                }

                StubAdapter.setDelegate( stub, del ) ;
            }
        } catch (SystemException exc) {
            throw new RemoteException("CORBA SystemException", exc );
        }

        return ior ;
    }
}
