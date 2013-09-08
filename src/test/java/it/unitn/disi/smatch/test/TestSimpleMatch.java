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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unitn.disi.smatch.test;

import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchException;
import java.io.FileNotFoundException;
import java.io.IOException;
import junit.framework.TestCase;

/**
 * @author Moaz Reyad <reyad@disi.unitn.it>
 * @date Jul 11, 2013
 */
public class TestSimpleMatch extends TestCase {

    public TestSimpleMatch() {
        super("Test S-Match");
    }

    public void testSMatch() throws SMatchException, FileNotFoundException, 
            ConfigurableException, IOException, ClassNotFoundException {
               
        String[] convertC = {"convert", "test-data/cw/c.txt",
            "test-data/cw/c.xml", "-config=src/main/resources/conf/s-match-Tab2XML.properties"};

        String[] convertW = {"convert", "test-data/cw/w.txt",
            "test-data/cw/w.xml", "-config=src/main/resources/conf/s-match-Tab2XML.properties"};

        String[] offlineC = {"offline", "test-data/cw/c.xml",
            "test-data/cw/c.xml"};

        String[] offlineW = {"offline", "test-data/cw/w.xml",
            "test-data/cw/w.xml"};
        
        String[] online = {"online", "test-data/cw/c.xml",
            "test-data/cw/w.xml","test-data/cw/result-cw.txt"};

        MatchManager.main(convertC);
        MatchManager.main(convertW);

        MatchManager.main(offlineC);
        MatchManager.main(offlineW);
        
        MatchManager.main(online);
    }
}
