使用JIRA管理测试集的工具.
  首先在jira中记录要测试的项, 比如JIRA-1,JIRA-2. 注意必须在某个地方记录函数名称.
  然后使用bamboo运行daily.build,生成一个xml.
  此工具分析这个xml,修改对应的JIRA号的状态

Test1