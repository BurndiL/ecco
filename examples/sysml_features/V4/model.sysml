package SensorSystem {
    item def Measurement {
        attribute value : ScalarValues::Real;
    }
    
    port def DataPort;
    
    action def SampleSensor {
        out item result : Measurement;
    }
    
    part def SensorNode {
        attribute sampleRate : ScalarValues::Real;
        item latestMeasurement : Measurement;
        port dataOut : DataPort;
        action sample : SampleSensor;
    }
    
    part def Gateway {
        port dataIn : DataPort;
    }
    
    part sensor : SensorNode;
    part gateway : Gateway;
    
    connection sensorToGateway connect sensor.dataOut to gateway.dataIn;
    flow of Measurement from sensor.dataOut to gateway.dataIn;
}
