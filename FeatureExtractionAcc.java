/**
 * Created by martin on 12/04/2020.
 */

import org.apache.commons.math3.stat.descriptive.moment.*;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.util.ArrayList;

public class FeatureExtractionAcc {

    public String [] feature_names = new String[] {"x_mean","x_variance","x_kurtosis","x_skewness","x_percentile_25","x_percentile_75","x_kurtosis",
                                            "x_diff_mean","x_diff_variance","x_diff_kurtosis","x_diff_skewness","x_diff_percentile_25","x_diff_percentile_75","x_diff_kurtosis",
                                                   
                                            "y_mean","y_variance","y_kurtosis","y_skewness","y_percentile_25","y_percentile_75","y_kurtosis",
                                            "y_diff_mean","y_diff_variance","y_diff_kurtosis","y_diff_skewness","y_diff_percentile_25","y_diff_percentile_75","y_diff_kurtosis",
                                                   
                                            "z_mean","z_variance","z_kurtosis","z_skewness","z_percentile_25","z_percentile_75","z_kurtosis",
                                            "z_diff_mean","z_diff_variance","z_diff_kurtosis","z_diff_skewness","z_diff_percentile_25","z_diff_percentile_75","z_diff_kurtosis",
                                                   
                                            "magn_mean","magn_variance","magn_kurtosis","magn_skewness","magn_percentile_25","magn_percentile_75","magn_kurtosis",
                                            "magn_diff_mean","magn_diff_variance","magn_diff_kurtosis","magn_diff_skewness","magn_diff_percentile_25","magn_diff_percentile_75","magn_diff_kurtosis",
                                                   
                                            "xy_correlation","xz_correlation","yz_correlation"};

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

    //For each axis, their magnitude, and their first derivatives calculate:
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

        //calculate signal's first derivative
        double [ ] x_acc_diff = getDerivative(x_acc);
        double [ ] y_acc_diff = getDerivative(y_acc);
        double [ ] z_acc_diff = getDerivative(z_acc);
        double [ ] magnitude_diff = getDerivative(magnitude);

        double xy_correlation = pearsonsCorrelation.correlation(x_acc,y_acc);
        double xz_correlation = pearsonsCorrelation.correlation(x_acc,z_acc);
        double yz_correlation = pearsonsCorrelation.correlation(y_acc,z_acc);


        return new double[]{mean.evaluate(x_acc),variance.evaluate(x_acc),kurtosis.evaluate(x_acc), skewness.evaluate(x_acc),               //x-axis features
                MCR(x_acc),percentile_25.evaluate(x_acc),percentile_75.evaluate(x_acc),                                                     //x-axis features

                mean.evaluate(x_acc_diff),variance.evaluate(x_acc_diff),kurtosis.evaluate(x_acc_diff), skewness.evaluate(x_acc_diff),                       //x-derivative features
                MCR(x_acc_diff),percentile_25.evaluate(x_acc_diff),percentile_75.evaluate(x_acc_diff),                                                     //x-derivative features

                mean.evaluate(y_acc),variance.evaluate(y_acc),kurtosis.evaluate(y_acc), skewness.evaluate(y_acc),               //y-axis features
                MCR(y_acc),percentile_25.evaluate(y_acc),percentile_75.evaluate(y_acc),                                         //y-axis features

                mean.evaluate(y_acc_diff),variance.evaluate(y_acc_diff),kurtosis.evaluate(y_acc_diff), skewness.evaluate(y_acc_diff),               //y-derivative features
                MCR(y_acc_diff),percentile_25.evaluate(y_acc_diff),percentile_75.evaluate(y_acc_diff),                                             //y-derivative features

                mean.evaluate(z_acc),variance.evaluate(z_acc),kurtosis.evaluate(z_acc), skewness.evaluate(z_acc),               //z-axis features
                MCR(z_acc),percentile_25.evaluate(z_acc),percentile_75.evaluate(z_acc),                                         //z-axis features

                mean.evaluate(z_acc_diff),variance.evaluate(z_acc_diff),kurtosis.evaluate(z_acc_diff), skewness.evaluate(z_acc_diff),               //z-derivative features
                MCR(z_acc_diff),percentile_25.evaluate(z_acc_diff),percentile_75.evaluate(z_acc_diff),                                             //z-derivative features

                mean.evaluate(magnitude),variance.evaluate(magnitude),kurtosis.evaluate(magnitude), skewness.evaluate(magnitude),               //magnitude features
                MCR(magnitude),percentile_25.evaluate(magnitude),percentile_75.evaluate(magnitude),                                             //magnitude features

                mean.evaluate(magnitude_diff),variance.evaluate(magnitude_diff),kurtosis.evaluate(magnitude_diff), skewness.evaluate(magnitude_diff),           //magnitude-derivative features
                MCR(magnitude_diff),percentile_25.evaluate(magnitude_diff),percentile_75.evaluate(magnitude_diff),                                             //magnitude-derivative features

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

    //calculate first derivative
    private double [] getDerivative(double [] array)
    {
        double [] array_diff =  new double[array.length-1];
        for (int i=0; i<array.length-1; i++)
            array_diff[i] =  array[i+1]-array[i];

        return array_diff;

    }


}
