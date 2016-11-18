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

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import io.silverware.microservices.Context;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mockit.Expectations;

/**
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class FirstFoundLocalLookupStrategyTest extends AbstractLookupStrategyTest {
   @BeforeMethod
   public void initStrategy() {
      strategy = new FirstFoundLocalLookupStrategy();
      strategy.initialize(context, META_DATA, emptySet());
   }

   @Test
   public void testGetService() throws Exception {
      Set<Object> expectedResult = new HashSet<>(asList("1", "2", "3"));
      new Expectations(Context.class) {{
         context.lookupLocalMicroservice(META_DATA);
         times = 4;
         result = expectedResult;
      }};

      List<Object> result = new ArrayList<>();
      for (int i = 0; i < 4; i++) {
         result.add(strategy.getService());
      }
      assertThat(result).containsExactly("1", "1", "1", "1");

   }
}