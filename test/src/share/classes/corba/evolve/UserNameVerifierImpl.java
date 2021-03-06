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
package corba.evolve;

import java.rmi.RemoteException ;
import javax.rmi.PortableRemoteObject;
import java.lang.reflect.Constructor;
import mymath.BigDecimal ;

public class UserNameVerifierImpl 
    extends PortableRemoteObject implements UserNameVerifier
{
    public UserNameVerifierImpl() throws RemoteException
    {
        super();
    }

    public void verifyName(UserNameInt input)
        throws RemoteException
    {
        if (input == null)
            throw new RemoteException("Input to verifyName was null!");

        if (!input.validate())
            throw new RemoteException("Input to verifyName was invalid!");
    }

    public UserNameInt requestName() throws RemoteException
    {
        try {

            Class userNameClass = Class.forName("UserName");

            UserNameInt name = (UserNameInt)userNameClass.newInstance();

            return name;

        } catch (ClassNotFoundException e) {
            throw new RemoteException(e.getMessage());
        } catch (InstantiationException e) {
            throw new RemoteException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    private static final String FEATURE_NAME = "TestFeature" ;
    private static final String FEATURE_DESCRIPTION = "Description of test feature" ;

    public FeatureInfo getFeatureInfo() throws RemoteException {
        try {
            Class fimpl = Class.forName( "FeatureInfoImpl" ) ;
            Constructor cons = fimpl.getConstructor( String.class, String.class ) ;
            return (FeatureInfo)cons.newInstance( FEATURE_NAME, FEATURE_DESCRIPTION ) ;
        } catch (Exception exc) {
            throw new RemoteException( "", exc ) ;
        }
    }

    public boolean validateFeatureInfo( FeatureInfo info ) throws RemoteException {
        return info.getName().equals( FEATURE_NAME ) &&
            info.getDescription().equals( FEATURE_DESCRIPTION ) ;
    }

    /*
    public Object echo( Object obj ) {
        return obj ;
    }
    */

    public BigDecimal echo( BigDecimal obj ) {
        return obj ;
    }

    public WithoutPrimitives echo( WithoutPrimitives obj ) {
        return obj ;
    }
}
