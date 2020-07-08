package mole;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.*;
import java.util.Random;

public class GMM {
    private static final int PSEQLEN = 100;
    private static final int DIM=2;
    private static final int ITNUM = 5;
    private static int nMix=6;
    private static final String PARAMPATH = "PARAM.dat";
    private static double[]points;
    private static double[] means;
    private static double[] variances;
    private static double[] prob;
    private static double[][] gamma;

    public GMM(int _nMix,boolean loadFile) throws IOException {
        nMix = _nMix;
        points = new double[PSEQLEN];
        prob = new double[nMix];
        means = new double[nMix];
        variances = new double[nMix];
        gamma = new double[nMix][PSEQLEN];
        Random rnd1 = new Random();

        for(int i =0;i<PSEQLEN;i++){
            points[i] = rnd1.nextDouble();
        }
        //randomly generated
        if (!loadFile){
            for(int i=0;i<nMix;i++){
                prob[i] = rnd1.nextDouble();
                means[i] = rnd1.nextDouble();
                variances[i] = rnd1.nextDouble();
                saveParameters(PARAMPATH);
            }
        }else{
            loadParameters(PARAMPATH);
        }
    }
    public double[] getPosterior(double xt){
        double[] post = new double[nMix];
        double[] likelh = new double[nMix];
        double sum = 0.0;
        for(int i=0;i<nMix;i++){
            likelh[i] = prob[i]*gaussianProb(xt,means[i],variances[i]);
            sum += likelh[i];
        }
        for(int i=0;i<nMix;i++){
            post[i] = likelh[i]/sum;
        }
        return post;
    }
    private static double gaussianProb(double point, double mean, double variance) {
        double prob = 0.0;
        prob = (1/(Math.sqrt(2*Math.PI)*Math.sqrt(variance)))*Math.exp(-(point-mean)*(point-mean)/(2*variance));
        return prob;
    }
    public void maximize(WritableStruct writableStruct){
        double[] ss0 = writableStruct.getSs0();
        double[] ss1 = writableStruct.getSs1();
        double[] ss2 = writableStruct.getSs2();
        double Nk = 0;
        for(int i=0;i<nMix;i++){
            Nk+=ss0[i];
        }
        for(int i=0;i<nMix;i++) {
            prob[i] = ss0[i] / Nk;
            means[i] = ss1[i] / ss0[i];
            variances[i] = ss2[i] / ss0[i] - variances[i] * variances[i];
        }
    }
    public void loadParameters(String filepath)throws IOException{
        Path pt = new Path(filepath);
        FileSystem fs = FileSystem.get(new Configuration());
        BufferedReader br = new BufferedReader(
                new InputStreamReader(fs.open(pt))
        );
        String line = br.readLine();
        String[] token = line.split(" ");
        for(int i=0;i<nMix;i++){
            prob[i] = Double.parseDouble(token[i]);
        }
        line = br.readLine();
        token = line.split(" ");
        for(int i=0;i<nMix;i++){
            means[i] = Double.parseDouble(token[i]);
        }
        line = br.readLine();
        token = line.split(" ");
        for(int i=0;i<nMix;i++){
            variances[i] = Double.parseDouble(token[i]);
        }
        br.close();
    }
    public void saveParameters(String filepath){
        Path pt = new Path(filepath);
        FileSystem fs;
        try {
            fs =FileSystem.get(new Configuration());
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fs.create(pt,true)));
            bw.write(this.toString());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void setMeans(double[] means) {
        GMM.means = means;
    }

    public static void setVariances(double[] variances) {
        GMM.variances = variances;
    }

    public static void setProb(double[] prob) {
        GMM.prob = prob;
    }

    public double[] getMeans() {
        return means;
    }

    public static double[] getPoints() {
        return points;
    }

    public static double[] getVariances() {
        return variances;
    }

    public static double[] getProb() {
        return prob;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<nMix;i++)
            sb.append(String.format("%.5f ",prob[i]));
        sb.append("\n");
        for(int i=0;i<nMix;i++)
            sb.append(String.format("%.5f ",means[i]));
        sb.append("\n");
        for(int i=0;i<nMix;i++)
            sb.append(String.format("%.5f ",variances[i]));
        sb.append("\n");
        return sb.toString();
    }
}
