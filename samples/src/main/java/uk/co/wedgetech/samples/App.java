package uk.co.wedgetech.samples;


import com.sfeatherstone.geodesy.LatLon;
import com.sfeatherstone.geodesy.model.ellipsoidal.Vincenty;
import com.sfeatherstone.geodesy.model.spherical.Spherical;
import com.sfeatherstone.geodesy.model.spherical.vectors.Vectors;

public class App {
    public String getGreeting() {

        LatLon cambridge = new LatLon(52.205, 0.119);
        LatLon greenwich = new LatLon(51.4778, -0.0015);

        double distance = Vincenty.distance(cambridge, greenwich);
        double bearing = Vincenty.finalBearingTo(cambridge, greenwich);

        distance = Spherical.distance(cambridge, greenwich);
        bearing = Spherical.finalBearingTo(cambridge, greenwich);

        distance = Vectors.distance(cambridge, greenwich);
        bearing = Vectors.bearingTo(cambridge, greenwich);

        return "Hello";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
    }
}