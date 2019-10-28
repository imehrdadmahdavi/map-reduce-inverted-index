# Hadoop MapReduce Inverted Index

This is a Java program for creating an inverted index of words occurring in a large set of documents extracted from web pages using [Hadoop MapReduce](https://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html) and [Google Dataproc](https://cloud.google.com/dataproc/).

As our dataset, we are using a subset of 74 files from a total of 408 files (text extracted from HTML tags) derived from the Stanford WebBase project that is available [here](https://ebiquity.umbc.edu/resource/html/id/351). It was obtained from a web crawl done in February 2007. It is one of the largest collections totaling more than 100 million web pages from more than 50,000 websites. This version has already been cleaned.

In this project we first set up a sample Hadoop cluster using Local (Standalone) Mode and then implement the actual project in Fully-Distributed Mode on Google Dataproc on the actual data set.

## Local (Standalone) Mode


### Setup Hadoop

We need to setup the project on GNU/Linux as it is the supported development and production platform for Hadoop. To get a Hadoop distribution, download a recent stable release from one of the [Apache Download Mirrors](http://www.apache.org/dyn/closer.cgi/hadoop/common/). Please note that this project was deployed and tested using `hadoop-3.1.1`. Unpack the downloaded Hadoop distribution. In the distribution folder, edit the file `etc/hadoop/hadoop-env.sh` to define environment variable parameters as follows:

```shell
# add these line to the hadoop-env.sh or set them in terminal
export JAVA_HOME=/usr/java/latest
export PATH=${JAVA_HOME}/bin:${PATH}
export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar
 ```
Then try the following command:
 ```shell
$ bin/hadoop
 ```
This should display the usage documentation for the Hadoop script. Now you are ready to start your Hadoop cluster in the standalone mode.

### Setup and run a simple Hadoop job

This simple Hadoop job, gets two text files from the "input" folder as the arguments of the Mapper.
```shell
#file01
5722018411	Hello World Bye World
```
```shell
#file02
6722018415	Hello Hadoop Goodbye Hadoop
```
And by submitting a Hadoop job and applying Reduce step, it generates an inverted index as below:
```shell
bye	5722018411:1 
goodbye	6722018415:1 
hadoop	6722018415:2 
hello	5722018411:1 6722018415:1 
world	5722018411:2
```
To submit a Hadoop job, the MadReduce implementation should be packaged as a `jar` file. To do so, copy the `InvertedIndex.java` file of this project to the Hadoop's distribution root folder and while you are still there run the following commands to compile `InvertedIndex.java` and create a `jar` file.
```shell
$ bin/hadoop com.sun.tools.javac.Main InvertedIndex.java
$ jar cf invertedindex.jar InvertedIndex*.class
```
Copy `input/file01` and `input/file02` of this project and place them inside the `input` folder of the Hadoop distrubution folder. While you are still there, run the following command to submit the job, get the input files from `input` folder, generate the inverted index and store its output in the `output` folder:
```shell
$ bin/hadoop jar invertedindex.jar InvertedIndex input output
```
And finally to see the output, run the below command:
```shell
$ bin/hadoop dfs -cat output/part-r-00000
```
## Fully-Distributed Mode

In this section we create a cluster with 3 worker nodes on Google Dataproc and run `invertedindex.jar` job on the actual data set. 

### Google Cloud Platform Setup

First we need an account on [Google Cloud Platform](https://cloud.google.com/). You can sign up for a trial with a $300 free credits if you don't have one already.

#### Setting up Your Initial Machine
In the Google Cloud Console either create a new project or select an existing one. For this exercise we will use Dataproc. Using Dataproc, we can quickly create a cluster of compute instances running Hadoop. The alternative to Dataproc would be to individually setup each compute node, install Hadoop on it, set up HDFS, set up master node, etc. Dataproc automates this grueling process for us.

#### Creating a Hadoop Cluster on Google Cloud Platform

In Google Cloud Consol, select Dataproc from the navigation list on the left. If this is the first time you’re using Dataproc then you might need to enable Dataproc API first. Clicking on 'Create Cluster' will take you to the cluster configuration section. Give any unique name to your cluster and select a desired zone. You need to create a master and 3 worker nodes. Select the default configuration processors (n1-standard-4 4vCPU 15 GB memory) for master and each member and reduce the storage to 32 GB HDD storage. Change the number of Worker nodes to 3. Leave everything else default and click on 'Create'.

Now that the cluster is setup we’ll have to configure it a little before we can run jobs on it. Select the cluster you just created from the list of clusters under the cloud Dataproc section on your console. Go to the VM Instances tab and click on the SSH button next to the instance with the Master Role.

Clicking on the SSH button will take you to a Command line Interface(CLI) like an xTerm or Terminal. All the commands in the following steps are to be entered in the
CLI. There is no home directory on HDFS for the current user do we’ll have to set this up before proceeding further. (To find out your user name run `whoami`)
```shell
$ hadoop fs -mkdir -p /user/<your username here>
```
#### Set up environment variables
`JAVA_HOME` has already been setup and we don't need to set it up again.

Please note that this step has to be done each time you open a new SSH terminal. For eliminating this step you can also setup this up `JAVA_HOME`, `PATH`, and `HADOOP_CLASSPATH` in the `etc/hadoop/hadoop-env.sh`.
```shell
$ export PATH=${JAVA_HOME}/bin:${PATH}
$ export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar
```
Now run:
```shell
$ hadoop fs -ls
```
If there is no error this implies that your cluster was successfully set up. If you do encounter an error it’s most likely due to a missing environment variable or user home directory not being set up right.

To ensure that the environment variables are set, run the command `env`.

**NOTE**: Please disable the billing for the cluster when you are not using it. Leaving it running will cost extra credits.
#### Upload data to Google Cloud Storage

Download the dataset from this [link](https://drive.google.com/drive/u/1/folders/1Z4KyalIuddPGVkIm6dUjkpD_FiXyNIcq) and unzip the contents. You will find two folders inside named 'development' and 'full data'. 'development' data can be used for development and testing purposes.

Click on 'Dataproc' in the left navigation menu. Next, locate the address of the default Google cloud storage staging bucket for your cluster.

Go to the storage section in the left navigation bar and select your cluster’s default bucket from the list of buckets. Click on the `Upload Folder` button and upload the `devdata` and `fulldata` folders individually.

#### Submit Hadoop job and generate output
Now we are ready to submit a Hadoop job to run our MapReduce implementation on the actual data. Either use SSH or `nano`/`vi` command to copy or create `InvertedIndex.java` on the master cluster.

Run the following command to create the `jar` file:
```shell
$ hadoop com.sun.tools.javac.Main InvertedIndex.java
$ jar cf invertedindex.jar InvertedIndex*.class
```
Now you have a jar file for your job. You need to place this jar file in the default cloud bucket of your cluster. Just create a folder called JAR on your bucket and upload it to that folder. If you created your jar file on the cluster’s master node itself use the following commands to copy it to the JAR folder. 
```shell
$ hadoop fs -copyFromLocal ./invertedindex.jar
$ hadoop fs -cp ./invertedindex.jar gs://dataproc-69070.../JAR
```
The `gs://dataproc-69070...` part is the default bucket of your cluster. It needs to be prepended by the gs:// to tell the Hadoop environment that it is a bucket and not a regular location on the filesystem.

#### Submitting the Hadoop job to your cluster

Go to the 'Jobs' section in the left navigation bar of the Dataproc page and click on 'Submit job'. Fill the job parameters as follows:

* **Cluster**: Select the cluster you created
* **Job Type:** Hadoop
* **Jar File:** Full path to the jar file you uploaded earlier to the Google storage bucket. E.g. `gs:///dataproc-69070.../JAR/invertedindex.jar`.
* **Main Class or jar:** The name of the java class you wrote the mapper and reducer in.
* **Arguments:** This takes two arguments.
    * **Input:** Path to the input data you uploaded e.g. `gs:///dataproc-69070.../fulldata`.
    * **Output:** Path to the storage bucket followed by a new folder name e.g. `gs:///dataproc-69070.../fulloutput`. The folder is created during execution. You will get an error if you give the name of an existing folder.
* Leave the rest at their default settings.

Now submit the job. You can see the log while it is running.

**NOTE**: If you encounter a `Java.lang.Interrupted Exception` you can safely ignore it. Your job will still execute.

The output files will be stored in the output folder on the bucket. If you open this folder you’ll notice that the inverted index is in several segments.(Delete the _SUCCESS file in the folder before mergin all the output files). To merge the files and create a final text output run the following commands:
```shell
$ hadoop fs -getmerge gs://dataproc-69070.../fulloutput ./output.txt
$ hadoop fs -copyFromLocal ./output.txt
$ hadoop fs -cp ./output.txt gs://dataproc-69070.../output.txt
```
Now you have successfully created an inverted index of the whole data set. You can use `grep` to see index entries for any apecific word:
```shell
$ grep -w '^peace' output.txt
```
#### Note
If you want to re-compile and submit a new job again you can remove the `.jar`, `.class` ,`.java` and Hadoop files using the command below accordingly.
```shell
$ hadoop fs -rm ./wordcount.jar ./output.txt WordCount*.class
$ hadoop fs -rm gs://dataproc-69070.../JAR/wordcount.jar; gs://dataproc-69070.../output.txt
$ hadoop fs -rm gs://dataproc-69070.../fulloutput/* 
$ hadoop fs -rmdir gs://dataproc-69070.../fulloutput
```
## References
* [Hadoop MapReduce documentation](https://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html)
* [Google Dataproc documentation](https://cloud.google.com/dataproc/docs/)

## See Also
* [Google's Map Reduce Engineer Round Table Discussion](https://youtu.be/NXCIItzkn3E)

## License
<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">Creative Commons Attribution 4.0 International License</a>.
