mvn clean

mvn -T 1C spring-javaformat:apply

mvn -T 1C compile -DparallelTests=true test sonar:sonar \
  -DparallelTests=true \
  -Dsonar.host.url=http://192.168.3.19:9001 \
  -Dsonar.login=squ_9769fe801279ad52dfe625464403d32ef4d62d55 \
  -Dsonar.coverage.jacoco.xmlReportPaths=**/target/jacoco-ut/jacoco.xml,**/target/site/jacoco/jacoco.xml

# 单线程
mvn clean compile sonar:sonar \
    -Dsonar.host.url=http://192.168.3.19:9001 \
    -Dsonar.login=squ_9769fe801279ad52dfe625464403d32ef4d62d55 \
    -Dsonar.coverage.jacoco.xmlReportPaths=**/target/jacoco-ut/jacoco.xml,**/target/site/jacoco/jacoco.xml

mvn clean
mvn -T 1C spring-javaformat:apply
mvn -T 1C compile -DparallelTests=true test
mvn compile -DparallelTests=false test
