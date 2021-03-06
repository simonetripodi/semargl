/*
 * Copyright 2012 Lev Khomich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.semarglproject.sesame;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.semarglproject.source.StreamProcessor;
import org.semarglproject.rdf.NTriplesParser;
import org.semarglproject.rdf.NTriplesTestBundle;
import org.semarglproject.rdf.ParseException;
import org.semarglproject.sesame.core.sink.SesameSink;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public final class NTriplesParserTest {

    private StatementCollector model;
    private StreamProcessor sp;

    @BeforeClass
    public void init() {
        NTriplesTestBundle.prepareTestDir();
        model = new StatementCollector();
        sp = new StreamProcessor(NTriplesParser.connect(SesameSink.connect(model)));
    }

    @BeforeMethod
    public void setUp() {
        model.clear();
    }

    @Test(dataProvider = "getTestFiles")
    public void NTriplesTestsSesame(String caseName) throws Exception {
        NTriplesTestBundle.runTest(caseName, new NTriplesTestBundle.SaveToFileCallback() {
            @Override
            public void run(Reader input, String inputUri, Writer output) throws ParseException {
                try {
                    sp.process(input, inputUri);
                } finally {
                    RDFWriter rdfWriter = Rio.createWriter(RDFFormat.TURTLE, output);
                    try {
                        rdfWriter.startRDF();
                        for(Statement nextStatement : model.getStatements()) {
                            rdfWriter.handleStatement(nextStatement);
                        }
                        rdfWriter.endRDF();
                    } catch(RDFHandlerException e) {
                        // do nothing
                    }
                }
            }
        });
    }

    @DataProvider
    public Object[][] getTestFiles() throws IOException {
        return NTriplesTestBundle.getTestFiles();
    }
}
