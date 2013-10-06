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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Unzipper {
	private Log log = LogFactory.getLog(Unzipper.class);
	

	private void copyStreams(InputStream in, OutputStream out, int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		
		for(int numBytesRead = in.read(buffer); numBytesRead!=-1; numBytesRead = in.read(buffer)){
			out.write(buffer, 0, numBytesRead);
		}
	}
	
	public boolean isDirectoryWrapper(File zipFile) throws UnzipException {
		try {
			ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile));
			
			boolean firstEntryWasDirectory = false;
			int numEntries = 0;
			for(ZipEntry entry = in.getNextEntry(); entry!=null;entry = in.getNextEntry()){
				String name = entry.getName().trim();
				if(name.startsWith("/"))
					name = name.substring(1);
				if(name.endsWith("/"))
					name = name.substring(0, name.length()-2);
				
				if(name.indexOf('/')==-1){
					// This is at the 'root' level.
					numEntries++;
					if(numEntries==1 && entry.isDirectory()){
						firstEntryWasDirectory = true;
					}
				}
			}
			in.close();
			return (numEntries==1) && firstEntryWasDirectory;
		} catch (IOException e) {
			throw new UnzipException("Error processing " + zipFile.getAbsolutePath(), e);
		}
		
	}
	
	public  void unzipFile(File zipFile, File destination, boolean unwrap) throws UnzipException {
		try {
			ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile));
			boolean done = false;
			while(!done){
				ZipEntry nextEntry = in.getNextEntry();
				
				if(nextEntry==null) done=true;
				else{
					String name = nextEntry.getName();
					if(unwrap){
						name = name.substring(name.indexOf('/'));
					}
					File outputFile = new File(destination, name);
					log.info("   " + outputFile.getAbsolutePath());
					
					if(!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) throw new UnzipException("Could not create directory " + outputFile.getParent());
					
					if(nextEntry.isDirectory()){
						if(!outputFile.exists() && !outputFile.mkdir())
							throw new UnzipException("Ddirectory does not exist and could not be created:" + outputFile.getAbsolutePath());
					}else {
						if(!outputFile.createNewFile()) throw new UnzipException("Could not create file " + outputFile.getAbsolutePath());
						FileOutputStream out = new FileOutputStream(outputFile);
						copyStreams(in, out, 1024*100);
						out.close();
						in.closeEntry();
					}
					
				}
			}
			
		} catch (Exception e) {
			throw new UnzipException("There was an error executing the unzip of file " + zipFile.getAbsolutePath(), e);
		}
		
//		String pathOfZipFile = zipFile.getAbsolutePath();
//		
//		String command = "unzip -o "  + pathOfZipFile + " -d " + destination.getAbsolutePath() ;
//		
//		try {
//			log.info("Running " + command);
//			
//			int result = new CommandRunner().runCommand(command);
//			if(result!=0) throw new UnzipException("Unzip command exited with status " + result);
//			log.info("Unzip complete");
//		} catch (CommandException e) {
//			throw new UnzipException("There was an error executing the unzip command: \"" + command + "\"", e);
//		}
	}

	public static class UnzipException extends Exception {

		private UnzipException(String message, Throwable cause) {
			super(message, cause);
		}

		private UnzipException(String message) {
			super(message);
		}
		
	}
	
}
