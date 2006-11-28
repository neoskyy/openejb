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
package org.apache.openejb.assembler.classic;

import java.util.ArrayList;
import java.util.List;

public class ContainerSystemInfo extends InfoObject {

    public final List<ContainerInfo> containers = new ArrayList<ContainerInfo>();

    public final List<AppInfo> applications = new ArrayList<AppInfo>();
    public final List<ClientInfo> clients = new ArrayList<ClientInfo>();
    public final List<EjbJarInfo> ejbJars = new ArrayList<EjbJarInfo>();

    public final List<EntityContainerInfo> entityContainers = new ArrayList<EntityContainerInfo>();
    public final List<StatelessSessionContainerInfo> statelessContainers = new ArrayList<StatelessSessionContainerInfo>();
    public final List<StatefulSessionContainerInfo> statefulContainers = new ArrayList<StatefulSessionContainerInfo>();
    public final List<SecurityRoleInfo> securityRoles = new ArrayList<SecurityRoleInfo>();
    public final List<MethodPermissionInfo> methodPermissions= new ArrayList<MethodPermissionInfo>();
    public final List<MethodTransactionInfo> methodTransactions = new ArrayList<MethodTransactionInfo>();
}
