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
<xsl:template match="projects">
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
            <xsl:call-template name="project-info"/>
            <xsl:call-template name="story-info"/>
            <xsl:call-template name="people-info"/>
            <xsl:call-template name="totalpeople-info"/>
            <xsl:call-template name="valuation-info"/>
            <xsl:call-template name="totalvaluation-info"/>
            <xsl:call-template name="log-detail"/>
            <xsl:call-template name="duetask-info"/>
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

<xsl:template name="project-info">
    <xsl:for-each select="project">
		    <h2><xsl:value-of select="@name"/></h2>
    		<table border="1">
			  <tr>
			        <th>KEY</th>
			        <th>状态</th>
			        <th>开发者</th>
			        <th>日志时间</th>
			        <th>日志次数</th>
			        <th>总结</th>
			  </tr>
        <xsl:for-each select="projectstory">
		        <tr>
			        <td><xsl:value-of select="@key"/></td>
			        <td><xsl:value-of select="@status"/></td>
			        <td><xsl:value-of select="@developer"/></td>
			        <td><xsl:value-of select="@logtimespent"/></td>
			        <td><xsl:value-of select="@logworkcount"/></td>
			        <td><xsl:value-of select="@summary"/></td>
		　　　　 </tr>
        </xsl:for-each>
        </table>       
		    <hr size="1"/>
    </xsl:for-each>
</xsl:template>


    <xsl:template name="story-info">
		    <h2>故事统计</h2>
    		<table border="1">
			    <tr>
			        <th>名称</th>
			        <th>完成数量</th>
			        <th>完成点数</th>
			        <th>剩余数量</th>
			        <th>剩余点数</th>
			        <th>未知数量</th>
			    </tr>
        <xsl:for-each select="storystats/storystat">
	        <tr>
		        <td><xsl:value-of select="@name"/></td>
		        <td><xsl:value-of select="@finishedstroycount"/></td>
		        <td><xsl:value-of select="@finishedstroypoint"/></td>
		        <td><xsl:value-of select="@remainstroycount"/></td>
		        <td><xsl:value-of select="@remainstroypoint"/></td>
		        <td><xsl:value-of select="@unknownstroycount"/></td>
	　　　　 </tr>
        </xsl:for-each>
        </table>
		    <hr size="1"/>
    </xsl:template>

    <xsl:template name="people-info">
		    <h2>个人统计</h2>
    		<table border="1">
			    <tr>
			        <th>名称</th>
			        <th>任务时间</th>
			        <th>日志时间</th>
			        <th>日志次数</th>
			    </tr>
        <xsl:for-each select="peoplestats/peoplestat">
			    <tr>
			        <td><xsl:value-of select="@name"/></td>
			        <td><xsl:value-of select="@timespent"/></td>
			        <td><xsl:value-of select="@logtimespent"/></td>
			        <td><xsl:value-of select="@logworkcount"/></td>
			    </tr>
        </xsl:for-each>
        </table>
		    <hr size="1"/>
    </xsl:template>

    <xsl:template name="totalpeople-info">
        <h2>个人汇总统计</h2>
        <table border="1">
            <tr>
                <th>名称</th>
                <th>任务时间</th>
                <th>日志时间</th>
                <th>挣值</th>
                <th>日志次数</th>
                <th>备注</th>
            </tr>
            <xsl:for-each select="totalpeoplestats/peoplestat">
                <tr>
                    <td><xsl:value-of select="@name"/></td>
                    <td><xsl:value-of select="@timespent"/></td>
                    <td><xsl:value-of select="@logtimespent"/></td>
                    <td><xsl:value-of select="@earnedvalue"/></td>
                    <td><xsl:value-of select="@logworkcount"/></td>
                    <td><xsl:value-of select="@memo"/></td>
                </tr>
            </xsl:for-each>
        </table>
        <hr size="1"/>
    </xsl:template>

    <xsl:template name="valuation-info">
        <h2>个人完成情况统计</h2>
        <table border="1">
            <tr>
                <th>名称</th>
                <th>ISSUETYPE</th>
                <th>ISSUEKEY</th>
                <th>VALUATION</th>
                <th>SUMMARY</th>
            </tr>
            <xsl:for-each select="valuationstats/valuationstat">
                <tr>
                    <td><xsl:value-of select="@name"/></td>
                    <td><xsl:value-of select="@issuetype"/></td>
                    <td><xsl:value-of select="@issuekey"/></td>
                    <td><xsl:value-of select="@valuation"/></td>
                    <td><xsl:value-of select="@issuesummary"/></td>
                </tr>
            </xsl:for-each>
        </table>
        <hr size="1"/>
    </xsl:template>

    <xsl:template name="totalvaluation-info">
        <h2>个人完成情况汇总统计</h2>
        <table border="1">
            <tr>
                <th>名称</th>
                <th>ISSUETYPE</th>
                <th>ISSUEKEY</th>
                <th>VALUATION</th>
                <th>SUMMARY</th>
            </tr>
            <xsl:for-each select="totalvaluationstats/valuationstat">
                <tr>
                    <td><xsl:value-of select="@name"/></td>
                    <td><xsl:value-of select="@issuetype"/></td>
                    <td><xsl:value-of select="@issuekey"/></td>
                    <td><xsl:value-of select="@valuation"/></td>
                    <td><xsl:value-of select="@issuesummary"/></td>
                </tr>
            </xsl:for-each>
        </table>
        <hr size="1"/>
    </xsl:template>

<xsl:template name="log-detail">
    <h2>个人日志汇总</h2>
    <xsl:for-each select="logdetails">
		    <h2><xsl:value-of select="@name"/></h2>
    		<table border="1">
			  <tr>
			        <th>ISSUETYPE</th>
			        <th>ISSUEKEY</th>
			        <th>总结</th>
			        <th></th>
			        <th>日志时间</th>
			        <th>日志内容</th>
			        <th>创建日期</th>
			  </tr>
        <xsl:for-each select="logdetail">
		        <tr>
			        <td><xsl:value-of select="@issuetype"/></td>
			        <td><xsl:value-of select="@issuekey"/></td>
			        <td><xsl:value-of select="@issuesummary"/></td>
			        <td>
			        <xsl:for-each select="worklogdetail">
			        	    <tr>
			        	    <td></td>
			        	    <td></td>
			        	    <td></td>
			        	    <td></td>
						        <td><xsl:value-of select="@timespent"/></td>
						        <td width="25%"><xsl:value-of select="@comment"/></td>
						        <td><xsl:value-of select="@createdate"/></td>
						        </tr>
			        </xsl:for-each>
			        </td>
		　　　　 </tr>
        </xsl:for-each>
        </table>       
		    <hr size="1"/>
    </xsl:for-each>
</xsl:template>

    <xsl:template name="duetask-info">
        <h2>工作量超期任务</h2>
        <xsl:for-each select="duetaskbyprojects">
            <h2><xsl:value-of select="@name"/></h2>
            <table border="1">
                <tr>
                    <th>KEY</th>
                    <th>状态</th>
                    <th>开发者</th>
                    <th>估计时间</th>
                    <th>日志时间</th>
                    <th>日志次数</th>
                    <th>总结</th>
                </tr>
                <xsl:for-each select="duetask">
                    <tr>
                        <td><xsl:value-of select="@key"/></td>
                        <td><xsl:value-of select="@status"/></td>
                        <td><xsl:value-of select="@developer"/></td>
                        <td><xsl:value-of select="@originalestimate"/></td>
                        <td><xsl:value-of select="@logtimespent"/></td>
                        <td><xsl:value-of select="@logworkcount"/></td>
                        <td width="35%"><xsl:value-of select="@summary"/></td>
                    </tr>
                </xsl:for-each>
            </table>
            <hr size="1"/>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
