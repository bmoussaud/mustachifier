/*
 * @(#)Predicates2.java     Jul 31, 2011
 *
 * Copyright © 2010 Andrew Phillips.
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package com.xebialabs.deployit.cli.ext.mustachify.base;

import com.google.common.base.Predicate;

/**
 * @author aphillips
 * @since Jul 31, 2011
 *
 */
public class Predicates2 {

    public static IsPredicate is(Object ref) {
        return new IsPredicate(ref);
    }
    
    private static class IsPredicate implements Predicate<Object> {
        private final Object ref;
        
        private IsPredicate(Object ref) {
            this.ref = ref;
        }
        
        @Override
        public boolean apply(Object input) {
            return (input == ref);
        }
    }
}
