/**
 * Created by martin on 12/04/2020.
 */

import org.apache.commons.math3.stat.descriptive.moment.*;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.util.ArrayList;

public class FeatureExtractionAcc {

    Kurtosis kurtosis;
    Mean mean ;
    Variance variance ;
    Skewness skewness ;
    Percentile percentile_25;
    Percentile percentile_75;
    PearsonsCorrelation pearsonsCorrelation;

    public FeatureExtractionAcc()
    {
        mean = new Mean();
        variance = new Variance();
        kurtosis = new Kurtosis();
        skewness = new Skewness();
        percentile_25 = new Percentile(25);
        percentile_75 = new Percentile(75);
        pearsonsCorrelation = new PearsonsCorrelation();
    }

    //For each axis and their magnitude calculate:
    //four statistical moments
    // zero crossing rate, 25th and 75th percentile
    // between-axis correlation
    public double [] extractStatFeatures(ArrayList<float[]> mSensorReadings)
    {
        //Android acceleration sensor provides data in float values. Transform them to double to applay apache functions easier, as they work with doubles
        double [] x_acc = new double[mSensorReadings.size()];
        double [] y_acc = new double[mSensorReadings.size()];
        double [] z_acc = new double[mSensorReadings.size()];
        double [] magnitude = new double[mSensorReadings.size()];

        for (int i=0; i<mSensorReadings.size(); i++) {
            float[] sample = mSensorReadings.get(i);
            x_acc[i] = sample[0];
            y_acc[i] = sample[1];
            z_acc[i] = sample[2];
            float mag = (float) Math.sqrt(Math.pow(sample[0], 2) + Math.pow(sample[1], 2) + Math.pow(sample[2], 2));
            magnitude[i] = mag;
        }

        //perform moving average with window size of 3 samples
        x_acc = movingAverageFilter(x_acc,3);
        y_acc = movingAverageFilter(y_acc,3);
        z_acc = movingAverageFilter(z_acc,3);
        magnitude = movingAverageFilter(magnitude,3);

        double xy_correlation = pearsonsCorrelation.correlation(x_acc,y_acc);
        double xz_correlation = pearsonsCorrelation.correlation(x_acc,z_acc);
        double yz_correlation = pearsonsCorrelation.correlation(y_acc,z_acc);


        return new double[]{mean.evaluate(x_acc),variance.evaluate(x_acc),kurtosis.evaluate(x_acc), skewness.evaluate(x_acc),               //x-axis features
                MCR(x_acc),percentile_25.evaluate(x_acc),percentile_75.evaluate(x_acc),                                                     //x-axis features

                mean.evaluate(y_acc),variance.evaluate(y_acc),kurtosis.evaluate(y_acc), skewness.evaluate(y_acc),               //y-axis features
                MCR(y_acc),percentile_25.evaluate(y_acc),percentile_75.evaluate(y_acc),                                         //y-axis features

                mean.evaluate(z_acc),variance.evaluate(z_acc),kurtosis.evaluate(z_acc), skewness.evaluate(z_acc),               //z-axis features
                MCR(z_acc),percentile_25.evaluate(z_acc),percentile_75.evaluate(z_acc),                                         //z-axis features

                mean.evaluate(magnitude),variance.evaluate(magnitude),kurtosis.evaluate(magnitude), skewness.evaluate(magnitude),               //magnitude features
                MCR(magnitude),percentile_25.evaluate(magnitude),percentile_75.evaluate(magnitude),                                             //magnitude features

                xy_correlation,xz_correlation,yz_correlation};                                                                  //correlation features
    }



    //calculate zero crossing rate
    private float MCR(double [] array)
    {
        double mean_value = mean.evaluate(array);
        float MCR = 0;
        for (int i=1; i<array.length; i++)
            if ((array[i] - mean_value) * (array[i - 1] - mean_value) < 0)
                MCR++;
        return  MCR/array.length;
    }


    //perform moving average
    private double [] movingAverageFilter(double [] array, int window_size)
    {
        double [] ma_array =  new double[array.length-window_size];
        for (int i=0; i<array.length-window_size; i++)
            ma_array[i] =  mean.evaluate(array,i,window_size);

        return ma_array;

    }

}
