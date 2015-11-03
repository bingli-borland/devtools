<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
        xmlns:lxslt="http://xml.apache.org/xslt"
        xmlns:stringutils="xalan://org.apache.tools.ant.util.StringUtils">
<xsl:output method="html" indent="yes" encoding="US-ASCII"
  doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" />
<xsl:decimal-format decimal-separator="." grouping-separator="," />

<xsl:param name="TITLE">成都研发项目信息.</xsl:param>

<!--

 Sample stylesheet to be used with Ant JUnitReport output.

 It creates a non-framed report that can be useful to send via
 e-mail or such.

-->
<xsl:template match="sprints">
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
            <xsl:call-template name="sprint-detail"/>
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

<xsl:template name="sprint-detail">
    <h2>SPRINT汇总</h2>
    <xsl:for-each select="sprintdetails">
		    <h2><xsl:value-of select="@name"/> 开始:<xsl:value-of select="@startdate"/> 结束:<xsl:value-of select="@enddate"/></h2>
    		<table border="1">
			  <tr>
			        <th>ITYPE</th>
			        <th>IKEY</th>
			        <th>总结</th>
			        <th></th>
			        <th>ST名称</th>
			        <th>ST类型</th>
			        <th>ST KEY</th>
			        <th>ST总结</th>
			        <th></th>
			        <th>名称</th>
			        <th>日志时间</th>
			        <th>日志内容</th>
			        <th>创建日期</th>
			  </tr>
        <xsl:for-each select="sprintdetail">
		        <tr>
			        <td width="4%"><xsl:value-of select="@issuetype"/></td>
			        <td width="9%"><xsl:value-of select="@issuekey"/>, <xsl:value-of select="@issuestatus"/>, <xsl:value-of select="@issuepoint"/></td>
			        <td width="12%"><xsl:value-of select="@issuesummary"/></td>
			        
			        <td>
			        <xsl:for-each select="taskdetail">
			        	    <tr>
			        	    <td></td>
			        	    <td></td>
			        	    <td></td>
			        	    <td width="1%"></td>
						        <td width="5%"><xsl:value-of select="@name"/></td>
						        <td width="4%"><xsl:value-of select="@tasktype"/></td>
						        <td width="7%"><xsl:value-of select="@taskkey"/>, <xsl:value-of select="@taskstatus"/></td>
						        <td width="12%"><xsl:value-of select="@tasksummary"/></td>
						        <td>
						        <xsl:for-each select="worklogdetail">
						        	    <tr>
						        	    <td></td>
						        	    <td></td>
						        	    <td></td>
						        	    <td></td>
						        	    <td></td>
						        	    <td></td>
						        	    <td></td>
						        	    <td></td>
						        	    <td width="1%"></td>
									        <td width="5%"><xsl:value-of select="@name"/></td>
									        <td width="5%"><xsl:value-of select="@timespent"/></td>
									        <td width="16%"><xsl:value-of select="@comment"/></td>
									        <td width="7%"><xsl:value-of select="@createdate"/></td>
									        </tr>
						        </xsl:for-each>
						        </td>
						        </tr>
			        </xsl:for-each>
			        </td>
			        
			        <td>
			        <xsl:for-each select="worklogdetail">
			        	    <tr>
			        	    <td></td>
			        	    <td></td>
			        	    <td></td>
			        	    <td></td>
			        	    <td></td>
			        	    <td></td>
			        	    <td></td>
			        	    <td></td>
			        	    <td></td>
						        <td width="5%"><xsl:value-of select="@name"/></td>
						        <td width="5%"><xsl:value-of select="@timespent"/></td>
						        <td width="16%"><xsl:value-of select="@comment"/></td>
						        <td width="7%"><xsl:value-of select="@createdate"/></td>
						        </tr>
			        </xsl:for-each>
			        </td>
			        
		　　　　 </tr>
        </xsl:for-each>
        </table>       
		    <hr size="1"/>
    </xsl:for-each>
</xsl:template>


</xsl:stylesheet>
