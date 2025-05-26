# TourGuide Project

[![Java CI with Maven](https://github.com/AIR01BRUN/JavaPathENProject8/actions/workflows/maven.yml/badge.svg)](https://github.com/AIR01BRUN/JavaPathENProject8/actions)

## Technologies
> Java 17  
> Spring Boot 3.X  
> JUnit 5  

## Project Structure
```
TourGuide/
├── src/
│   ├── main/
│   └── test/
├── libs/
│   ├── gpsUtil.jar
│   ├── RewardCentral.jar
│   └── TripPricer.jar
└── pom.xml
```

## Dependencies Setup
Run the following commands:
```bash
mvn install:install-file -Dfile=libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar
```
