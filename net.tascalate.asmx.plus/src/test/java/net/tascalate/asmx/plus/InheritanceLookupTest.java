/**
 * BSD 3-Clause License
 * 
 * Copyright (c) 2019-2022, Valery Silaev (http://vsilaev.com)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.tascalate.asmx.plus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Closeable;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

public class InheritanceLookupTest {

    ClassHierarchy lookup;
    
    @Before
    public void setup() {
        lookup = new ClassHierarchy(new ClasspathResourceLoader(ClassLoader.getSystemClassLoader()));
    }
   

    @Test
    public void testCache() throws IOException {
        ClassHierarchy.TypeInfo info1 = lookup.getTypeInfo("java/util/LinkedList");
        ClassHierarchy.TypeInfo info2 = lookup.getTypeInfo("java/util/LinkedList");
        assertEquals(info1, info2);
    }

    @Test
    public void testCommonSuperInterface() throws IOException {
        assertEquals("java/util/Collection", lookup.getCommonSuperClass("java/util/List", "java/util/Set"));
    }
    
    @Test
    public void testCommonSuperClass() throws IOException {
        assertEquals("java/util/AbstractCollection", lookup.getCommonSuperClass("java/util/LinkedList", "java/util/HashSet"));
    }
    
    @Test
    public void testCommonSuperClassSymmetrical() throws IOException {
        assertEquals(
            lookup.getCommonSuperClass("java/util/LinkedList", "java/util/List"),
            lookup.getCommonSuperClass("java/util/List", "java/util/LinkedList")            
        );
    }

    @Test
    public void testCommonSuperInterfaceRecursive() throws IOException {
        assertEquals("java/util/Queue", lookup.getCommonSuperClass("net/tascalate/asmx/plus/InheritanceLookupTest$TestList", "java/util/Queue"));
    }
    
    
    @Test
    public void testTypeInfo() throws IOException {
        ClassHierarchy.TypeInfo info1 = lookup.getTypeInfo("java/util/LinkedList");
        assertTrue(info1.isSubclassOf(lookup.getTypeInfo("java/lang/Object")));
        assertTrue(info1.isSubclassOf(lookup.getTypeInfo("java/util/AbstractCollection")));
        assertTrue(info1.isSubclassOf(lookup.getTypeInfo("java/util/Queue")));
        
        ClassHierarchy.TypeInfo info2 = lookup.getTypeInfo("java/util/LinkedHashSet");
        ClassHierarchy.TypeInfo info3 = lookup.getTypeInfo("net/tascalate/asmx/plus/InheritanceLookupTest$TestList");
        ClassHierarchy.TypeInfo info4 = lookup.getTypeInfo("java/util/TreeMap");
        
        System.out.println(info1.flattenHierarchy());
        System.out.println(info2.flattenHierarchy());
        System.out.println(info3.flattenHierarchy());
        System.out.println(info4.flattenHierarchy());
        
        System.out.println(lookup.getCommonSuperClass("java/util/LinkedList", "java/util/LinkedHashSet"));
        System.out.println(lookup.getCommonSuperClass("java/util/List", "java/util/Set"));
    }
    
    static class TestList<T> extends LinkedList<T> implements Externalizable, Closeable {

        @Override
        public void close() {
        }

        @Override
        public void writeExternal(ObjectOutput out) {
        }

        @Override
        public void readExternal(ObjectInput in) {
        }
        
    }

    static class ClasspathResourceLoader implements ResourceLoader {
        private final Reference<ClassLoader> classLoaderRef;

        ClasspathResourceLoader(ClassLoader classLoader) {
            this.classLoaderRef = new WeakReference<ClassLoader>(classLoader);
        }      

        public boolean hasResource(String name) {
            ClassLoader classLoader = classLoaderRef.get();
            return null != classLoader && null != classLoader.getResource(name);
        }
    
        public InputStream getResourceAsStream(String name) throws IOException {
            ClassLoader classLoader = classLoaderRef.get();
            if (null == classLoader) {
                throw new IOException("Underlying class loader was evicted from memory, this resource loader is unusable");
            }
        
            InputStream result = classLoader.getResourceAsStream(name);
            if (null == result) {
                throw new IOException("Unable to find resource " + name);
            }
            return result;
        }  
    }
 
    
}
