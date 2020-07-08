package mole;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class WritableStruct implements Writable {
    private final static int nMix =Config.nMix;
    private final static int DIM = 1;
    private double[] ss0; 	// 0th-order sufficient statistics //sum gamma
    private double[] ss1; // 1st-order sufficient statistics //sum gamma*x
    private double[] ss2; // 2nd-order sufficient statistics //sum gamma*(x-mu)^2
    public WritableStruct(){
        ss0 = new double[nMix];
        ss1 = new double[nMix];
        ss2 = new double[nMix];
    }
    @Override
    public void write(DataOutput dataOutput) throws IOException {
        writeDoubleArray(dataOutput,ss0);
        writeDoubleArray(dataOutput,ss1);
        writeDoubleArray(dataOutput,ss2);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        readDoubleArray(dataInput,ss0);
        readDoubleArray(dataInput,ss1);
        readDoubleArray(dataInput,ss2);
    }
    private void writeDoubleArray(DataOutput out, double[] dArray) throws IOException {
        for (double d : dArray) {
            out.writeDouble(d);
        }
    }
    private void readDoubleArray(DataInput in, double[] dArray) throws IOException {
        for (int i=0; i<dArray.length; i++) {
            dArray[i] = in.readDouble();
        }
    }
    public void accumulate(double[] gamma, double xt, double[] mu) {
        for (int i = 0; i < gamma.length; i++) {
            ss0[i] += gamma[i];
            ss1[i] += gamma[i]*xt;
            ss2[i] += gamma[i]*xt*xt;
        }
    }
    public void accumulate(WritableStruct curwritableStruct){
        for(int i=0;i<curwritableStruct.ss0.length;i++){
            ss0[i]+=curwritableStruct.ss0[i];
            ss1[i]+=curwritableStruct.ss1[i];
            ss2[i]+=curwritableStruct.ss2[i];
        }
    }

    public double[] getSs0() {
        return ss0;
    }

    public double[] getSs1() {
        return ss1;
    }

    public double[] getSs2() {
        return ss2;
    }

}
