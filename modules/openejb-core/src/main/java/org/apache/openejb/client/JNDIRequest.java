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
package org.apache.openejb.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * 
 * @since 11/25/2001
 */
public class JNDIRequest implements Request {
    
    private transient int requestMethod = -1;
    private transient String clientModuleID;
    private transient String requestString;

    public JNDIRequest() {
    }
    
    public JNDIRequest(int requestMethod, String requestString) {
        this(requestMethod,null,requestString);
    }

    public JNDIRequest(int requestMethod, String clientModuleID, String requestString) {
        assert requestString != null;
        this.requestMethod = requestMethod;
        this.clientModuleID = clientModuleID;
        this.requestString = requestString;
    }

    public byte getRequestType(){
        return JNDI_REQUEST;
    }
    
    public int getRequestMethod(){
        return requestMethod;
    }
    
    public String getRequestString(){
        return requestString;
    }

    public String getClientModuleID() {
        return clientModuleID;
    }

    /**
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     *
     * @param in the stream to read data from in order to restore the object
     * @exception IOException if I/O errors occur
     * @exception ClassNotFoundException If the class for an object being
     *              restored cannot be found.
     */
    public void readExternal(ObjectInput in) throws IOException,ClassNotFoundException {
        requestMethod = in.readByte();
        clientModuleID = (String) in.readObject();
        requestString = in.readUTF();
    }
    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @serialData Overriding methods should use this tag to describe
     *             the data layout of this Externalizable object.
     *             List the sequence of element types and, if possible,
     *             relate the element to a public/protected field and/or
     *             method of this Externalizable class.
     *
     * @param out the stream to write the object to
     * @exception IOException Includes any I/O exceptions that may occur
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeByte((byte)requestMethod);
        out.writeObject(clientModuleID);
        out.writeUTF (requestString);
    }

}
