/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package tools.modifypackage.file ;

import java.util.List ;
import java.util.Map ;
import java.util.HashMap ;

import java.io.File ;
import java.io.IOException ;

import tools.modifypackage.file.Scanner ;
import tools.modifypackage.file.Recognizer ;
import tools.modifypackage.file.FileWrapper ;
import tools.modifypackage.file.ActionFactory ;
import tools.modifypackage.file.Block ;
import tools.modifypackage.file.BlockParser ;

import tools.modifypackage.argparser.ArgParser ;
import tools.modifypackage.argparser.Help ;
import tools.modifypackage.argparser.DefaultValue ;
import tools.modifypackage.argparser.Separator ;

import tools.modifypackage.generic.UnaryFunction ;

public class CopyrightProcessor {
    private CopyrightProcessor() {} 

    private interface Arguments {
	@DefaultValue( "true" ) 
	@Help( "Set to true to validate copyright header; if false, generate/update/insert copyright headers as needed" ) 
	boolean validate() ;

	@DefaultValue( "0" ) 
	@Help( "Set to >0 to get information about actions taken for every file.  Larger values give more detail." ) 
	int verbose() ;

	@DefaultValue( "true" ) 
	@Help( "Set to true to avoid modifying any files" ) 
	boolean dryrun() ;

	@Help( "List of directories to process" ) 
	@DefaultValue( "" ) 
	List<File> roots() ;

	@Help( "List of directory names that should be skipped" ) 
	@DefaultValue( "" ) 
	List<String> skipdirs() ;

	@Help( "File containing text of copyright header.  This must not include any comment characters" ) 
	@DefaultValue( "" ) 
	FileWrapper copyright() ;

	@DefaultValue( "1997" )
	@Help( "Default copyright start year, if not otherwise specified" ) 
	String startyear() ;
    }

    private static boolean validate ;
    private static int verbose ;

    private static final String[] JAVA_LIKE_SUFFIXES = {
	"c", "h", "java", "sjava", "idl" } ;
    private static final String JAVA_COMMENT_START = "/*" ;
    private static final String JAVA_COMMENT_PREFIX = " *" ;
    private static final String JAVA_COMMENT_END = "*/" ; 

    private static final String[] XML_LIKE_SUFFIXES = {
	"htm", "html", "xml", "dtd" } ;
    private static final String XML_COMMENT_START = "<!--" ;
    private static final String XML_COMMENT_PREFIX = " " ;
    private static final String XML_COMMENT_END = "-->" ;

    private static final String[] JAVA_LINE_LIKE_SUFFIXES = {
	"tdesc", "policy", "secure" } ;
    private static final String JAVA_LINE_PREFIX = "// " ;

    private static final String[] SCHEME_LIKE_SUFFIXES = {
	"mc", "mcd", "scm", "vthought" } ;
    private static final String SCHEME_PREFIX = "; " ;

    // Shell scripts must always start with #! ..., others need not.
    private static final String[] SHELL_SCRIPT_LIKE_SUFFIXES = {
	"ksh", "sh" } ;
    private static final String[] SHELL_LIKE_SUFFIXES = {
	"classlist", "config", "jmk", "properties", "prp", "xjmk", "set",
	"data", "txt", "text" } ;
    private static final String SHELL_PREFIX = "# " ;

    // Files whose names match these also use the SHELL_PREFIX style line comment.
    private static final String[] MAKEFILE_NAMES = {
	"Makefile.corba", "Makefile.example", "ExampleMakefile", "Makefile" } ;

    private static final String[] BINARY_LIKE_SUFFIXES = {
	"sxc", "sxi", "sxw", "odp", "gif", "png", "jar", "zip", "jpg", "pom",
	"pdf", "doc", "mif", "fm", "book", "zargo", "zuml", "cvsignore", 
	"hgignore", "list", "old", "orig", "rej", "swp", "swo", "class", "o",
	"javaref", "idlref", "css" } ;

    // Special file names to ignore
    private static final String[] IGNORE_FILE_NAMES = {
	"NORENAME", "errorfile", "sed_pattern_file.version"
    } ;

    // Block tags
    private static final String COPYRIGHT_BLOCK_TAG = "CopyrightBlock" ;
    private static final String SUN_COPYRIGHT_TAG = "SunCopyright" ;
    private static final String CORRECT_COPYRIGHT_TAG = "CorrectCopyright" ;
    private static final String JAVA_FORMAT_TAG = "JavaFormat" ;
    private static final String JAVA_LINE_FORMAT_TAG = "JavaLineFormat" ;
    private static final String XML_FORMAT_TAG = "XmlFormat" ;
    private static final String SCHEME_FORMAT_TAG = "SchemeFormat" ;
    private static final String SHELL_FORMAT_TAG = "ShellFormat" ;

    private static void trace( String msg ) {
	System.out.println( msg ) ;
    }

    private static Block makeCopyrightLineCommentBlock( final Block copyrightText, 
	final String prefix, final String tag ) {

	final Block result = new Block( copyrightText ) ;
	result.addPrefixToAll( prefix ) ;

	result.addTag( tag ) ;
	result.addTag( CORRECT_COPYRIGHT_TAG ) ;

	return result ;
    }

    private static Block makeCopyrightBlockCommentBlock( Block copyrightText, 
	String start, String prefix, String end, String tag ) {

	final Block result = new Block( copyrightText ) ;
	result.addPrefixToAll( prefix ) ;
	result.addBeforeFirst( start ) ;
	result.addAfterLast( end ) ;
	
	result.addTag( tag ) ;
	result.addTag( CORRECT_COPYRIGHT_TAG ) ;

	return result ;
    }

    private static final String COPYRIGHT = "Copyright" ;

    // Copyright year is first non-blank after COPYRIGHT
    private static String getSunCopyrightStart( String str ) {
	int index = str.indexOf( COPYRIGHT ) ;
	if (index == -1) 
	    return null ;

	int pos = index + COPYRIGHT.length() ;
	char ch = str.charAt( pos ) ;
	while (Character.isWhitespace(ch) && (pos<str.length())) {
	    ch = str.charAt( ++pos ) ;
	}
	
	int start = pos ;
	ch = str.charAt( pos ) ;
	while (Character.isDigit(ch) && (pos<str.length())) {
	    ch = str.charAt( ++pos ) ;
	}

	if (pos==start) 
	    return null ;

	return str.substring( start, pos ) ;
    }

    private static final String START_YEAR = "StartYear" ;

    private static Block makeCopyrightBlock( String startYear, 
	Block copyrightText) throws IOException {

	if (verbose > 1) {
	    trace( "makeCopyrightBlock: startYear = " + startYear ) ;
	    trace( "makeCopyrightBlock: copyrightText = " + copyrightText ) ;

	    trace( "Contents of copyrightText block:" ) ;
	    for (String str : copyrightText.contents()) {
		trace( "\t" + str ) ;
	    }
	}

	Map<String,String> map = new HashMap<String,String>() ;
	map.put( START_YEAR, startYear ) ;
	Block withStart = copyrightText.instantiateTemplate( map ) ;

	if (verbose > 1) {
	    trace( "Contents of copyrightText block withStart date:" ) ;
	    for (String str : withStart.contents()) {
		trace( "\t" + str ) ;
	    }
	}   

	return withStart ;
    }

    private interface BlockParserCall extends UnaryFunction<FileWrapper,List<Block>> {}

    private static BlockParserCall makeBlockCommentParser( final String start, 
	final String end ) {

	return new BlockParserCall() {
	    public String toString() {
		return "BlockCommentBlockParserCall[start=," + start
		    + ",end=" + end + "]" ;
	    }

	    public List<Block> evaluate( FileWrapper fw ) {
		try {
		    return BlockParser.parseBlocks( fw, start, end ) ;
		} catch (IOException exc) {
		    throw new RuntimeException( exc ) ;
		}
	    }
	} ;
    }

    private static BlockParserCall makeLineCommentParser( final String prefix ) {

	return new BlockParserCall() {
	    public String toString() {
		return "LineCommentBlockParserCall[prefix=," + prefix + "]" ;
	    }

	    public List<Block> evaluate( FileWrapper fw ) {
		try {
		    return BlockParser.parseBlocks( fw, prefix ) ;
		} catch (IOException exc) {
		    throw new RuntimeException( exc ) ;
		}
	    } 
	} ;
    }

    private static void validationError( Block block, String msg, FileWrapper fw ) {
	trace( "Copyright validation error: " + msg + " for " + fw ) ;
	if ((verbose > 0) && (block != null)) {
	    trace( "Block=" + block ) ;
	    trace( "Block contents:" ) ;
	    for (String str : block.contents()) {
		trace( "\"" + str + "\"" ) ;
	    }
	}
    }

    // Strip out old Sun copyright block.  Prepend new copyrightText.
    // copyrightText is a Block containing a copyright template in the correct comment format.
    // parseCall is the correct block parser for splitting the file into Blocks.
    // defaultStartYear is the default year to use in copyright comments if not
    // otherwise specified in an old copyright block.
    // afterFirstBlock is true if the copyright needs to start after the first block in the
    // file.
    private static Scanner.Action makeCopyrightBlockAction( final Block copyrightText, 
	final BlockParserCall parserCall, final String defaultStartYear, 
	final boolean afterFirstBlock ) {

	if (verbose > 0) {
	    trace( "makeCopyrightBlockAction: copyrightText = " + copyrightText ) ;
	    trace( "makeCopyrightBlockAction: parserCall = " + parserCall ) ;
	    trace( "makeCopyrightBlockAction: defaultStartYear = " + defaultStartYear ) ;
	    trace( "makeCopyrightBlockAction: afterFirstBlock = " + afterFirstBlock ) ;
	}

	return new Scanner.Action() {
	    public String toString() {
		return "CopyrightBlockAction[copyrightText=" + copyrightText
		    + ",parserCall=" + parserCall 
		    + ",defaultStartYear=" + defaultStartYear 
		    + ",afterFirstBlock=" + afterFirstBlock + "]" ;
	    }

	    public boolean evaluate( FileWrapper fw ) {
		try {
		    String startYear = defaultStartYear ;
		    boolean hadAnOldSunCopyright = false ;
		    
		    // Convert file into blocks
		    final List<Block> fileBlocks = parserCall.evaluate( fw ) ;

		    // Tag blocks
		    for (Block block : fileBlocks) {
			String str = block.find( COPYRIGHT ) ;
			if (str != null) {
			    block.addTag( COPYRIGHT_BLOCK_TAG ) ;
			    if (str.contains( "Sun" )) {
				startYear = getSunCopyrightStart( str ) ;
				block.addTag( SUN_COPYRIGHT_TAG ) ;
				hadAnOldSunCopyright = true ;
			    }
			}
		    }

		    if (verbose > 1) {
			trace( "copyrightBlockAction: blocks in file " + fw ) ;
			for (Block block : fileBlocks) {
			    trace( "\t" + block ) ;
			    for (String str : block.contents()) {
				trace( "\t\t" + str ) ;
			    }
			}
		    }

		    Block cb = makeCopyrightBlock( startYear, copyrightText ) ;

		    if (validate) {
			// There should be a Sun copyright block in the first block
			// (if afterFirstBlock is false), otherwise in the second block.
			// It should entirely match copyrightText
			int count = 0 ;
			for (Block block : fileBlocks) {
			    // Generally always return true, because we want to see ALL validation errors.
			    if (!afterFirstBlock && (count == 0)) {
				if (block.hasTags( SUN_COPYRIGHT_TAG, COPYRIGHT_BLOCK_TAG, 
				    BlockParser.COMMENT_BLOCK_TAG)) {
				    if (!cb.equals( block )) {
					validationError( block, "First block has incorrect copyright text", fw ) ;
				    }
				} else {
				    validationError( block, "First block should be copyright but isn't", fw ) ;
				}

				return true ;
			    } else if (afterFirstBlock && (count == 1)) {
				if (block.hasTags( SUN_COPYRIGHT_TAG, COPYRIGHT_BLOCK_TAG, 
				    BlockParser.COMMENT_BLOCK_TAG)) {
				    if (!cb.equals( block )) {
					validationError( block, "Second block has incorrect copyright text", fw ) ;
				    }
				} else {
				    validationError( block, "Second block should be copyright but isn't", fw ) ;
				}

				return true ;
			    } 
			    
			    if (count > 1) {
				// should not get here!  Return false only in this case, because this is
				// an internal error in the validator.
				validationError( null, "Neither first nor second block checked", fw ) ;
				return false ;
			    }

			    count++ ;
			}
		    } else {
			// Re-write file, replacing the first block tagged
			// SUN_COPYRIGHT_TAG, COPYRIGHT_BLOCK_TAG, and commentBlock with
			// the copyrightText block.
			
			if (fw.canWrite()) {
			    trace( "Updating copyright/license header on file " + fw ) ;

			    // XXX this is dangerous: a crash before close will destroy the file!
			    fw.delete() ; 
			    fw.open( FileWrapper.OpenMode.WRITE ) ;

			    boolean firstMatch = true ;
			    boolean firstBlock = true ;
			    for (Block block : fileBlocks) {
				if (!hadAnOldSunCopyright && firstBlock) {
				    if (afterFirstBlock) {
					block.write( fw ) ;
					cb.write( fw ) ;
				    } else {
					cb.write( fw ) ;
					block.write( fw ) ;
				    }
				    firstBlock = false ;
				} else if (block.hasTags( SUN_COPYRIGHT_TAG, COPYRIGHT_BLOCK_TAG, 
				    BlockParser.COMMENT_BLOCK_TAG) && firstMatch)  {
				    firstMatch = false ;
				    if (hadAnOldSunCopyright) {
					cb.write( fw ) ;
				    }
				} else {
				    block.write( fw ) ;
				}
			    }
			} else {
			    if (verbose > 1) {
				trace( "Skipping file " + fw + " because is is not writable" ) ;
			    }
			}
		    }
		} catch (IOException exc ) {
		    trace( "Exception while processing file " + fw + ": " + exc ) ;
		    exc.printStackTrace() ;
		    return false ;
		} finally {
		    fw.close() ;
		}

		return true ;
	    }
	} ;
    }
    
    public static void main(String[] strs) {
	ArgParser<Arguments> ap = new ArgParser( Arguments.class ) ;
	Arguments args = ap.parse( strs ) ;

	String startYear = args.startyear() ;
	verbose = args.verbose() ;
	validate = args.validate() ;

	if (verbose > 0) {
	    trace( "Main: args:\n" + args ) ;
	}

	try {
	    // Create the blocks needed for different forms of the 
	    // copyright comment template
	    final Block copyrightText = BlockParser.getBlock( args.copyright() ) ;

	    final Block javaCopyrightText = 
		makeCopyrightBlockCommentBlock( copyrightText, 
		    JAVA_COMMENT_START, JAVA_COMMENT_PREFIX, " " + JAVA_COMMENT_END,
		    JAVA_FORMAT_TAG ) ;
	    final Block xmlCopyrightText = 
		makeCopyrightBlockCommentBlock( copyrightText, 
		    XML_COMMENT_START, XML_COMMENT_PREFIX, XML_COMMENT_END, 
		    XML_FORMAT_TAG ) ;

	    final Block javaLineCopyrightText =
		makeCopyrightLineCommentBlock( copyrightText, JAVA_LINE_PREFIX,
		JAVA_LINE_FORMAT_TAG ) ;
	    final Block schemeCopyrightText = 
		makeCopyrightLineCommentBlock( copyrightText, SCHEME_PREFIX,
		SCHEME_FORMAT_TAG ) ;
	    final Block shellCopyrightText = 
		makeCopyrightLineCommentBlock( copyrightText, SHELL_PREFIX,
		SHELL_FORMAT_TAG ) ;

	    if (verbose > 0) {
		trace( "Main: copyrightText = " + copyrightText ) ;
		trace( "Main: javaCopyrightText = " + javaCopyrightText ) ;
		trace( "Main: xmlCopyrightText = " + xmlCopyrightText ) ;
		trace( "Main: javaLineCopyrightText = " + javaLineCopyrightText ) ;
		trace( "Main: schemeCopyrightText = " + schemeCopyrightText ) ;
		trace( "Main: shellCopyrightText = " + shellCopyrightText ) ;
	    }

	    ActionFactory af = new ActionFactory( args.verbose(), args.dryrun() ) ;

	    // Create the actions we need
	    Recognizer recognizer = af.getRecognizerAction() ; // recognizer is the scanner action

	    // Create the BlockParserCalls needed for the actions
	    BlockParserCall javaBlockParserCall = makeBlockCommentParser( 
		JAVA_COMMENT_START, JAVA_COMMENT_END ) ;
	    BlockParserCall xmlBlockParserCall = makeBlockCommentParser(
		XML_COMMENT_START, XML_COMMENT_END ) ;

	    BlockParserCall javaLineParserCall = makeLineCommentParser( JAVA_LINE_PREFIX ) ;
	    BlockParserCall schemeLineParserCall = makeLineCommentParser( SCHEME_PREFIX ) ;
	    BlockParserCall shellLineParserCall = makeLineCommentParser( SHELL_PREFIX ) ;

	    // Now, create the actions needed in the recognizer
	    Scanner.Action skipAction = af.getSkipAction() ;

	    Scanner.Action javaAction = makeCopyrightBlockAction( javaCopyrightText,
		javaBlockParserCall, startYear, false ) ;
	    Scanner.Action xmlAction = makeCopyrightBlockAction( xmlCopyrightText,
		xmlBlockParserCall, startYear, true ) ;
	    Scanner.Action schemeAction = makeCopyrightBlockAction( schemeCopyrightText, 
		schemeLineParserCall, startYear, false ) ;
	    Scanner.Action javaLineAction = makeCopyrightBlockAction( javaLineCopyrightText, 
		javaLineParserCall, startYear, false ) ;
	    Scanner.Action shellAction = makeCopyrightBlockAction( shellCopyrightText, 
		shellLineParserCall, startYear, false ) ;
	    Scanner.Action shellScriptAction = makeCopyrightBlockAction( shellCopyrightText, 
		shellLineParserCall, startYear, true ) ;

	    // Configure the recognizer
	    // Java
	    for (String str : JAVA_LIKE_SUFFIXES) {
		recognizer.addKnownSuffix( str, javaAction ) ;
	    }

	    // Java line 
	    for (String str : JAVA_LINE_LIKE_SUFFIXES) {
		recognizer.addKnownSuffix( str, javaLineAction ) ;
	    }

	    // XML
	    for (String str : XML_LIKE_SUFFIXES) {
		recognizer.addKnownSuffix( str, xmlAction ) ;
	    }

	    // Scheme
	    for (String str : SCHEME_LIKE_SUFFIXES) {
		recognizer.addKnownSuffix( str, schemeAction ) ;
	    }

	    // Shell
	    for (String str : SHELL_LIKE_SUFFIXES) {
		recognizer.addKnownSuffix( str, shellAction ) ;
	    }

	    for (String str : MAKEFILE_NAMES) {
		recognizer.addKnownName( str, shellAction ) ;
	    }

	    for (String str : SHELL_SCRIPT_LIKE_SUFFIXES) {
		recognizer.addKnownSuffix( str, shellScriptAction ) ;
	    }
	    recognizer.setShellScriptAction( shellScriptAction ) ;
	    
	    // Binary
	    for (String str : BINARY_LIKE_SUFFIXES) {
		recognizer.addKnownSuffix( str, skipAction ) ;
	    }

	    for (String str : IGNORE_FILE_NAMES) {
		recognizer.addKnownName( str, skipAction ) ;
	    }

	    if (verbose > 0) {
		trace( "Main: contents of recognizer:" ) ;
		recognizer.dump() ;
	    }

	    Scanner scanner = new Scanner( verbose, args.roots() ) ;
	    for (String str : args.skipdirs() )
		scanner.addDirectoryToSkip( str ) ;

	    // Finally, we process all files
	    scanner.scan( recognizer ) ;
	} catch (IOException exc) {
	    System.out.println( "Exception while processing: " + exc ) ;
	    exc.printStackTrace() ;
	}
    }
}

