mvn clean

mvn spring-javaformat:apply

mvn clean compile test sonar:sonar \
  -Dsonar.host.url=http://192.168.3.19:9001 \
  -Dsonar.login=squ_9769fe801279ad52dfe625464403d32ef4d62d55 \
  -Dsonar.coverage.jacoco.xmlReportPaths=**/target/jacoco-ut/jacoco.xml,**/target/site/jacoco/jacoco.xml

mvn clean compile test sonar:sonar \
  -Dsonar.host.url=http://192.168.3.4:9001 \
  -Dsonar.login=squ_f790f0b8842f038fec6c899b5d914d8df00547be \
  -Dsonar.coverage.jacoco.xmlReportPaths=**/target/jacoco-ut/jacoco.xml,**/target/site/jacoco/jacoco.xml

# 仅编译与测试（不跑 Sonar）
# mvn clean
# mvn spring-javaformat:apply
# mvn compile test
