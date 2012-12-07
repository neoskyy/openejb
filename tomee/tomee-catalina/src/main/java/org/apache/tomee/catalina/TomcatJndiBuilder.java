/**
 *
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
package org.apache.tomee.catalina;

import org.apache.catalina.core.NamingContextListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.deploy.ContextEjb;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceEnvRef;
import org.apache.catalina.deploy.ContextTransaction;
import org.apache.catalina.deploy.NamingResources;
import org.apache.naming.ContextAccessController;
import org.apache.naming.ContextBindings;
import org.apache.naming.factory.Constants;
import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.EjbLocalReferenceInfo;
import org.apache.openejb.assembler.classic.EjbReferenceInfo;
import org.apache.openejb.assembler.classic.EnvEntryInfo;
import org.apache.openejb.assembler.classic.InjectableInfo;
import org.apache.openejb.assembler.classic.PersistenceContextReferenceInfo;
import org.apache.openejb.assembler.classic.PersistenceUnitReferenceInfo;
import org.apache.openejb.assembler.classic.PortRefInfo;
import org.apache.openejb.assembler.classic.ResourceEnvReferenceInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.ResourceReferenceInfo;
import org.apache.openejb.assembler.classic.ServiceReferenceInfo;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.assembler.classic.WsBuilder;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.core.ivm.naming.SystemComponentReference;
import org.apache.openejb.core.webservices.HandlerChainData;
import org.apache.openejb.core.webservices.PortRefData;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.JtaEntityManager;
import org.apache.openejb.persistence.JtaEntityManagerRegistry;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Contexts;
import org.apache.tomee.common.EjbFactory;
import org.apache.tomee.common.EnumFactory;
import org.apache.tomee.common.LookupFactory;
import org.apache.tomee.common.NamingUtil;
import org.apache.tomee.common.PersistenceContextFactory;
import org.apache.tomee.common.PersistenceUnitFactory;
import org.apache.tomee.common.ResourceFactory;
import org.apache.tomee.common.SystemComponentFactory;
import org.apache.tomee.common.UserTransactionFactory;
import org.apache.tomee.common.WsFactory;
import org.omg.CORBA.ORB;

import javax.ejb.spi.HandleDelegate;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.tomee.common.EnumFactory.ENUM_VALUE;
import static org.apache.tomee.common.NamingUtil.COMPONENT_TYPE;
import static org.apache.tomee.common.NamingUtil.DEPLOYMENT_ID;
import static org.apache.tomee.common.NamingUtil.EXTENDED;
import static org.apache.tomee.common.NamingUtil.EXTERNAL;
import static org.apache.tomee.common.NamingUtil.JNDI_NAME;
import static org.apache.tomee.common.NamingUtil.JNDI_PROVIDER_ID;
import static org.apache.tomee.common.NamingUtil.LOCAL;
import static org.apache.tomee.common.NamingUtil.LOCALBEAN;
import static org.apache.tomee.common.NamingUtil.NAME;
import static org.apache.tomee.common.NamingUtil.RESOURCE_ID;
import static org.apache.tomee.common.NamingUtil.UNIT;
import static org.apache.tomee.common.NamingUtil.WSDL_URL;
import static org.apache.tomee.common.NamingUtil.WS_CLASS;
import static org.apache.tomee.common.NamingUtil.WS_ID;
import static org.apache.tomee.common.NamingUtil.WS_PORT_QNAME;
import static org.apache.tomee.common.NamingUtil.WS_QNAME;
import static org.apache.tomee.common.NamingUtil.setStaticValue;

public class TomcatJndiBuilder {
    private final StandardContext standardContext;
    private final WebAppInfo webAppInfo;
    private final Collection<Injection> injections;
    private final boolean replaceEntry;
    private boolean useCrossClassLoaderRef = true;
    private NamingContextListener namingContextListener;

    public TomcatJndiBuilder(StandardContext standardContext, WebAppInfo webAppInfo, Collection<Injection> injections) {
        this.injections = injections;
        this.standardContext = standardContext;
        this.namingContextListener = BackportUtil.getNamingContextListener(standardContext);
        this.webAppInfo = webAppInfo;

        String parameter = standardContext.findParameter("openejb.start.late");
        replaceEntry = Boolean.parseBoolean(parameter);
    }

    public boolean isUseCrossClassLoaderRef() {
        return useCrossClassLoaderRef;
    }

    public void setUseCrossClassLoaderRef(boolean useCrossClassLoaderRef) {
        this.useCrossClassLoaderRef = useCrossClassLoaderRef;
    }

    public void mergeJndi() throws OpenEJBException {

        NamingResources naming = standardContext.getNamingResources();

        URI moduleUri;
        try {
            moduleUri = new URI(webAppInfo.moduleId);
        } catch (URISyntaxException e) {
            throw new OpenEJBException(e);
        }

        for (EnvEntryInfo ref : webAppInfo.jndiEnc.envEntries) {
            mergeRef(naming, ref);
        }
        for (EjbReferenceInfo ref : webAppInfo.jndiEnc.ejbReferences) {
            mergeRef(naming, ref);
        }
        for (EjbLocalReferenceInfo ref : webAppInfo.jndiEnc.ejbLocalReferences) {
            mergeRef(naming, ref);
        }
        for (PersistenceContextReferenceInfo ref : webAppInfo.jndiEnc.persistenceContextRefs) {
            mergeRef(naming, ref, moduleUri);
        }
        for (PersistenceUnitReferenceInfo ref : webAppInfo.jndiEnc.persistenceUnitRefs) {
            mergeRef(naming, ref, moduleUri);
        }
        for (ResourceReferenceInfo ref : webAppInfo.jndiEnc.resourceRefs) {
            mergeRef(naming, ref);
        }
        for (ResourceEnvReferenceInfo ref : webAppInfo.jndiEnc.resourceEnvRefs) {
            mergeRef(naming, ref);
        }
        for (ServiceReferenceInfo ref : webAppInfo.jndiEnc.serviceRefs) {
            mergeRef(naming, ref);
        }

        ContextTransaction contextTransaction = new ContextTransaction();
        contextTransaction.setProperty(Constants.FACTORY, UserTransactionFactory.class.getName());
        naming.setTransaction(contextTransaction);
    }

    public static void mergeJava(StandardContext standardContext) {
        ContainerSystem cs = SystemInstance.get().getComponent(org.apache.openejb.spi.ContainerSystem.class);
        ContextAccessController.setWritable(standardContext.getNamingContextListener().getName(), standardContext);
        Context root = null;
        try {
            root = (Context) ContextBindings.getClassLoader().lookup("");
        } catch (NamingException ignored) { // shouldn't occur
            // no-op
        }

        // classical deployment - needed because can be overriden through META-INF/context.xml
        String path = standardContext.findParameter(TomcatWebAppBuilder.OPENEJB_WEBAPP_MODULE_ID);
        if (path == null) { // standardContext not created by OpenEJB
            path = standardContext.getHostname();
            if (standardContext.getPath().startsWith("/")) {
                path += standardContext.getPath();
            } else {
                path += "/" + standardContext.getPath();
            }
        }

        WebContext webContext = cs.getWebContext(path);
        if (webContext == null) { // tomee-embedded deployment
            webContext = cs.getWebContext(standardContext.getPath().replaceFirst("/", ""));
            if (webContext == null) {
                webContext = cs.getWebContext(standardContext.getPath());
            }
        }

        final TomcatWebAppBuilder builder = (TomcatWebAppBuilder) SystemInstance.get().getComponent(WebAppBuilder.class);
        TomcatWebAppBuilder.ContextInfo contextInfo = null;
        if (builder != null) {
            contextInfo = builder.getContextInfo(standardContext);
            if (webContext == null && contextInfo.appInfo != null) { // can happen if deployed from apps/
                for (WebAppInfo webAppInfo : contextInfo.appInfo.webApps) {
                    if (webAppInfo.path != null && webAppInfo.path.equals(standardContext.getDocBase())) {
                        webContext = cs.getWebContext(webAppInfo.moduleId);
                        if (webContext != null) {
                            break;
                        }
                    }
                }
            }
        }
        Collection<String> ignoreNames = null;
        if (contextInfo != null) {
            ignoreNames = contextInfo.resourceNames;
        }

        if (webContext != null && webContext.getBindings() != null && root != null) {
            for (Map.Entry<String, Object> entry : webContext.getBindings().entrySet()) {
                try {
                    final String key = entry.getKey();
                    if (key.startsWith("global/")) { // will be done later
                        continue;
                    }

                    Object value = normalize(entry.getValue());
                    if (ignoreNames.contains(removeCompEnv(key))) { // keep tomcat resources
                        try {
                            // tomcat can get the reference but the bound value
                            // can come from OpenEJB (ejb-jar.xml for instance)
                            // so check the lookup can be resolved before skipping it
                            root.lookup(key);
                            continue;
                        } catch (NameNotFoundException nnfe) {
                            // no-op: let it be rebound or bound
                        }
                    }

                    Contexts.createSubcontexts(root, key);
                    root.rebind(key, value);
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
        }

        // merge global: we bind our global to be able to get a real global context and not a local one (bindigns)
        if (root != null) {
            try {
                root.bind("global", SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext().lookup("global"));
            } catch (NamingException e) {
                // bind only global bindings
                if (webContext != null && webContext.getBindings() != null) {
                    for (Map.Entry<String, Object> entry : webContext.getBindings().entrySet()) {
                        try {
                            final String key = entry.getKey();
                            if (!key.startsWith("global/")) {
                                continue;
                            }

                            final Object value = normalize(entry.getValue());
                            Contexts.createSubcontexts(root, key);
                            root.rebind(key, value);
                        } catch (NamingException ignored) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        // try to force some binding which probably failed earlier (see in TomcatWebappBuilder)
        try {
            Context comp = (Context) ContextBindings.getClassLoader().lookup("comp");
            TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
            comp.rebind("TransactionManager", transactionManager);

            // bind TransactionSynchronizationRegistry
            TransactionSynchronizationRegistry synchronizationRegistry = SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class);
            comp.rebind("TransactionSynchronizationRegistry", synchronizationRegistry);

            comp.rebind("ORB", new SystemComponentReference(ORB.class));
            comp.rebind("HandleDelegate", new SystemComponentReference(HandleDelegate.class));

            if (webContext != null) {
                comp.rebind("BeanManager", webContext.getAppContext().getBeanManager());
            } else { // possible?
                comp.rebind("BeanManager", cs.getAppContext(
                        ((TomcatWebAppBuilder) SystemInstance.get().getComponent(WebAppBuilder.class))
                            .getContextInfo(standardContext).appInfo.appId).getBeanManager());
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
            // no-op
        }

        // merge comp/env in app if available (some users are doing it, JBoss habit?)
        try {
            final Context app = (Context) ContextBindings.getClassLoader().lookup("app");
            final Context ctx = (Context) ContextBindings.getClassLoader().lookup("comp/env");
            final List<Binding> bindings = Collections.list(ctx.listBindings("app"));
            for (Binding binding : bindings) {
                try {
                    app.bind(binding.getName(), binding.getObject());
                } catch (NamingException ne) { // we don't want to rebind
                    // no-op
                }
            }
        } catch (Exception ne) {
            // no-op
        }

        ContextAccessController.setReadOnly(standardContext.getNamingContextListener().getName());
    }

    private static String removeCompEnv(final String key) {
        if (key.startsWith("comp/env/")) {
            return key.substring("comp/env/".length());
        }
        return key;
    }

    /**
     * LinkRef addresses need to be prefixed with java: or they won't resolve
     *
     * OpenEJB is fine with this, but Tomcat needs them
     *
     * @param value
     * @return
     */
    private static Object normalize(final Object value) {
        try {

            if (!(value instanceof LinkRef)) return value;

            final LinkRef ref = (LinkRef) value;

            final RefAddr refAddr = ref.getAll().nextElement();

            final String address = refAddr.getContent().toString();

            if (address.startsWith("openejb:")) return value;

            if (!address.startsWith("java:")) {
                return new LinkRef("java:" + address);
            }

        } catch (Exception e) {
            // no-op
        }

        return value;
    }

    public void mergeRef(NamingResources naming, EnvEntryInfo ref) {
//        if (!ref.referenceName.startsWith("comp/")) return;
        if ("java.lang.Class".equals(ref.type)) {
            ContextResourceEnvRef resourceEnv = new ContextResourceEnvRef();
            resourceEnv.setName(ref.referenceName.replaceAll("^comp/env/", ""));
            resourceEnv.setProperty(Constants.FACTORY, ResourceFactory.class.getName());
            resourceEnv.setType(ref.type);
            resourceEnv.setProperty(RESOURCE_ID, ref.value);
            resourceEnv.setOverride(false);
            naming.addResourceEnvRef(resourceEnv);

            return;
        }


        try {
            final ClassLoader loader = this.standardContext.getLoader().getClassLoader();
            final Class<?> type = loader.loadClass(ref.type);
            if (Enum.class.isAssignableFrom(type)) {

                final ContextResourceEnvRef enumRef = new ContextResourceEnvRef();
                enumRef.setName(ref.referenceName.replaceAll("^comp/env/", ""));
                enumRef.setProperty(Constants.FACTORY, EnumFactory.class.getName());
                enumRef.setProperty(ENUM_VALUE, ref.value);
                enumRef.setType(ref.type);
                enumRef.setOverride(false);
                naming.addResourceEnvRef(enumRef);

                return;
            }
        } catch (Throwable e) {
            // no-op
        }

        if (isLookupRef(naming, ref)) return;

        ContextEnvironment environment = naming.findEnvironment(ref.referenceName.replaceAll("^comp/env/", ""));
        boolean addEntry = false;
        if (environment == null) {
            environment = new ContextEnvironment();
            environment.setName(ref.referenceName.replaceAll("^comp/env/", ""));
            addEntry = true;
        }

        environment.setType(ref.type);
        environment.setValue(ref.value);
        environment.setOverride(false);

        if (addEntry) {
            naming.addEnvironment(environment);
        }

        if (replaceEntry) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) namingContextListener.removeEnvironment(environment.getName());
            namingContextListener.addEnvironment(environment);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }
    }

    private boolean isLookupRef(NamingResources naming, InjectableInfo ref) {
        if (ref.location == null) return false;
        if (ref.location.jndiName == null) return false;
        if (!ref.location.jndiName.startsWith("java:")) return false;

        final ContextResourceEnvRef lookup = new ContextResourceEnvRef();

        lookup.setName(ref.referenceName.replaceAll("^comp/env/", ""));
        lookup.setProperty(Constants.FACTORY, LookupFactory.class.getName());
        lookup.setProperty(JNDI_NAME, ref.location.jndiName);
        lookup.setType(Object.class.getName());
        lookup.setOverride(false);

        naming.addResourceEnvRef(lookup);

        return true;
    }

    public void mergeRef(NamingResources naming, EjbReferenceInfo ref) {
        if (isLookupRef(naming, ref)) return;

        ContextEjb ejb = naming.findEjb(ref.referenceName.replaceAll("^comp/env/", ""));
        boolean addEntry = false;
        if (ejb == null) {
            ejb = new ContextEjb();
            ejb.setName(ref.referenceName.replaceAll("^comp/env/", ""));
            addEntry = true;
        }

        ejb.setProperty(Constants.FACTORY, EjbFactory.class.getName());
        ejb.setProperty(NAME, ref.referenceName.replaceAll("^comp/env/", ""));
        ejb.setHome(ref.homeClassName);
        ejb.setRemote(ref.interfaceClassName);
        ejb.setLink(null);
        ejb.setType(ref.interfaceClassName);
        if (useCrossClassLoaderRef) {
            ejb.setProperty(EXTERNAL, "" + ref.externalReference);
        }

        if (ref.ejbDeploymentId != null) {
            ejb.setProperty(DEPLOYMENT_ID, ref.ejbDeploymentId);
        }

        if (ref.location != null) {
            ejb.setProperty(JNDI_NAME, ref.location.jndiName);
            ejb.setProperty(JNDI_PROVIDER_ID, ref.location.jndiProviderId);
        }

        if (addEntry) {
            naming.addEjb(ejb);
        }

        if (replaceEntry) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) namingContextListener.removeEjb(ejb.getName());
            namingContextListener.addEjb(ejb);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }
    }

    public void mergeRef(NamingResources naming, EjbLocalReferenceInfo ref) {
        if (isLookupRef(naming, ref)) return;

        // NamingContextListener.addLocalEjb is empty so we'll just use an ejb ref
        ContextEjb ejb = naming.findEjb(ref.referenceName.replaceAll("^comp/env/", ""));
        boolean addEntry = false;
        if (ejb == null) {
            ejb = new ContextEjb();
            ejb.setName(ref.referenceName.replaceAll("^comp/env/", ""));
            addEntry = true;
        }

        ejb.setProperty(Constants.FACTORY, EjbFactory.class.getName());
        ejb.setProperty(NAME, ref.referenceName.replaceAll("^comp/env/", ""));
        ejb.setHome(ref.homeClassName);
        ejb.setRemote(null);
        ejb.setProperty(ref.localbean ? LOCALBEAN : LOCAL, ref.interfaceClassName);
        ejb.setLink(null);
        ejb.setType(ref.interfaceClassName);

        if (ref.ejbDeploymentId != null) {
            ejb.setProperty(DEPLOYMENT_ID, ref.ejbDeploymentId);
        }

        if (ref.location != null) {
            ejb.setProperty(JNDI_NAME, ref.location.jndiName);
            ejb.setProperty(JNDI_PROVIDER_ID, ref.location.jndiProviderId);
        }

        if (addEntry) {
            naming.addEjb(ejb);
        }

        if (replaceEntry) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) namingContextListener.removeEjb(ejb.getName());
            namingContextListener.addEjb(ejb);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void mergeRef(NamingResources naming, PersistenceContextReferenceInfo ref, URI moduleUri) {
        if (isLookupRef(naming, ref)) return;

        ContextResource resource = naming.findResource(ref.referenceName.replaceAll("^comp/env/", ""));
        boolean addEntry = false;
        if (resource == null) {
            resource = new ContextResource();
            resource.setName(ref.referenceName.replaceAll("^comp/env/", ""));
            addEntry = true;
        }

        resource.setProperty(Constants.FACTORY, PersistenceContextFactory.class.getName());
        resource.setProperty(NAME, ref.referenceName.replaceAll("^comp/env/", ""));
        resource.setType(EntityManager.class.getName());

        if (ref.persistenceUnitName != null) {
            resource.setProperty(UNIT, ref.persistenceUnitName);
        }
        resource.setProperty(EXTENDED, "" + ref.extended);
        if (ref.properties != null) {
            // resource.setProperty(NamingConstants.PROPERTIES, ref.properties);
        }

        if (ref.location != null) {
            resource.setProperty(JNDI_NAME, ref.location.jndiName);
            resource.setProperty(JNDI_PROVIDER_ID, ref.location.jndiProviderId);
        } else {
            Context context = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();
            EntityManagerFactory factory;
            try {
                factory = (EntityManagerFactory) context.lookup("openejb/PersistenceUnit/" + ref.unitId);
            } catch (NamingException e) {
                throw new IllegalStateException("PersistenceUnit '" + ref.unitId + "' not found for EXTENDED ref '" + ref.referenceName.replaceAll("^comp/env/", "") + "'");
            }

            JtaEntityManagerRegistry jtaEntityManagerRegistry = SystemInstance.get().getComponent(JtaEntityManagerRegistry.class);
            setResource(resource, new JtaEntityManager(ref.persistenceUnitName, jtaEntityManagerRegistry, factory, ref.properties, ref.extended));
        }

        if (addEntry) {
            naming.addResource(resource);
        }

        if (replaceEntry) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) namingContextListener.removeResource(resource.getName());
            namingContextListener.addResource(resource);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void mergeRef(NamingResources naming, PersistenceUnitReferenceInfo ref, URI moduleUri) {
        if (isLookupRef(naming, ref)) return;

        ContextResource resource = naming.findResource(ref.referenceName.replaceAll("^comp/env/", ""));
        boolean addEntry = false;
        if (resource == null) {
            resource = new ContextResource();
            resource.setName(ref.referenceName.replaceAll("^comp/env/", ""));
            addEntry = true;
        }

        resource.setProperty(Constants.FACTORY, PersistenceUnitFactory.class.getName());
        resource.setProperty(NAME, ref.referenceName.replaceAll("^comp/env/", ""));
        resource.setType(EntityManagerFactory.class.getName());

        if (ref.persistenceUnitName != null) {
            resource.setProperty(UNIT, ref.persistenceUnitName);
        }

        if (ref.location != null) {
            resource.setProperty(JNDI_NAME, ref.location.jndiName);
            resource.setProperty(JNDI_PROVIDER_ID, ref.location.jndiProviderId);
        } else {
            // TODO: This will not work if webapps don't use AutoConfi
            Context context = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();
            EntityManagerFactory factory;
            try {
                factory = (EntityManagerFactory) context.lookup("openejb/PersistenceUnit/" + ref.unitId);
            } catch (NamingException e) {
                throw new IllegalStateException("PersistenceUnit '" + ref.unitId + "' not found for EXTENDED ref '" + ref.referenceName.replaceAll("^comp/env/", "") + "'");
            }
            setResource(resource, factory);
        }

        if (addEntry) {
            naming.addResource(resource);
        }

        if (replaceEntry) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) namingContextListener.removeResource(resource.getName());
            namingContextListener.addResource(resource);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }
    }

    public void mergeRef(NamingResources naming, ResourceReferenceInfo ref) {
        if (isLookupRef(naming, ref)) return;

        ContextResource resource = naming.findResource(ref.referenceName.replaceAll("^comp/env/", ""));
        boolean addEntry = false;
        if (resource == null) {
            resource = new ContextResource();
            resource.setName(ref.referenceName.replaceAll("^comp/env/", ""));
            addEntry = true;
        }

        resource.setProperty(Constants.FACTORY, ResourceFactory.class.getName());
        resource.setProperty(NAME, ref.referenceName.replaceAll("^comp/env/", ""));
        resource.setType(ref.referenceType);
        resource.setAuth(ref.referenceAuth);

        if (ref.resourceID != null) {
            resource.setProperty(RESOURCE_ID, ref.resourceID);
        }

        if (ref.properties != null) {
            // resource.setProperty(NamingConstants.PROPERTIES, ref.properties);
        }

        if (ref.location != null) {
            resource.setProperty(JNDI_NAME, ref.location.jndiName);
            resource.setProperty(JNDI_PROVIDER_ID, ref.location.jndiProviderId);
        }

        if (addEntry) {
            naming.addResource(resource);
        }

        if (replaceEntry) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) namingContextListener.removeResource(resource.getName());
            namingContextListener.addResource(resource);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }
    }

    public void mergeRef(NamingResources naming, ResourceEnvReferenceInfo ref) {
        if (isLookupRef(naming, ref)) return;

        ContextResourceEnvRef resourceEnv = naming.findResourceEnvRef(ref.referenceName.replaceAll("^comp/env/", ""));
        boolean addEntry = false;
        if (resourceEnv == null) {
            resourceEnv = new ContextResourceEnvRef();
            resourceEnv.setName(ref.referenceName.replaceAll("^comp/env/", ""));
            addEntry = true;
        }

        resourceEnv.setType(ref.resourceEnvRefType);
        if (UserTransaction.class.getName().equals(ref.resourceEnvRefType)) {
            resourceEnv.setProperty(Constants.FACTORY, UserTransactionFactory.class.getName());
        } else if (TransactionManager.class.getName().equals(ref.resourceEnvRefType)) {
            resourceEnv.setProperty(Constants.FACTORY, SystemComponentFactory.class.getName());
            resourceEnv.setProperty(COMPONENT_TYPE, TransactionManager.class.getName());
        } else if (TransactionSynchronizationRegistry.class.getName().equals(ref.resourceEnvRefType)) {
            resourceEnv.setProperty(Constants.FACTORY, SystemComponentFactory.class.getName());
            resourceEnv.setProperty(COMPONENT_TYPE, TransactionSynchronizationRegistry.class.getName());
        } else if (ORB.class.getName().equals(ref.resourceEnvRefType)) {
            resourceEnv.setProperty(Constants.FACTORY, SystemComponentFactory.class.getName());
            resourceEnv.setProperty(COMPONENT_TYPE, ORB.class.getName());
        } else if (HandleDelegate.class.getName().equals(ref.resourceEnvRefType)) {
            resourceEnv.setProperty(Constants.FACTORY, SystemComponentFactory.class.getName());
            resourceEnv.setProperty(COMPONENT_TYPE, HandleDelegate.class.getName());
        } else {
            resourceEnv.setProperty(Constants.FACTORY, ResourceFactory.class.getName());
            resourceEnv.setProperty(NAME, ref.referenceName.replaceAll("^comp/env/", ""));

            if (ref.resourceID != null) {
                resourceEnv.setProperty(RESOURCE_ID, ref.resourceID);
            }

            if (ref.location != null) {
                resourceEnv.setProperty(JNDI_NAME, ref.location.jndiName);
                resourceEnv.setProperty(JNDI_PROVIDER_ID, ref.location.jndiProviderId);
            }
        }

        if (addEntry) {
            naming.addResourceEnvRef(resourceEnv);
        }

        if (replaceEntry) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) namingContextListener.removeResourceEnvRef(resourceEnv.getName());
            namingContextListener.addResourceEnvRef(resourceEnv);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }
    }

    public void mergeRef(NamingResources naming, ServiceReferenceInfo ref) {
        if (isLookupRef(naming, ref)) return;

        ContextResource resource = naming.findResource(ref.referenceName.replaceAll("^comp/env/", ""));
        boolean addEntry = false;
        if (resource == null) {
            resource = new ContextResource();
            resource.setName(ref.referenceName.replaceAll("^comp/env/", ""));
            addEntry = true;
        }

        resource.setProperty(Constants.FACTORY, WsFactory.class.getName());
        resource.setProperty(NAME, ref.referenceName.replaceAll("^comp/env/", ""));
        if (ref.referenceType != null) {
            resource.setType(ref.referenceType);
        } else {
            resource.setType(ref.serviceType);
        }

        if (ref.location != null) {
            resource.setProperty(JNDI_NAME, ref.location.jndiName);
            resource.setProperty(JNDI_PROVIDER_ID, ref.location.jndiProviderId);
        } else {
            // ID
            if (ref.id != null) {
                resource.setProperty(WS_ID, ref.id);
            }
            // Service QName
            if (ref.serviceQName != null) {
                resource.setProperty(WS_QNAME, ref.serviceQName.toString());
            }
            // Service Class
            resource.setProperty(WS_CLASS, ref.serviceType);

            // Port QName
            if (ref.portQName != null) {
                resource.setProperty(WS_PORT_QNAME, ref.portQName.toString());
            }

            // add the wsdl url
            URL wsdlURL = getWsdlUrl(ref);
            if (wsdlURL != null) {
                resource.setProperty(WSDL_URL, wsdlURL.toString());
            }

            // add port refs
            if (!ref.portRefs.isEmpty()) {
                List<PortRefData> portRefs = new ArrayList<PortRefData>(ref.portRefs.size());
                for (PortRefInfo portRefInfo : ref.portRefs) {
                    PortRefData portRef = new PortRefData();
                    portRef.setQName(portRefInfo.qname);
                    portRef.setServiceEndpointInterface(portRefInfo.serviceEndpointInterface);
                    portRef.setEnableMtom(portRefInfo.enableMtom);
                    portRef.getProperties().putAll(portRefInfo.properties);
                    portRefs.add(portRef);
                }
                setResource(resource, "port-refs", portRefs);
            }

            // add the handle chains
            if (!ref.handlerChains.isEmpty()) {
                try {
                    List<HandlerChainData> handlerChains = WsBuilder.toHandlerChainData(ref.handlerChains, standardContext.getLoader().getClassLoader());
                    setResource(resource, "handler-chains", handlerChains);
                    setResource(resource, "injections", injections);
                } catch (OpenEJBException e) {
                    throw new IllegalArgumentException("Error creating handler chain for web service-ref " + ref.referenceName.replaceAll("^comp/env/", ""));
                }
            }
        }

        // if there was a service entry, remove it
        String serviceName = BackportUtil.findServiceName(naming, ref.referenceName.replaceAll("^comp/env/", ""));
        if (serviceName != null) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) BackportUtil.removeService(namingContextListener, serviceName);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }

        // add the new resource entry
        if (addEntry) {
            naming.addResource(resource);
        }

        // or replace the exisitng resource entry
        if (replaceEntry) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) namingContextListener.removeResource(resource.getName());
            namingContextListener.addResource(resource);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }
    }

    private URL getWsdlUrl(ServiceReferenceInfo ref) {
        if (ref.wsdlFile == null) return null;

        URL wsdlUrl = null;
        try {
            wsdlUrl = new URL(ref.wsdlFile);
        } catch (MalformedURLException e) {
            // no-op
        }

        if (wsdlUrl == null) {
            wsdlUrl = standardContext.getLoader().getClassLoader().getResource(ref.wsdlFile);
        }

        if (wsdlUrl == null) {
            try {
                wsdlUrl = standardContext.getServletContext().getResource("/" + ref.wsdlFile);
            } catch (MalformedURLException e) {
                // no-op
            }
        }

        if (wsdlUrl == null) {
            throw new IllegalArgumentException("WSDL file " + ref.wsdlFile + " for web service-ref " + ref.referenceName.replaceAll("^comp/env/", "") + " not found");
        }

        return wsdlUrl;
    }

    private void setResource(ContextResource resource, String name, Object object) {
        setStaticValue(new Resource(resource), name, object);
    }

    private void setResource(ContextResource resource, Object object) {
        setStaticValue(new Resource(resource), object);
    }

    public static void importOpenEJBResourcesInTomcat(final Collection<ResourceInfo> resources, final StandardServer server) {
        final NamingResources naming = server.getGlobalNamingResources();

        for (ResourceInfo info : resources) {
            final String name = info.id;
            if (name == null || naming.findResource(name) != null) {
                continue;
            }

            final ContextResource resource = new ContextResource();
            resource.setName(name);
            resource.setProperty(Constants.FACTORY, ResourceFactory.class.getName());
            resource.setProperty(NAME, name);
            resource.setType(info.className);
            resource.setAuth("Container");

            naming.addResource(resource);
        }
    }

    private static class Resource implements NamingUtil.Resource {
        private final ContextResource contextResource;

        public Resource(ContextResource contextResource) {
            this.contextResource = contextResource;
        }

        public void setProperty(String name, Object value) {
            contextResource.setProperty(name, value);
        }
    }
}
