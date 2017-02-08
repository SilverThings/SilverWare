/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2016 the original author or authors.
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
package io.silverware.microservices.monitoring;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Tomas Borcin | tborcin@redhat.com | created: 20.12.16.
 */
public class MetricsParentTest {

   protected BigDecimal getRandomTime() {
      return getRandomTime(1, 1000);
   }

   protected BigDecimal getRandomTime(int min, int max) {
      return new BigDecimal(ThreadLocalRandom.current().nextInt(min, max + 1));
   }
}
