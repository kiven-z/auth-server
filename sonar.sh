mvn spring-javaformat:apply spotless:apply && mvn clean compile test sonar:sonar \
  -Dsonar.host.url=http://192.168.3.4:9001 \
  -Dsonar.login=squ_12e3bffa9a742d1fa769f73d3bc699b51f88f456 \
  -Dsonar.coverage.jacoco.xmlReportPaths=**/target/jacoco-ut/jacoco.xml,**/target/site/jacoco/jacoco.xml

# 仅编译与测试（不跑 Sonar）
# mvn clean
# mvn spring-javaformat:apply
# mvn compile test
