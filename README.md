RapidMiner SSFS-APSO Extension (Swarm Search Feature Selection - Accelerated Particle Swarm Optimization
=============================

### Prerequisite
~~* Requires Gradle 2.3+ (get it [here](http://gradle.org/installation) or use the Gradle wrapper shipped with this template)~~

* Rapidminer Studio 7.6 or newer https://my.rapidminer.com/nexus/account/index.html#downloads

### Getting started

1. Clone the extension template to a folder named 'rmextension_ssfsapso'

2. ~~Change the extension settings in _build.gradle_ (e.g. replace 'Template' by the desired extension name)~~

3. ~~Initialize the extension project by executing the _initializeExtensionProject_ Gradle task (e.g. via 'gradlew initializeExtensionProject')~~

4. ~~Add an extension icon by placing an image named "icon.png" in  _src/main/resources/META-INF/_.~~ 

5. Build and install your extension by executing the _installExtension_ Gradle task (e.g. via 'gradlew installExtension')

6. Start RapidMiner Studio and check whether the extension has been loaded

## Synopsis
This is an Operator based on the articles Text Analytics for Predicting Question Acceptance Rates (Fong, Zhou and Moutinho, 2015), Swarm Search for Feature Selection in Classification (Fong, Yang and Deb, 2013) and Accelerated Particle Swarm Optimization and Support Vector Machine for Business Optimization and Applications (Yang, Deb and Fong, 2012).
 
## Description
The Swarm Search APSO is a technique that processes several subset combinations of an initial features dataset. The main goal of this technique is to optimize the classification/training without running all possible subsets.
 
Firstly, it is defined the number of iterations as a criteria to stop the execution. and the number of particles. The particles choose a subset to be classified in each iteration. At the end of an iteration it is checked which of the examined subsets has the best accuracy. Then the operator stores this subset configuration as the best one to the moment. Based on the global best value and own best value, the particles modify their subsets to another according with the velocity and inertia parameters.
 
This operator is adapted from the JSwarm-PSO package (http://jswarm-pso.sourceforge.net/).
