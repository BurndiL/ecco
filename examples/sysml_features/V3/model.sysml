package SensorSystem {
    item def Measurement {
        attribute value : ScalarValues::Real;
    }
    
    port def DataPort;
    
    part def SensorNode {
        attribute sampleRate : ScalarValues::Real;
        item latestMeasurement : Measurement;
        port dataOut : DataPort;
    }
    
    part def Gateway {
        port dataIn : DataPort;
    }
    
    part sensor : SensorNode;
    part gateway : Gateway;
    
    connection sensorToGateway connect sensor.dataOut to gateway.dataIn;
}
