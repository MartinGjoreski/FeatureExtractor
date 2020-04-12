# Example usage

1. In the dependencies of your Gradle file (app) add:

implementation 'org.apache.commons:commons-math3:3.6.1'



2. Uset the FeatureExtractionAcc in your code:

arrayList<float[]> mSensorReadings;
FeatureExtractionAcc featueExtractor;

 @Override
    public void onSensorChanged(SensorEvent event) {
      float[] values = event.values;
      float x = values[0];
      float y = values[1];
      float z = values[2];
      mSensorReadings.add(new float[]{x,y,z});
      
      
      feature_extraction_window = 100 //extract features every 100 samples
      
      if (mSensorReadings.size()>feature_extraction_window){
         double [] features = featueExtractor.extractStatFeatures(mSensorReadings);    
      }
      
   }
