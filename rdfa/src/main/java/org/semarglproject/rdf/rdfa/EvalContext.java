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

package org.semarglproject.rdf.rdfa;

import org.semarglproject.rdf.ParseException;
import org.semarglproject.ri.IRI;
import org.semarglproject.ri.MalformedIRIException;
import org.semarglproject.vocab.RDFa;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

final class EvalContext {

    // Initial context described in http://www.w3.org/2011/rdfa-context/rdfa-1.1.html
    private static final Map<String, String> RDFA11_INITIAL_CONTEXT = new HashMap<String, String>();

    private static final String XHTML_VOCAB = "http://www.w3.org/1999/xhtml/vocab#";
    private static final String POWDER_DESCRIBED_BY = "http://www.w3.org/2007/05/powder-s#describedby";

    private static final String[] XHTML_VOCAB_PROPS = {
            // XHTML Metainformation Vocabulary
            "alternate", "appendix", "bookmark", "cite", "chapter", "contents",
            "copyright", "first", "glossary", "help", "icon", "index", "itsRules",
            "last", "license", "meta", "next", "p3pv1", "prev", "previous", "role",
            "section", "stylesheet", "subsection", "start","top", "up",

            // Items from the XHTML Role Module
            "banner", "complementary", "contentinfo", "definition", "main",
            "navigation", "note", "search",

            // Items from the Accessible Rich Internet Applications Vocabulary
            "alert", "alertdialog", "application", "article", "button", "checkbox",
            "columnheader", "combobox", "dialog", "directory", "document", "form",
            "grid", "gridcell", "group", "heading", "img", "link", "list", "listbox",
            "listitem", "log", "marquee", "math", "menu", "menubar", "menuitem",
            "menuitemcheckbox", "menuitemradio", "option", "presentation",
            "progressbar", "radio", "radiogroup", "region", "row", "rowgroup",
            "rowheader", "scrollbar", "separator", "slider", "spinbutton", "status",
            "tab", "tablist", "tabpanel", "textbox", "timer", "toolbar", "tooltip",
            "tree", "treegrid", "treeitem"
    };

    static {
        // Vocabulary Prefixes of W3C Documents
        RDFA11_INITIAL_CONTEXT.put("owl", "http://www.w3.org/2002/07/owl#");
        RDFA11_INITIAL_CONTEXT.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        RDFA11_INITIAL_CONTEXT.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        RDFA11_INITIAL_CONTEXT.put("rdfa", "http://www.w3.org/ns/rdfa#");
        RDFA11_INITIAL_CONTEXT.put("xhv", "http://www.w3.org/1999/xhtml/vocab#");
        RDFA11_INITIAL_CONTEXT.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        RDFA11_INITIAL_CONTEXT.put("grddl", "http://www.w3.org/2003/g/data-view#");
        RDFA11_INITIAL_CONTEXT.put("ma", "http://www.w3.org/ns/ma-ont#");
        RDFA11_INITIAL_CONTEXT.put("rif", "http://www.w3.org/2007/rif#");
        RDFA11_INITIAL_CONTEXT.put("skos", "http://www.w3.org/2004/02/skos/core#");
        RDFA11_INITIAL_CONTEXT.put("skosxl", "http://www.w3.org/2008/05/skos-xl#");
        RDFA11_INITIAL_CONTEXT.put("wdr", "http://www.w3.org/2007/05/powder#");
        RDFA11_INITIAL_CONTEXT.put("void", "http://rdfs.org/ns/void#");
        RDFA11_INITIAL_CONTEXT.put("wdrs", "http://www.w3.org/2007/05/powder-s#");
        RDFA11_INITIAL_CONTEXT.put("xml", "http://www.w3.org/XML/1998/namespace");

        // Widely used Vocabulary prefixes
        RDFA11_INITIAL_CONTEXT.put("cc", "http://creativecommons.org/ns#");
        RDFA11_INITIAL_CONTEXT.put("ctag", "http://commontag.org/ns#");
        RDFA11_INITIAL_CONTEXT.put("dc", "http://purl.org/dc/terms/");
        RDFA11_INITIAL_CONTEXT.put("dcterms", "http://purl.org/dc/terms/");
        RDFA11_INITIAL_CONTEXT.put("foaf", "http://xmlns.com/foaf/0.1/");
        RDFA11_INITIAL_CONTEXT.put("gr", "http://purl.org/goodrelations/v1#");
        RDFA11_INITIAL_CONTEXT.put("ical", "http://www.w3.org/2002/12/cal/icaltzd#");
        RDFA11_INITIAL_CONTEXT.put("og", "http://ogp.me/ns#");
        RDFA11_INITIAL_CONTEXT.put("rev", "http://purl.org/stuff/rev#");
        RDFA11_INITIAL_CONTEXT.put("sioc", "http://rdfs.org/sioc/ns#");
        RDFA11_INITIAL_CONTEXT.put("v", "http://rdf.data-vocabulary.org/#");
        RDFA11_INITIAL_CONTEXT.put("vcard", "http://www.w3.org/2006/vcard/ns#");
        RDFA11_INITIAL_CONTEXT.put("schema", "http://schema.org/");
    }

    private final DocumentContext documentContext;

    public Map<String, String> iriMappings;
    public String subject;
    public String object;
    public List<Object> incomplTriples;
    public String lang;
    public String objectLit;
    public String objectLitDt;
    public String properties;
    public boolean parsingLiteral;
    public Map<String, List<Object>> listMapping;

    private Vocabulary vocab;
    private String profile;

    public static EvalContext createInitialContext(DocumentContext documentContext) {
        // RDFa Core 1.0 processing sequence step 1
        EvalContext initialContext = new EvalContext(null, null, null, documentContext);
        initialContext.subject = documentContext.base;
        initialContext.listMapping = new HashMap<String, List<Object>>();
        initialContext.iriMappings = new TreeMap<String, String>();
        return initialContext;
    }

    private EvalContext(String lang, Vocabulary vocab, String profile, DocumentContext documentContext) {
        this.subject = null;
        this.object = null;
        this.iriMappings = null;
        this.incomplTriples = new ArrayList<Object>();
        this.lang = lang;
        this.objectLit = null;
        this.objectLitDt = null;
        this.vocab = vocab;
        this.profile = profile;
        this.properties = null;
        this.parsingLiteral = false;
        this.listMapping = null;
        this.documentContext = documentContext;
    }

    public EvalContext initChildContext(String profile, String vocab, String lang,
                                        Map<String, String> overwriteMappings) {
        // RDFa Core 1.0 processing sequence step 2
        EvalContext current = new EvalContext(this.lang, this.vocab, this.profile, documentContext);
        current.listMapping = this.listMapping;
        if (overwriteMappings.isEmpty()) {
            current.iriMappings = this.iriMappings;
        } else {
            current.iriMappings = new TreeMap<String, String>(iriMappings);
            current.iriMappings.putAll(overwriteMappings);
        }

        if (documentContext.rdfaVersion > RDFa.VERSION_10) {
            for (String prefix : overwriteMappings.keySet()) {
                String standardMapping = RDFA11_INITIAL_CONTEXT.get(prefix);
                String newMapping = overwriteMappings.get(prefix);
                if (standardMapping != null && !standardMapping.equals(newMapping)) {
                    documentContext.parser.warning(RDFa.PREFIX_REDEFINITION, "Standard prefix "
                            + prefix + ": redefined to <" + newMapping + '>');
                }
            }
            if (profile != null) {
                String newProfile = profile + "#";
                if (current.profile == null) {
                    current.profile = newProfile;
                } else {
                    current.profile = newProfile + ' ' + current.profile;
                }
            }
            if (vocab != null) {
                if (vocab.length() == 0) {
                    current.vocab = null;
                } else {
                    current.vocab = documentContext.loadVocabulary(vocab);
                }
            }
        }

        // RDFa Core 1.0 processing sequence step 3
        if (lang != null) {
            current.lang = lang;
        }
        if (current.lang != null && current.lang.isEmpty()) {
            current.lang = null;
        }
        return current;
    }

    public List<Object> getMappingForIri(String iri) {
        if (!listMapping.containsKey(iri)) {
            listMapping.put(iri, new ArrayList<Object>());
        }
        return listMapping.get(iri);
    }

    public void addContent(String content) {
        objectLit += content;
    }

    public void updateBase(String oldBase, String base) {
        if (object != null && object.equals(oldBase)) {
            object = base;
        }
        if (subject != null && subject.equals(oldBase)) {
            subject = base;
        }
    }

    /**
     * Resolves @predicate or @datatype according to RDFa Core 1.1 section 5
     *
     * @param value value of attribute
     * @return resource IRI
     * @throws org.xml.sax.SAXException wrapped ParseException in case if IRI can not be resolved
     */
    public String resolvePredOrDatatype(String value) throws SAXException {
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (value == RdfaParser.AUTODETECT_DATE_DATATYPE) {
            return RdfaParser.AUTODETECT_DATE_DATATYPE;
        }
        try {
            if (documentContext.rdfaVersion > RDFa.VERSION_10) {
                return resolveTermOrSafeCURIEOrAbsIri(value);
            } else {
                return resolveTermOrSafeCURIE(value);
            }
        } catch (MalformedIRIException e) {
            throw new SAXException(new ParseException(e));
        }
    }

    /**
     * Resolves @about or @resource according to RDFa Core 1.1 section 5
     *
     * @param value value of attribute
     * @return resource IRI
     * @throws org.semarglproject.rdf.ParseException if IRI can not be resolved
     */
    public String resolveAboutOrResource(String value) throws MalformedIRIException {
        String result = documentContext.resolveBNode(value);
        if (result == null) {
            String iri = resolveCurie(value);
            result = documentContext.resolveIri(iri);
        }
        if (result == null) {
            result = documentContext.resolveIri(value);
        }
        return result;
    }

    /**
     * Resolves TERMorCURIEorAbsIRI according to RDFa Core 1.1 section A
     * @param value value to be resolved
     * @return resource IRI
     * @throws org.semarglproject.ri.MalformedIRIException
     */
    private String resolveTermOrSafeCURIEOrAbsIri(String value)
            throws MalformedIRIException {
        if (IRI.isAbsolute(value)) {
            return value;
        }
        return resolveTermOrSafeCURIE(value);
    }

    /**
     * Resolves TERMorSafeCURIE according to RDFa Core 1.1 section A
     * @param value value to be resolved
     * @return resource IRI
     * @throws org.semarglproject.ri.MalformedIRIException
     */
    private String resolveTermOrSafeCURIE(String value) throws MalformedIRIException {
        if (value.matches("[a-zA-Z0-9_]+")) {
            if (vocab != null) {
                String term = vocab.resolveTerm(value);
                if (term == null) {
                    documentContext.parser.warning(RDFa.UNRESOLVED_TERM, "Can't resolve term " + value);
                }
                return term;
            }
            if (documentContext.rdfaVersion > RDFa.VERSION_10) {
                if ("describedby".equals(value)) {
                    return POWDER_DESCRIBED_BY;
                }
            }
            String term = resolveXhtmlTerm(value);
            if (term == null) {
                documentContext.parser.warning(RDFa.UNRESOLVED_TERM, "Can't resolve term " + value);
            }
            return term;
        }
        String iri = resolveCurie(value);
        return documentContext.resolveIri(iri);
    }

    public String resolveRole(String value) {
        if (IRI.isAbsolute(value)) {
            return value;
        }
        if (value.indexOf(':') == -1) {
            return resolveXhtmlTerm(value);
        }
        try {
            String iri = resolveCurie(value);
            return documentContext.resolveIri(iri);
        } catch (MalformedIRIException e) {
            return null;
        }
    }

    private String resolveCurie(String curie) {
        if (curie == null || curie.isEmpty()) {
            return null;
        }
        boolean safeSyntax = curie.charAt(0) == '[' && curie.charAt(curie.length() - 1) == ']';
        if (safeSyntax) {
            curie = curie.substring(1, curie.length() - 1);
        }

        int delimPos = curie.indexOf(':');
        if (delimPos == -1) {
            documentContext.parser.warning(RDFa.UNRESOLVED_CURIE, "CURIE with no prefix (" + curie + ") found");
            return null;
        }
        String localName = curie.substring(delimPos + 1);
        String prefix = curie.substring(0, delimPos);

        if (prefix.equals("_")) {
            documentContext.parser.warning(RDFa.UNRESOLVED_CURIE, "CURIE with invalid prefix (" + curie + ") found");
            return null;
        }
        // TODO: check for correct prefix
        if (!iriMappings.containsKey(prefix)) {
            if (documentContext.rdfaVersion > RDFa.VERSION_10 && RDFA11_INITIAL_CONTEXT.containsKey(prefix)) {
                String nsUri = RDFA11_INITIAL_CONTEXT.get(prefix);
                iriMappings.put(prefix, nsUri);
                return nsUri + localName;
            }
            if (safeSyntax) {
                documentContext.parser.warning(RDFa.UNRESOLVED_CURIE,
                        "CURIE with unresolvable prefix (" + curie + ") found");
            }
            return null;
        }
        return iriMappings.get(prefix) + localName;
    }

    public Iterable<String> expand(String pred) {
        if (vocab == null) {
            return Collections.EMPTY_LIST;
        }
        return vocab.expand(pred);
    }

    private static String resolveXhtmlTerm(String predicate) {
        for (String link : XHTML_VOCAB_PROPS) {
            if (link.equalsIgnoreCase(predicate)) {
                return XHTML_VOCAB + link;
            }
        }
        return null;
    }

}
