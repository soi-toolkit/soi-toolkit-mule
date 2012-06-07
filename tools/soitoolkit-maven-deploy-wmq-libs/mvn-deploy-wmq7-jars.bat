rem --------------------------------------------------------
rem Upload WebsphereMQ Java-libs to Maven-repo.
rem
rem 2012-06-04 Håkan Dahl
rem --------------------------------------------------------

set REPO_URL=http://MY_REPO_MANAGER_HOST/repo/content/repositories/thirdparty
set REPO_ID=thirdparty
set WMQ_VERSION=7.1.0.1
set WMQ_LIB_PATH=wmq_libs

call mvn deploy:deploy-file -Durl=%REPO_URL% -DrepositoryId=%REPO_ID% -Dfile=%WMQ_LIB_PATH%/dhbcore.jar -DgroupId=com.ibm.mq -DartifactId=dhbcore -Dversion=%WMQ_VERSION% -Dpackaging=jar

call mvn deploy:deploy-file -Durl=%REPO_URL% -DrepositoryId=%REPO_ID% -Dfile=%WMQ_LIB_PATH%/com.ibm.mqjms.jar -DgroupId=com.ibm.mq -DartifactId=mqjms -Dversion=%WMQ_VERSION% -Dpackaging=jar

call mvn deploy:deploy-file -Durl=%REPO_URL% -DrepositoryId=%REPO_ID% -Dfile=%WMQ_LIB_PATH%/com.ibm.mq.jmqi.jar -DgroupId=com.ibm.mq -DartifactId=jmqi -Dversion=%WMQ_VERSION% -Dpackaging=jar

call mvn deploy:deploy-file -Durl=%REPO_URL% -DrepositoryId=%REPO_ID% -Dfile=%WMQ_LIB_PATH%/com.ibm.mq.jar -DgroupId=com.ibm.mq -DartifactId=mq -Dversion=%WMQ_VERSION% -Dpackaging=jar

