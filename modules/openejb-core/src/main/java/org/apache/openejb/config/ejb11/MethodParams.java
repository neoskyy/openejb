/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.config.ejb11;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class MethodParams.
 *
 * @version $Revision$ $Date$
 */
public class MethodParams implements java.io.Serializable {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _id
     */
    private java.lang.String _id;

    /**
     * Field _methodParamList
     */
    private java.util.Vector _methodParamList;


    //----------------/
    //- Constructors -/
    //----------------/

    public MethodParams() {
        super();
        _methodParamList = new Vector();
    } //-- org.apache.openejb.config.ejb11.MethodParams()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Method addMethodParam
     *
     * @param vMethodParam
     */
    public void addMethodParam(java.lang.String vMethodParam)
            throws java.lang.IndexOutOfBoundsException {
        _methodParamList.addElement(vMethodParam);
    } //-- void addMethodParam(java.lang.String) 

    /**
     * Method addMethodParam
     *
     * @param index
     * @param vMethodParam
     */
    public void addMethodParam(int index, java.lang.String vMethodParam)
            throws java.lang.IndexOutOfBoundsException {
        _methodParamList.insertElementAt(vMethodParam, index);
    } //-- void addMethodParam(int, java.lang.String) 

    /**
     * Method enumerateMethodParam
     */
    public java.util.Enumeration enumerateMethodParam() {
        return _methodParamList.elements();
    } //-- java.util.Enumeration enumerateMethodParam() 

    /**
     * Returns the value of field 'id'.
     *
     * @return the value of field 'id'.
     */
    public java.lang.String getId() {
        return this._id;
    } //-- java.lang.String getId() 

    /**
     * Method getMethodParam
     *
     * @param index
     */
    public java.lang.String getMethodParam(int index)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _methodParamList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (String) _methodParamList.elementAt(index);
    } //-- java.lang.String getMethodParam(int) 

    /**
     * Method getMethodParam
     */
    public java.lang.String[] getMethodParam() {
        int size = _methodParamList.size();
        java.lang.String[] mArray = new java.lang.String[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (String) _methodParamList.elementAt(index);
        }
        return mArray;
    } //-- java.lang.String[] getMethodParam() 

    /**
     * Method getMethodParamCount
     */
    public int getMethodParamCount() {
        return _methodParamList.size();
    } //-- int getMethodParamCount() 

    /**
     * Method isValid
     */
    public boolean isValid() {
        try {
            validate();
        } catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * Method marshal
     *
     * @param out
     */
    public void marshal(java.io.Writer out)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {

        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * Method marshal
     *
     * @param handler
     */
    public void marshal(org.xml.sax.ContentHandler handler)
            throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {

        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Method removeAllMethodParam
     */
    public void removeAllMethodParam() {
        _methodParamList.removeAllElements();
    } //-- void removeAllMethodParam() 

    /**
     * Method removeMethodParam
     *
     * @param index
     */
    public java.lang.String removeMethodParam(int index) {
        java.lang.Object obj = _methodParamList.elementAt(index);
        _methodParamList.removeElementAt(index);
        return (String) obj;
    } //-- java.lang.String removeMethodParam(int) 

    /**
     * Sets the value of field 'id'.
     *
     * @param id the value of field 'id'.
     */
    public void setId(java.lang.String id) {
        this._id = id;
    } //-- void setId(java.lang.String) 

    /**
     * Method setMethodParam
     *
     * @param index
     * @param vMethodParam
     */
    public void setMethodParam(int index, java.lang.String vMethodParam)
            throws java.lang.IndexOutOfBoundsException {
        //-- check bounds for index
        if ((index < 0) || (index > _methodParamList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _methodParamList.setElementAt(vMethodParam, index);
    } //-- void setMethodParam(int, java.lang.String) 

    /**
     * Method setMethodParam
     *
     * @param methodParamArray
     */
    public void setMethodParam(java.lang.String[] methodParamArray) {
        //-- copy array
        _methodParamList.removeAllElements();
        for (int i = 0; i < methodParamArray.length; i++) {
            _methodParamList.addElement(methodParamArray[i]);
        }
    } //-- void setMethodParam(java.lang.String) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.ejb11.MethodParams) Unmarshaller.unmarshal(org.apache.openejb.config.ejb11.MethodParams.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     */
    public void validate()
            throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
