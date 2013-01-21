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
package org.soitoolkit.tools.encryption.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EncryptorServlet extends HttpServlet {
	private static final long serialVersionUID = -9105814376923197613L;
	private static final String ENV_VAR_SOITOOLKIT_ENCRYPTION_PASSWORD = "SOITOOLKIT_ENCRYPTION_PASSWORD";
	private static final String FORM_PARAM_INPUT = "input";
	private static final String REQUEST_ATTRIBUTE_ENCRYPTED_INPUT = "soitoolkit-encrypted-input";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		produceUI(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String input = req.getParameter(FORM_PARAM_INPUT);
		if (input.trim().length() > 0) {
			String masterPassword = getMasterPasswordFromEnvironment();

			if (getMasterPasswordFromEnvironment() == null) {
				throw new IllegalStateException(
						"Cannot perform encryption. Required environment variable not configured: "
								+ ENV_VAR_SOITOOLKIT_ENCRYPTION_PASSWORD);
			}

			String encryptedInput = EncryptionUtil.encrypt(input,
					masterPassword);
			req.setAttribute(REQUEST_ATTRIBUTE_ENCRYPTED_INPUT, encryptedInput);
		}

		produceUI(req, resp);
	}

	void produceUI(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		resp.setContentType("text/html");
		
		PrintWriter out = resp.getWriter();
		out.print("<html><head><title>soi-toolkit encryption web tool</title></head><body>");
		out.print("<h2>soi-toolkit encryption web tool</h2>");
		out.print("Read more on: ");
		out.print("<a href='http://code.google.com/p/soi-toolkit/wiki/UG_PropertyFile#Encrypted_passwords'>How to use encrypted passwords</a><br><br>");

		// form
		out.print("<form name='encryption_form' method='POST' action='.'>");
		out.print("<input type='password' name='" + FORM_PARAM_INPUT + "' />");
		out.print("<input type='submit' value='Encrypt' />");
		out.print("</form>");

		Object encryptedInput = req
				.getAttribute(REQUEST_ATTRIBUTE_ENCRYPTED_INPUT);
		if (encryptedInput != null) {
			out.println("Encrypted input: <br>");
			out.println(encryptedInput);
		}

		out.print("</body></html>");
	}

	String getMasterPasswordFromEnvironment() {
		return System.getenv(ENV_VAR_SOITOOLKIT_ENCRYPTION_PASSWORD);
	}

}
