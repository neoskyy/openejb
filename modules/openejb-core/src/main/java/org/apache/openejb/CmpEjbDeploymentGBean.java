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
package org.apache.openejb;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.kernel.Kernel;
import org.apache.openejb.corba.TSSBean;
import org.apache.openejb.entity.cmp.ModuleCmpEngine;

import javax.security.auth.Subject;
import java.util.SortedMap;
import java.util.Map;
import java.util.Set;

/**
 * @version $Revision$ $Date$
 */
public final class CmpEjbDeploymentGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(CmpEjbDeploymentGBean.class, CmpEjbDeployment.class, NameFactory.ENTITY_BEAN);

        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("ejbName", String.class, true);

        infoFactory.addAttribute("homeInterfaceName", String.class, true);
        infoFactory.addAttribute("remoteInterfaceName", String.class, true);
        infoFactory.addAttribute("localHomeInterfaceName", String.class, true);
        infoFactory.addAttribute("localInterfaceName", String.class, true);
        infoFactory.addAttribute("primaryKeyClassName", String.class, true);
        infoFactory.addAttribute("beanClassName", String.class, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addReference("ejbContainer", CmpEjbContainer.class, "CmpEjbContainer");

        infoFactory.addAttribute("jndiNames", String[].class, true);
        infoFactory.addAttribute("localJndiNames", String[].class, true);

        infoFactory.addAttribute("securityEnabled", boolean.class, true);
        infoFactory.addAttribute("policyContextId", String.class, true);
        infoFactory.addAttribute("defaultPrincipal", DefaultPrincipal.class, true);
        infoFactory.addAttribute("runAs", Subject.class, true);

        infoFactory.addAttribute("transactionPolicies", SortedMap.class, true);

        infoFactory.addAttribute("componentContextMap", Map.class, true);

        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.addReference("TSSBean", TSSBean.class);

        infoFactory.addAttribute("unshareableResources", Set.class, true);
        infoFactory.addAttribute("applicationManagedSecurityResources", Set.class, true);

        infoFactory.addReference("moduleCmpEngine", ModuleCmpEngine.class, "moduleCmpEngine");
        infoFactory.addAttribute("cmp2", boolean.class, true);
        infoFactory.addAttribute("reentrant", boolean.class, true);

        infoFactory.setConstructor(new String[]{
                "objectName",
                "ejbName",

                "homeInterfaceName",
                "remoteInterfaceName",
                "localHomeInterfaceName",
                "localInterfaceName",
                "primaryKeyClassName",
                "beanClassName",
                "classLoader",

                "ejbContainer",

                "jndiNames",
                "localJndiNames",

                "securityEnabled",
                "policyContextId",
                "defaultPrincipal",
                "runAs",

                "transactionPolicies",

                "componentContextMap",

                "kernel",

                "TSSBean",

                "unshareableResources",
                "applicationManagedSecurityResources",

                "moduleCmpEngine",
                "cmp2",
                "reentrant",
        });

        infoFactory.addInterface(CmpEjbDeployment.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
