package VehicleModel {
    requirement def SafetyRequirement {
        doc /* The vehicle must protect its passengers. */
    }
    
    part def Vehicle {
        satisfy SafetyRequirement;
    }
    
    part myVehicle : Vehicle;
}