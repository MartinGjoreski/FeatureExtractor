# Example usage

1. In the dependencies of your Gradle file (app) add:

implementation 'org.apache.commons:commons-math3:3.6.1'



2. Uset the FeatureExtractionAcc in your code:

arrayList<float[]> mSensorReadings;
FeatureExtractionAcc featueExtractor = new FeatureExtractionAcc();

 @Override
    public void onSensorChanged(SensorEvent event) {
      float[] values = event.values;
      float x = values[0];
      float y = values[1];
      float z = values[2];
      mSensorReadings.add(new float[]{x,y,z});
      
      
      feature_extraction_window = 100 //extract features every 100 samples
      
      if (mSensorReadings.size()>feature_extraction_window){
         //get features for the current sensor readings
         double [] features = featueExtractor.extractStatFeatures(mSensorReadings);   
         
         //print features values and feature names in Logcat
         Log.d(TAG, Arrays.toString(features));
         Log.d(TAG, Arrays.toString(fa.feature_names));
      }
      
   }
