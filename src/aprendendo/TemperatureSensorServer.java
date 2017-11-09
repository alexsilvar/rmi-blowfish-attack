/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aprendendo;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 *
 * @author Avell B155 MAX
 */
public class TemperatureSensorServer extends UnicastRemoteObject implements ITemperatureSensor, Runnable {

    private volatile double temp;
    private List<ITemperatureListener> monitores;

    public TemperatureSensorServer() throws RemoteException {
        super();
        monitores = new ArrayList<>();
        temp = 98.0;
    }

    @Override
    public double getTemperature() throws RemoteException {
        return temp;
    }

    @Override
    public void addTemperatureListener(ITemperatureListener listener) throws RemoteException {
        System.out.println("Adiconando listener -" + listener);
        monitores.add(listener);
    }

    @Override
    public void removeTemperatureListener(ITemperatureListener listener) throws RemoteException {
        System.out.println("Removendo listener -" + listener);
        monitores.remove(listener);
    }

    public void run() {
        Random r = new Random();
        for (;;) {
            try {
                // Sleep for a random amount of time
                int duration = r.nextInt() % 10000 + 2000;
                // Check to see if negative, if so, reverse
                if (duration < 0) {
                    duration = duration * -1;
                }
                Thread.sleep(duration);
            } catch (Exception e) {
            }
            // Get a number, to see if temp goes up or down
            int num = r.nextInt();

            if (num < 0) {
                temp += 0.5;
            } else {
                temp -= 0.5;
            }
            // Notify registered listeners
            notifyListeners();
        }
    }

    private void notifyListeners() {
        // Notify every listener in the registered list
        for (ITemperatureListener sensor : monitores) {
            ITemperatureListener listener = sensor;//(ITemperatureListener) e.nextElement();
            // Notify, if possible a listener
            try {
                listener.temperatureChanged(temp);
            } catch (RemoteException re) {
                System.out.println("removing listener -" + listener);
                // Remove the listener
                monitores.remove(listener);
            }
        }
    }

    public static void main(String args[]) {
        System.out.println("Loading temperature service");
        // Only required for dynamic class loading
        //System.setSecurityManager ( new RMISecurityManager() );
        try {
            LocateRegistry.createRegistry(1099);
        } catch (RemoteException ex) {
            
        }
        try {
            // Load the service
            TemperatureSensorServer sensor = new TemperatureSensorServer();
            // Check to see if a registry was specified
            String registry = "localhost";
            if (args.length >= 1) {
                registry = args[0];
            }
            // Registration format //registry_hostname:port
            // service // Note the :port field is optional 

            String registration = "rmi://" + registry + "/TemperatureSensor";
            // Register with service so that clients can
            // find us
            System.out.println(registration);
            Naming.rebind(registration, sensor);
            // Create a thread, and pass the sensor server.
            // This will activate the run() method, and
            // trigger regular temperature changes.
            Thread thread = new Thread(sensor);
            thread.start();

        } catch (RemoteException re) {
            System.err.println("Remote Error - " + re);
        } catch (Exception e) {
            System.err.println("Error - " + e);
        }
    }
}
