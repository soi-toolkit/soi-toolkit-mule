/* 
 * Licensed to the soi-toolkit project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The soi-toolkit project licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.soitoolkit.commons.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XPathUtil {

	private static final Logger log = LoggerFactory.getLogger(XPathUtil.class);

	private static class MapNamespaceContext implements NamespaceContext {
        private Map<String, String> namespaceMap;

        public MapNamespaceContext(Map<String, String> namespaceMap) {
            this.namespaceMap = namespaceMap;
        }

        public String getNamespaceURI(String prefix) {
            return namespaceMap.get(prefix);
        }

        public String getPrefix(String namespaceURI) {
            String prefix = null;
        	for (Map.Entry<String, String> entry : namespaceMap.entrySet()) {
                if (entry.getValue().equals(namespaceURI)) {
                    prefix = entry.getKey();
                    break;
                }
            }
            return prefix;
        }

        public Iterator<String> getPrefixes(String namespaceURI) {
            return null;
        }

    }

    /**
	 * Hidden constructor.
	 */
	private XPathUtil() {
		throw new UnsupportedOperationException("Not allowed to create an instance of this class");
	}

	static public Document createDocument(InputStream content) {
		try {
			return getBuilder().parse(content);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static public Document createDocument(String content) {
		return createDocument(content, "UTF-8");
	}

	static public Document createDocument(String content, String charset) {
		try {
			InputStream is = new ByteArrayInputStream(content.getBytes(charset));
			return getBuilder().parse(is);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static public DocumentBuilder getBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		return builder;
	}

	static public String getDocumentComment(Document doc) {
		String docComment = null;
		try {
			NodeList childs = doc.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++) {
				Node child = childs.item(i);
				if (child.getNodeType() == Node.COMMENT_NODE) {
					docComment = child.getNodeValue();
				}
			}
			return docComment;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static public NodeList getXPathResult(Node node, String namespacePrefix, String namespaceURI, String expression) {
		Map<String, String> namespaceMap = new HashMap<String, String>();
		namespaceMap.put(namespacePrefix, namespaceURI);
		return getXPathResult(node, namespaceMap, expression);
	}

	static public NodeList getXPathResult(Node node, Map<String, String> namespaceMap, String expression) {
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			if (namespaceMap != null) {
			    xpath.setNamespaceContext(new MapNamespaceContext(namespaceMap));
			}

			XPathExpression expr = xpath.compile(expression);

			Object result = expr.evaluate(node, XPathConstants.NODESET);
			return (NodeList) result;

		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	static public String getFirstValue(NodeList nodes) {
		return (nodes.getLength() == 0 ? null : getValues(nodes).get(0));
	}

	static public List<String> getValues(NodeList nodes) {
		List<String> values = new ArrayList<String>();
		for (int i = 0; i < nodes.getLength(); i++) {
			values.add(nodes.item(i).getNodeValue());
		}
		return values;
	}

	/**
	  * @param parent
	  *          node to add fragment to
	  * @param fragment
	  *          a well formed XML fragment
 	  * @throws ParserConfigurationException 
	  */
	public static void appendXmlFragment(Node parent, String fragment) throws IOException, SAXException, ParserConfigurationException {

		DocumentBuilder docBuilder = getBuilder();
		Document doc = parent.getOwnerDocument();

	    Node fragmentNode = docBuilder.parse(new InputSource(new StringReader(fragment))).getDocumentElement();

	    fragmentNode = doc.importNode(fragmentNode, true);
	    
	    parent.appendChild(fragmentNode);
	}

    public static String getXml(Node node) {
    	return getXml(node, false, 0);
	}


    /**
     * Creates a string representation of the dom node.
     * NOTE: The string can be formatted and indented with a specified indent size, but be aware that this is depending on a Xalan implementation of the XSLT library.
     * 
     * @param node
     * @param indentXml
     * @param indentSize
     * @return
     */
    public static String getXml(Node node, boolean indentXml, int indentSize) {
    	try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer;
			
			if (indentXml) {
				transformer = tf.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", new Integer(indentSize).toString());
			} else {
				transformer = tf.newTransformer(new StreamSource(XPathUtil.class.getResourceAsStream("remove-whitespace.xsl")));
			}
			
			//initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(node);
			transformer.transform(source, result);

			String xmlString = result.getWriter().toString();
			return xmlString;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }


    public static XMLGregorianCalendar convertDateToXmlDate(Date date) {
		try {
			GregorianCalendar fromDate = new GregorianCalendar();
			fromDate.setTime(date);
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(fromDate);
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
    
    /**
     * Search for patterns [key=value] and returns the value given the key.
     * 
     * @param key
     * @param string
     * @return
     */
	public static String lookupParameterValue(String key, String string) {
		Pattern p = Pattern.compile("\\[" + key + "=[^\\]]*\\]");
		Matcher m = p.matcher(string);
		m.find();
    	String f = m.group();
    	int p1 = f.indexOf('=');
    	int p2 = f.indexOf(']');
    	return f.substring(p1+1, p2);
	}
	
	public static String normalizeXmlString(String xml) {

		log.debug("XML from start:\n{}", xml);
		// First convert the xml string to a single long line
		Document doc = createDocument(xml);
		doc.setXmlStandalone(true);
		xml = getXml(doc, false, 0);

		log.debug("XML after removing whitespace:\n{}", xml);

		// Next indent the xml string
		doc = createDocument(xml);
		doc.setXmlStandalone(true);
		xml = getXml(doc, true, 2);
		
		log.debug("XML fully normalized:\n{}", xml);
		
		return xml;
	}
}