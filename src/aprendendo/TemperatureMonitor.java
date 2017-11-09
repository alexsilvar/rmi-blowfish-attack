/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aprendendo;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Avell B155 MAX
 */
public class TemperatureMonitor extends UnicastRemoteObject implements ITemperatureListener {

    public TemperatureMonitor() throws RemoteException {
        // no code req'd
    }

    @Override
    public void temperatureChanged(double temperature) throws RemoteException {
        System.out.println("Temperature change event : " + temperature);
    }

    public static void main(String args[]) {
        System.out.println("Looking for temperature sensor");
        // Only required for dynamic class loading
        //System.setSecurityManager(new RMISecurityManager());

        try {
            // Check to see if a registry was specified

            String registry;
            if (args.length >= 1) {
                registry = args[0];
            }
            registry = "192.168.0.104";

            String s = System.getProperty("os.name");
            if (s.equals("Linux")) {
                //LINUX E UM LIXO QUE TEM Q FAZER ISSO TUDO PRA PEGAR O IP DA MAQUINA
                String save = "";
                Enumeration e = NetworkInterface.getNetworkInterfaces();
                while (e.hasMoreElements()) {
                    NetworkInterface i = (NetworkInterface) e.nextElement();
                    Enumeration ds = i.getInetAddresses();
                    while (ds.hasMoreElements()) {
                        InetAddress myself = (InetAddress) ds.nextElement();
                        Pattern p = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}");
                        Matcher m1 = p.matcher(myself.getHostName());
                        if (m1.matches()) {
                            save = myself.getHostName();
                        }
                    }
                }
                System.out.println(System.setProperty("java.rmi.server.hostname", save));
            } else {
                System.out.println(InetAddress.getLocalHost().getHostAddress());
                System.setProperty("java.rmi.server.hostname", InetAddress.getLocalHost().getHostAddress());
            }

            String registration = "//" + registry + ":1099/TemperatureSensor";
            // Lookup the service in the registry, and obtain
            // a remote service

            Remote remoteService = Naming.lookup(registration);
            // Cast to a TemperatureSensor interface
            ITemperatureSensor sensor = (ITemperatureSensor) remoteService;
            // Get and display current temperature
            double reading = sensor.getTemperature();
            System.out.println("Original temp : " + reading);
            // Create a new monitor and register it as a
            // listener with remote sensor
            TemperatureMonitor monitor = new TemperatureMonitor();
            sensor.addTemperatureListener(monitor);
        } catch (NotBoundException nbe) {
            System.out.println("No sensors available");
        } catch (RemoteException re) {
            System.out.println("RMI Error - " + re);
        } catch (Exception e) {
            System.out.println("Error - " + e);
        }
    }
}
