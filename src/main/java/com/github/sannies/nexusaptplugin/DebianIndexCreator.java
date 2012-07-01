package com.github.sannies.nexusaptplugin;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0    
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.github.sannies.nexusaptplugin.DEBIAN;
import com.github.sannies.nexusaptplugin.deb.DebControlParser;
import com.github.sannies.nexusaptplugin.deb.GetControl;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.maven.index.*;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.creator.AbstractIndexCreator;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;
import org.apache.maven.index.util.zip.ZipFacade;
import org.apache.maven.index.util.zip.ZipHandle;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.FileUtils;

/**
 * A Maven Archetype index creator used to detect and correct the artifact packaging to "maven-archetype" if the
 * inspected JAR is an Archetype. Since packaging is already handled by Minimal creator, this Creator only alters the
 * supplied ArtifactInfo packaging field during processing, but does not interferes with Lucene document fill-up or the
 * ArtifactInfo fill-up (the update* methods are empty).
 *
 * @author cstamas
 */
@Component(role = IndexCreator.class, hint = DebianIndexCreator.ID)
public class DebianIndexCreator
        extends AbstractIndexCreator {
    public static final String ID = "debian-package";

    public static final IndexerField PACKAGE = new IndexerField(DEBIAN.PACKAGE, IndexerFieldVersion.V1, "deb_package",
            DEBIAN.PACKAGE.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField ARCHITECTURE = new IndexerField(DEBIAN.ARCHITECTURE, IndexerFieldVersion.V1, "deb_architecture",
            DEBIAN.ARCHITECTURE.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField INSTALLED_SIZE = new IndexerField(DEBIAN.INSTALLED_SIZE, IndexerFieldVersion.V1, "deb_installed_size",
            DEBIAN.INSTALLED_SIZE.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField MAINTAINER = new IndexerField(DEBIAN.MAINTAINER, IndexerFieldVersion.V1, "deb_maintainer",
            DEBIAN.MAINTAINER.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField VERSION = new IndexerField(DEBIAN.VERSION, IndexerFieldVersion.V1, "deb_version",
            DEBIAN.VERSION.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField DEPENDS = new IndexerField(DEBIAN.DEPENDS, IndexerFieldVersion.V1, "deb_depends",
            DEBIAN.DEPENDS.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField SECTION = new IndexerField(DEBIAN.SECTION, IndexerFieldVersion.V1, "deb_section",
            DEBIAN.SECTION.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField PRIORITY = new IndexerField(DEBIAN.PRIORITY, IndexerFieldVersion.V1, "deb_priority",
            DEBIAN.PRIORITY.getDescription(), Field.Store.YES, Field.Index.NO);

    public static final IndexerField DESCRIPTION = new IndexerField(DEBIAN.DESCRIPTION, IndexerFieldVersion.V1, "deb_description",
            DEBIAN.DESCRIPTION.getDescription(), Field.Store.YES, Field.Index.NO);

    public DebianIndexCreator() {
        super(ID, Arrays.asList(MinimalArtifactInfoIndexCreator.ID));
    }

    public void populateArtifactInfo(ArtifactContext ac) throws IOException {
        if ("deb".equals(ac.getArtifactInfo().fextension)) {
            List<String> control = GetControl.doGet(ac.getArtifact());
            ac.getArtifactInfo().getAttributes().putAll(DebControlParser.parse(control));
        }

    }


    public void updateDocument(ArtifactInfo ai, Document doc) {
        if ("deb".equals(ai.fextension)) {
            doc.add(PACKAGE.toField(ai.getAttributes().get(PACKAGE.getOntology().getFieldName())));
            doc.add(ARCHITECTURE.toField(ai.getAttributes().get(ARCHITECTURE.getOntology().getFieldName())));
            doc.add(INSTALLED_SIZE.toField(ai.getAttributes().get(INSTALLED_SIZE.getOntology().getFieldName())));
            doc.add(MAINTAINER.toField(ai.getAttributes().get(MAINTAINER.getOntology().getFieldName())));
            doc.add(VERSION.toField(ai.getAttributes().get(VERSION.getOntology().getFieldName())));
            doc.add(DEPENDS.toField(ai.getAttributes().get(DEPENDS.getOntology().getFieldName())));
            doc.add(SECTION.toField(ai.getAttributes().get(SECTION.getOntology().getFieldName())));
            doc.add(PRIORITY.toField(ai.getAttributes().get(PRIORITY.getOntology().getFieldName())));
            doc.add(DESCRIPTION.toField(ai.getAttributes().get(DESCRIPTION.getOntology().getFieldName())));
        }
    }

    public boolean updateArtifactInfo(Document doc, ArtifactInfo ai) {
        if ("deb".equals(ai.fextension)) {
            ai.getAttributes().put(PACKAGE.getOntology().getFieldName(), doc.get(PACKAGE.getKey()));
            ai.getAttributes().put(ARCHITECTURE.getOntology().getFieldName(), doc.get(ARCHITECTURE.getKey()));
            ai.getAttributes().put(INSTALLED_SIZE.getOntology().getFieldName(), doc.get(INSTALLED_SIZE.getKey()));
            ai.getAttributes().put(MAINTAINER.getOntology().getFieldName(), doc.get(MAINTAINER.getKey()));
            ai.getAttributes().put(VERSION.getOntology().getFieldName(), doc.get(VERSION.getKey()));
            ai.getAttributes().put(DEPENDS.getOntology().getFieldName(), doc.get(DEPENDS.getKey()));
            ai.getAttributes().put(SECTION.getOntology().getFieldName(), doc.get(SECTION.getKey()));
            ai.getAttributes().put(PRIORITY.getOntology().getFieldName(), doc.get(PRIORITY.getKey()));
            ai.getAttributes().put(DESCRIPTION.getOntology().getFieldName(), doc.get(DESCRIPTION.getKey()));
            return true;
        }
        return false;
    }

    // ==

    @Override
    public String toString() {
        return ID;
    }

    public Collection<IndexerField> getIndexerFields() {
        // it does not "add" any new field, it actually updates those already maintained by minimal creator.
        return Collections.emptyList();
    }
}
