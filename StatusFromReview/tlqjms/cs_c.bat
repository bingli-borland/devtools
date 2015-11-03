set JMS_SERVER=E:\dev\CD_TLQ\jmsbroker\server
astyle --style=java --indent=spaces=2 --recursive %JMS_SERVER%\applications\*.c %JMS_SERVER%\applications\*.h

astyle --style=java --indent=spaces=2 --recursive %JMS_SERVER%\comfra\*.c %JMS_SERVER%\comfra\*.h

astyle --style=java --indent=spaces=2 --recursive %JMS_SERVER%\modules\*.c %JMS_SERVER%\modules\*.h
