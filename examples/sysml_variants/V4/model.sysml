package VehicleModel {
    part def Vehicle {
        attribute passengerCapacity : ScalarValues::Integer;
        part engine : Engine;
    }
    
    part def Engine;
    
    part myVehicle : Vehicle;
}