package VehicleModel {
    part def Vehicle {
        attribute batteryCapacity : ScalarValues::Real;
        part engine : Engine;
    }
    
    part def Engine;
    
    part myVehicle : Vehicle;
}