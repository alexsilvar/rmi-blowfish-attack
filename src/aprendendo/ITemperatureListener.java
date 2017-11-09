/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aprendendo;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Avell B155 MAX
 */
public interface ITemperatureListener extends Remote {

    public void temperatureChanged(double temperature) throws RemoteException;
}
