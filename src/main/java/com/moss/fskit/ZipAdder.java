/**
 * Copyright (C) 2013, Moss Computing Inc.
 *
 * This file is part of fs-kit.
 *
 * fs-kit is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * fs-kit is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with fs-kit; see the file COPYING.  If not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version.
 */
package com.moss.fskit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.tools.ant.taskdefs.Zip;

import com.moss.fskit.Unzipper.UnzipException;

public class ZipAdder {
	
	public void addToZip(File addition, String atPath, File zipPath) throws IOException, UnzipException {
		TempDir tempDir = TempDir.create();
		new Unzipper().unzipFile(zipPath, tempDir, false);
		
		File path = new File(tempDir, atPath);
		if(!path.getParentFile().exists() && !path.getParentFile().mkdirs())
			throw new RuntimeException("Could not create directory at " + path.getParentFile().getAbsolutePath());
		
		copy(addition, path);
		
		Ant ant = new Ant();
		
		Zip zip = new Zip();
		zip.setBasedir(tempDir);
		zip.setIncludes("**");
		zip.setDestFile(zipPath);
		ant.run(zip);
		
	}

	private void copy(File from, File to) throws IOException {
		InputStream in = new FileInputStream(from);
		OutputStream out = new FileOutputStream(to);

		byte[] buffer = new byte[1024*1024];
		for(int read = in.read(buffer);read!=-1;read = in.read(buffer)){
			out.write(buffer, 0, read);
		}
		in.close();
		out.close();
	}

	
}
