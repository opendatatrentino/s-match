/**
 * *****************************************************************************
 * Copyright 2012-2013 University of Trento - Department of Information
 * Engineering and Computer Science (DISI)
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Lesser General Public License (LGPL)
 * version 2.1 which accompanies this distribution, and is available at
 *
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 *****************************************************************************
 */
package it.unitn.disi.smatch.test;

import it.unitn.disi.smatch.preprocessors.DefaultContextPreprocessor;
import java.util.Arrays;
import java.util.List;
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;

/**
 *
 * @author Moaz Reyad <reyad@disi.unitn.it>
 */
public class TestEnclosingParentheses extends TestCase {
  
    public TestEnclosingParentheses() {
        super("Test Enclosing Parentheses");
    }

    public void testEnclosingParentheses() {

        List<String> vec1 = Arrays.asList("n1.0");
        List<String> vec2 = Arrays.asList("n1.0", "n2.0");
        List<String> vec3 = Arrays.asList("n1.0", "n2.0", "n3.0");
        List<String> vec4 = Arrays.asList("n1.0", "n2.0", "n3.0", "n4.0");
        List<String> vec5 = Arrays.asList("n1.0", "n2.0", "n3.0", "n4.0", "n5.0");

        String expected1 = "[n1.0]";
        String expected2 = "[n1.0, n2.0]";
        String expected3 = "[[n1.0, n2.0], [n3.0]]";
        String expected4 = "[[n1.0, n2.0], [n3.0, n4.0]]";
        String expected5 = "[[[n1.0, n2.0], [n3.0, n4.0]], [n5.0]]";

        String out = DefaultContextPreprocessor.encloseWithParentheses(vec1);
        assertEquals("Parentheses error", expected1, out);

        out = DefaultContextPreprocessor.encloseWithParentheses(vec2);
        assertEquals("Parentheses error", expected2, out);

        out = DefaultContextPreprocessor.encloseWithParentheses(vec3);
        assertEquals("Parentheses error", expected3, out);

        out = DefaultContextPreprocessor.encloseWithParentheses(vec4);
        assertEquals("Parentheses error", expected4, out);

        out = DefaultContextPreprocessor.encloseWithParentheses(vec5);
        assertEquals("Parentheses error", expected5, out);
    }
}
