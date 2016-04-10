# kylix

## Introduction

Kylix is a set of functional libraries (still under development) for Kotlin. Part of a broader collection of libararies called kotyle

Currently it contains two primary classes, Option and Either

`Option` represents a possible existence of a value, a `None` (non existence of a value) or `Some` (existence of a value). 
One of the characteristics of this class is it intentionally does not offer a `get()` method (that would raise an exception 
in case of a `None`). Use the `orNull()` function if you really need it which will return an instance of a nullable type. 
`Option` does implement a `Collection` interface, so all the methods of a `Collection` are available on an `Option`.

More details about the `Option` class can be had from [Option Samples](OptionSamples.md)

Either represents an existence of one out of two possible values - a Left or a Right. Detailed documentation still TODO

## Availability

Kylix jars are published to bintray.com

## Usage

### Gradle Repository configuration

To use kylix from gradle, you need to configure gradle to use bintray repository

```    
repositories {
    mavenCentral()
	jcenter()
}
```   

### Gradle Dependency configuration

Add the following dependencies to your gradle compile path

```   
dependencies {
    compile "org.kotyle:kylix:0.0.1"
}  
```   

### Maven Repository configuration

_(TODO: This needs to be verified)_

To use kylix from maven, you need to configure maven to use the bintray repository in settings.xml

```
<repositories>
  <repository>
    <id>jcenter</id>
    <name>bintray</name>
    <url>http://jcenter.bintray.com</url>
    <releases>
      <enabled>true</enabled>
      <checksumPolicy>warn</checksumPolicy>
    </releases>
   </repository>
</repositories>
```

### Maven Dependency configuration

Add the following dependencies to your maven dependencies

_(TODO: This needs to be verified)_

```
<dependency>
    <groupId>org.kotyle</groupId>
    <artifactId>kylix</artifactId>
    <version>0.0.1</version>
</dependency>   
```
