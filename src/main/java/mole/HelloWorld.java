package mole;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * 一維情況下的EM算法實現
 * @author aturbo
 *1、求期望（e-step)
 *2、期望最大化（估值）（M-step)
 *3、循環以上兩部直到收斂
 */
public class HelloWorld {
    private static final double[] points={1.0,1.3,2.2,2.6,2.8,5.0,7.3,7.4,7.5,7.7,7.9};
    private static double[][] w;
    private static double[] means = {7.7,2.3};//均值
    private static double[] variances= {1,1};//方差
    private static double[] probs = {0.5,0.5};//每个类的概率；这里默认选择k=2了；

    /**
     * 高斯分布计算公式，也就是先验概率
     * @param point
     * @param mean
     * @param variance
     * @return
     */
    private static double gaussianPro(double point,double mean,double variance){
        double prob = 0.0;
        prob = (1/(Math.sqrt(2*Math.PI)*Math.sqrt(variance)))*Math.exp(-(point-mean)*(point-mean)/(2*variance));
        return prob;
    }
    /**
     * E-step的主要逻辑
     * @param means
     * @param variances
     * @param points
     * @param probs
     * @return
     */
    private static double[][] countPostprob(double[] means,double[] variances,double[] points,double[] probs){
        int clusterNum = means.length;
        int pointNum = points.length;
        double[][] postProbs = new double[clusterNum][pointNum];
        double[] denominator = new double[pointNum];
        for(int m = 0;m <pointNum;m++){
            denominator[m] = 0.0;
            for(int n = 0;n<clusterNum;n++){
                denominator[m]+=(gaussianPro(points[m], means[n], variances[n])*probs[n]);
            }
        }
        for(int i = 0;i<clusterNum;i++){
            for(int j = 0;j<pointNum;j++){
                postProbs[i][j]=(gaussianPro(points[j], means[i], variances[i])*probs[i])/(denominator[j]);
            }
        }
        return postProbs;
    }
    private static void  eStep(){
        w = countPostprob(means, variances, points, probs);
    }
    /**
     * M-step的主要逻辑之一：由E-step得到的期望，重新计算均值
     * @param w
     * @param points
     * @return
     */
    private static double[] guessMean(double[][] w,double[] points){

        int wLength = w.length;
        double[] means = new double[w.length];
        double[] wi = new double[wLength];
        for (int m = 0; m < wLength; m++) {
            wi[m] = 0.0;
            for(int n = 0; n<points.length;n++){
                wi[m] += w[m][n];
            }
        }
        for(int i = 0;i<w.length;i++){
            means[i] = 0.0;
            for(int j = 0;j<points.length;j++){
                means[i]+=(w[i][j]*points[j]);
            }
            means[i] /= wi[i];
        }
        return means;
    }
    /**
     * M-step的主要逻辑之一：由E-step得到的期望，重新计算方差
     * @param w
     * @param points
     * @return
     */
    private static double[] guessVariance(double[][] w,double[] points){
        int wLength = w.length;
        double[] means = new double[w.length];
        double[] variances = new double[wLength];
        double[] wi = new double[wLength];
        for (int m = 0; m < wLength; m++) {
            wi[m] = 0.0;
            for(int n = 0; n<points.length;n++){
                wi[m] += w[m][n];
            }
        }
        means = guessMean(w, points);
        for(int i = 0;i<wLength;i++){
            variances[i] = 0.0;
            for(int j = 0;j<points.length;j++){
                variances[i] +=(w[i][j]*(points[j]-means[i])*(points[j]-means[i]));
            }
            variances[i] /= wi[i];
        }

        return variances;
    }
    /**
     * M-step的主要逻辑之一：由E-step得到的期望，重新计算概率
     * @param w
     * @return
     */
    private static double[] guessProb(double[][] w){
        int wLength = w.length;
        double[] probs = new double[wLength];
        for(int i = 0;i<wLength;i++){
            probs[i] = 0.0;
            for(int j = 0;j<w[i].length;j++){
                probs[i]+=w[i][j];
            }
            probs[i] /=w[i].length;
        }
        return probs;
    }
    private static void mStep(){
        means = guessMean(w, points);
        variances = guessVariance(w, points);
        probs = guessProb(w);
    }
    /**
     * 计算前后两次迭代的参数的差值
     * @param bef_values
     * @param values
     * @return
     */
    private static double threshold(double[] bef_values,double[] values){
        double diff = 0.0;
        for(int i = 0 ; i < values.length;i++){
            diff += (values[i]-bef_values[i]);
        }
        return Math.abs(diff);
    }
    public static void main(String[] args)throws Exception{

        int k = 2;
        w = new double[k][points.length];
        double[] bef_means;
        double[] bef_var;
        do{
            bef_means = means;
            bef_var = variances;
            eStep();
            mStep();
        }while(threshold(bef_means, means)<0.01&&threshold(bef_var, variances)<0.01);
        for(double prob:probs)
            System.out.println(prob);
        save();
    }
    public static void save() throws IOException {
        BufferedWriter pts = new BufferedWriter(new FileWriter("points.dat"));
        String pts_data = Arrays.toString(points);
        pts.write(pts_data);
        pts.close();
    }
}