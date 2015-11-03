#!/usr/bin/python

# JIRA commit acceptance python client for SVN
# Author: istvan.vamosi@midori.hu
# $Id: jira-client.py 20980 2009-07-23 06:16:03Z dchui $

import os
import sys
import urlparse
import xmlrpclib

def handlebyjira(projectKey):
	# configure JIRA access
	# ("projectKey" can contain multiple comma-separated JIRA project keys like "projectKey = 'TST,ARP'".
	# If you specify multiple keys, the commit will be accepted if at least one project listed accepts it.
	# Or you can specify "projectKey = '*'" to force using the global commit acceptance settings if you don't
	# want to specify any exact project key.)
	jiraBaseURL = 'http://10.10.22.206:8081'
	jiraLogin = 'linjinga'
	jiraPassword = 'linjing'
	# projectKey = 'Proxy2'
	import sys
	reload(sys)
	sys.setdefaultencoding('utf8')
	
	# configure svnlook path
	svnlookPath = 'D:/program/subversion/bin/svnlook.exe'
	
	# get committer
	committer = ''
	try:
		f = os.popen(svnlookPath + ' author ' + sys.argv[2] + ' --transaction ' + sys.argv[3])
		committer = f.read()
		if f.close():
			raise 1
		committer = committer.rstrip("\n\r")
	except:
		print >> sys.stderr, 'Unable to get committer with svnlook.'
		sys.exit(1)
	
	#print >> sys.stderr, 'Commit.' + commiter
	if committer == 'nostat':
		print >> sys.stderr, 'nostat, Commit accepted.'
		sys.exit(0)

	# get commit message
	try:
		f = os.popen(svnlookPath + ' log ' + sys.argv[2] + ' --transaction ' + sys.argv[3])
		commitMessage = f.read()
		if f.close():
			raise 1
		commitMessage = commitMessage.rstrip('\n\r')
	except:
		print >> sys.stderr, 'Unable to get commit message with svnlook.'
		sys.exit(1)
	
	# print arguments
	print >> sys.stderr, 'Committer: ' + committer
	print >> sys.stderr, 'Commit message: "' + commitMessage + '"'
	
	# invoke JIRA web service
	xmlrpcUrl = jiraBaseURL + '/rpc/xmlrpc'
	try:
		s = xmlrpclib.ServerProxy(xmlrpcUrl)
		acceptance, comment = s.commitacc.acceptCommit(jiraLogin, jiraPassword, committer, projectKey, commitMessage).split('|');
	except:
		acceptance, comment = ['false', 'Unable to connect to the JIRA server at "' + jiraBaseURL + '".']
	
	if acceptance == 'true':
		print >> sys.stderr, 'Commit accepted.'
		sys.exit(0)
	else:
		print >> sys.stderr, 'Commit rejected: ' + comment
		sys.exit(1)

def usage_and_exit(errmsg=None):
	stream = errmsg and sys.stderr or sys.stdout
	msg = "python to_pomxml.py "
	stream.write(msg)
	if errmsg:
		stream.write("ERROR: %s\n" % (errmsg))
	sys.exit(errmsg and 1 or 0)


def main():
	argc = len(sys.argv)
	if argc < 2:
		usage_and_exit('Not enough arguments.')
	projectkey = sys.argv[1]
	handlebyjira(projectkey)
	sys.stdout.write("%s\n" % ('finished.'))
	
if __name__ == "__main__":
	main()