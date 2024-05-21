# Jack - A jvm build tool, that aims to simplify things

Jack is a jvm build tool, that aims to simplify the build process by providing easy and understandable configuration
options and tools.

## Motivation

A big problem with popular jvm build tools like gradle or maven is, that they have grown to be complicated over the last years.
Gradle's configuration files are scripts in kotlin or groovy, so there are actually plenty of different ways
to do the same thing. With that said and the fact that many developers tend to overengineer their products, these
configuration files often get very complicated and hard to understand.
Maven on the other hand uses xml as it's configuration language, which has the advantage that there's only few (predefined)
ways to configure a specific element. But it's no secret that xml is verbose, which leads to big, hard to understand configuration files.

Furthermore, both maven and gradle have problems, when it comes to providing the right java version in environments that are not
FHS compliant by default like nix(os). Additionally, gradle nor maven use a sort of lockfile by default, which leads to 
a lack of reproducibility. 

This is only a part of the problems, that both gradle and maven have, but it should give a first impression on the issues
jack tries to address.

## Goals
Jack has 4 big goals, which include:
- Reproducibility: Each project should be reproducible by default. That means that all dependencies, jack itself and the used languages are locked with hashes and versions.
- Simplicity: Jack aims to provide a simple and straightforward way to configure the project and default tasks like compilation, testing, publishing or benchmarking.
- Predictability: Jack does not do "magic" things. Jack isn't providing strange problem solutions for most parts. If there's a problem, the user should tell jack what to do.
- Speed: Jack tries to add as little overhead as possible to all day development tasks, such as compilation or testing.

## Implementation
Each jack project consists of
- A `jack.toml`, which is the primary configuration file 
- A `src` directory, which included the source code of this project
- A `.jack` directory, that stores user specific configuration and caches. It should not be published.
- A `.jack/user.toml` file, that contains user specific configuration
- A SBOM file, which contains a numeration of all dependencies, including the hashes. This file also works as a cache of the dependency tree and a lock file.

### The main configuration - jack.toml
The `jack.toml` is the main configuration file the jack build tool. In this file all properties of a project are stored, such as for example
- the name
- the version
- the author[s]
- manifest information (such as the main class)
- dependencies/repositories
- publishing information

#### Structure
A `jack.toml` file is toml (version 1.0.0) file, that has the following structure:
```toml
[package]
name = "project name"
version = "project version, semver"
author = "the project author"
authors = ["author 1", "author 2", "..."]

[repositories]
"url" = "type, one of: maven" # Example: "https://repo.maven.apache.org/maven2/" = "maven"
"mavenCentral" = true # enabling predefined repositories

[dependencies]
"groupId:artifactId" = "version"

[manifest]
mainClass = "the programs main class" # Example: mainClass = "de.test.Main"
```

### Source directory
The source directory contains all source files.

### User specific directory -- `.jack` directory
The user specific `.jack` directory contains internal jack files, such as caches (perhaps).
Also, it contains the `user.toml` file, which includes user specific configuration options.

#### User.toml
The `user.toml` cannot override anything in `jack.toml`. It's only purpose is to provide user specific information, such 
as paths to already installed java installations and options on caching etc.

### SBOM file
An SBOM file is automatically generated at the directories root by jack. The files content is stored according to the
SPDX Specification.
The content includes:
- all dependencies (even transitive ones) with as much available information as possible and it's hashes
- information to licencing
- information to the project

This file serves as an entry point to see the project's whole dependency tree and as a kind of lockfile for use by jack.

### Global cache
Each installation has a global cache in the user directory, which stores dependencies, jdk's and other jack related files.

### JDK installation resolution
As said before jack doesn't make assumptions where the used languages are stored.
The paths for the jdk to be used is determinated by the following steps, starting at 1:
1. the jdk is searched in the project's `.jack` directory 
2. the jdk is searched in the user's global cache
3. the jdk is downloaded and stored in either the project's `.jack` directory or the global cache

### Dependency binary resolution
After resolving the dependency tree, each dependency binary location is determinated by the following steps, starting at 1:
1. the binary is searched in the project's `.jack` directory
2. the binary is searched in the user's global cache
3. the binary is downloaded and stored in either the project's `.jack` directory or the global cache

Additionally, all dependencies hashes and versions are checked against the project SBOM file.

### Jack cli tool
Jack's cli tool is the main tool jack provides. With this tool following tasks can be accomplished:
- building the project
- testing the project
- benchmarking the project
- publishing the project

#### Jack wrapper
Each project should be self-contained and not dependent on any installation on the system, except an arbitrary jack installation.

To achieve this, the version and hash of jack is stored in the SBOM.
If the current used installation doesn't match with the one defined in the SBOM. Jack downloads an installation 
and stores it in the cache (either global, or project's `.jack`). The installation on the project now just delegated all
inputs to this wrapper installation, without doing any tasks itself.


