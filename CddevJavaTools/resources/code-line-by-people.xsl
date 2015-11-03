<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
        xmlns:lxslt="http://xml.apache.org/xslt"
        xmlns:stringutils="xalan://org.apache.tools.ant.util.StringUtils">
<xsl:output method="html" indent="yes" encoding="US-ASCII"
  doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" />
<xsl:decimal-format decimal-separator="." grouping-separator="," />
<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 -->

<xsl:param name="TITLE">成都研发项目信息.</xsl:param>

<!--

 Sample stylesheet to be used with Ant JUnitReport output.

 It creates a non-framed report that can be useful to send via
 e-mail or such.

-->
<xsl:template match="svnstat">
    <html>
        <head>
            <title><xsl:value-of select="$TITLE"/></title>
    <style type="text/css">
      body {
        font:normal 68% verdana,arial,helvetica;
        color:#000000;
      }
      table tr td, table tr th {
          font-size: 68%;
      }
      table.details tr th{
        font-weight: bold;
        text-align:left;
        background:#a6caf0;
      }
      table.details tr td{
        background:#eeeee0;
      }

      p {
        line-height:1.5em;
        margin-top:0.5em; margin-bottom:1.0em;
      }
      h1 {
        margin: 0px 0px 5px; font: 165% verdana,arial,helvetica
      }
      h2 {
        margin-top: 1em; margin-bottom: 0.5em; font: bold 125% verdana,arial,helvetica
      }
      h3 {
        margin-bottom: 0.5em; font: bold 115% verdana,arial,helvetica
      }
      h4 {
        margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
      }
      h5 {
        margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
      }
      h6 {
        margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
      }
      .Error {
        font-weight:bold; color:red;
      }
      .Failure {
        font-weight:bold; color:purple;
      }
      .Properties {
        text-align:right;
      }
      </style>
        </head>
        <body>
            <a name="top"></a>
            <xsl:call-template name="pageHeader"/> 
            <xsl:call-template name="svnstat"/>
        </body>
    </html>
</xsl:template>

<!-- Page HEADER -->
<xsl:template name="pageHeader">
    <h1><xsl:value-of select="$TITLE"/></h1>
    <table width="100%">
    <tr>
        <td align="left"></td>
        <td align="right">Designed for use with <a href='http://www.tongtech.com'>CDEV</a>.</td>
    </tr>
    </table>
    <hr size="1"/>
</xsl:template>

<xsl:template name="svnstat">
    <xsl:for-each select="peoplestats">
		    <h2><xsl:value-of select="@startdate"/>-<xsl:value-of select="@enddate"/></h2>
    		<table border="1">
			  <tr>
			        <th>名称</th>
			        <th>次数</th>
			        <th>源码增加</th>
			        <th>源码删除</th>
			        <th>测试增加</th>
			        <th>测试删除</th>
			        <th>累计次数</th>
			        <th>累计源码增加</th>
			        <th>累计源码删除</th>
			        <th>累计测试增加</th>
			        <th>累计测试删除</th>
			  </tr>
        <xsl:for-each select="peoplestat">
		        <tr>
			        <td><xsl:value-of select="@name"/></td>
			        <td><xsl:value-of select="@commitcount"/></td>
			        <td><xsl:value-of select="@srcadded"/></td>
			        <td><xsl:value-of select="@srcdeleted"/></td>
			        <td><xsl:value-of select="@testadded"/></td>
			        <td><xsl:value-of select="@testdeleted"/></td>
			        <td><xsl:value-of select="@toatalcommitcount"/></td>
			        <td><xsl:value-of select="@toatalsrcadded"/></td>
			        <td><xsl:value-of select="@toatalsrcdeleted"/></td>
			        <td><xsl:value-of select="@toataltestadded"/></td>
			        <td><xsl:value-of select="@toataltestdeleted"/></td>
		　　　　 </tr>
        </xsl:for-each>
        </table>       
		    <hr size="1"/>
    </xsl:for-each>
</xsl:template>

</xsl:stylesheet>
