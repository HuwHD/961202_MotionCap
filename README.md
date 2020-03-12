# 961202_MotionCap
## Motion tracking Visor for Alternate Mouse Input
Documentation to be written here...
## To do List

* Discuss Licences
  * Add outcome to Documentation (Need a licences section)
* Make the Micro-Bits communicate Wirelessly
* Make the Micro-Bit register X(head rotation left and right)-Y(head tilt up and down)
* Visualise the data received (as a graph)
* Translate the raw X Y data into mouse movement
* Design and implement a clicking method


#Development Notes..

I used a gradle java fx template found here: 

https://github.com/openjfx/samples/tree/master/HelloFX/Gradle/hellofx

I made the following changes to my build.gradle file:

* I Downgraded the example to Java 11 as that is the version I�m using. I also altered the project name accordingly.

* I added the Maven Repositories: mavenLocal() and mavenCentral(). This allowed me to add the following dependencies:

  * com.github.purejavacomm:purejavacomm:1.0.2.RELEASE
  * junit:junit:4.12

I searched the internet for the best API for reading and writing to a serial port in Java and found that: com.github.purejavacomm:purejavacomm:1.0.2.RELEASE seemed most appropriate for my application.

This allows me to use the serial and serial monitor API�s in my code.

I added junit for testing purposes.

* I created a jar task, which builds an executable jar. This jar contains everything that my application will need in order to run, within a single file. 

This allows me to run my application with just the command: 

```bash
java -jar 961202_MotionCap.jar
```


