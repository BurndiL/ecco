package SensorSystem {
    item def Measurement {
        attribute value : ScalarValues::Real;
    }
    
    part def SensorNode {
        attribute sampleRate : ScalarValues::Real;
        item latestMeasurement : Measurement;
    }
    
    part sensor : SensorNode;
}
