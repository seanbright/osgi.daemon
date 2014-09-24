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

package com.seanbright.osgi.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.osgi.framework.launch.FrameworkFactory;
import com.seanbright.osgi.launcher.util.PropertiesParser;

public class Daemon implements org.apache.commons.daemon.Daemon
{
	private DaemonContext context;
	private FrameworkFactory factory;
	private FrameworkThread thread;

	private Map<String, String> frameworkProps;

	@Override
	public void init(DaemonContext context)
			throws DaemonInitException, Exception
	{
		this.context = context;

		frameworkProps = loadFrameworkProperties(
				context.getArguments().length == 0
				? "framework.properties"
				: context.getArguments()[0]);

		factory = FrameworkFactories.get();
	}

	@Override
	public void start() throws Exception
	{
		// Find and launch a framework
		thread = new FrameworkThread(
				context,
				factory,
				frameworkProps);

		thread.start();
	}

	@Override
	public void stop() throws Exception
	{
		thread.shutdown();
		thread.join();
		thread = null;
	}

	@Override
	public void destroy()
	{
	}

	public static void main(String[] args)
			throws Exception
	{
		Daemon d = new Daemon();

		d.init(null);
		d.start();
	}

	private Map<String, String> loadFrameworkProperties(String filename)
	{
		Map<String, String> properties = new HashMap<String, String>();

		File f = new File(filename);

		if (!f.exists() || !f.isFile() || !f.canRead()) {
			return properties;
		}

		Properties workingCopy = new Properties();

		FileInputStream stream = null;

		try {
			stream = new FileInputStream(f);

			workingCopy.load(stream);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					/* Ignore */
				}
			}
		}

		for (String key : workingCopy.stringPropertyNames()) {
			properties.put(
					key,
					workingCopy.getProperty(key));
		}

		PropertiesParser.performSubstitutions(properties);

		return properties;
	}
}