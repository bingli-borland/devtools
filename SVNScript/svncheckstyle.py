#!/usr/bin/env python 
"""
http://yarenty.blogspot.com/2014/02/svn-hooks-to-automatically-check-code.html

Subversion pre-commit hook script that runs PMD static code analysis.

Functionality:
Runs PMD checks on java source code.
Commit will be rejected if PMD rules are voilated.
If there are more than 40 files committed at once - commit will be rejected.
Don't kill SVN server - commit in smaller chunks. 
To avoid PMD checks - put NOPMD into SVN log.
The script expects a pmd-check.conf file placed in the conf dir under
the repo the commit is for.

c:/python27/python pre_commit.py %1 %2
exit %ERRORLEVEL%;

3) Copy pmd-check.conf into conf directory (c:\svnrepo\conf)
[DEFAULT]
svnlook = c:\\opt\\svn\\svnlook.exe
pmd = c:\\opt\\pmd\\bin\\pmd.bat
pmd_rules = java-basic,java-braces,java-clone,java-codesize,java-comments,java-design,java-empty,java-finalizers,java-imports,java-j2ee,java-javabeans,java-junit,java-logging-java,java-naming,java-optimizations,java-strictexception,java-strings,java-typeresolution,java-unnecessary,java-unusedcode
 
#not in scope
#java-controversial, java-coupling, java-android, java-javabeans, java-logging-jakarta-commons, java-sunsecure
#java-migrating, java-migrating_to_13, java-migrating_to_14, java-migrating_to_15, java-migrating_to_junit14

"""

import sys
import os
import subprocess
import fnmatch
import tempfile
import io
import yaml

svnlook = 'd:\\program\\subversion\\bin\\svnlook.exe'
pmd = 'd:\\program\\pmd-bin-5.1.3\\bin\\pmd.bat'
pmd_rules = 'java-basic,java-braces,java-clone,java-codesize,java-comments,java-design,java-empty,java-finalizers,java-imports,java-j2ee,java-javabeans,java-junit,java-logging-java,java-naming,java-optimizations,java-strictexception,java-strings,java-typeresolution,java-unnecessary,java-unusedcode'
cloc = 'd:\\Repositories\\_SCRIPT\\cloc.exe'
comment_per_min = 6

# Deal with the rename of ConfigParser to configparser in Python3
try:
    # Python >= 3.0
    import configparser
except ImportError:
    # Python < 3.0
    import ConfigParser as configparser


class Commands:
    """Class to handle logic of running commands"""

    def __init__(self, config):
        self.config = config

    def svnlook_changed(self, repo, txn):
        """Provide list of files changed in txn of repo"""
        # svnlook = self.config.get('DEFAULT', 'svnlook')
        cmd = "%s changed -t %s %s" % (svnlook, txn, repo)
        sys.stderr.write("Command:: %s <END>\n" % cmd)

        # get commit message
        try:
            f = os.popen(cmd)
            commitMessage = f.read()
            if f.close():
                raise 1
            commitMessage = commitMessage.rstrip('\n\r')
            sys.stderr.write("lines:: %s\n" % commitMessage)
        except:
            print('Unable to get commit message with svnlook.', file=sys.stderr)
            sys.exit(1)

        lineMsg = io.StringIO(commitMessage)
        changed = []
        while True:
            line = lineMsg.readline()
            if (len(line) == 0):
                break
            sys.stderr.write("line:: %s\n" % line)
            if line[-1] != "/" and line[0] in ("A", "U"):
                changed.append(line[4:].rstrip('\n\r'))

        sys.stderr.write("changed:: %s\n" % changed)
        return changed

    def svnlook_getlog(self, repo, txn):
        """ Gets content of svn log"""
        # svnlook = self.config.get('DEFAULT', 'svnlook')

        cmd = "%s log -t %s %s" % (svnlook, txn, repo)

        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE)
        data = p.communicate()

        return (p.returncode, data[0].decode())


    def svnlook_getfile(self, repo, txn, fn, tmp):
        """ Gets content of svn file"""
        # svnlook = self.config.get('DEFAULT', 'svnlook')

        cmd = "%s cat -t %s %s %s > %s" % (svnlook, txn, repo, fn, tmp)

        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE)
        data = p.communicate()

        return (p.returncode, data[1].decode())

    def pmd_command(self, repo, txn, fn, tmp):
        """ Run the PMD scan over created temporary java file"""
        # pmd = self.config.get('DEFAULT', 'pmd')
        # pmd_rules = self.config.get('DEFAULT', 'pmd_rules')

        cmd = "%s -f text -R %s -d %s" % (pmd, pmd_rules, tmp)
        sys.stderr.write("Command:: %s\n" % cmd)
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE)
        data = p.communicate()

        # pmd is not working on error codes ..
        return (p.returncode, data[0].decode())

    def cloc_command(self, repo, txn, fn, tmp):
        cmd = "%s %s --quiet --yaml" % (cloc, tmp)
        sys.stderr.write("CLOC Command:: %s\n" % cmd)
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE)
        data = p.communicate()
        clocresult = data[0].decode()
        dict = yaml.load(clocresult)
        comment = dict["SUM"]["comment"]
        codeline = dict["SUM"]["code"]
        result = comment * 100/codeline
        sys.stderr.write("CLOC:: %s, %s, %s\n" % (comment, codeline, result))
        if result > comment_per_min:
            existcode = 0;
        else:
            existcode = 1;
        return (existcode, comment, codeline, result);

def main(repo, txn):
    exitcode = 0
    config = configparser.SafeConfigParser()
    config.read(os.path.join(repo, 'conf', 'pmd-check.conf'))
    commands = Commands(config)

    # check if someone put magic string to not process code with PMD
    (returncode, log) = commands.svnlook_getlog(repo, txn)
    sys.stderr.write("LOG:: %s\n" % (log))
    if returncode != 0:
        sys.stderr.write("\nError retrieving log from svn (exit code %d):\n" % (returncode))
        sys.exit(returncode)

    if "NOPMD" in log:
        sys.stderr.write("No PMD check - mail should be sent instead.")
        sys.exit(1) #TODO:

    # get list of changed files during this commit
    changed = commands.svnlook_changed(repo, txn)
    sys.stderr.write("changed:: %s, len:: %d \n" % (changed, len(changed)))

    # this happens when you adding new project to repo
    if len(changed) == 0:
        sys.stderr.write("No files changed in SVN!!!\n")
        sys.exit(1) #TODO:

    # we don't want to kill svn server or wait hours for commit
    if len(fnmatch.filter(changed, "*.java")) >= 40:
        sys.stderr.write(
            "Too many files to process, try commiting " \
            " less than 40 java files per session \n" \
            " Or put 'NOPMD' in comment, if you need " \
            " to work with bigger chunks!\n")
        sys.exit(1)

    # create temporary file
    f = tempfile.NamedTemporaryFile(suffix='.java', prefix='x', delete=False)
    f.close()

    # only java files
    for fn in fnmatch.filter(changed, "*.java"):
        sys.stderr.write("=======================analysis %s \n" % fn)
        (returncode, err_mesg) = commands.svnlook_getfile(
            repo, txn, fn, f.name)
        if returncode != 0:
            sys.stderr.write(
                "\nError retrieving file '%s' from svn " \
                "(exit code %d):\n" % (fn, returncode))
            sys.stderr.write(err_mesg)

        (returncode1, commentline, codeline, result) = commands.cloc_command(repo, txn, fn, f.name)
        if returncode1 != 0:
            sys.stderr.write(
                "\nThe percentage of comments in source code too low '%s'" \
                "(comment %d):(code %d):(per:%d)\n" % (fn, commentline, codeline, result))
            sys.stderr.write(err_mesg)
            exitcode = 1

        (returncode, err_mesg) = commands.pmd_command(repo, txn, fn, f.name)
        if returncode != 0:
            sys.stderr.write(
                "\nError validating file '%s'" \
                "(exit code %d):\n" % (fn, returncode))
            sys.stderr.write(err_mesg)
            exitcode = 1
        if len(err_mesg) != 0:
            sys.stderr.write(
                "\nPMD violations in file '%s' \n" % fn)
            sys.stderr.write(err_mesg)
            exitcode = 1

    os.remove(f.name)
    return exitcode


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.stderr.write("invalid args\n")
        sys.exit(1)

    try:
        sys.stderr.write("args %s, %s\n" % (sys.argv[1], sys.argv[2]))
        returncode = main(sys.argv[1], sys.argv[2])
        sys.exit(1)
    except configparser.Error as e:
        sys.stderr.write("Error with the pmd-check.conf: %s\n" % e)
        sys.exit(1)
