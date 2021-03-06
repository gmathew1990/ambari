/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.controller.internal;

import java.util.*;

import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.RequestStatusResponse;
import org.apache.ambari.server.controller.ExtensionRequest;
import org.apache.ambari.server.controller.ExtensionResponse;
import org.apache.ambari.server.controller.spi.*;
import org.apache.ambari.server.controller.spi.Resource.Type;
import org.apache.ambari.server.controller.utilities.PropertyHelper;

/**
 * An extension version is like a stack version but it contains custom services.  Linking an extension
 * version to the current stack version allows the cluster to install the custom services contained in
 * the extension version.
 */
public class ExtensionResourceProvider extends ReadOnlyResourceProvider {

  public static final String EXTENSION_NAME_PROPERTY_ID = PropertyHelper
      .getPropertyId("Extensions", "extension_name");

  private static Set<String> pkPropertyIds = new HashSet<String>(
      Arrays.asList(new String[] { EXTENSION_NAME_PROPERTY_ID }));

  protected ExtensionResourceProvider(Set<String> propertyIds,
      Map<Type, String> keyPropertyIds,
      AmbariManagementController managementController) {
    super(propertyIds, keyPropertyIds, managementController);
  }


  @Override
  public Set<Resource> getResources(Request request, Predicate predicate)
      throws SystemException, UnsupportedPropertyException,
      NoSuchResourceException, NoSuchParentResourceException {

    final Set<ExtensionRequest> requests = new HashSet<ExtensionRequest>();

    if (predicate == null) {
      requests.add(getRequest(Collections.<String, Object>emptyMap()));
    } else {
      for (Map<String, Object> propertyMap : getPropertyMaps(predicate)) {
        requests.add(getRequest(propertyMap));
      }
    }

    Set<String> requestedIds = getRequestPropertyIds(request, predicate);

    Set<ExtensionResponse> responses = getResources(new Command<Set<ExtensionResponse>>() {
      @Override
      public Set<ExtensionResponse> invoke() throws AmbariException {
        return getManagementController().getExtensions(requests);
      }
    });

    Set<Resource> resources = new HashSet<Resource>();

    for (ExtensionResponse response : responses) {
      Resource resource = new ResourceImpl(Resource.Type.Extension);

      setResourceProperty(resource, EXTENSION_NAME_PROPERTY_ID,
          response.getExtensionName(), requestedIds);

      resource.setProperty(EXTENSION_NAME_PROPERTY_ID, response.getExtensionName());

      resources.add(resource);
    }

    return resources;
  }

  @Override
  public RequestStatus updateResources(Request request, Predicate predicate)
    throws SystemException, UnsupportedPropertyException,
    NoSuchResourceException, NoSuchParentResourceException {

    RequestStatusResponse response = modifyResources(
      new Command<RequestStatusResponse>() {

      @Override
      public RequestStatusResponse invoke() throws AmbariException {
        //return getManagementController().updateExtensions();
	    //TODO - do we need a separate method
        return getManagementController().updateStacks();
      }
    });

    notifyUpdate(Type.Extension, request, predicate);

    return getRequestStatus(response);
  }

  private ExtensionRequest getRequest(Map<String, Object> properties) {
    return new ExtensionRequest((String) properties.get(EXTENSION_NAME_PROPERTY_ID));
  }

  @Override
  protected Set<String> getPKPropertyIds() {
    return pkPropertyIds;
  }
}
