 import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class InvertedIndex {

	public static class Part1Mapper extends Mapper<Object, Text, Text, Text>{
		
		private Text Id = new Text();
        private Text word = new Text();

		public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
			
			String [] splitID = value.toString().split("\t",2);
			Id.set(splitID[0]);
			
			String words = splitID[1].toLowerCase().replaceAll("[^a-z ]"," ").replaceAll("\\s+", " ");
			StringTokenizer itr = new StringTokenizer(words);
			
			while (itr.hasMoreTokens()) {
				word.set(itr.nextToken());
				context.write(word, Id);
			}
		}
	}	

	public static class Part1Reducer extends Reducer<Text,Text,Text,Text> {
		public void reduce(Text key, Iterable<Text> values, Context context
                       	) throws IOException, InterruptedException {
			Map<String,Integer> m = new HashMap<String,Integer>();
			for (Text val : values) {
				String vals = val.toString();
				if (m.containsKey(vals)) m.put(vals, (Integer)(m.get(vals))+1);
				else m.put(vals, 1);
			}
			String ret = "";
			for (Map.Entry<String, Integer> entry:m.entrySet()) {
				if(!ret.equals("")) ret += '\t'; 
				ret += entry.getKey() + ":" + entry.getValue();
			}
			context.write(key, new Text(ret));
		}
  }

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Inverted Index");
		job.setJarByClass(InvertedIndex.class);
		job.setMapperClass(Part1Mapper.class);
    	job.setReducerClass(Part1Reducer.class);
    	job.setOutputKeyClass(Text.class);
    	job.setOutputValueClass(Text.class);
    	FileInputFormat.addInputPath(job, new Path(args[0]));
    	FileOutputFormat.setOutputPath(job, new Path(args[1]));
    	System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}