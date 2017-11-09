/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package attack;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Avell B155 MAX
 */
public interface IMaster extends Remote {

    public void addSlave(ISlavesListener listener) throws RemoteException;

    public void removeSlave(ISlavesListener listener) throws RemoteException;

    public void lastIndex(ISlavesListener listener, int index) throws RemoteException;

    public void passwordCandidate(ISlavesListener listener, String key) throws RemoteException;

    public byte[] getKey(int index) throws RemoteException;
}
