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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Echo;
import org.apache.tools.ant.types.FileSet;

import java.io.PrintStream;

public class Ant {

    private Project project;
    private final PrintStream rawOut = System.out;
    private final PrintStream rawErr = System.err;
    
    public Ant() {
        project = new Project();
        project.init();
        DefaultLogger logger = new DefaultLogger();
        project.addBuildListener(logger);
        logger.setOutputPrintStream(System.out);
        logger.setErrorPrintStream(System.err);
        logger.setMessageOutputLevel(Project.MSG_INFO);
        System.setOut(
          new PrintStream(new DemuxOutputStream(project, false)));
        System.setErr(
          new PrintStream(new DemuxOutputStream(project, true)));
        project.fireBuildStarted();
    }
    
    public void addFileset(FileSet fs){
    	fs.setProject(project);
    }
    
    public void run(Task...tasks) throws BuildException {
        project.log("running");
        BuildException caught=null;
        try {
        	for(Task echo: tasks){
        		echo.setTaskName("Echo");
        		echo.setProject(project);
        		echo.init();
        		echo.execute();
        	}
        } catch (BuildException e) {
            caught=e;
        }
        project.log("finished");
        project.fireBuildFinished(caught);
        
        System.setOut(rawOut);
        System.setErr(rawErr);
        
        if(caught!=null){
        	throw caught;
        }
    }
    
    public static void main(String args[]) {
        Ant embed=new Ant();
        embed.run();
    }
}
