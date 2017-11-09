/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package attack;

import blowfish.Decrypt;
import es.InputOutput;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Avell B155 MAX
 */
public class Master extends UnicastRemoteObject implements IMaster, Runnable {

    private final Vector<ISlavesListener> slaves;
    //Vector<ISlavesListener> sl;
    private static int qtdKeys;
    private static String[] keys;

    public Master() throws RemoteException {
        super();
//        List<ISlavesListener> list = new ArrayList<>();
//        slaves = Collections.synchronizedList(list);
        slaves = new Vector<>();
    }

    public static void main(String[] args) throws IOException {
        String so = System.getProperty("os.name");
        String save;
        if (so.equals("Linux")) {
            //LINUX E UM LIXO QUE TEM Q FAZER ISSO TUDO PRA PEGAR O IP DA MAQUINA
            System.out.println("Linux 'e um lixo, veja esse codigo...");
            save = "";
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
            System.setProperty("java.rmi.server.hostname", save);
            System.out.println(save);
        } else {
            System.out.println(save = InetAddress.getLocalHost().getHostAddress());
            System.setProperty("java.rmi.server.hostname", InetAddress.getLocalHost().getHostAddress());
        }
        try {
            LocateRegistry.createRegistry(1099);
        } catch (RemoteException ex) {

        }
        try {
            InputOutput es = new InputOutput();
            keys = es.readFileSt("dictionary.txt");
            qtdKeys = keys.length;

            IMaster master = new Master();
            String addres = save;
            System.out.println("rmi://" + addres + ":1099/BruteForceAttack");
            Naming.rebind("rmi://" + addres + ":1099/BruteForceAttack", master);
            ((Master) master).run();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void addSlave(ISlavesListener listener) throws RemoteException {
        if (!slaves.contains(listener)) {
            System.out.println("Adiconando listener - " + listener);
            slaves.add(listener);
        } else {
            System.out.println("Re-Registrado - " + listener);
        }
    }

    @Override
    public void removeSlave(ISlavesListener listener) throws RemoteException {
        for (Iterator<ISlavesListener> it = slaves.iterator(); it.hasNext();) {
            ISlavesListener next = it.next();
            if (next == listener) {
                System.out.println("Removendo listener -" + listener);
                it.remove();
                break;
            }
        }
    }
    private Timer timer;

    @Override
    public void run() {
        ExecutorService aplication = Executors.newFixedThreadPool(1);
        aplication.execute(new Cliente());

        timer = new Timer();
        TimerTask recadastrar = new TimerTask() {
            @Override
            public void run() {
                try {
                    verificaAtividade();
                } catch (Exception ex) {
                    Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        timer.scheduleAtFixedRate(recadastrar, 0, 30 * 1000);

    }

    @Override
    public void lastIndex(ISlavesListener listener, int index) throws RemoteException {
        System.out.println("Chave numero: " + index + " foi checado por: " + listener);
    }

    @Override
    public void passwordCandidate(ISlavesListener listener, String key) throws RemoteException {
        System.out.println("Chave candidata: " + key + " foi encontrada por: " + listener);
    }

    @Override
    public byte[] getKey(int index) throws RemoteException {
        return keys[index].getBytes();
    }

    private void verificaAtividade() {
        List<ISlavesListener> lista = new ArrayList<>();
        for (ISlavesListener slave : slaves) {
            try {
                if (slave.lastSubscribe() > 30 * 1000) {
                    lista.add(slave);
                    System.out.println(slave.lastSubscribe());
                }
            } catch (RemoteException ex) {
                System.out.println("Escravo perdido");
                lista.add(slave);
            }
        }
        for (Iterator<ISlavesListener> iterator = lista.iterator(); iterator.hasNext();) {
            ISlavesListener next = iterator.next();
            try {

                removeSlave(next);
            } catch (RemoteException ex) {
                Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        lista.clear();
    }

    private class Cliente extends Thread implements Runnable {

        //List<List<String>> lista = new ArrayList<>();
        @Override
        public void run() {
            Scanner ent = new Scanner(System.in);
            System.out.println("APERTE ENTER PARA INICIAR O ATAQUE");
            ent.nextLine();
            verificaAtividade();
            if (slaves.size() <= 0) {
                System.out.println("Não há escravos conectados");
                run();
                return;
            }
            int qtdCada = qtdKeys / slaves.size();
            int qtd = qtdCada;
            int inicio = 0;
            ExecutorService aplication = Executors.newFixedThreadPool(slaves.size());
            Date start = new Date();
            for (Iterator<ISlavesListener> it = slaves.iterator(); it.hasNext();) {
                ISlavesListener slave = it.next();
                aplication.execute(new Atacar(slave, inicio, qtdCada - 1));
                System.out.println("INICIO: " + inicio);
                System.out.println("FIM: " + qtdCada);
                inicio = qtdCada;
                qtdCada += qtd;
            }

            try {
                aplication.shutdown();
                aplication.awaitTermination(1, TimeUnit.DAYS);
            } catch (InterruptedException ex) {
                Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Terminou o ataque");
            System.out.println("Demorou em milisegundos: " + (new Date().getTime() - start.getTime()));
            for (List<String> list : respostas) {
                System.out.println(list);
                if (!list.isEmpty()) {
                    for (String string : list) {
                        Decrypt.decriptografa(string, "mensagem.cipher");
                    }
                }
            }

            killAll();
            System.exit(0);
        }
    }

    private void killIt(ISlavesListener slave) {
        try {
            slave.die();
        } catch (Exception ex) {
        }
    }

    private void killAll() {
        for (Iterator<ISlavesListener> iterator = slaves.iterator(); iterator.hasNext();) {
            ISlavesListener next = iterator.next();
            killIt(next);
        }
    }

    Vector<List<String>> respostas = new Vector<>();

    private class Atacar implements Runnable {

        private List<String> lista;

        public List<String> getLista() {
            return lista;
        }
        private ISlavesListener slave;
        private int inicio;
        private int fim;

        public Atacar(ISlavesListener slave, int inicio, int fim) {
            this.slave = slave;
            this.inicio = inicio;
            this.fim = fim;
        }

        @Override
        public void run() {
            try {
                this.lista = this.slave.startSubAttack(inicio, fim);
                respostas.add(this.lista);
            } catch (RemoteException ex) {
                try {
                    removeSlave(slave);
                } catch (RemoteException ex1) {
                    //ex.printStackTrace();
                }
            }
        }
    }
}
