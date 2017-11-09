/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package attack;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author Avell B155 MAX
 */
public interface ISlavesListener extends Remote {

    /**
     * @param start indice para comecar o ataque
     * @param end indice para parar o ataque
     * @return retorna a ultima chave candidata
     * @throws RemoteException pois trata-se de uma aplicacao RMI
     */
    public List<String> startSubAttack(int start, int end) throws RemoteException;

    /**
     * @return retorna o tempo de sua ultima inscricao date.getTime()
     * @throws java.rmi.RemoteException
     */
    public long lastSubscribe() throws RemoteException;

    /**
     * @throws java.rmi.RemoteException
     */
    public void die() throws RemoteException;
}
