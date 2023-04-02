package com.driver.Repository;

import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Repository
public class AirportRepository {

    HashMap<String,Airport> airportMap = new HashMap<>();
    HashMap<Integer,Flight> flightMap = new HashMap<>();
    HashMap<Integer,Passenger> passengerMap = new HashMap<>();
    HashMap<Integer,List<Integer>> flightPassengersMap = new HashMap<>();

    HashMap<Integer,Integer> flightFareMap = new HashMap<>();


    public String addAirport(Airport airport){

        //Simply add airport details to your database
        //Return a String message "SUCCESS"
        airportMap.put(airport.getAirportName(),airport);
        return "SUCCESS";
    }

   // @GetMapping("/get-largest-aiport")
    public String getLargestAirportName(){

        //Largest airport is in terms of terminals. 3 terminal airport is larger than 2 terminal airport
        //Incase of a tie return the Lexicographically smallest airportName
        int noOfTerminals = 0;
        for(Airport airport:airportMap.values()){
            if(airport.getNoOfTerminals()>=noOfTerminals){
                noOfTerminals=airport.getNoOfTerminals();
            }
        }

        String str = "";
            for(Airport airport:airportMap.values()){
                if(airport.getNoOfTerminals()==noOfTerminals){
                    str = str + airport.getAirportName() + ",";
                }
            }
//            Arrays.sort(arr,String.CASE_INSENSITIVE_ORDER);
        String[] s = str.split(",");
        Arrays.sort(s,String.CASE_INSENSITIVE_ORDER);
        return s[0];
    }

   // @GetMapping("/get-shortest-time-travel-between-cities")
    public double getShortestDurationOfPossibleBetweenTwoCities(City fromCity, City toCity){

        //Find the duration by finding the shortest flight that connects these 2 cities directly
        //If there is no direct flight between 2 cities return -1.
        double minTime = Integer.MAX_VALUE;
        for(Flight flight:flightMap.values()){
            if(flight.getFromCity()==fromCity && flight.getToCity()==toCity && minTime>=flight.getDuration()){
                minTime = flight.getDuration();
            }
        }
        if(minTime==0)
            return -1;
        else
            return minTime;
        //return 0;
    }

    //@GetMapping("/get-number-of-people-on-airport-on/{date}")
    public int getNumberOfPeopleOn(Date date,String airportName){

        //Calculate the total number of people who have flights on that day on a particular airport
        //This includes both the people who have come for a flight and who have landed on an airport after their flight
        List<Integer> l = new ArrayList<>();
        for(Flight flight:flightMap.values()){
            if(Objects.equals(flight.getFlightDate(), date)){
                l.add(flight.getFlightId());
            }
        }
        Airport airport = airportMap.get(airportName);
        int totalNoOfPeople = 0;
        for(int i:l){
            Flight flight = flightMap.get(i);
            if(flight.getFromCity()==airport.getCity() || flight.getToCity()==airport.getCity()){
                int flightId = flight.getFlightId();
                int noOfPassenger = flightPassengersMap.get(flightId).size();
                totalNoOfPeople += noOfPassenger;
            }
        }

        return totalNoOfPeople;
    }

   // @GetMapping("/calculate-fare")
    public int calculateFlightFare(Integer flightId){

        //Calculation of flight prices is a function of number of people who have booked the flight already.
        //Price for any flight will be : 3000 + noOfPeopleWhoHaveAlreadyBooked*50
        //Suppose if 2 people have booked the flight already : the price of flight will be 3000 + 2*50 = 3100
        //This will not include the current person who is trying to book, he might also be jsut checking price
        int flightFare = 0;
        List<Integer> passengerList = flightPassengersMap.get(flightId);
        if(passengerList.size()==0)
            flightFare = 3000;
        else
            flightFare = 3000 + (passengerList.size() * 50);

        return flightFare;

    }


   // @PostMapping("/book-a-ticket")
    public String bookATicket(Integer flightId,Integer passengerId){

        //If the numberOfPassengers who have booked the flight is greater than : maxCapacity, in that case :
        //return a String "FAILURE"
        //Also if the passenger has already booked a flight then also return "FAILURE".
        //else if you are able to book a ticket then return "SUCCESS"
        if(flightPassengersMap.containsKey(flightId) && flightMap.get(flightId).getMaxCapacity()<=flightPassengersMap.get(flightId).size())
            return "FAILURE";
        if(flightPassengersMap.containsKey(flightId) && flightPassengersMap.get(flightId).contains(passengerId))
            return "FAILURE";

        int fare = 0;
        if(flightPassengersMap.containsKey(flightId)){
            List<Integer> passengerList = flightPassengersMap.get(flightId);
            fare = 3000 + (passengerList.size() * 50);
            passengerList.add(passengerId);
            flightPassengersMap.put(flightId,passengerList);

            int totalRevenue = flightFareMap.get(flightId) + fare;
            flightFareMap.put(flightId,totalRevenue);

        }else {
            List<Integer> passengerList = new ArrayList<>();
            passengerList.add(passengerId);
            flightPassengersMap.put(flightId,passengerList);
            flightFareMap.put(flightId,3000);
        }
        return "SUCCESS";
    }

    //@PutMapping("/cancel-a-ticket")
    public String cancelATicket(Integer flightId,Integer passengerId){

        //If the passenger has not booked a ticket for that flight or the flightId is invalid or in any other failure case
        // then return a "FAILURE" message
        // Otherwise return a "SUCCESS" message
        // and also cancel the ticket that passenger had booked earlier on the given flightId
        if(!flightMap.containsKey(flightId))
            return "FAILURE";

        List<Integer> passengers = flightPassengersMap.get(flightId);
        if(!passengers.contains(passengerId))
            return "FAILURE";

        int fare = 3000 - (passengers.size() * 50);
        passengers.remove(passengerId);
        flightPassengersMap.put(flightId,passengers);

        int revenue = flightFareMap.get(flightId);
        revenue -= fare;
        flightFareMap.put(flightId,revenue);

        return "SUCCESS";
    }


    //@GetMapping("/get-count-of-bookings-done-by-a-passenger/{passengerId}")
    public int countOfBookingsDoneByPassengerAllCombined(Integer passengerId){

        //Tell the count of flight bookings done by a passenger: This will tell the total count of flight bookings done by a passenger :
        int count = 0;
        for(int id:flightPassengersMap.keySet()){
            List<Integer> passengerList = flightPassengersMap.get(id);
            if(passengerList.contains(passengerId))
                count++;
        }

        return count;
    }

    //@PostMapping("/add-flight")
    public String addFlight(Flight flight){

        flightMap.put(flight.getFlightId(),flight);
        //Return a "SUCCESS" message string after adding a flight.
        return "SUCCESS";
    }


    //@GetMapping("/get-aiportName-from-flight-takeoff/{flightId}")
    public String getAirportNameFromFlightId(Integer flightId){

        //We need to get the starting airport from where the flight will be taking off
        //return null incase the flightId is invalid or you are not able to find the airportName
        Flight flight = flightMap.get(flightId);
        if(!airportMap.isEmpty()) {
            for (Airport airport : airportMap.values()) {
                if (flight.getFromCity() == airport.getCity())
                    return airport.getAirportName();
            }
        }else
           return null;
        return null;
    }


    //@GetMapping("/calculate-revenue-collected/{flightId}")
    public int calculateRevenueOfAFlight(Integer flightId){

        //Calculate the total revenue that a flight could have
        int count = 0;
        count = flightFareMap.get(flightId);
        return count;
    }


    //@PostMapping("/add-passenger")adac
    public String addPassenger(Passenger passenger){
        passengerMap.put(passenger.getPassengerId(),passenger);
        //Add a passenger to the database
        return "SUCCESS";
    }
}
