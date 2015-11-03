# -*- coding: gbk -*-

import os
import re
import sys,subprocess
import tempfile
import time
import datetime
from fnmatch import fnmatch, fnmatchcase
import shutil
import zipfile

'''
'''
#部署本地文件到服务器. 
def renamedir(rootdir, srcdir, destdir):
    shoulddirs = []
    for root, dirs, files in os.walk(rootdir): # Walk directory tree
        for d in dirs:         
            #sys.stdout.write("%s, %s\n" % (root, d))
            currentdir = os.path.join(root, d)
            if (currentdir.endswith(srcdir)):
                sys.stdout.write("find %s>\n" % (currentdir))
                shoulddirs.append(currentdir)
    for shoulddir in shoulddirs:
        newdir = shoulddir.replace(srcdir, destdir)
        sys.stdout.write("rename %s, %s>\n" % (shoulddir, newdir))
        shutil.move(shoulddir, newdir)

def renamefile(rootdir, srcname, destname):
    shoulddirs = []
    srcprefix = srcname.split('*')[0]
    srcpostfix = srcname.split('*')[1]
    destprefix = destname.split('*')[0]
    destpostfix = destname.split('*')[1]
    for root, dirs, files in os.walk(rootdir): # Walk directory tree
        for f in files:         
            sys.stdout.write("%s, %s, %s, %s, %s\n" % (root, f, srcprefix, srcpostfix, destprefix))
            currentname = os.path.join(root, f)
            newname = os.path.join(root, f.replace(srcprefix, destprefix))
            if (currentname.endswith(srcpostfix) and currentname.find(srcprefix) != -1):
                sys.stdout.write("find %s, %s>\n" % (currentname, newname))
                shutil.move(currentname, newname)

srcommand = 'E:\\dev\\DevTools\\DevTools\\trunk\\CddevPythonTools\\tools\\fnr.exe --cl '
def replacedir(rootdir, dirrule, srcname, destname):
    shoulddirs = []
    dirname = dirrule.split('//')[0]
    postfix = dirrule.split('//')[1]
    if (dirname == '**'):
        command = srcommand + ' --includeSubDirectories --find ' + srcname + ' --replace ' + destname + ' --caseSensitive ' + \
                                  ' --dir ' + rootdir + ' --fileMask ' + postfix
        sys.stdout.write("command, %s, %s\n" % (command, rootdir))
        result = subprocess.Popen(command, cwd=rootdir, stdout=subprocess.PIPE).communicate()[0]
        sys.stdout.write("command, %s\n" % (result))
    else:
        for root, dirs, files in os.walk(rootdir): # Walk directory tree
            for d in dirs:         
                #sys.stdout.write("%s, %s\n" % (root, d))
                currentdir = os.path.join(root, d)
                if (currentdir.endswith(dirname)):
                    command = srcommand + ' --includeSubDirectories --find ' + srcname + ' --replace ' + destname + ' --caseSensitive ' + \
                                              ' --dir ' + currentdir + ' --fileMask ' + postfix
                    sys.stdout.write("command, %s, %s, %s\n" % (command, root, currentdir))
                    result = subprocess.Popen(command, cwd=currentdir, stdout=subprocess.PIPE).communicate()[0]
                    sys.stdout.write("command, %s\n" % (result))

def replacefile(rootdir, filerule, srcname, destname):
    shoulddirs = []
    fileprefix = filerule.split('*')[0]
    filepostfix = filerule.split('*')[1]
    for root, dirs, files in os.walk(rootdir): # Walk directory tree
        for f in files:         
            #sys.stdout.write("%s, %s, %s, %s\n" % (root, f, fileprefix, filepostfix))
            currentname = os.path.join(root, f)
            if (currentname.endswith(filepostfix) and currentname.find(fileprefix) != -1):
                command = srcommand + ' --find ' + srcname + ' --replace ' + destname + ' --caseSensitive ' + \
                                          ' --dir ' + root + ' --fileMask ' + filerule
                sys.stdout.write("command, %s, %s, %s\n" % (command, root, currentname))
                result = subprocess.Popen(command, cwd=root, stdout=subprocess.PIPE).communicate()[0]
                sys.stdout.write("command, %s\n" % (result))
                

def usage_and_exit(errmsg=None):
    stream = errmsg and sys.stderr or sys.stdout
    msg = "python tlqjms.py "
    stream.write(msg)
    if errmsg:
        stream.write("ERROR: %s\n" % (errmsg))
    sys.exit(errmsg and 1 or 0)

def main():
    argc = len(sys.argv)
    if argc < 2:
        usage_and_exit()
    filename = sys.argv[1]
    
    src_dir = ''
    product_dir = ''
    with open(filename, 'rt') as f:
        for line in f:
            if (line.startswith('#') or line.startswith('*')):
                continue
            if (line.startswith('src_dir')):
                src_dir = line.split('|')[1].strip()
            if (line.startswith('product_dir')):
                product_dir = line.split('|')[1].strip()
            if (line.startswith('renamedir')):
                renamedir(product_dir, line.split('|')[1].strip(), line.split('|')[2].strip())
            if (line.startswith('renamefile')):
                renamefile(product_dir, line.split('|')[1].strip(), line.split('|')[2].strip())
            if (line.startswith('replacedir')):
                replacedir(product_dir, line.split('|')[1].strip(), line.split('|')[2].strip(), line.split('|')[3].strip())
            if (line.startswith('replacefile')):
                replacefile(product_dir, line.split('|')[1].strip(), line.split('|')[2].strip(), line.split('|')[3].strip())
                
    sys.stdout.write("%s\n" % ('finished.'))

if __name__ == "__main__":
    main()