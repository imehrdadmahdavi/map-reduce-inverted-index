# MapReduce Inverted Index

This is a small Java program for creating an inverted index of words occurring in a large set of documents extracted from web pages using [Hadoop MapReduce](https://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html) and [Google Dataproc](https://cloud.google.com/dataproc/). Hadoop cluster can be run in one of the three supported modes: Local (Standalone) Mode, Pseudo-Distributed Mode, and Fully-Distributed Mode. In this project we run first set up a small hello world hadoop using standalone (run project locally) and the ship the the project to  Google Dataproc to run it in the fully-distributed mode on the actual data set.

It is using a subset of 74 files from a total of 408 files (text extracted from HTML tags) derived from the Stanford WebBase project that is available [here](https://ebiquity.umbc.edu/resource/html/id/351). It was obtained from a web crawl done in February 2007. It is one of the largest collections totaling more than 100 million web pages from more than 50,000 websites. This version has been cleaned for the purpose of this assignment.



## Local (Standalone) Mode

### Setup Hadoop
GNU/Linux is supported as a development and production platform. To get a Hadoop distribution, download a recent stable release from one of the [Apache Download Mirrors](http://www.apache.org/dyn/closer.cgi/hadoop/common/). Please note that this project has been tested on hadoop-3.1.1. 

Unpack the downloaded Hadoop distribution. In the distribution, edit the file etc/hadoop/hadoop-env.sh to define some parameters as follows:

```shell
# set to the root of your Java installation
$ export JAVA_HOME=/usr/java/latest
 ```
 Try the following command:
 ```shell
$ bin/hadoop
 ```
This will display the usage documentation for the hadoop script. Now you are ready to start your Hadoop cluster in one of the three supported modes: Local (Standalone) Mode, Pseudo-Distributed Mode, and Fully-Distributed Mode. In this project we use the Standalone.


<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">Creative Commons Attribution 4.0 International License</a>.
