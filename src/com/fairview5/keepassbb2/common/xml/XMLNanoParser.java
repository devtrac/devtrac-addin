/*
KeePass for BlackBerry
Copyright 2007 Fairview 5 Engineering, LLC <george.joseph@fairview5.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    
    Adapted from NanoXML
    
This software is provided 'as-is', without any express or implied
warranty. In no event will the authors be held liable for any damages
arising from the use of this software.

Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not
claim that you wrote the original software. If you use this software
in a product, an acknowledgment in the product documentation would be
appreciated but is not required.

2. Altered source versions must be plainly marked as such, and must not be
misrepresented as being the original software.

3. This notice may not be removed or altered from any source
distribution.
    
 */

package com.fairview5.keepassbb2.common.xml;

import java.io.*;
import java.util.Hashtable;

import com.fairview5.keepassbb2.common.io.IOUtils;

public class XMLNanoParser {

	private String name;
	private boolean ignoreWhitespace;
	private InputStreamReader reader;
	private int parserLineNr;
	private Hashtable entities = new Hashtable();
	private XMLNanoHandler handler;
	char[] buffer = new char[4096];
	int currentIx = 0;
	int bytesLeft = 0;
	int bufferLength = 0;

	public void parse(InputStream is, XMLNanoHandler handler)
			throws IOException, XMLNanoParseException {
		InputStreamReader isr = new InputStreamReader(is, "UTF-8");
		try {
			parse(isr, handler);
		} finally {
			IOUtils.closeReader(isr);
		}
	}

	public void parse(InputStreamReader reader, XMLNanoHandler handler)
			throws IOException, XMLNanoParseException {
		this.name = null;
		this.reader = reader;
		this.parserLineNr = 1;
		this.handler = handler;

		this.entities.put("amp", new char[] { '&' });
		this.entities.put("quot", new char[] { '"' });
		this.entities.put("apos", new char[] { '\'' });
		this.entities.put("lt", new char[] { '<' });
		this.entities.put("gt", new char[] { '>' });

		int c = readChar();
		if (c != 0xfeff) {
			unreadChar();
		}

		handler.startDocument();

		for (;;) {
			char ch = this.scanWhitespace();

			if (ch != '<') {
				throw this.expectedInput("'<' not '" + ch + "'");
			}

			ch = this.readChar();

			if ((ch == '!') || (ch == '?')) {
				this.skipSpecialTag(0);
			} else {
				this.unreadChar();
				this.scanElement();
				handler.endDocument();
				return;
			}
		}
	}

	protected void scanElement() throws IOException {
		StringBuffer buf = new StringBuffer();
		Hashtable attr = null;
		this.scanIdentifier(buf);
		String name = buf.toString();
		this.name = name;
		char ch = this.scanWhitespace();
		while ((ch != '>') && (ch != '/')) {
			buf.setLength(0);
			this.unreadChar();
			this.scanIdentifier(buf);
			String key = buf.toString();
			ch = this.scanWhitespace();
			if (ch != '=') {
				throw this.expectedInput("=");
			}
			this.scanWhitespace();
			this.unreadChar();
			buf.setLength(0);
			this.scanString(buf);
			if (attr == null)
				attr = new Hashtable();
			attr.put(key, buf.toString());
			ch = this.scanWhitespace();
		}
		handler.startElement(name, attr);

		if (ch == '/') {
			ch = this.readChar();
			if (ch != '>') {
				throw this.expectedInput(">");
			}
			handler.endElement(name);
			return;
		}
		buf.setLength(0);
		ch = this.scanWhitespace(buf);
		if (ch != '<') {
			this.unreadChar();
			this.scanPCData(buf);
		} else {
			for (;;) {
				ch = this.readChar();
				if (ch == '!') {
					if (this.checkCDATA(buf)) {
						this.scanPCData(buf);
						break;
					} else {
						ch = this.scanWhitespace(buf);
						if (ch != '<') {
							this.unreadChar();
							this.scanPCData(buf);
							break;
						}
					}
				} else {
					if ((ch != '/') || this.ignoreWhitespace) {
						buf.setLength(0);
					}
					if (ch == '/') {
						this.unreadChar();
					}
					break;
				}
			}
		}
		if (buf.length() == 0) {
			while (ch != '/') {
				if (ch == '!') {
					ch = this.readChar();
					if (ch != '-') {
						throw this.expectedInput("Comment or Element");
					}
					ch = this.readChar();
					if (ch != '-') {
						throw this.expectedInput("Comment or Element");
					}
					this.skipComment();
				} else {
					this.unreadChar();
					this.scanElement();
					this.name = name;
				}
				ch = this.scanWhitespace();
				if (ch != '<') {
					throw this.expectedInput("<");
				}
				ch = this.readChar();
			}
			this.unreadChar();
		} else {
			if (this.ignoreWhitespace) {
				handler.characters(buf.toString().toCharArray(), 0, buf.length());
			} else {
				handler.characters(buf.toString().toCharArray(), 0, buf.length());
			}
		}
		ch = this.readChar();
		if (ch != '/') {
			throw this.expectedInput("/");
		}
		this.scanWhitespace();
		this.unreadChar();
		if (!this.checkLiteral(name)) {
			throw this.expectedInput(name);
		}
		if (this.scanWhitespace() != '>') {
			throw this.expectedInput(">");
		}
		handler.endElement(name);
	}

	protected void scanIdentifier(StringBuffer result) throws IOException {
		for (;;) {
			char ch = this.readChar();
			if (((ch >= 'a') && (ch <= 'z')) || ((ch >= 'A') && (ch <= 'Z'))
					|| ((ch >= '0') && (ch <= '9')) || (ch == '_') || (ch == '.')
					|| (ch == ':') || (ch == '-') || (ch > '\u007E')) {
				result.append(ch);
			} else {
				this.unreadChar();
				return;
			}
		}
	}

	protected char scanWhitespace() throws IOException {
		for (;;) {
			char ch = this.readChar();
			switch (ch) {
			case ' ':
			case '\t':
			case '\n':
			case '\r':
				break;
			default:
				return ch;
			}
		}
	}

	protected char scanWhitespace(StringBuffer result) throws IOException {
		for (;;) {
			char ch = this.readChar();
			switch (ch) {
			case ' ':
			case '\t':
			case '\n':
				result.append(ch);
			case '\r':
				break;
			default:
				return ch;
			}
		}
	}

	protected void scanString(StringBuffer string) throws IOException {
		char delimiter = this.readChar();
		if ((delimiter != '\'') && (delimiter != '"')) {
			throw this.expectedInput("' or \"");
		}
		for (;;) {
			char ch = this.readChar();
			if (ch == delimiter) {
				return;
			} else if (ch == '&') {
				this.resolveEntity(string);
			} else {
				string.append(ch);
			}
		}
	}

	protected void scanPCData(StringBuffer data) throws IOException {
		for (;;) {
			char ch = this.readChar();
			if (ch == '<') {
				ch = this.readChar();
				if (ch == '!') {
					this.checkCDATA(data);
				} else {
					this.unreadChar();
					return;
				}
			} else if (ch == '&') {
				this.resolveEntity(data);
			} else {
				data.append(ch);
			}
		}
	}

	protected boolean checkCDATA(StringBuffer buf) throws IOException {
		char ch = this.readChar();
		if (ch != '[') {
			this.unreadChar();
			this.skipSpecialTag(0);
			return false;
		} else if (!this.checkLiteral("CDATA[")) {
			this.skipSpecialTag(1); // one [ has already been read
			return false;
		} else {
			int delimiterCharsSkipped = 0;
			while (delimiterCharsSkipped < 3) {
				ch = this.readChar();
				switch (ch) {
				case ']':
					if (delimiterCharsSkipped < 2) {
						delimiterCharsSkipped += 1;
					} else {
						buf.append(']');
						buf.append(']');
						delimiterCharsSkipped = 0;
					}
					break;
				case '>':
					if (delimiterCharsSkipped < 2) {
						for (int i = 0; i < delimiterCharsSkipped; i++) {
							buf.append(']');
						}
						delimiterCharsSkipped = 0;
						buf.append('>');
					} else {
						delimiterCharsSkipped = 3;
					}
					break;
				default:
					for (int i = 0; i < delimiterCharsSkipped; i += 1) {
						buf.append(']');
					}
					buf.append(ch);
					delimiterCharsSkipped = 0;
				}
			}
			return true;
		}
	}

	protected void skipComment() throws IOException {
		int dashesToRead = 2;
		while (dashesToRead > 0) {
			char ch = this.readChar();
			if (ch == '-') {
				dashesToRead -= 1;
			} else {
				dashesToRead = 2;
			}
		}
		if (this.readChar() != '>') {
			throw this.expectedInput(">");
		}
	}

	protected void skipSpecialTag(int bracketLevel) throws IOException {
		int tagLevel = 1; // <
		char stringDelimiter = '\0';
		if (bracketLevel == 0) {
			char ch = this.readChar();
			if (ch == '[') {
				bracketLevel += 1;
			} else if (ch == '-') {
				ch = this.readChar();
				if (ch == '[') {
					bracketLevel += 1;
				} else if (ch == ']') {
					bracketLevel -= 1;
				} else if (ch == '-') {
					this.skipComment();
					return;
				}
			}
		}
		while (tagLevel > 0) {
			char ch = this.readChar();
			if (stringDelimiter == '\0') {
				if ((ch == '"') || (ch == '\'')) {
					stringDelimiter = ch;
				} else if (bracketLevel <= 0) {
					if (ch == '<') {
						tagLevel += 1;
					} else if (ch == '>') {
						tagLevel -= 1;
					}
				}
				if (ch == '[') {
					bracketLevel += 1;
				} else if (ch == ']') {
					bracketLevel -= 1;
				}
			} else {
				if (ch == stringDelimiter) {
					stringDelimiter = '\0';
				}
			}
		}
	}

	protected boolean checkLiteral(String literal) throws IOException {
		int length = literal.length();
		for (int i = 0; i < length; i += 1) {
			if (this.readChar() != literal.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	protected char readChar() throws IOException {
		char i = 0;

		if (bytesLeft == 0) {
			if (bufferLength > 0)
				buffer[0] = buffer[bufferLength - 1];
			bufferLength = this.reader.read(buffer, 1, buffer.length - 1);
			bytesLeft = bufferLength;
			currentIx = 1;
		}
		i = (char) (buffer[currentIx++] & 0xffff);
		bytesLeft--;

		if (i == 10) {
			this.parserLineNr += 1;
		}
		return i;
	}

	protected void unreadChar() {
		currentIx--;
		bytesLeft++;
	}

	protected void resolveEntity(StringBuffer buf) throws IOException {
		char ch = '\0';
		StringBuffer keyBuf = new StringBuffer();
		for (;;) {
			ch = this.readChar();
			if (ch == ';') {
				break;
			}
			keyBuf.append(ch);
		}
		String key = keyBuf.toString();
		if (key.charAt(0) == '#') {
			try {
				if (key.charAt(1) == 'x') {
					ch = (char) Integer.parseInt(key.substring(2), 16);
				} else {
					ch = (char) Integer.parseInt(key.substring(1), 10);
				}
			} catch (NumberFormatException e) {
				throw this.unknownEntity(key);
			}
			buf.append(ch);
		} else {
			char[] value = (char[]) this.entities.get(key);
			if (value == null) {
				throw this.unknownEntity(key);
			}
			buf.append(value);
		}
	}

	protected XMLNanoParseException invalidValueSet(String name) {
		String msg = "Invalid value set (entity name = \"" + name + "\")";
		return new XMLNanoParseException(this.name, this.parserLineNr, msg);
	}

	protected XMLNanoParseException invalidValue(String name, String value) {
		String msg = "Attribute \"" + name + "\" does not contain a valid "
				+ "value (\"" + value + "\")";
		return new XMLNanoParseException(this.name, this.parserLineNr, msg);
	}

	protected XMLNanoParseException unexpectedEndOfData() {
		String msg = "Unexpected end of data reached";
		return new XMLNanoParseException(this.name, this.parserLineNr, msg);
	}

	protected XMLNanoParseException syntaxError(String context) {
		String msg = "Syntax error while parsing " + context;
		return new XMLNanoParseException(this.name, this.parserLineNr, msg);
	}

	protected XMLNanoParseException expectedInput(String charSet) {
		String msg = "Expected: " + charSet;
		return new XMLNanoParseException(this.name, this.parserLineNr, msg);
	}

	protected XMLNanoParseException unknownEntity(String name) {
		String msg = "Unknown or invalid entity: &" + name + ";";
		return new XMLNanoParseException(this.name, this.parserLineNr, msg);
	}

}
