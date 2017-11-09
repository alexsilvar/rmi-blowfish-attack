/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aprendendo;

import java.rmi.Remote;

/**
 *
 * @author Avell B155 MAX
 */
public interface ITemperatureSensor extends Remote {

    public double getTemperature() throws java.rmi.RemoteException;

    public void addTemperatureListener(ITemperatureListener listener) throws java.rmi.RemoteException;

    public void removeTemperatureListener(ITemperatureListener listener) throws java.rmi.RemoteException;
}
