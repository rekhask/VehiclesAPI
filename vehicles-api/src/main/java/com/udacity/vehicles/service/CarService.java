package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.Address;
import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.Price;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import java.util.List;
import java.util.Optional;

import org.hibernate.event.service.internal.EventListenerServiceInitiator;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.swing.text.html.Option;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository repository;
    private final MapsClient mapsClient;
    private final PriceClient priceClient;

    public CarService(CarRepository repository, MapsClient mapsClient, PriceClient priceClient) {
        this.repository = repository;
        this.mapsClient = mapsClient;
        this.priceClient = priceClient;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        List<Car> cars = repository.findAll();
        for (Car eachcar : cars) {

            //get the price
            String price = priceClient.getPrice(eachcar.getId());
            if (price != null) {
                eachcar.setPrice(price);
            }

            //get the location
            Location loc = mapsClient.getAddress(eachcar.getLocation());
            if (loc != null ) {
                eachcar.setLocation(loc);
            }
        }
        return cars;
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        Car car = new Car();
        Optional<Car> optionalCar = repository.findById(id);
        if (optionalCar.isPresent()) {
             car = optionalCar.get();

        } else {
            throw new CarNotFoundException("Car Not Found");
        }

         String price = priceClient.getPrice(id);
         if (price != null) {
             car.setPrice(price);
         }

        Location loc = mapsClient.getAddress(car.getLocation());
        if (loc != null ) {
            car.setLocation(loc);
        }
        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null && car.getId() != 0) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setCondition(car.getCondition());
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        carToBeUpdated.setModifiedAt(car.getModifiedAt());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }
        repository.save(car);
        return car ;
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        Optional<Car> optionalCar = repository.findById(id);
        if (optionalCar.isPresent()) {
            repository.deleteById(id);
        }
        else {
            throw new CarNotFoundException("Car Not Found");
        }
    }
}
