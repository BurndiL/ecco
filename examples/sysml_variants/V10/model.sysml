package VehicleModel {
    requirement def SafetyRequirement {
        doc /* The vehicle must protect its passengers. */
    }
    
    part def Vehicle {
        attribute passengerCapacity : ScalarValues::Integer;
        attribute batteryCapacity : ScalarValues::Real;
        part engine : Engine;
        
        satisfy SafetyRequirement;
    }
    
    part def Engine;
    
    part myVehicle : Vehicle;
}