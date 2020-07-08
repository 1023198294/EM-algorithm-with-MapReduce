package parellel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.function.Function2D;
import org.jfree.data.function.NormalDistributionFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.util.Random;

public class GMM {
    private static final int PSEQLEN = 100;
    private static final int DIM=2;
    private static final int ITNUM = 5;
    private static final int nMix=6;
    private static double[]points;
    private static double[] means;
    private static double[] variances;
    private static double[] prob;
    private static double[][] gamma;
    private static Paint[] paints= {Color.red,Color.black,Color.pink,Color.green,Color.blue,Color.orange};
    public GMM(){
        init();
    }
    public static void init(){
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

        for(int i=0;i<nMix;i++){
                prob[i] = rnd1.nextDouble();
                means[i] = rnd1.nextDouble();
                variances[i] = rnd1.nextDouble();
            }
        }

    public static void main(String[] args) {
        init();
        guess("before");
        for(int i=1;i<ITNUM;i++){
            e_step();
            m_step();
            guess("after "+i+" iterations");
        }

    }

    private static void e_step() {
        double[][] postProbs = new double[nMix][PSEQLEN];
        double[] denominator = new double[PSEQLEN];
        for(int m=0;m<PSEQLEN;m++){
            denominator[m] = 0.0;
            for(int n=0;n<nMix;n++){
                denominator[m] += gaussianProb(points[m],means[n],variances[n])*prob[n];
            }
        }
        for(int i=0;i<nMix;i++){
            for(int j=0;j<PSEQLEN;j++)
                postProbs[i][j] = (gaussianProb(points[j],means[i],variances[i])*prob[i])/(denominator[j]);
        }
        gamma = postProbs;
    }

    private static double gaussianProb(double point, double mean, double variance) {
        double prob = 0.0;
        prob = (1/(Math.sqrt(2*Math.PI)*Math.sqrt(variance)))*Math.exp(-(point-mean)*(point-mean)/(2*variance));
        return prob;
    }

    private static void m_step() {
        double[] ms = new double[nMix];
        double[] gi = new double[nMix];
        double[] vs = new double[nMix];
        double[] ps = new double[nMix];
        for(int m=0;m<nMix;m++){
            gi[m] = 0.0;
            for(int n=0;n<PSEQLEN;n++)
                gi[m]+=gamma[m][n];
        }
        for(int i=0;i<nMix;i++){
            ms[i] = 0.0;
            for(int j=0;j<PSEQLEN;j++){
                ms[i]+=(gamma[i][j]*points[j]);
            }
            ms[i] /= gi[i];
        }
        means = ms;
        // update means
        for(int m=0;m<nMix;m++){
            gi[m] = 0.0;
            for(int n=0;n<PSEQLEN;n++){
                gi[m] += gamma[m][n];
            }
        }
        for(int i=0;i<nMix;i++){
            vs[i] = 0.0;
            for(int j=0;j<PSEQLEN;j++){
                vs[i] += (gamma[i][j]*(points[j]-means[i])*(points[j]-means[i]));
            }
        }
        variances = vs;
        //update variance

        for(int i=0;i<nMix;i++){
            ps[i] = 0;
            for(int j=0;j<PSEQLEN;j++){
                ps[i] += gamma[i][j];
            }
            ps[i] /= PSEQLEN;
        }
        prob = ps;
    }
    public static void guess(String flag){
        double err = 0;
        double[] pred = new double[PSEQLEN];
        final JFreeChart chart = ChartFactory.createXYLineChart(
                "distribution: "+flag,
                "x",
                "y",
                null,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        XYPlot xyPlot = chart.getXYPlot();
        for(int i=0;i<nMix;i++) {
            NumberAxis numberAxis = new NumberAxis("Axis "+i);
            numberAxis.setLabelPaint(paints[i]);
            Function2D normal = new NormalDistributionFunction2D(means[i],variances[i]);
            XYDataset dataset = DatasetUtilities.sampleFunction2D(normal,-2.0,2.0,100,"part "+i);
            xyPlot.setDataset(i,dataset);
            StandardXYItemRenderer standardxyitemrenderer = new StandardXYItemRenderer();
            standardxyitemrenderer.setSeriesPaint(i, paints[i]);
            xyPlot.setRenderer(i, standardxyitemrenderer);
            //xyPlot.setRangeAxis(i,numberAxis);
        }
        XYItemRenderer xyitem = xyPlot.getRenderer();
        xyPlot.setRenderer(xyitem);

        xyPlot.setBackgroundPaint(Color.LIGHT_GRAY);
        xyPlot.setRangeGridlinePaint(Color.BLUE);//背景底部横虚线
        xyPlot.setOutlinePaint(Color.RED);//边界线


        ChartFrame mChartFrame = new ChartFrame("折线图", chart);
        mChartFrame.pack();
        mChartFrame.setVisible(true);

        for(double p:points){
            System.out.println(p);
        }
        /*for(int i=0;i<PSEQLEN;i++){
            for(int j=0;j<nMix;j++){
                //double temp = prob[j]*Math.pow(2*Math.PI,DIM/2.0)*1/variances[j]* Math.exp(0.5*(points[i]-means[j]));
                pred[i] += prob[j]*gaussianProb(points[i],means[j],variances[j]);
                err += Math.abs(points[i]-pred[i]);
                }
            }*/
            /*
            System.out.print(flag);
            System.out.print(":\npoints: ");
            System.out.println(Arrays.toString(points));
            System.out.println(Arrays.toString(pred));
            System.out.println("error:"+err);
            */
    }
}


