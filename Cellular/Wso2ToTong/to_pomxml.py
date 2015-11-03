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
E:\dev\Cellular\Platform\dependencies\hive\0.8.1-wso2v6\build.properties-->wso2v6改为tongv6, 否则orbit/hive编译失败
E:\dev\Cellular\Platform\dependencies\hadoop\0.20.203.1-wso2v4\build.xml, wso2v4改为tongv4
'''
'''
F:\_MY\.m2\repository\com\tongtech\cellular\com.tongtech.cellular.jaxws.webapp.mgt.server.feature\4.1.0, 
    feature的目录名修改为com.tongtech.cellular, 
    feature.xml的id改为com.tongtech.cellular
F:\_MY\.m2\repository\com\tongtech\cellular\com.tongtech.cellular.webapp.mgt.server.feature\4.1.0, 
F:\_MY\.m2\repository\com\tongtech\cellular\com.tongtech.cellular.logging.mgt.feature\4.1.0
F:\_MY\.m2\repository\com\tongtech\cellular\com.tongtech.cellular.statistics.transport.feature\4.1.0
'''
#部署本地文件到服务器. 
def deploy_directory_tomvn():
    repodir = 'F:\\_MY\\.m2\\repository.1\\a_test\\'
    for root, dirs, files in os.walk(repodir): # Walk directory tree
        for f in files:
            packageroot = root.replace(repodir, '').strip()
            manychar = packageroot.split('\\')
            version = manychar[len(manychar)-1]
            artifactid = manychar[len(manychar)-2]
            groupid = ''
            for i in range(len(manychar)-2):
                groupid = groupid + manychar[i]
                if (i != len(manychar) - 3):
                    groupid = groupid + '.'

            if ((f.endswith('.jar') or f.endswith('.zip') or f.endswith('.mex'))and \
                ((groupid.find('com.tongtech') != -1 or artifactid.find('com.tongtech') != -1 or (version.find('tongv') != -1)))):
                if (f.endswith('-sources.jar')):
                    continue
                #找到对应的pom文件
                if (f.endswith('.jar')):
                    pomfile = f.replace('.jar', '.pom')
                    packaging = 'jar'
                elif (f.endswith('.zip')):
                    pomfile = f.replace('.zip', '.pom')
                    packaging = 'zip'
                elif (f.endswith('.mex')):
                    pomfile = f.replace('.mex', '.pom')
                    packaging = 'mex'
                    
                '''mvncommand = 'D:/Devtools/apache-maven/bin/mvn.bat install:install-file -DpomFile=' + root + '\\pom.xml ' + \
                    ' -Dfile=' + root + '\\' + f + \
                    ' -DgroupId=' + groupid + ' -DartifactId=' + artifactid + ' -Dversion=' + version + ' -Dpackaging=' + packaging
                sys.stdout.write("get command: %s\n" % (mvncommand))
                subprocess.call(mvncommand)'''
                
                if (os.path.isfile(root + '\\' + pomfile) == False):
                    oldpomfile = pomfile
                    pos1 = pomfile.rfind('-')
                    pomfile = pomfile[0:pos1]
                    pomfile = pomfile + '.pom'
                    if (os.path.isfile(root + '\\' + pomfile) == False):
                        sys.stdout.write("!!!pom file fail: %s, %s\n" % (oldpomfile, pomfile))
                        continue

                mvncommand = 'D:/Devtools/apache-maven/bin/mvn.bat deploy:deploy-file -DpomFile=' + root + '\\' + pomfile + ' ' + \
                    ' -Dfile=' + root + '\\' + f + \
                    ' -DrepositoryId=Releases' + \
                    ' -DgroupId=' + groupid + ' -DartifactId=' + artifactid + ' -Dversion=' + version + ' -Dpackaging=' + packaging + \
                    ' -Durl=http://10.10.22.206:8090/nexus/content/repositories/releases/'
                sys.stdout.write("get command: %s\n" % (mvncommand))
                subprocess.call(mvncommand)
            elif (f.endswith('.pom') and  \
                ((groupid.find('com.tongtech') != -1 or artifactid.find('com.tongtech') != -1 or (version.find('tongv') != -1)))):
                jarfilename = f.replace('.pom', '.jar')
                zipfilename = f.replace('.pom', '.zip')
                mexfilename = f.replace('.pom', '.mex')
                if (os.path.isfile(root + '\\' + jarfilename) or os.path.isfile(root + '\\' + zipfilename) or os.path.isfile(root + '\\' + mexfilename)):
                    continue #等待上面的处理或者已经处理过了
                '''
                mvncommand = 'D:/Devtools/apache-maven/bin/mvn.bat install:install-file ' + ' -Dfile=' + root + '\\' + f
                    #' -DgroupId=' + groupid + ' -DartifactId=' + artifactid + ' -Dversion=' + version + ' -Dpackaging=' + packaging
                sys.stdout.write("get command: %s\n" % (mvncommand))
                subprocess.call(mvncommand)
                '''
                mvncommand = 'D:/Devtools/apache-maven/bin/mvn.bat deploy:deploy-file ' + \
                    ' -Dfile=' + root + '\\' + f + \
                    ' -DrepositoryId=Releases' + \
                    ' -DgroupId=' + groupid + ' -DartifactId=' + artifactid + ' -Dversion=' + version + ' -Dpackaging=' + 'pom' + \
                    ' -Durl=http://10.10.22.206:8090/nexus/content/repositories/releases/'
                sys.stdout.write("get command: %s\n" % (mvncommand))
                subprocess.call(mvncommand)
            elif (f.endswith('.repositories') or f.endswith('.lastUpdated') or f.endswith('.xml') or f.endswith('.sha1') or f.endswith('.properties') or \
                  f.endswith('.md5') or \
                  f.endswith('.zip') or f.endswith('.jar') or f.endswith('.mex') or f.endswith('.pom')):
                i = 0
            else:
                sys.stdout.write("!!!UNKNDOWN PREFIX %s\n" % (f))


def checkout_lib_install():
    packet_file = 'F:/_Backup/study/Wso2/wso2_packet.txt'
    working_directory = 'F:/_Backup/study/Wso2/maven_wso2_org/'
    repo_directory = 'F:/_MY/.m2/repository/'
    curl = 'http://maven.wso2.org/nexus/content/groups/wso2-public/'
    #abdera = '[org.apache.abdera:abdera-client:jar:1.0-tongv2 (compile?), org.apache.abdera:abdera-core:jar:1.0-tongv2 (compile?), org.apache.abdera:abdera-parser:jar:1.0-tongv2 (compile?), org.apache.abdera:abdera-server:jar:1.0-tongv2 (compile?), org.apache.abdera:abdera-i18n:jar:1.0-tongv2 (compile?)]'
    with open(packet_file, 'rt') as f:
        for line in f:
            if (line.startswith('---')):
                continue
            abdera = line.strip() #'[org.apache.abdera:abdera-client:jar:1.0-tongv2 (compile?)]'
            abdera = abdera.replace('[', '')
            abdera = abdera.replace(']', '')
            #abdera = abdera.replace('tong', 'wso2')
            words = abdera.split(',')  
            for word in words:    
                is_reserve = False
                pos = word.find('(')
                if (pos != -1):
                    repos = word.split('(')
                    repocontent = repos[0]
                else:
                    repocontent = word
                sys.stdout.write("get content: %s\n" % (repocontent))
                if (repocontent.find('***') != -1):
                    is_reserve = True
                    repocontent = repocontent.replace('***', '')
                repopath = repocontent.split(':')
                #mvn install:install-file -Dfile=<path-to-file> -DgroupId=<group-id> -DartifactId=<artifact-id> -Dversion=<version> -Dpackaging=<packaging>        
                classifier = None
                if (len(repopath) == 4):
                    groupid = repopath[0].strip()
                    artifactid = repopath[1].strip()
                    packaging = repopath[2].strip()
                    version = repopath[3].strip()   
                elif (len(repopath) == 5):
                    groupid = repopath[0].strip()
                    artifactid = repopath[1].strip()
                    packaging = repopath[2].strip()
                    classifier = repopath[3].strip()   
                    version = repopath[4].strip()   
                else:
                    sys.stdout.write("!!!ERROR!!!%s\n" % (repocontent))
                    return
                if (classifier == None):
                    filename = artifactid + '-' + version + '.' + packaging
                else:
                    filename = artifactid + '-' + version + '-' + classifier + '.' + packaging
                groupidpath = groupid.replace('.', '/')
                fullname = repo_directory + groupidpath + '/' + artifactid + '/' + version + '/' + filename
                if (os.path.isfile(fullname)):
                    sys.stdout.write("!!!file exist: %s\n" % (fullname))
                    continue #文件存在, 忽略
                groupid = groupid.replace('com.tongtech.cellular', 'org.wso2.carbon')
                groupid = groupid.replace('com.tongtech', 'org.wso2')
                groupid = groupid.replace('tong', 'wso2')
                artifactid = artifactid.replace('com.tongtech.cellular', 'org.wso2.carbon')
                artifactid = artifactid.replace('com.tongtech', 'org.wso2')
                artifactid = artifactid.replace('tong', 'wso2')
                version = version.replace('tong', 'wso2')
                groupid = groupid.replace('.', '/')
                if (packaging != 'pom'):
                    if (classifier == None):
                        filename = artifactid + '-' + version + '.' + packaging
                    else:
                        filename = artifactid + '-' + version + '-' + classifier + '.' + packaging
                    filenamepom = artifactid + '-' + version + '.pom'
                    command = 'E:/Tools/TOOLS/curl.exe -o ' + working_directory + filename + ' ' + curl + groupid + '/' + artifactid + '/' + version + '/' + filename
                    commandpom = 'E:/Tools/TOOLS/curl.exe -o ' + working_directory + filenamepom + ' ' + curl + groupid + '/' + artifactid + '/' + version + '/' + filenamepom
                    sys.stdout.write("get command: %s,%s\n" % (command,commandpom))
                    #if (os.path.isfile(working_directory + filename) == False or os.path.isfile(working_directory + filenamepom) == False):
                    subprocess.call(command) #如果放到windows/system32目录,则报错.说找不到.可能是权限问题
                    subprocess.call(commandpom)
            
                    groupid = groupid.replace('/', '.')
                    if (is_reserve == False):
                        groupid = groupid.replace('org.wso2.carbon', 'com.tongtech.cellular')
                        groupid = groupid.replace('org.wso2', 'com.tongtech')
                        groupid = groupid.replace('wso2', 'tong')
                        artifactid = artifactid.replace('org.wso2.carbon', 'com.tongtech.cellular')
                        artifactid = artifactid.replace('org.wso2', 'com.tongtech')
                        #artifactid = artifactid.replace('wso2', 'tong')
                        version = version.replace('wso2', 'tong')
                    
                        generate_pom(working_directory + filenamepom, working_directory, filenamepom, 'checkout')
                    else:
                        shutil.copy(working_directory + filenamepom, working_directory + 'pom.xml')
                    if (groupid == 'com.tongtech.cellular' and version == '4.0.0'):
                        version = '4.1.0'
                    mvncommand = 'D:/Devtools/apache-maven/bin/mvn.bat install:install-file -DpomFile=' + working_directory + 'pom.xml ' + ' -Dfile=' + working_directory + filename + \
                        ' -DgroupId=' + groupid + ' -DartifactId=' + artifactid + ' -Dversion=' + version + ' -Dpackaging=' + packaging
                    #mvncommand = 'D:/Devtools/apache-maven/bin/mvn.bat install:install-file ' + ' -Dfile=' + working_directory + filename + \
                    #    ' -DgroupId=' + groupid + ' -DartifactId=' + artifactid + ' -Dversion=' + version + ' -Dpackaging=' + packaging
                    if (classifier != None):
                        mvncommand = mvncommand + ' -Dclassifier=' + classifier
                    sys.stdout.write("get command: %s\n" % (mvncommand))
                    subprocess.call(mvncommand)
    
                    mvncommand = 'D:/Devtools/apache-maven/bin/mvn.bat deploy:deploy-file -DpomFile=' + working_directory + 'pom.xml' + ' ' + \
                        ' -Dfile=' + working_directory + filename + \
                        ' -DrepositoryId=Releases' + \
                        ' -Dversion=' + version + \
                        ' -Durl=http://10.10.22.206:8090/nexus/content/repositories/releases/'
                    sys.stdout.write("get command: %s\n" % (mvncommand))
                    #subprocess.call(mvncommand)
                    
                    if (artifactid.endswith('.feature') and packaging == 'zip'):
                        groupid12 = groupid.replace('.', '/')
                        handle_feature(repo_directory + groupid12 + '/', artifactid, version)
                elif (packaging == 'pom'):
                    filename = artifactid + '-' + version + '.' + packaging
                    command = 'E:/Tools/TOOLS/curl.exe -o ' + working_directory + filename + ' ' + curl + groupid + '/' + artifactid + '/' + version + '/' + filename
                    sys.stdout.write("get command: %s\n" % (command))
                    subprocess.call(command) #如果放到windows/system32目录,则报错.说找不到.可能是权限问题
            
                    groupid = groupid.replace('/', '.')
                    groupid = groupid.replace('org.wso2.carbon', 'com.tongtech.cellular')
                    groupid = groupid.replace('org.wso2', 'com.tongtech')
                    groupid = groupid.replace('wso2', 'tong')
                    artifactid = artifactid.replace('org.wso2.carbon', 'com.tongtech.cellular') #与下面的一致即可
                    artifactid = artifactid.replace('org.wso2', 'com.tongtech')
                    #if (artifactid.find('wso2eventing') == -1):
                    #    artifactid = artifactid.replace('wso2', 'tong')
                    version = version.replace('wso2', 'tong')
                    
                    generate_pom(working_directory + filename, working_directory, filename, 'checkout')
                    mvncommand = 'D:/Devtools/apache-maven/bin/mvn.bat install:install-file ' + ' -Dfile=' + working_directory + 'pom.xml' + \
                        ' -DgroupId=' + groupid + ' -DartifactId=' + artifactid + ' -Dversion=' + version + ' -Dpackaging=' + packaging
                    sys.stdout.write("get command: %s\n" % (mvncommand))
                    subprocess.call(mvncommand)
    
                    mvncommand = 'D:/Devtools/apache-maven/bin/mvn.bat deploy:deploy-file ' + \
                        ' -Dfile=' + working_directory + 'pom.xml' + \
                        ' -DrepositoryId=Releases' + \
                        ' -DgroupId=' + groupid + ' -DartifactId=' + artifactid + ' -Dversion=' + version + ' -Dpackaging=' + packaging + \
                        ' -Durl=http://10.10.22.206:8090/nexus/content/repositories/releases/'
                    sys.stdout.write("get command: %s\n" % (mvncommand))
                    #subprocess.call(mvncommand)

def modify_p2_profile():
    baserepo = 'F:/_MY/.m2/repository/com/tongtech/cellular/'
    p2xml = 'F:/_Backup/study/Wso2/pom.bam.xml'
    version = '4.1.0'
    parent_groupid = ['org.wso2.carbon', 'com.tongtech.cellular']
    is_feature = False
    is_featureArtifacts = False
    with open(p2xml, 'rt') as infile:
        irofile = iter(infile)
        for line in infile:
            stripline = line.strip()     
            if (stripline.endswith('<!--IGNORE-->')):
                continue
            elif (stripline.startswith('<feature>')):
                is_feature = True
            elif (stripline.startswith('</feature>')):
                is_feature = False
            else:
                if (is_feature and stripline.startswith('<id>')):
                    stripline = stripline.replace('<id>', '')
                    stripline = stripline.replace('</id>', '')
                    stripline = stripline.strip();
                    stripline = stripline.replace('.group', '')
                    nextline = next(irofile)
                    if (nextline.find('stratos.platform.patch.version') != -1 or nextline.find('stratos.version') != -1):
                        version = '2.1.0'
                    elif (stripline.find('com.tongtech.cellular.cassandra') != -1):
                        version = '4.1.1'
                    else:
                        version = '4.1.0'
                    handle_feature(baserepo, stripline, version)
                        
def handle_feature(baserepo, stripline, version):
    workingdir = baserepo + stripline + '/' + version + '/'
    workingfile = workingdir + stripline + '-' + version + '.zip'
    file = zipfile.ZipFile(workingfile, "r")
    zipdir = 'f:/TEMP/cellular.tong.temp/' + 'cep/' + stripline + '/'
    zextract = 'c:/program files/7-zip/7z.exe' + ' x ' + workingfile + ' -o' + zipdir + ' -y'
    sys.stdout.write("get zextract: %s\n" % (zextract))
    subprocess.call(zextract) #解包
    sys.stdout.write("get zipdir + 'features': %s\n" % (zipdir + 'features'))
    for root, dirs, files in os.walk(zipdir + 'features'): # Walk directory tree
        for mydir in dirs:
            #if (mydir.find('org.wso2.carbon') == -1):#不是需要处理的目录
            #    continue
            sys.stdout.write("get mydir: %s\n" % (mydir))
            featuredir = zipdir + 'features' + '/' + mydir.replace('org.wso2.carbon', 'com.tongtech.cellular')
            sys.stdout.write("get featuredir: %s\n" % (featuredir))
            if (featuredir.find('com.tongtech') == -1):
                sys.stdout.write("!!!ERROR!!!: %s\n" % (featuredir))
                continue                        
            #if (os.path.isdir(featuredir) == False): #不存在
            src_featuredir = zipdir + 'features' + '/' + mydir.replace('com.tongtech.cellular', 'org.wso2.carbon')
            sys.stdout.write("get src_featuredir: %s\n" % (src_featuredir))
            is_exist_src_featuredir = True
            if (os.path.isdir(src_featuredir) == False):
                sys.stdout.write("!!!not exist wso feature dir: %s\n" % (src_featuredir))
                is_exist_src_featuredir = False
                #continue
            if (is_exist_src_featuredir):
                shutil.rmtree(featuredir,  ignore_errors=True)
                #os.makedirs(featuredir)
                shutil.copytree(src_featuredir, featuredir)
                shutil.rmtree(src_featuredir,  ignore_errors=True)
            if (os.path.isdir(featuredir)): #存在了正确的包
                #打开文件
                featurefile = featuredir + '/' + 'feature.xml'
                sys.stdout.write("get featurefile: %s\n" % (featurefile))
                featurefile_tong = open(featurefile + '.tong', 'wt')
                with open(featurefile, 'rt') as featuref:
                    for featureline in featuref:
                        featurestripline = featureline.strip() ;
                        #sys.stdout.write("get feature featureline: %s\n" % (featurestripline))
                        if (featurestripline.startswith('<feature')):
                            featureline = featureline.replace('org.wso2.carbon', 'com.tongtech.cellular')
                        if (featurestripline.startswith('<plugin')):
                            featureline = featureline.replace('org.wso2.carbon', 'com.tongtech.cellular')
                        if (featurestripline.startswith('<import')):
                            featureline = featureline.replace('org.wso2.carbon', 'com.tongtech.cellular')
                        if (featurestripline.startswith('<includes')):
                            featureline = featureline.replace('org.wso2.carbon', 'com.tongtech.cellular')
                        #sys.stdout.write("after get feature featureline: %s\n" % (featureline))
                        featurefile_tong.write(featureline)
                featurefile_tong.close()
                shutil.move(featurefile, featurefile + '.bak')
                shutil.move(featurefile + '.tong', featurefile)

    #处理plungins目录
    pluginsdir = zipdir + 'plugins' + '/'
    for root, dirs, files in os.walk(pluginsdir): # Walk directory tree
        for f in files:
            sys.stdout.write("plugin f: %s\n" % (f))
            newfilename = f.replace('org.wso2.carbon', 'com.tongtech.cellular')
            newjarname = pluginsdir + newfilename
            if (f.endswith('.jar') and f.find('org.wso2') != -1):
                shutil.move(pluginsdir + f, newjarname)
            
            if (newjarname.endswith('.jar')):
                newjardir = 'f:/TEMP/cellular.tong.temp/tempjar/'                    
                zextract = 'c:/program files/7-zip/7z.exe' + ' x ' + newjarname + ' -o' + newjardir + ' -y'
                sys.stdout.write("get zextract: %s\n" % (zextract))
                subprocess.call(zextract) #解包
                    
                manifestfile = newjardir + '/' + 'META-INF/MANIFEST.MF'
                sys.stdout.write("get manifestfile: %s\n" % (manifestfile))
                if (os.path.isfile(manifestfile)): #处理manifest文件
                    manifestfile_tong = open(manifestfile + '.tong', 'wt')
                    is_import_package = False
                    with open(manifestfile, 'rt') as manifestf:
                        for manifestline in manifestf:
                            manifesttripline = manifestline.strip() ;
                            sys.stdout.write("get manifesttripline: %s\n" % (manifesttripline))
                            if (manifesttripline.startswith('Bundle-Name')):
                                manifestline = manifestline.replace('org.wso2.carbon', 'com.tongtech.cellular')
                            if (manifesttripline.startswith('Bundle-SymbolicName')):
                                manifestline = manifestline.replace('org.wso2.carbon', 'com.tongtech.cellular')
                            if (manifesttripline.startswith('Import-Package')):
                                is_import_package = True
                            #Fragment-Host: org.wso2.carbon.cep.core
                            if (manifesttripline.startswith('Fragment-Host')):
                                manifestline = manifestline.replace('org.wso2.carbon', 'com.tongtech.cellular')
                            if (is_import_package):
                                manifestline = manifestline.replace('wso2v', 'tongv')
                            sys.stdout.write("after get feature manifestline: %s\n" % (manifestline))
                            manifestfile_tong.write(manifestline)
                            
                    
                    manifestfile_tong.close()
                    shutil.move(manifestfile, manifestfile + '.bak')
                    shutil.move(manifestfile + '.tong', manifestfile)
                    zextract = 'c:/program files/7-zip/7z.exe' + ' a -tzip -y ' + newjardir + 'tong.temp.zip' + ' ' + newjardir + '/*'
                    sys.stdout.write("get zextract: %s\n" % (zextract))
                    subprocess.call(zextract)
                    shutil.copy(newjardir + 'tong.temp.zip', newjarname)
                    shutil.rmtree(newjardir, ignore_errors=True)
    if (os.path.isfile(zipdir + 'tong.temp.zip')):
        os.remove(zipdir + 'tong.temp.zip')
    zextract = 'c:/program files/7-zip/7z.exe' + ' a -tzip -y ' + zipdir + 'tong.temp.zip' + ' ' + zipdir + '/*'
    sys.stdout.write("get zextract: %s\n" % (zextract))
    subprocess.call(zextract)
    shutil.move(zipdir + 'tong.temp.zip', workingfile)
    #shutil.rmtree(zipdir, ignore_errors=True)


def extract_all():
    p2xml = 'E:/dev/Cellular/Platform.new/products/cep/2.1.0/modules/p2-profile/pom1.xml'
    baserepo = 'F:/_MY/.m2/repository/com/tongtech/cellular/'
    version = '4.1.0'
    is_feature = False
    is_featureArtifacts = False
    with open(p2xml, 'rt') as infile:
        irofile = iter(infile)
        for line in infile:
            stripline = line.strip()     
            if (stripline.startswith('<featureArtifactDef>')):
                is_feature = True
            elif (stripline.startswith('</featureArtifactDef>')):
                is_feature = False
            else:
                if (is_feature):
                    striplines = stripline.split(':')
                    stripline = striplines[1]
                    if (striplines[2].find('stratos.platform.patch.version') != -1 or striplines[2].find('stratos.version') != -1):
                        version = '2.1.0'
                    elif (stripline.find('com.tongtech.cellular.cassandra') != -1):
                        version = '4.1.1'
                    else:
                        version = '4.1.0'
                    if (striplines[0] == 'com.tongtech.cep'):
                        baserepo = 'F:/_MY/.m2/repository/com/tongtech/cep/'
                    workingdir = baserepo + stripline + '/' + version + '/'
                    workingfile = workingdir + stripline + '-' + version + '.zip'
                    file = zipfile.ZipFile(workingfile, "r")
                    zipdir = 'f:/TEMP/cellular.tong.temp/' + 'cep/' + stripline + '/'
                    extract_zipfile(zipdir, workingfile)

def extract_taglib(tag):
    packet_file = 'F:/_Backup/study/Wso2/wso2_packet.txt'
    working_directory = 'f:/TEMP/cellular.tong.temp/' + tag + '/'
    repo_directory = 'F:/_MY/.m2/repository/'
    with open(packet_file, 'rt') as f:
        for line in f:
            #if (line.startswith('---')):
            #    continue
            line = line.replace('-', '')
            abdera = line.strip() #'[org.apache.abdera:abdera-client:jar:1.0-tongv2 (compile?)]'
            abdera = abdera.replace('[', '')
            abdera = abdera.replace(']', '')
            words = abdera.split(',')  
            for word in words:    
                is_reserve = False
                pos = word.find('(')
                if (pos != -1):
                    repos = word.split('(')
                    repocontent = repos[0]
                else:
                    repocontent = word
                sys.stdout.write("get content: %s\n" % (repocontent))
                repopath = repocontent.split(':')
                classifier = None
                if (len(repopath) == 4):
                    groupid = repopath[0].strip()
                    artifactid = repopath[1].strip()
                    packaging = repopath[2].strip()
                    version = repopath[3].strip()   
                elif (len(repopath) == 5):
                    groupid = repopath[0].strip()
                    artifactid = repopath[1].strip()
                    packaging = repopath[2].strip()
                    classifier = repopath[3].strip()   
                    version = repopath[4].strip()   
                else:
                    sys.stdout.write("!!!ERROR!!!%s\n" % (repocontent))
                    continue
                if (classifier == None):
                    filename = artifactid + '-' + version + '.' + packaging
                else:
                    filename = artifactid + '-' + version + '-' + classifier + '.' + packaging
                groupidpath = groupid.replace('.', '/')
                fullname = repo_directory + groupidpath + '/' + artifactid + '/' + version + '/' + filename
                if (os.path.isfile(fullname)):
                    if (artifactid.endswith('.feature') and packaging == 'zip'):
                        extract_zipfile(working_directory + filename.replace('.zip', '') + '/', fullname)
                    
                    
def extract_zipfile(zipdir, workingfile):                    
    shutil.rmtree(zipdir, ignore_errors=True)
    zextract = 'c:/program files/7-zip/7z.exe' + ' x ' + workingfile + ' -o' + zipdir + ' -y'
    sys.stdout.write("get zextract: %s\n" % (zextract))
    subprocess.call(zextract) #解包
    sys.stdout.write("get zipdir + 'features': %s\n" % (zipdir + 'features'))
    for root, dirs, files in os.walk(zipdir + 'features'): # Walk directory tree
        for mydir in dirs:
            i = 0

    #处理plungins目录
    pluginsdir = zipdir + 'plugins' + '/'
    for root, dirs, files in os.walk(pluginsdir): # Walk directory tree
        for f in files:
            sys.stdout.write("plugin f: %s\n" % (f))
            newfilename = f
            newjarname = pluginsdir + newfilename
            
            if (newjarname.endswith('.jar')):
                newjardir = root + f.replace('.jar', '')                    
                zextract = 'c:/program files/7-zip/7z.exe' + ' x ' + newjarname + ' -o' + newjardir + ' -y'
                sys.stdout.write("get zextract: %s\n" % (zextract))
                subprocess.call(zextract) #解包                                    

def generate_pom(path, filepath, filename, command):
    parent_groupid = ['org.wso2.carbon', 'com.tongtech.cellular']
    parent_relativePath = ['pom.xml', 'pom_tong.xml']
    artifactId0 = ['org.wso2.carbon', 'com.tongtech.cellular']
    artifactId1 = ['org.wso2', 'com.tongtech']
    groupid0 = ['org.wso2', 'com.tongtech']
    groupid1 = ['.wso2', '.tong']
    name0 = ['.wso2', '.tong']
    name1 = ['WSO2 Carbon', 'TONGTECH cellular']
    version = ['wso2', 'tong']
    url = ['wso2.org', 'tongtech.com']
    if (command == 'tongtech'):
        if (os.path.isfile(path + '.src')):
            shutil.copy(path + '.src', path)
        else:
            shutil.copy(path, path + '.src')
    is_parent = False
    is_description = False
    is_properties = False
    is_dependencies = False
    is_repositories = False
    found_relativePath = False
    is_modules = False
    is_execute = False
    is_bundles = False
    is_includedFeatures = False
    is_featureArtifacts = False
    is_featureGeeration = False
    is_features = False
    is_maven_bundle = False
    pom_tong = open(filepath + '/' + 'pom_tong.xml', 'wt')
    with open(path, 'rt') as f:
        for line in f:
            stripline = line.strip()     
            if (stripline.startswith('~')):
                continue
            if (stripline.endswith('<!--PRESERVE-->')):
                pom_tong.write(line)
                continue
            elif (stripline.startswith('<parent>')):
                is_parent = True
            elif (stripline.startswith('</parent>')):
                is_parent = False
                '''
                if (found_relativePath == False):
                    if (os.path.isfile(filepath + '/../' + 'pom.xml')):
                        pom_tong.write('    <relativePath>../pom.xml</relativePath>\n')
                    elif (os.path.isfile(filepath + '/../../' + 'pom.xml')):
                        pom_tong.write('    <relativePath>../../pom.xml</relativePath>\n')
                    elif (os.path.isfile(filepath + '/../../../' + 'pom.xml')):
                        pom_tong.write('    <relativePath>../../../pom.xml</relativePath>\n')
                    '''
            elif (stripline.startswith('<groupId>')):
                if (is_dependencies):
                    line = line.replace(parent_groupid[0], parent_groupid[1])
                    line = line.replace(groupid0[0], groupid0[1])
                    line = line.replace(groupid1[0], groupid1[1])
                elif (is_parent):
                    line = line.replace(parent_groupid[0], parent_groupid[1])
                    line = line.replace(groupid0[0], groupid0[1])
                else:
                    line = line.replace(parent_groupid[0], parent_groupid[1])
                    line = line.replace(groupid0[0], groupid0[1])
                    line = line.replace(groupid1[0], groupid1[1])
            elif (stripline.startswith('<relativePath>')):
                '''
                if (is_parent):
                    line = line.replace(parent_relativePath[0], parent_relativePath[1])
                else:
                    line = line
                '''
                line = line
                found_relativePath = True
            elif (stripline.startswith('<artifactId>')):
                line = line.replace(artifactId0[0], artifactId0[1])
                line = line.replace(artifactId1[0], artifactId1[1])
            elif (stripline.startswith('<name>')):
                if (is_parent):
                    line = line
                else:
                    line = line.replace(name0[0], name0[1])
                    line = line.replace(name1[0], name1[1])
            elif (stripline.startswith('<version>')):
                line = line.replace(version[0], version[1])
            elif (stripline.startswith('<url>')):
                if (is_repositories):
                    line = line
                else:
                    line = line.replace(url[0], url[1])
            elif (stripline.startswith('<description>')):
                is_description = True
                line = line.replace('org.wso2.carbon', 'com.tongtech.cellular')
                line = line.replace('Carbon', 'Cellular')
                if (stripline.find('</description>') != -1): #同一行结束
                    is_description = False
            elif (stripline.startswith('</description>')):
                is_description = False
            elif (stripline.startswith('<properties>')):
                is_properties = True
                line = line.replace('org.wso2.carbon', 'com.tongtech.cellular')
                line = line.replace('org.wso2', 'com.tongtech')
                line = line.replace('wso2', 'tong')
                line = line.replace('Carbon', 'Cellular')
            elif (stripline.startswith('</properties>')):
                is_properties = False
            elif (stripline.startswith('<dependencies>')):
                is_dependencies = True
            elif (stripline.startswith('</dependencies>')):
                is_dependencies = False
            elif (stripline.startswith('<repositories>')):
                is_repositories = True
            elif (stripline.startswith('</repositories>')):
                is_repositories = False
            elif (stripline.startswith('<modules>')):
                is_modules = True
            elif (stripline.startswith('</modules>')):
                is_modules = False
            elif (stripline.startswith('<execution>')):
                is_execute = True
            elif (stripline.startswith('</execution>')):
                is_execute = False
            elif (stripline.startswith('<bundles>')):
                is_bundles = True
            elif (stripline.startswith('</bundles>')): #includedFeatures
                is_bundles = False
            elif (stripline.startswith('<includedFeatures>')):
                is_includedFeatures = True
            elif (stripline.startswith('</includedFeatures>')): #<>
                is_includedFeatures = False
            elif (stripline.startswith('<featureArtifacts>')):
                is_featureArtifacts = True
            elif (stripline.startswith('</featureArtifacts>')): #<featureArtifacts> 
                is_featureArtifacts = False
            elif (stripline.find('p2-feature-generation') != -1):
                is_featureGeeration = True
            elif (stripline.startswith('</execution>')): #<featureArtifacts> <id>4-p2-feature-generation</id>
                is_featureGeeration = False
            elif (stripline.startswith('<features>')):
                is_features = True
            elif (stripline.startswith('</features>')): #<>
                is_features = False
            elif (stripline.find('<artifactId>maven-bundle-plugin</artifactId>') != -1):
                is_maven_bundle = True
            elif (stripline.startswith('</plugin>')): 
                is_maven_bundle = False
            elif (stripline.lower().find('carbon') != -1 or stripline.lower().find('wso2') != -1):
                if (is_description):
                    line = line.replace('org.wso2.carbon', 'com.tongtech.cellular')
                    line = line.replace('Carbon', 'Cellular')
                if (is_properties and stripline.find('wso2caching') == -1):
                    line = line.replace('org.wso2.carbon', 'com.tongtech.cellular')
                    line = line.replace('org.wso2', 'com.tongtech')
                    line = line.replace('wso2', 'tong')
                if (is_bundles):
                    line = line.replace('org.wso2.carbon', 'com.tongtech.cellular')
                    line = line.replace('org.wso2', 'com.tongtech')
                    line = line.replace('wso2', 'tong')
                if (is_includedFeatures):
                    line = line.replace('org.wso2.carbon', 'com.tongtech.cellular')
                if (is_featureArtifacts):
                    line = line.replace('org.wso2.carbon', 'com.tongtech.cellular')
                if (is_execute):
                    if (stripline.startswith('<pathelement location')):
                        line = line.replace('/wso2/', '/tong/')
                    if (stripline.startswith('location="${settings.localRepository}/org/apache/axis2/wso2/')):
                        line = line.replace('/wso2/', '/tong/')
                if (is_featureGeeration):
                    line = line.replace('org.wso2.carbon', 'com.tongtech.cellular')
                if (stripline.startswith('<Require-Bundle>')):
                    line = line.replace('org.wso2.carbon', 'com.tongtech.cellular')
                if (is_features):
                    line = line.replace('org.wso2.carbon', 'com.tongtech.cellular')
                if (is_maven_bundle):
                    line = line.replace('<Fragment-Host>org.wso2.carbon', '<Fragment-Host>com.tongtech.cellular')
            else:
                line = line
            lowerline = line.strip().lower()
            if (lowerline.find('wso2') != -1 or lowerline.find('carbon') != -1):
                sys.stdout.write("!!!!!!get line: %s, %s\n" % (path, line))
            pom_tong.write(line)
    pom_tong.close()        
    shutil.move(filepath + '/' + 'pom_tong.xml', filepath + '/' + 'pom.xml')            

def generate_directory(path):
    (filepath, filename) = os.path.split(path)
    #sys.stdout.write("get file: %s, %s\n" % (filepath, filename))
    if (filename == 'pom.xml'):
        generate_pom(path, filepath, filename, 'tongtech')
    elif (filename == 'carbon.product'):
        if (os.path.isfile(path + '.src')):
            shutil.copy(path + '.src', path)
        else:
            shutil.copy(path, path + '.src')
        pom_tong = open(path + '.temp', 'wt')
        is_features = False
        with open(path, 'rt') as f:
            for line in f:
                stripline = line.strip()  
                if (stripline.startswith('<features>')):
                    is_features = True
                elif (stripline.startswith('</features>')): #<>
                    is_features = False
                elif (stripline.lower().find('carbon') != -1 or stripline.lower().find('wso2') != -1):
                    if (is_features):
                        line = line.replace('org.wso2.carbon', 'com.tongtech.cellular')
                pom_tong.write(line)
        pom_tong.close()
        shutil.move(path + '.temp', path) 
                                
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
    subcommand = sys.argv[1]
    if subcommand == 'help':
        usage_and_exit()
    elif subcommand == 'tongtech': #将指定目录下pom.xml文件根据规则修改为使用com.tongtech.cellular, tong
        if argc < 2:
            usage_and_exit('No viewspec file specified.')
        working_directory = sys.argv[2] #"http://119.4.167.16:8080/svn/sailfish/trunk"
        for root, dirs, files in os.walk(working_directory): # Walk directory tree
            for f in files:
                generate_directory(root + '/' + f)
    elif subcommand == 'checkout':
        if argc < 2:
            usage_and_exit('No viewspec file specified.')
        checkout_lib_install()
    elif subcommand == 'p2':
        modify_p2_profile()
    elif subcommand.startswith('ex'):
        if (subcommand == 'ex'):
            extract_all()
        else:
            extract_taglib(subcommand)
    elif subcommand == 'deploy':
        deploy_directory_tomvn()
    else:
        usage_and_exit('Unknown subcommand "%s".' % (subcommand))
    sys.stdout.write("%s\n" % ('finished.'))

if __name__ == "__main__":
    main()