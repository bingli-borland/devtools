import requests, json
import sys
import os
import sys

base_url = "http://10.10.22.206:8060/rest-service/reviews-v1"
svnlookPath = 'D:/program/subversion/bin/svnlook.exe'
 
def statusbysvnlog(projectkey, repopath, txid):
    # get committer
    committer = ''
    try:
        f = os.popen(svnlookPath + ' author ' + repopath + ' --transaction ' + txid)
        committer = f.read()
        if f.close():
            raise 1
        committer = committer.rstrip("\n\r")
    except:
        print('Unable to get committer with svnlook.', file=sys.stderr)
        sys.exit(1)

    #print ('Committer: ' + committer, file=sys.stderr)
    if committer == 'nostat':
        print ('Committer: ' + committer, file=sys.stderr)
        sys.exit(0)
    # get commit message
    try:
        f = os.popen(svnlookPath + ' log ' + repopath + ' --transaction ' + txid)
        commitMessage = f.read()
        if f.close():
            raise 1
        commitMessage = commitMessage.rstrip('\n\r')
    except:
        print('Unable to get commit message with svnlook.', file=sys.stderr)
        sys.exit(1)
    
    # print arguments
    print ('Committer: ' + committer, file=sys.stderr)
    print ('Commit message: "' + commitMessage + '"', file=sys.stderr)
    idpos = commitMessage.find(projectkey + '-')
    print ('idpos: "' + str(idpos) + '"', file=sys.stderr)
    if (idpos != -1):
        idpos0 = idpos + len(projectkey + '-')
        while(idpos0 < len(commitMessage) and commitMessage[idpos0].isdigit()):
            print ('review id: "' + str(idpos0) + ', ' + str(len(commitMessage)) + '"', file=sys.stderr)
            idpos0 = idpos0 + 1
        id = commitMessage[idpos:idpos0]      
        print ('review id: "' + id + '"', file=sys.stderr)
        result = status_review(id)
        print ('1:' + str(result), file=sys.stderr)
        if (result != 0):
            idpos0 = idpos + len(projectkey + '-')
            print ('info : "' + projectkey + '"' + commitMessage, file=sys.stderr)
            if (idpos0 >= len(commitMessage)):
                print ('cannot find : "' + projectkey + '"' + commitMessage, file=sys.stderr)
                return 7
            submessage = commitMessage[idpos0:]      
            print ('info : "' + projectkey + '"' + submessage, file=sys.stderr)
            idpos = submessage.find(projectkey + '-')
            if (idpos != -1):
                idpos0 = idpos + len(projectkey + '-')
                while(idpos0 < len(submessage) and submessage[idpos0].isdigit()):
                    print ('review id: "' + str(idpos0) + ', ' + str(len(submessage)) + '"', file=sys.stderr)
                    idpos0 = idpos0 + 1
                id = submessage[idpos:idpos0]      
                print ('review id: "' + id + '"', file=sys.stderr)
                return status_review(id)
            else:
                return 5
        else:
            sys.stderr.write("2\n")
            return 0
    else:
        return 6

def status_review(id):
    data = json.dumps({'Accept':'application/json'}) 
    r = requests.get(base_url + '/' + id)   
    sys.stderr.write("!!!!!!get line: %s, %s, %s\n" % (r, r.json, r.content))
    if (r.status_code == 200 or r.status_code == 304):
        content = str(r.content)
        pos1 = content.find('<state>')
        pos2 = content.find('</state>')
        state = content[pos1 + len('<state>') : pos2]
        if (state.lower() == 'closed'):
            sys.stderr.write("ok.\n")
            return 0
        else:
            print ('id right, status error', file=sys.stderr)
            return 1
    else:
        print (sys.stderr, 'status code error', file=sys.stderr)
        return 2
 
def usage_and_exit(errmsg=None):
    stream = errmsg and sys.stderr or sys.stdout
    msg = "python statusbyreview status projectkey %1 %2"
    stream.write(msg)
    if errmsg:
        stream.write("ERROR: %s\n" % (errmsg))
    sys.exit(2)    
    
def main():
    argc = len(sys.argv)
    if argc < 2:
        usage_and_exit('Not enough arguments.')
    subcommand = sys.argv[1]
    if subcommand == 'help':
        usage_and_exit()
    elif subcommand == 'status':
        if argc < 5:
            usage_and_exit('No viewspec file specified.')
        projectkey = sys.argv[2]
        result = statusbysvnlog(projectkey, sys.argv[3], sys.argv[4])
        sys.stderr.write("3\n")
        if (result == 0):
            sys.stderr.write("4\n")
        else:
            print (sys.stderr, 'review fail: "' + projectkey + '"' , file=sys.stderr)
            sys.exit(result)
    else:
        usage_and_exit('Unknown subcommand "%s".' % (subcommand))
    sys.stderr.write("%s\n" % ('finished.'))
    return 0

if __name__ == "__main__":
    main()