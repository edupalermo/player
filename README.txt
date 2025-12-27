mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.awt.headless=false"


export MAVEN_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.awt.headless=false" -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"

