/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2015 - 2017 the original author or authors.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------------/
 */
package io.silverware.microservices.providers.cdi.util;

import io.silverware.microservices.annotations.MicroserviceVersion;

/**
 * This class contains hierarchies for testing version resolution.
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
class VersionsHierarchy {
   static final String WithBothVersionsOnClassApi = "WithBothVersionsOnClassApi";
   static final String WithBothVersionsOnClassImpl = "WithBothVersionsOnClassImpl";

   static final String WithVersionInterfaceApi = "WithVersionInterfaceApi";
   static final String WithVersionInterfaceImpl = "WithVersionInterfaceImpl";

   static final String WithVersionInterfaceApi2 = "WithVersionInterfaceApi2";
   static final String WithVersionInterfaceImpl2 = "WithVersionInterfaceImpl2";

   static final String WithImplVersionOnClassAndApiVersionOnInterfaceImpl = "WithImplVersionOnClassAndApiVersionOnInterfaceImpl";
   static final String WithImplVersionOnClassAndApiVersionOnParent = "WithImplVersionOnClassAndApiVersionOnParent";

   static final String WithApiVersionOnClassAndImplVersionOnParent = "WithApiVersionOnClassAndImplVersionOnParent";
   static final String WithApiVersionOnClassAndImplVersionOnInterface = "WithApiVersionOnClassAndImplVersionOnInterface";

   // @formatter:off
   interface WithoutVersion1 {  }

   interface WithoutVersion2 {  }

   @MicroserviceVersion(implementation = WithVersionInterfaceImpl, api = WithVersionInterfaceApi)
   interface WithVersionInterface {  }

   @MicroserviceVersion(implementation = WithVersionInterfaceApi2, api = WithVersionInterfaceImpl2)
   interface WithVersionInterface2  {  }


   static class WithVersionsOnInterface implements WithVersionInterface,WithoutVersion1, WithoutVersion2 {   }

   static class WithoutVersions implements WithoutVersion1, WithoutVersion2 {   }

   @MicroserviceVersion(api = WithBothVersionsOnClassApi, implementation = WithBothVersionsOnClassImpl)
   static class WithBothVersionsOnClass implements WithoutVersion2, WithVersionInterface {   }

   @MicroserviceVersion(api = WithApiVersionOnClassAndImplVersionOnParent)
   static class WithApiVersionOnClassAndImplVersionOnParent extends WithBothVersionsOnClass implements WithoutVersion2 {   }

   @MicroserviceVersion(implementation = WithImplVersionOnClassAndApiVersionOnParent)
   static class WithImplVersionOnClassAndApiVersionOnParent extends WithBothVersionsOnClass  implements WithoutVersion2 {   }

   @MicroserviceVersion(api = WithApiVersionOnClassAndImplVersionOnInterface)
   static class WithApiVersionOnClassAndImplVersionOnInterface extends WithBothVersionsOnClass implements WithoutVersion2, WithVersionInterface {   }

   @MicroserviceVersion(implementation = WithImplVersionOnClassAndApiVersionOnInterfaceImpl)
   static class WithImplVersionOnClassAndApiVersionOnInterface extends WithBothVersionsOnClass  implements WithoutVersion2,WithVersionInterface {   }

   static class WithVersionOn2Interfaces implements WithVersionInterface, WithVersionInterface2 {   }
   // @formatter:on
}
