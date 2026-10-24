package SensorSystem {
    item def Measurement {
        attribute value : ScalarValues::Real;
    }
    
    port def DataPort;
    
    requirement def ReliableSampling {
        doc /* The sensor shall provide a measurement at every sampling interval. */
    }
    
    constraint def ValidMeasurement {
        inv valueIsNonNegative {
            value >= 0.0
        }
    }
    
    state def SensorLifecycle {
        state idle;
        state sampling;
        state transmitting;
        transition idleToSampling first idle then sampling;
        transition samplingToTransmitting first sampling then transmitting;
    }
    
    action def SampleSensor {
        out item result : Measurement;
    }
    
    part def SensorNode {
        attribute sampleRate : ScalarValues::Real;
        item latestMeasurement : Measurement;
        port dataOut : DataPort;
        action sample : SampleSensor;
        satisfy ReliableSampling;
        state lifecycle : SensorLifecycle;
    }
    
    part def Gateway {
        port dataIn : DataPort;
    }
    
    part sensor : SensorNode;
    part gateway : Gateway;
    
    connection sensorToGateway connect sensor.dataOut to gateway.dataIn;
    flow of Measurement from sensor.dataOut to gateway.dataIn;
}
