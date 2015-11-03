# -*- coding: gbk -*-

import os
import re
import sys,subprocess
import tempfile
import time
import pysvn
import datetime
from fnmatch import fnmatch, fnmatchcase
import smtplib
import configparser

from email.mime.image import MIMEImage
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

urls = ['http://119.4.167.16:8080/svn/sailfish', 'http://119.4.167.16:8080/svn/Betta', 'http://119.4.167.16:8080/svn/tongcomfra', 'http://119.4.167.16:8080/svn/Cellular_CEP', 'http://119.4.167.16:8080/svn/TI_EDGE', 'http://119.4.167.16:8080/svn/Cellular_BAM', 'http://119.4.167.16:8080/svn/JMSV2']
authors = [['lanyong', 'liurl', 'chengwb'], ['yanym', 'liyin1'], ['chengcc', 'luohq'], ['tanjian', 'yaoli', 'huangqian'], ['hedb', 'sunlei'], [], [], []]
working_dirs = ['e:/dev/sailfish', 'e:/dev/betta', 'e:/dev/proxy_server', 'e:/dev/TI_CEP', 'e:/dev/TI_EDGE', 'e:/dev/BAM', 'e:/dev/JMSV2']

def get_login( realm, username, may_save ):
    return True, 'nostat', 'tongtech9211', False

def _get_raw_commits_by_url(url, author, date_range):
    client = pysvn.Client()
    client.callback_get_login = get_login
    start_revision = pysvn.Revision(pysvn.opt_revision_kind.date, 
                                    time.mktime(date_range[0].timetuple()))
    end_revision = pysvn.Revision(pysvn.opt_revision_kind.date, 
                                  time.mktime(date_range[1].timetuple()))
    print("log %s %s %s\n" % (url, start_revision, end_revision))
    raw_commits = client.log(url, 
                             revision_start=start_revision, 
                             revision_end=end_revision,
                             discover_changed_paths=True)

    # Need to double-check dates as the revision look-up method isn't 100% watertight
    filtered_raw_commits = []
    for commit in raw_commits:
        commit_datetime = datetime.datetime.fromtimestamp(commit.date)
        commit_author = commit.author
        if commit_datetime > date_range[0] and commit_datetime < date_range[1] and commit_author==author:
            filtered_raw_commits.append(commit)
            sys.stdout.write("got %d\n" % (commit.revision.number))
    return filtered_raw_commits

def _get_raw_commits_by_revision(url, author, start, end):
    client = pysvn.Client()
    client.callback_get_login = get_login
    start_revision = pysvn.Revision(pysvn.opt_revision_kind.number, start)
    end_revision = pysvn.Revision(pysvn.opt_revision_kind.number, end)
    #end_revision = pysvn.Revision( pysvn.opt_revision_kind.head )    
    
    print("log %s %s %s\n" % (url, start_revision, end_revision))
    raw_commits = client.log(url, 
                             revision_start=start_revision, 
                             revision_end=end_revision,
                             discover_changed_paths=True)

    # Need to double-check dates as the revision look-up method isn't 100% watertight
    filtered_raw_commits = []
    for commit in raw_commits:
        commit_datetime = datetime.datetime.fromtimestamp(commit.date)
        commit_author = commit.author
        if commit_author == author:
            filtered_raw_commits.append(commit)
            sys.stdout.write("got %d\n" % (commit.revision.number))
    return filtered_raw_commits

def _get_diff_by_revsion(url, working_directory, author, raw_commits):
    outputfile = 'summary_' + author + time.strftime('%Y-%m-%d-%H', time.gmtime()) + '.diff'
    return _get_diff_file_by_revsion(url, working_directory, author, raw_commits, outputfile)

def _get_diff_file_by_revsion(url, working_directory, author, raw_commits, outputfile):
    client = pysvn.Client()
    client.callback_get_login = get_login
    difffile = open(outputfile, 'wb')

    sys.stdout.write("diff directory: %s\n" % (working_directory))
    for commit in raw_commits:
        rev1 = pysvn.Revision(pysvn.opt_revision_kind.number, commit.revision.number-1)
        rev2 = pysvn.Revision(pysvn.opt_revision_kind.number, commit.revision.number)
        diff_log = None

        #diff_log = client.diff("f:/temp/user", url, revision1=rev1, revision2=rev2,
        #            recurse=True,ignore_ancestry=True,ignore_content_type=False,
        #            header_encoding='gbk', diff_deleted=True)
        svn_diff = subprocess.Popen('svn diff -r ' + str(rev1.number) + ':' + str(rev2.number), cwd=working_directory, stdout=subprocess.PIPE).communicate()[0]
        try:
            svn_diff = str(svn_diff, "gbk")
        except:
            svn_diff = str(svn_diff, "utf8", "ignore")
        difffile.write(bytes(svn_diff, 'utf8'))
        difffile.write(bytes('\r\n', 'utf8'))
    difffile.close()
    return None
"""
Index: api/src/Makefile
===================================================================
--- api/src/Makefile	(版本 303)
+++ api/src/Makefile	(版本 304)
@@ -6,10 +6,11 @@
 	-I../../cmq/include \
 	-I../../libs/apr-2.0/include \
 	-I../../libs/gtest/include \
+	-I../../protocol/src \
 	-I../../business/include \
 	-I../../libs/tlrt_zlog_linux/zlog-linux/src \
 	-I../../libs/tlrt_zlog_linux
-LOCLIBS=-L../../lib -lapr-2 -lzmq -lczmq -ltlrt_busi -lzlog -ltlrt_zlog
+LOCLIBS=-L../../lib -lapr-2 -lzmq -lczmq -ltlrt_busi -lzlog -ltlrt_zlog -lprotobuf-c

 SRC_OBJECT=rum_api.c
 SRC_UTEST = ut_main.cpp rum_api_ut.cpp rum_api_mock.cpp
 """
def _get_stat_by_diff(author):
    delete_count = 0
    test_delete_count = 0
    add_count = 0
    test_add_count = 0
    status = 0
    is_test = False
    found_aa = False
    filename = 'summary_' + author + time.strftime('%Y-%m-%d-%H', time.gmtime()) + '.diff'
    with open(filename, 'rt', encoding='utf8') as f:
        for line in f:
            if (line.startswith('Index:')):
                status = 1
                found_aa = False
                if (line.find('_test') != -1):
                    is_test = True
                else:
                    is_test = False
                continue
            if (line.startswith('===============================')):
                if (status == 1):
                    status = 2
                else:
                    sys.stdout.write("status error, Index: should by ==========, %s,%s\n" % (filename, line))
                    #return
                continue
            if (line.startswith('---')):
                if (status == 2):
                    status = 3
                else:
                    sys.stdout.write("status error, Index: should by ---, %s,%s\n" % (filename, line))
                    #return
                continue
            if (line.startswith('+++')):
                if (status == 3):
                    status = 4
                else:
                    sys.stdout.write("status error, Index: should by +++, %s,%s\n" % (filename,line))
                    #return
                continue
            if (line.startswith('@@')):
                if (status == 4 and found_aa == False):
                    status = 5
                    found_aa = True
                elif (found_aa):
                    # Nothing
                    i = 0
                else:
                    sys.stdout.write("status error, Index: should by @@, %s,%s\n" % (filename,line))
                    #return
                continue
            if (line.startswith('-')):
                if (status == 5):
                    if (is_test):
                        test_delete_count += 1
                        #sys.stdout.write("test delete, %s,%s\n" % (filename,line))
                    else:
                        delete_count += 1
                        #sys.stdout.write("real delete, %s,%s\n" % (filename,line))
                elif (status == 4):
                    i = 0
                else:
                    sys.stdout.write("status error, Index: should by -, %s,%s\n" % (filename,line))
                    #return
                continue
            if (line.startswith('+')):
                if (status == 5):
                    if (is_test):
                        test_add_count += 1
                        #sys.stdout.write("test add, %s,%s\n" % (filename,line))
                    else:
                        add_count += 1
                        #sys.stdout.write("real add, %s,%s\n" % (filename,line))
                elif (status == 4):
                    i = 0
                else:
                    sys.stdout.write("status error, Index: should by +, %s,%s\n" % (filename,line))
                    #return
                continue
    sys.stdout.write("stat result, %s, %d, %d, %d, %d\n" % (author, add_count, test_add_count, delete_count, test_delete_count))
    stat_result = [add_count, test_add_count, delete_count, test_delete_count]
    return stat_result

def usage_and_exit(errmsg=None):
    stream = errmsg and sys.stderr or sys.stdout
    msg = "python diffbyauthordate.py command[summary] url working_directory author"
    stream.write(msg)
    if errmsg:
        stream.write("ERROR: %s\n" % (errmsg))
    sys.exit(errmsg and 1 or 0)

def main():
    argc = len(sys.argv)
    if argc < 3:
        usage_and_exit('Not enough arguments.')
    subcommand = sys.argv[1]
    if subcommand == 'help':
        usage_and_exit()
    elif subcommand == 'help-format':
        msg = FORMAT_HELP.replace("__SCRIPTNAME__",
                                  os.path.basename(sys.argv[0]))
        sys.stdout.write(msg)
    elif subcommand == 'gendiff':
        if argc < 3:
            usage_and_exit('No viewspec file specified.')
        url = sys.argv[2] #"http://119.4.167.16:8080/svn/sailfish/trunk"
        working_directory = sys.argv[3]
        author = sys.argv[4]
        
        startdate = sys.argv[5]
        startdate = time.strptime(startdate,"%Y-%m-%d")                           
        start = datetime.datetime(startdate[0], startdate[1], startdate[2], 0, 0, 0)

        enddate = sys.argv[6]
        enddate = time.strptime(enddate,"%Y-%m-%d")                            #字符串转换成time类型
        end = datetime.datetime(enddate[0],enddate[1],enddate[2], 0, 0, 1)               #time
        
        outputfile = sys.argv[7]

        daterange = [start, end]
        raw_commits = _get_raw_commits_by_url(url, author, daterange)
        _get_diff_file_by_revsion(url, working_directory, author, raw_commits, outputfile)

    elif subcommand == 'summary':
        if argc < 3:
            usage_and_exit('No viewspec file specified.')
        url = sys.argv[2] #"http://119.4.167.16:8080/svn/sailfish/trunk"
        working_directory = sys.argv[3]
        author = sys.argv[4]
        if argc >= 6:
            date = sys.argv[5]
            date = time.strptime(date,"%Y-%m-%d")                            #字符串转换成time类型
            end = datetime.datetime(date[0],date[1],date[2], 23, 59, 59)               #time类型转换成datetime类型
        else:
            end = datetime.datetime.now()
        fiveday = datetime.timedelta(days=5)
        start0 = end - fiveday
        start = datetime.datetime(start0.date().year, start0.date().month, start0.date().day, 0, 0, 0)
        daterange = [start, end]
        raw_commits = _get_raw_commits_by_url(url, author, daterange)
        _get_diff_by_revsion(url, working_directory, author, raw_commits)
        _get_stat_by_diff(author)
    elif subcommand == 'statall':
        stat_names = []
        stat_results = []
        if argc >= 3:
            date = sys.argv[2]
            date = time.strptime(date,"%Y-%m-%d")                            #字符串转换成time类型
            end = datetime.datetime(date[0],date[1],date[2], 23, 59, 59)               #time类型转换成datetime类型
        else:
            end = datetime.datetime.now()
        if argc >= 4: #输入开始日期
            inputdays = sys.argv[3]
            interdays = datetime.timedelta(days=int(inputdays))
        else:
            interdays = datetime.timedelta(days=7)
        start0 = end - interdays
        start = datetime.datetime(start0.date().year, start0.date().month, start0.date().day, 0, 0, 0)
        daterange = [start, end]
        for index in range(len(urls)):
            repourl = urls[index]
            for repoauthor in authors[index]:
                raw_commits = _get_raw_commits_by_url(repourl, repoauthor, daterange)
                _get_diff_by_revsion(repourl, working_dirs[index], repoauthor, raw_commits)
                stat_result = _get_stat_by_diff(repoauthor)
                stat_names.append(repoauthor)
                stat_results.append(stat_result)
        #write to file
        summary_stat = open('summary_stat.txt', 'at')
        summary_stat.write(time.ctime())
        summary_stat.write('\n')
        for index in range(len(stat_names)):
            stat_author = stat_names[index]
            stat_result = stat_results[index]
            print("%s, %d, %d, %d, %d\n" % (stat_author, stat_result[0], stat_result[1], stat_result[2], stat_result[3]), file = summary_stat)
        summary_stat.close()
        
    else:
        usage_and_exit('Unknown subcommand "%s".' % (subcommand))
    sys.stdout.write("%s\n" % ('finished.'))

if __name__ == "__main__":
    main()