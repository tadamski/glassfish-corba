package org.omg.CORBA;

/**
* org/omg/CORBA/RepositoryHolder.java .
* IGNORE Generated by the IDL-to-Java compiler (portable), version "3.2"
* from idlj/src/main/java/com/sun/tools/corba/ee/idl/ir.idl
* IGNORE Sunday, January 21, 2018 1:54:22 PM EST
*/

public final class RepositoryHolder implements org.omg.CORBA.portable.Streamable
{
  public org.omg.CORBA.Repository value = null;

  public RepositoryHolder ()
  {
  }

  public RepositoryHolder (org.omg.CORBA.Repository initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.omg.CORBA.RepositoryHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.omg.CORBA.RepositoryHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return org.omg.CORBA.RepositoryHelper.type ();
  }

}