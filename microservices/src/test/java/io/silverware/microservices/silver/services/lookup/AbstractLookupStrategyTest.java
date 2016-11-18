/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *
 * Copyright (C) 2010 - 2016 the original author or authors.
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
package io.silverware.microservices.silver.services.lookup;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.silver.services.LookupStrategy;

import org.testng.annotations.Test;

import mockit.StrictExpectations;
import mockit.Tested;

/**
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public abstract class AbstractLookupStrategyTest {

   public static final String API = "api";
   public static final String IMPL = "impl";
   public static final MicroserviceMetaData META_DATA = new MicroserviceMetaData(AbstractLookupStrategyTest.class.getName(), AbstractLookupStrategyTest.class, emptySet(), emptySet(), API, IMPL);

   protected Context context = new Context();

   @Tested
   protected LookupStrategy strategy;

   @Test
   public void testGetServiceWithoutResult() throws Exception {
      new StrictExpectations(Context.class) {{
         context.lookupMicroservice(META_DATA);
         minTimes = 0;
         maxTimes = 1;
         result = emptySet();
         context.lookupLocalMicroservice(META_DATA);
         minTimes = 0;
         maxTimes = 1;
         result = emptySet();
      }};
      assertThatThrownBy(() -> strategy.getService()).isInstanceOf(RuntimeException.class).hasMessageContaining(META_DATA.toString());
   }

}