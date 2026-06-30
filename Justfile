prepare:
  mvn -pl paper-nms paper-nms:init

full-build:
  mvn clean install

fast-build:
  mvn install -Dmaven.test.skip=true -Dmaven.javadoc.skip=true -Dmaven.gpg.skip=true

server-test:
  fish ./test-runs.fish target/*.jar
