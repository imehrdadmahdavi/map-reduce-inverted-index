import java.io.IOException;
import java.util.StringTokenizer;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
//import org.apache.hadoop.mapreduce.MapContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class InvertedIndex {

  /*
  This is the Mapper class. It extends the hadoop's Mapper class.
  This maps input key/value pairs to a set of intermediate(output) key/value pairs.
  Here our input key is a Object and input value is a Text.
  And the output key is a Text and value is an Text. [word<Text> DocID<Text>]<->[aspect 5722018411]
  */
  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, Text>{

    /*
    Hadoop supported datatypes. This is a hadoop specific datatype that is used to handle
    numbers and Strings in a hadoop environment. IntWritable and Text are used instead of
    Java's Integer and String datatypes.
    Here 'one' is the number of occurance of the 'word' and is set to value 1 during the
    Map process.
    */
    //private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {

      // Split DocID and the actual text
      String DocId = value.toString().substring(0, value.toString().indexOf("\t"));
      String value_raw =  value.toString().substring(value.toString().indexOf("\t") + 1);
      
      // Reading input one line at a time and tokenizing by using space, "'", and "-" characters as tokenizers.
      StringTokenizer itr = new StringTokenizer(value_raw, " '-");
      
      // Iterating through all the words available in that line and forming the key/value pair.
      while (itr.hasMoreTokens()) {
        // Remove special characters
        word.set(itr.nextToken().replaceAll("[^a-zA-Z]", "").toLowerCase());
        if(word.toString() != "" && !word.toString().isEmpty()){
        /*
        Sending to output collector(Context) which in-turn passed the output to Reducer.
        The output is as follows:
          'word1' 5722018411
          'word1' 6722018415
          'word2' 6722018415
        */
          context.write(word, new Text(DocId));
        }
      }
    }
  }

  public static class IntSumReducer
       extends Reducer<Text,Text,Text,Text> {
    /*
    Reduce method collects the output of the Mapper calculate and aggregate the word's count.
    */
    public void reduce(Text key, Iterable<Text> values,
                       Context context
                       ) throws IOException, InterruptedException {

      HashMap<String,Integer> map = new HashMap<String,Integer>();
      /*
      Iterable through all the values available with a key [word] and add them together and give the
      final result as the key and sum of its values along with the DocID.
      */
      for (Text val : values) {
        if (map.containsKey(val.toString())) {
          map.put(val.toString(), map.get(val.toString()) + 1);
        } else {
          map.put(val.toString(), 1);
        }
      }
      StringBuilder docValueList = new StringBuilder();
      for(String docID : map.keySet()){
        docValueList.append(docID + ":" + map.get(docID) + " ");
      }
      context.write(key, new Text(docValueList.toString()));
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "inverted index");
    job.setJarByClass(InvertedIndex.class);
    job.setMapperClass(TokenizerMapper.class);
    // Commend out this part if you want to use combiner. Mapper and Reducer input and outputs type matching might be needed in this case. 
    //job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
