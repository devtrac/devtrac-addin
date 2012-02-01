package com.fairview5.keepassbb2.common.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLPrinter extends DefaultHandler {

	private OutputStream _out;
	private int indent;

	public XMLPrinter(OutputStream out) {
		_out = out;
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		StringBuffer sb = new StringBuffer(length * 2);
		for (int i = start; i < start + length; i++) {
			switch (ch[i]) {
			case '\'':
				sb.append("&apos;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			default:
				sb.append(ch[i]);
			}
		}
		try {
			_out.write(sb.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startDocument() throws SAXException {
		try {
			_out
					.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
							.getBytes());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void endDocument() throws SAXException {
	}

	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		try {
			_out.write(("<" + localName + ">").getBytes("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		indent++;
	}

	public void endElement(String uri, String localName, String name) {
		indent--;
		try {
			_out.write(("</" + localName + ">").getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void endPrefixMapping(String prefix) throws SAXException {
	}

	public void error(SAXParseException e) throws SAXException {
	}

	public void fatalError(SAXParseException e) throws SAXException {
	}

	public void ignorableWhitespace(char[] ch, int start, int length) {
		try {
			_out.write((new String(ch, start, length).getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void notationDecl(String name, String publicId, String systemId)
			throws SAXException {
	}

	public void processingInstruction(String target, String data)
			throws SAXException {
	}

	public InputSource resolveEntity(String publicId, String systemId)
			throws IOException, SAXException {
		return null;
	}

	public void setDocumentLocator(Locator locator) {
	}

	public void skippedEntity(String name) throws SAXException {
	}

	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
	}

	public void unparsedEntityDecl(String name, String publicId,
			String systemId, String notationName) throws SAXException {
	}

	public void warning(SAXParseException e) throws SAXException {
	}

}
