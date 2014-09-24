/*
 * Copyright (C) 2014, Sean Bright. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seanbright.osgi.launcher.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesParser
{
	public static void performSubstitutions(Map<String, String> properties)
	{
		Pattern p = Pattern.compile("\\$\\{(.*?)\\}");

		while (true) {
			boolean subsOnPass = false;

			for (String key : properties.keySet()) {
				String value = properties.get(key);

				Matcher m = p.matcher(value);

				StringBuffer buffer = new StringBuffer();

				while (m.find()) {
					String var = m.group(1);
					String replace;

					if (var.startsWith("-")) {
						replace = System.getProperty(var.substring(1));
					} else {
						replace = properties.get(var);
					}

					if (replace != null) {
						// We need to escape backslashes and $s (a few times)
						replace = replace.replaceAll("\\\\", "\\\\\\\\");
						replace = replace.replaceAll("\\$", "\\\\\\$");

						m.appendReplacement(buffer, replace);
						subsOnPass = true;
					}
				}

				m.appendTail(buffer);

				properties.put(key, buffer.toString());
			}

			if (!subsOnPass) {
				break;
			}
		}
	}
}