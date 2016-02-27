/*
 * Copyright  2000-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.phenix.pct;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.util.FileNameMapper;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for compiling Progress procedures
 * 
 * @author <a href="mailto:g.querret+PCT@gmail.com">Gilles QUERRET </a>
 */
public class PCTBgCompile extends PCTBgRun {
    private List<FileSet> filesets = new ArrayList<FileSet>();
    private boolean minSize = false;
    private boolean md5 = true;
    private boolean forceCompile = false;
    private boolean failOnError = true;
    private boolean xcode = false;
    private boolean noCompile = false;
    private boolean runList = false;
    private boolean listing = false;
    private boolean preprocess = false;
    private boolean debugListing = false;
    private boolean keepXref = false;
    private boolean multiCompile = false;
    private boolean streamIO = false;
    private boolean v6Frame = false;
    private String xcodeKey = null;
    private String languages = null;
    private int growthFactor = -1;
    private File destDir = null;
    private File xRefDir = null;
    private File preprocessDir = null;
    private File debugListingDir = null;
    private Mapper mapperElement = null;
    private String ignoredIncludes = null;

    private Set<CompilationUnit> units = new HashSet<CompilationUnit>();
    private int compOk = 0;
    private int compNotOk = 0;
    private int compSkipped = 0;

    public void setRelativePaths(boolean rel) {
        getOptions().setRelativePaths(rel);
    }

    /**
     * Reduce r-code size ? MIN-SIZE option of the COMPILE statement
     * 
     * @param minSize "true|false|on|off|yes|no"
     */
    public void setMinSize(boolean minSize) {
        this.minSize = minSize;
    }

    /**
     * Enables STREAM-IO attribute in COMPILE statement
     * 
     * @param streamIO "true|false|on|off|yes|no"
     */
    public void setStreamIO(boolean streamIO) {
        this.streamIO = streamIO;
    }
    
    /**
     * Enables v6Frame attribute in COMPILE statement
     * 
     * @param v6Frame "true|false|on|off|yes|no"
     */
    public void setv6Frame(boolean v6Frame) {
        this.v6Frame = v6Frame;
    }

    /**
     * Force compilation, without xref generation
     * 
     * @param forceCompile "true|false|on|off|yes|no"
     * 
     * @since 0.3b
     */
    public void setForceCompile(boolean forceCompile) {
        this.forceCompile = forceCompile;
    }

    /**
     * Create listing files during compilation
     * 
     * @param listing "true|false|on|off|yes|no"
     * 
     * @since 0.10
     */
    public void setListing(boolean listing) {
        this.listing = listing;
    }

    /**
     * Ignore Includes matching this pattern
     * 
     * @param pattern "can-do pattern for includefile"
     * 
     * @since 2.x
     */
    public void setIgnoredIncludes(String pattern) {
        this.ignoredIncludes = pattern;
    }

    /**
     * Create preprocessing files during compilation
     * 
     * @param preprocess "true|false|on|off|yes|no"
     * 
     * @since 0.10
     */
    public void setPreprocess(boolean preprocess) {
        this.preprocess = preprocess;
    }

    /**
     * 
     * @param dir
     * @since 0.19
     */
    public void setPreprocessDir(File dir) {
        this.preprocessDir = dir;
    }

    /**
     * Create debug list files during compilation
     * 
     * @param debugListing "true|false|on|off|yes|no"
     * 
     * @since PCT 0.13
     */
    public void setDebugListing(boolean debugListing) {
        this.debugListing = debugListing;
    }

    /**
     * 
     * @param dir
     * @since 0.19
     */
    public void setDebugListingDir(File dir) {
        this.debugListingDir = dir;
    }

    /**
     * Immediatly quit if a progress procedure fails to compile
     * 
     * @param failOnError "true|false|on|off|yes|no"
     */
    @Override
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * Don't use XREF (and so compile everything). Removed since 0.5, use forceCompile
     * 
     * @param noXref "true|false|on|off|yes|no"
     * 
     * @deprecated
     */
    public void setNoXref(boolean noXref) {
        log(Messages.getString("PCTCompile.1")); //$NON-NLS-1$
        this.forceCompile = noXref;
    }

    /**
     * Generates a .xref in the .pct directory, result of XREF option in the COMPILE statement
     * 
     * @param keepXref "true|false|on|off|yes|no"
     */
    public void setKeepXref(boolean keepXref) {
        this.keepXref = keepXref;
    }

    /**
     * Enables/Disables compiler:multi-compile option
     * 
     * @param multiCompile "true|false|on|off|yes|no"
     */
    public void setMultiCompile(boolean multiCompile) {
        this.multiCompile = multiCompile;
    }

    /**
     * Directory where to store CRC and includes files : .pct subdirectory is created there
     * 
     * @param xrefDir File
     */
    public void setXRefDir(File xrefDir) {
        this.xRefDir = xrefDir;
    }

    /**
     * Put MD5 in r-code ? GENERATE-MD5 option of the COMPILE statement
     * 
     * @param md5 "true|false|on|off|yes|no"
     */
    public void setMD5(boolean md5) {
        this.md5 = md5;
    }

    /**
     * No real compilation ; just print the files which should be recompiled
     * 
     * @param noCompile "true|false|on|off|yes|no"
     */
    public void setNoCompile(boolean noCompile) {
        this.noCompile = noCompile;
    }

    /**
     * Generates a .run file in the .pct directory, which shows internal and external procedures
     * calls
     * 
     * @param runList "true|false|on|off|yes|no"
     */
    public void setRunList(boolean runList) {
        this.runList = runList;
    }

    /**
     * Location to store the .r files
     * 
     * @param destDir Destination directory
     */
    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    /**
     * Procedures are encrypted ?
     * 
     * @param xcode boolean
     */
    public void setXCode(boolean xcode) {
        this.xcode = xcode;
    }

    /**
     * Compile using a specific key instead of the default key
     * 
     * @param xcodeKey String
     */
    public void setXCodeKey(String xcodeKey) {
        this.xcodeKey = xcodeKey;
    }

    /**
     * Identifies which language segments to include in the compiled r-code. LANGUAGES option of the COMPILE statement
     * 
     * @param languages String
     */
    public void setLanguages(String languages) {
        this.languages = languages;
    }

    /**
     * Specifies the factor by which ABL increases the length of strings. TEXT-SEG-GROWTH option of the COMPILE statement
     * 
     * @param growthFactor int (must be positive)
     */
    public void setTextGrowth(int growthFactor) {
        this.growthFactor = growthFactor;
    }

    /**
     * Adds a set of files to archive.
     * 
     * @param set FileSet
     */
    public void addFileset(FileSet set) {
        filesets.add(set);
    }

    /**
     * Add a nested filenamemapper.
     * 
     * @param fileNameMapper the mapper to add.
     * @since Ant 1.6.3
     */
    public void add(FileNameMapper fileNameMapper) {
        createMapper().add(fileNameMapper);
    }

    private synchronized void addCompilationCounters(int ok, int notOk) {
        compOk += ok;
        compNotOk += notOk;
    }

    /**
     * Define the mapper to map source to destination files.
     * 
     * @return a mapper to be configured.
     * @exception BuildException if more than one mapper is defined.
     */
    public Mapper createMapper() throws BuildException {
        if (mapperElement != null) {
            throw new BuildException("Cannot define more than one mapper", getLocation());
        }
        mapperElement = new Mapper(getProject());
        return mapperElement;
    }

    /**
     * returns the mapper to use based on nested elements or the flatten attribute.
     */
    private FileNameMapper getMapper() {
        FileNameMapper mapper = null;
        if (mapperElement != null) {
            mapper = mapperElement.getImplementation();
        } /*else {
            mapper = new RCodeMapper();
        }*/
        return mapper;
    }

    /**
     * Do the work
     * 
     * @throws BuildException Something went wrong
     */
    @Override
    public void execute() throws BuildException {
        if (this.destDir == null) {
            this.cleanup();
            throw new BuildException(Messages.getString("PCTCompile.34")); //$NON-NLS-1$
        }

        // Test output directory
        if (this.destDir.exists()) {
            if (!this.destDir.isDirectory()) {
                this.cleanup();
                throw new BuildException(Messages.getString("PCTCompile.35")); //$NON-NLS-1$
            }
        } else {
            if (!this.destDir.mkdir()) {
                this.cleanup();
                throw new BuildException(Messages.getString("PCTCompile.36")); //$NON-NLS-1$
            }
        }

        // Test xRef directory
        if (this.xRefDir == null) {
            this.xRefDir = new File(this.destDir, ".pct"); //$NON-NLS-1$
        }

        if (this.xRefDir.exists()) {
            if (!this.xRefDir.isDirectory()) {
                this.cleanup();
                throw new BuildException(Messages.getString("PCTCompile.38")); //$NON-NLS-1$
            }
        } else {
            if (!this.xRefDir.mkdir()) {
                this.cleanup();
                throw new BuildException(Messages.getString("PCTCompile.39")); //$NON-NLS-1$
            }
        }

        // If preprocessDir is set, then preprocess is always set to true
        if (preprocessDir != null)
            preprocess = true;
        if (debugListingDir != null)
            debugListing = true;

        log(Messages.getString("PCTCompile.40"), Project.MSG_INFO); //$NON-NLS-1$

        // Checking xcode and (listing || preprocess) attributes -- They're mutually exclusive
        if (this.xcode && (this.listing || this.preprocess)) {
            log(Messages.getString("PCTCompile.43"), Project.MSG_INFO); //$NON-NLS-1$ // TODO Update this message
            this.listing = false; // Useless for now, but just in case...
            this.preprocess = false; // Useless for now, but just in case...
        }

        initializeCompilationUnits();

        try {
            super.execute();
        } finally {
            log(MessageFormat.format(Messages.getString("PCTCompile.44"), new Object[]{Integer //$NON-NLS-1$
                    .valueOf(compOk)}));
            if (compNotOk > 0) {
                log(MessageFormat.format(Messages.getString("PCTCompile.45"), new Object[]{Integer //$NON-NLS-1$
                        .valueOf(compNotOk)}));
            }
        }
    }

    /**
     * Generates a list of compilation unit (which is a source file name, associated with output
     * file names (.r, XREF, listing, and so on). This list is then consumed by the background
     * workers and transmitted to the OpenEdge procedures.
     */
    private void initializeCompilationUnits() {
        for (FileSet fs : filesets) {
            
            for (String str : fs.getDirectoryScanner(getProject()).getIncludedFiles()) {
                CompilationUnit unit = new CompilationUnit();
                unit.fsRootDir = fs.getDir(getProject());
                unit.fsFile = str;
                unit.targetFile = getMapper() == null ? null : getMapper().mapFileName(str)[0];
                units.add(unit);
            }
        }

    }

    protected BackgroundWorker createOpenEdgeWorker(Socket socket) {
        CompilationBackgroundWorker worker = new CompilationBackgroundWorker(this);
        try {
            worker.initialize(socket);
        } catch (Throwable uncaught) {
            throw new BuildException(uncaught);
        }

        return worker;
    }

    public class CompilationBackgroundWorker extends BackgroundWorker {
        private int customStatus = 0;

        public CompilationBackgroundWorker(PCTBgCompile parent) {
            super(parent);
        }

        protected boolean performCustomAction() throws IOException {
            if (customStatus == 0) {
                customStatus = 3;
                sendCommand("launch", "pct/pctBgCompile.p");
                return true;
            } else if (customStatus == 3) {
                customStatus = 4;
                sendCommand("setOptions", getOptions());
                return true;
            } else if (customStatus == 4) {
                List<CompilationUnit> sending = new ArrayList<CompilationUnit>();
                boolean noMoreFiles = false;
                synchronized (units) {
                    int size = units.size();
                    if (size > 0) {
                        int numCU = (size > 100 ? 10 : 1);
                        Iterator<CompilationUnit> iter = units.iterator();
                        for (int zz = 0; zz < numCU; zz++) {
                            sending.add((CompilationUnit) iter.next());
                        }
                        for (Iterator<CompilationUnit> iter2 = sending.iterator(); iter2.hasNext();) {
                            units.remove((CompilationUnit) iter2.next());
                        }
                    } else {
                        noMoreFiles = true;
                    }
                }
                StringBuilder sb = new StringBuilder();
                if (noMoreFiles) {
                    return false;
                } else {
                    for (Iterator<CompilationUnit> iter = sending.iterator(); iter.hasNext();) {
                        CompilationUnit cu = iter.next();
                        if (sb.length() > 0)
                            sb.append('*');
                        sb.append(cu.toString());
                    }
                    sendCommand("PctCompile", sb.toString());
                    return true;
                }
            } else {
                return false;
            }
        }

        public void setCustomOptions(Map<String, String> options) {

        }

        private String getOptions() {
            StringBuilder sb = new StringBuilder();
            sb.append(Boolean.toString(runList)).append(';');
            sb.append(Boolean.toString(minSize)).append(';');
            sb.append(Boolean.toString(md5)).append(';');
            sb.append(Boolean.toString(xcode)).append(';');
            sb.append(xcodeKey == null ? "" : xcodeKey).append(';');
            sb.append(Boolean.toString(forceCompile)).append(';');
            sb.append(Boolean.toString(noCompile)).append(';');
            sb.append(Boolean.toString(keepXref)).append(';');
            sb.append(languages == null ? "" : languages).append(';');
            sb.append(Integer.toString((growthFactor > 0 ? growthFactor : 100))).append(';');
            sb.append(Boolean.toString(multiCompile)).append(';');
            sb.append(Boolean.toString(streamIO)).append(';');
            sb.append(Boolean.toString(v6Frame)).append(';');
            sb.append(Boolean.toString(PCTBgCompile.this.getOptions().useRelativePaths())).append(';');
            sb.append(destDir.getAbsolutePath()).append(';');
            sb.append(Boolean.toString(preprocess)).append(';');
            sb.append(preprocessDir == null ? "" : preprocessDir.getAbsolutePath()).append(';');
            sb.append(Boolean.toString(listing)).append(';');
            sb.append(Boolean.toString(debugListing)).append(';');
            sb.append(debugListingDir == null ? "" : debugListingDir.getAbsolutePath()).append(';');
            sb.append(ignoredIncludes);

            return sb.toString();
        }

        @Override
        public void handleResponse(String command, String parameter, boolean err,
                String customResponse, List<Message> returnValues) {
            if ("pctCompile".equalsIgnoreCase(command)) {
                String[] str = customResponse.split("/");
                int ok = 0, notOk = 0;
                try {
                    ok = Integer.parseInt(str[0]);
                    notOk = Integer.parseInt(str[1]);
                } catch (NumberFormatException nfe) {
                    throw new BuildException("Invalid response from command " + command + "(" + parameter + ") : '" 
                            + customResponse + "'", nfe);
                }
                addCompilationCounters(ok, notOk);
                logMessages(returnValues);
                if (err && failOnError) {
                    setBuildException(new BuildException(command + "(" + parameter + ") : " + customResponse));
                    quit();
                }
            }
        }
    }

    private static class CompilationUnit {
        private File fsRootDir; // Fileset root directory
        private String fsFile; // Fileset relative file name
        private String targetFile;

        @Override
        public int hashCode() {
            return new File(fsRootDir, fsFile).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;

            if (obj instanceof CompilationUnit) {
                CompilationUnit other = (CompilationUnit) obj;
                return new File(fsRootDir, fsFile).equals(new File(other.fsRootDir, other.fsFile));
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return fsRootDir + "|" + fsFile + "|" + (targetFile == null ? "" : targetFile);
        }

    }

}