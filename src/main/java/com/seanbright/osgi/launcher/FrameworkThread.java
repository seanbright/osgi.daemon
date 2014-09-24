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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.daemon.DaemonContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class FrameworkThread extends Thread
{
	private final FrameworkFactory factory;
	private Framework framework;
	private final Map<String, String> properties;

	public FrameworkThread(DaemonContext context, FrameworkFactory factory, Map<String, String> properties)
	{
		super(factory.toString());

		this.factory = factory;
		this.properties = properties;
	}

	@Override
	public void run()
	{
		framework = factory.newFramework(properties);

		try {
			framework.init();
			framework.start();

			installBundles(framework.getBundleContext(), properties.get("launcher.autostart"));

			while (true) {
				FrameworkEvent stopEvent = framework.waitForStop(0);

				if (stopEvent.getType() != FrameworkEvent.STOPPED_UPDATE) {
					break;
				}
			}
		} catch (BundleException ex) {
			ex.printStackTrace(System.err);
		} catch (InterruptedException ex) {
			ex.printStackTrace(System.err);
		}
	}

	public void shutdown()
			throws Exception
	{
		framework.stop();
	}

	private void installBundles(BundleContext context, String path)
	{
		List<Bundle> bundles = new ArrayList<Bundle>();

		if (path == null) {
			// They don't want to autostart anything
			return;
		}

		File parent = new File(path);

		if (!parent.exists()) {
			throw new RuntimeException(path + " does not exist.");
		}

		if (!parent.isDirectory()) {
			throw new RuntimeException(path + " is not a directory.");
		}

		for (File candidate : parent.listFiles()) {
			if (!candidate.isFile()) {
				continue;
			}

			try {
				bundles.add(context.installBundle(candidate.getAbsoluteFile().toURI().toString()));
			} catch (BundleException ex) {
				ex.printStackTrace(System.err);
			}
		}

		for (Bundle bundle : bundles) {
			try {
				bundle.start();
			} catch (BundleException ex) {
				ex.printStackTrace(System.err);
			}
		}
	}
}