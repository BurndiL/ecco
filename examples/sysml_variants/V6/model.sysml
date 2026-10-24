package VehicleModel {
    part def Vehicle {
        attribute passengerCapacity : ScalarValues::Integer;
        attribute batteryCapacity : ScalarValues::Real;
    }
    
    part myVehicle : Vehicle;
}