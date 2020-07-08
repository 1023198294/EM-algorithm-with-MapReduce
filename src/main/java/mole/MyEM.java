package mole;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.Iterator;

/**
 * 一維情況下的EM算法實現
 * @author aturbo
 *1、求期望（e-step)
 *2、期望最大化（估值）（M-step)
 *3、循環以上兩部直到收斂
 */
public class MyEM {
    private static int ifLoad = 0;
    private static GMM gmm;

    static {
        try {
            gmm = new GMM(Config.nMix,false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception{
        Configuration conf = new Configuration();
        System.out.println("debug start");
        Job job = Job.getInstance(conf,"MyEM");

        job.setJarByClass(MyEM.class);

        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(WritableStruct.class);

        job.setMapperClass(myMapper.class);
        job.setCombinerClass(myCombiner.class);
        job.setReducerClass(myReducer.class);
        job.setNumReduceTasks(1);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.addInputPath(job,new Path(args[0]));
        FileOutputFormat.setOutputPath(job,new Path(args[1]));
        ifLoad = Integer.parseInt(args[2]);
        if(ifLoad>0){
            gmm.loadParameters("PARAM.dat");
        }
        job.waitForCompletion(true);
    }
    public static class myMapper extends Mapper<LongWritable,Text,IntWritable,WritableStruct>{
        private final static IntWritable keyOut = new IntWritable(1);
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String token = value.toString();
            double xt = Double.parseDouble(token);
            WritableStruct writableStruct = new WritableStruct();
            double[] gamma = new double[Config.nMix];
            //int PSEQLEN = Config.PSEQLEN;
            int nMix = Config.nMix;
            double[] gmmPosterior = gmm.getPosterior(xt);
            writableStruct.accumulate(gmmPosterior,xt, gmm.getMeans());
            context.write(keyOut,writableStruct);
        }
    }
    public static class myCombiner extends Reducer<IntWritable,WritableStruct,IntWritable,WritableStruct>{
        @Override
        protected void reduce(IntWritable key, Iterable<WritableStruct> values, Context context) throws IOException, InterruptedException {
            Iterator<WritableStruct> iter = values.iterator();
            WritableStruct writableStruct = new WritableStruct();
            while(iter.hasNext()){
                WritableStruct curWritableStruct = iter.next();
                writableStruct.accumulate(curWritableStruct);
            }
            context.write(key,writableStruct);
        }
    }
    public static class myReducer extends Reducer<IntWritable,WritableStruct,IntWritable,Text>{
        @Override
        protected void reduce(IntWritable key, Iterable<WritableStruct> values, Context context) throws IOException, InterruptedException {
            Iterator<WritableStruct> iter = values.iterator();
            WritableStruct writableStruct = new WritableStruct();
            while (iter.hasNext()){
                WritableStruct curwritableStruct = iter.next();
                writableStruct.accumulate(curwritableStruct);
            }
            gmm.maximize(writableStruct);
            gmm.saveParameters("PARAM.dat");
            System.out.println(gmm.toString());
            Text valueOut = new Text();
            valueOut.set(gmm.toString());
            context.write(key,valueOut);
        }
    }
}