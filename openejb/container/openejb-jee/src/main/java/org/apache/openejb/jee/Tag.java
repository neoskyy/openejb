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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * web-jsptaglibrary_2_1.xsd
 *
 * <p>Java class for tagType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="tagType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="name" type="{http://java.sun.com/xml/ns/javaee}tld-canonical-nameType"/>
 *         &lt;element name="tag-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/>
 *         &lt;element name="tei-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *         &lt;element name="body-content" type="{http://java.sun.com/xml/ns/javaee}body-contentType"/>
 *         &lt;element name="variable" type="{http://java.sun.com/xml/ns/javaee}variableType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="attribute" type="{http://java.sun.com/xml/ns/javaee}tld-attributeType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="dynamic-attributes" type="{http://java.sun.com/xml/ns/javaee}generic-booleanType" minOccurs="0"/>
 *         &lt;element name="example" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/>
 *         &lt;element name="tag-extension" type="{http://java.sun.com/xml/ns/javaee}tld-extensionType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tagType", propOrder = {
    "descriptions",
    "displayNames",
    "icon",
    "name",
    "tagClass",
    "teiClass",
    "bodyContent",
    "variable",
    "attribute",
    "dynamicAttributes",
    "example",
    "tagExtension"
})
public class Tag {
    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();

    @XmlElement(required = true)
    protected String name;
    @XmlElement(name = "tag-class", required = true)
    protected String tagClass;
    @XmlElement(name = "tei-class")
    protected String teiClass;
    @XmlElement(name = "body-content", required = true)
    protected BodyContent bodyContent;
    protected List<Variable> variable;
    protected List<TldAttribute> attribute;
    @XmlElement(name = "dynamic-attributes")
    protected String dynamicAttributes;
    protected String example;
    @XmlElement(name = "tag-extension")
    protected List<TldExtension> tagExtension;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    @XmlElement(name = "display-name", required = true)
    public Text[] getDisplayNames() {
        return displayName.toArray();
    }

    public void setDisplayNames(Text[] text) {
        displayName.set(text);
    }

    public String getDisplayName() {
        return displayName.get();
    }

    public Collection<Icon> getIcons() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon;
    }

    public Map<String, Icon> getIconMap() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon.toMap();
    }

    public Icon getIcon() {
        return icon.getLocal();
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getTagClass() {
        return tagClass;
    }

    public void setTagClass(String value) {
        this.tagClass = value;
    }

    public String getTeiClass() {
        return teiClass;
    }

    public void setTeiClass(String value) {
        this.teiClass = value;
    }

    public BodyContent getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(BodyContent value) {
        this.bodyContent = value;
    }

    public List<Variable> getVariable() {
        if (variable == null) {
            variable = new ArrayList<Variable>();
        }
        return this.variable;
    }

    public List<TldAttribute> getAttribute() {
        if (attribute == null) {
            attribute = new ArrayList<TldAttribute>();
        }
        return this.attribute;
    }

    public String getDynamicAttributes() {
        return dynamicAttributes;
    }

    public void setDynamicAttributes(String value) {
        this.dynamicAttributes = value;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String value) {
        this.example = value;
    }

    public List<TldExtension> getTagExtension() {
        if (tagExtension == null) {
            tagExtension = new ArrayList<TldExtension>();
        }
        return this.tagExtension;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
