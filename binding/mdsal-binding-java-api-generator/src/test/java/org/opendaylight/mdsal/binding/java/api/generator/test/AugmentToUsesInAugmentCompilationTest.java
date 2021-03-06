/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.Test;

public class AugmentToUsesInAugmentCompilationTest extends BaseCompilationTest {

    @Test
    public void testAugmentToUsesInAugment() throws IOException, URISyntaxException {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("augment-uses-to-augment");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("augment-uses-to-augment");
        generateTestSources("/compilation/augment-uses-to-augment", sourcesOutputDir);

        // Test if all sources are generated from 'module foo'
        File fooParent = new File(sourcesOutputDir, CompilationTestUtils.NS_FOO);
        CompilationTestUtils.assertFilesCount(fooParent, 4);
        assertTrue(new File(fooParent, "IgpLinkAttributes.java").exists());
        assertTrue(new File(fooParent, "Link1.java").exists());
        assertTrue(new File(fooParent, "Link1Builder.java").exists());

        // Test if all sources are generated from 'module bar'
        File barParent = new File(sourcesOutputDir, CompilationTestUtils.NS_BAR);
        CompilationTestUtils.assertFilesCount(barParent, 7);
        assertTrue(new File(barParent, "BarData.java").exists());
        assertTrue(new File(barParent, "NetworkTopology.java").exists());
        assertTrue(new File(barParent, "NetworkTopologyBuilder.java").exists());
        assertTrue(new File(barParent, "Link.java").exists());
        assertTrue(new File(barParent, "LinkAttributes.java").exists());

        File networkParent = new File(barParent, "network");
        CompilationTestUtils.assertFilesCount(networkParent, 1);
        File topologyParent = new File(networkParent, "topology");
        CompilationTestUtils.assertFilesCount(topologyParent, 3);
        assertTrue(new File(topologyParent, "Topology.java").exists());
        assertTrue(new File(topologyParent, "TopologyBuilder.java").exists());
        assertTrue(new File(topologyParent, "TopologyKey.java").exists());

        File linkParent = new File(barParent, "link");
        CompilationTestUtils.assertFilesCount(linkParent, 3);
        assertTrue(new File(linkParent, "Link.java").exists());
        assertTrue(new File(linkParent, "LinkBuilder.java").exists());
        assertTrue(new File(linkParent, "LinkKey.java").exists());

        // Test if all sources are generated from 'module baz'
        File bazParent = new File(sourcesOutputDir, CompilationTestUtils.NS_BAZ);
        CompilationTestUtils.assertFilesCount(bazParent, 4);
        assertTrue(new File(bazParent, "IgpLinkAttributes1.java").exists());
        assertTrue(new File(bazParent, "IgpLinkAttributes1Builder.java").exists());
        assertTrue(new File(bazParent, "LinkAttributes.java").exists());

        // Test if sources are compilable
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);

        ClassLoader loader = new URLClassLoader(new URL[] { compiledOutputDir.toURI().toURL() });

        try {
            Class<?> link1Class = Class.forName(CompilationTestUtils.BASE_PKG + ".urn.opendaylight.foo.rev131008.Link1", true, loader);
            String augmentableNode = CompilationTestUtils.BASE_PKG + ".urn.opendaylight.bar.rev131008.link.Link";
            CompilationTestUtils.testAugmentation(link1Class, augmentableNode);

            Class<?> igpLinkAttributesClass = Class.forName(CompilationTestUtils.BASE_PKG
                    + ".urn.opendaylight.foo.rev131008.IgpLinkAttributes", true, loader);
            CompilationTestUtils.assertImplementsIfc(link1Class, igpLinkAttributesClass);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Class for augment wasn't generated");
        }

        try {
            Class<?> igpLinkAttributes1Class = Class.forName(CompilationTestUtils.BASE_PKG
                    + ".urn.opendaylight.baz.rev131008.IgpLinkAttributes1", true, loader);
            String augmentableNode = CompilationTestUtils.BASE_PKG + ".urn.opendaylight.foo.rev131008.igp.link.attributes.IgpLinkAttributes";
            CompilationTestUtils.testAugmentation(igpLinkAttributes1Class, augmentableNode);

            Class<?> linkAttributesClass = Class.forName(CompilationTestUtils.BASE_PKG + ".urn.opendaylight.baz.rev131008.LinkAttributes",
                    true, loader);
            CompilationTestUtils.assertImplementsIfc(igpLinkAttributes1Class, linkAttributesClass);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Class for augment wasn't generated");
        }

        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

}
