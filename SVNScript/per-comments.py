#!/usr/bin/env python 
"""

"""

import sys
import os
import subprocess
import fnmatch
import tempfile
import io

cloc = 'd:\\Repositories\\_SCRIPT\\'

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
                changed.append(line[4:])

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


def main(repo, txn):
    exitcode = 0

    # check if someone put magic string to not process code with PMD
    (returncode, log) = commands.svnlook_getlog(repo, txn)
    sys.stderr.write("\n %s 1 " % (log))
    if returncode != 0:
        sys.stderr.write("\nError retrieving log from svn (exit code %d):\n" % (returncode))
        sys.exit(returncode)

    sys.stderr.write("\n 2 ")
    if "NOPOC" in log:
        sys.stderr.write("No POC check - mail should be sent instead.")
        sys.exit(1) #TODO:

    sys.stderr.write("\n 3 ")
    # get list of changed files during this commit
    changed = commands.svnlook_changed(repo, txn)
    sys.stderr.write("\n 4 %s " % (changed))

    # this happens when you adding new project to repo
    if len(changed) == 0:
        sys.stderr.write("No files changed in SVN!!!\n")
        sys.exit(1) #TODO:

    sys.stderr.write("\n 5 ")
    # we don't want to kill svn server or wait hours for commit
    if len(fnmatch.filter(changed, "*.java")) >= 40:
        sys.stderr.write(
            "Too many files to process, try commiting " \
            " less than 40 java files per session \n" \
            " Or put 'NOPMD' in comment, if you need " \
            " to work with bigger chunks!\n")
        sys.exit(1)

    sys.stderr.write("\n 6 ")
    # create temporary file
    f = tempfile.NamedTemporaryFile(suffix='.java', prefix='x', delete=False)
    f.close()

    sys.stderr.write("\n 7 ")
    # only java files
    for fn in fnmatch.filter(changed, "*.java"):
        (returncode, err_mesg) = commands.svnlook_getfile(
            repo, txn, fn, f.name)
        if returncode != 0:
            sys.stderr.write(
                "\nError retrieving file '%s' from svn " \
                "(exit code %d):\n" % (fn, returncode))
            sys.stderr.write(err_mesg)

        (returncode, err_mesg) = commands.pmd_command(
            repo, txn, fn, f.name)
        returncode = 1
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
