/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package attack;

import es.InputOutput;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Avell B155 MAX
 */
public class Slave extends UnicastRemoteObject implements ISlavesListener {

    private byte[] encryptedMessage;
    private String[] keys;
    private IMaster master;
    private InputOutput es;
    private Date oldTime;
    private Timer timer;
    private int indexAtual;

    public static void main(String[] args) {
        //Este trecho que configura o Slave deve ser est'atico para que  as configuracoes se mantenham
        try {
            String so = System.getProperty("os.name");
            if (so.equals("Linux")) {
                //LINUX E UM LIXO QUE TEM Q FAZER ISSO TUDO PRA PEGAR O IP DA MAQUINA
                System.out.println("Linux 'e um lixo, veja esse codigo...");
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
                System.setProperty("java.rmi.server.hostname", save);
                System.out.println(save);
            } else {
                System.out.println(InetAddress.getLocalHost().getHostAddress());
                System.setProperty("java.rmi.server.hostname", InetAddress.getLocalHost().getHostAddress());
            }
//Este trecho que configura o Slave deve ser est'atico para que  as configuracoes se mantenham
            new Slave().executar("mensagem.cipher", "dictionary.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Slave() throws RemoteException {
    }

    @Override
    public List<String> startSubAttack(int inicio, int fim) throws RemoteException {
        System.out.println("INICIANDO ATAQUE");
        String s = null;
        oldTime = new Date();
        List<String> lista = new ArrayList<>();
        Slave sla = this;
        TimerTask verificaIndex = new TimerTask() {
            @Override
            public void run() {
                try {
                    master.lastIndex(sla, indexAtual);
                    oldTime = new Date();
                } catch (RemoteException ex) {
                    Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        timer.scheduleAtFixedRate(verificaIndex, 10 * 1000, 10 * 1000);

        //RETORNAR UMA LISTA POIS PODE ACHAR MAIS DE UMA CHAVE CANDIDATA
        for (int i = inicio; i < fim; i++) {
            //Registrando denovo a cada 30segundos
            try {
                Thread.sleep(2);
            } catch (InterruptedException ex) {
                Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
            }
            indexAtual = i - 1;
            try {
                byte[] key = keys[i].getBytes();
                SecretKeySpec keySpec = new SecretKeySpec(key, "Blowfish");
                Cipher cipher = Cipher.getInstance("Blowfish");
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
                byte[] decrypted = cipher.doFinal(encryptedMessage);
                s = new String(decrypted);
            } catch (javax.crypto.BadPaddingException e) {
                // essa exceção é lançada quando a senha está incorreta
                // porém não quer dizer que a senha está correta se não lançar
                // essa exceção
                s = null;
            } catch (Exception e) {
                // don't try this at home
                e.printStackTrace();
            }

            if (s != null && (s.contains("cefet") || s.contains("PDF"))) {
                master.passwordCandidate(this, new String(keys[i]));
                lista.add(keys[i]);
            } else {
                s = null;
            }
        }
        System.out.println("Minha Parte Foi Finalizada");
        master.lastIndex(this, fim);
        timer.cancel();
        return lista;
    }

    @Override
    public long lastSubscribe() {
        return new Date().getTime() - oldTime.getTime();
    }

    private void executar(String pathEncripted, String pathKeys) {
        es = new InputOutput();
        try {
            System.out.println("Carregando Mensagem");
            encryptedMessage = es.readFile(pathEncripted);
            if (encryptedMessage == null) {
                System.out.println("Arquivo da MENSAGEM não encontrado");
                return;
            }
            System.out.println("Carregando Keys");
            keys = es.readFileSt(pathKeys);
            if (keys == null) {
                System.out.println("Arquivo das CHAVES não encontrado");
                return;
            }

        } catch (Exception ex) {
            Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {

            System.out.print("Insira o IP do mestre:");
            String servidor = new Scanner(System.in).nextLine();
            System.out.print("Encontrando Servidor: ");
            master = (IMaster) Naming.lookup("//" + servidor + ":1099/BruteForceAttack");
            System.out.println("Encontrado, aguardando inicio de ataque");
            timer = new Timer();
            Slave s = this;
            TimerTask recadastrar = new TimerTask() {
                @Override
                public void run() {
                    try {
                        master.addSlave(s);
                        oldTime = new Date();
                    } catch (RemoteException ex) {
                        Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            this.oldTime = new Date();
            timer.scheduleAtFixedRate(recadastrar, 0, 5 * 1000);

        } catch (Exception ex) {
            Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void die() throws RemoteException {
        System.exit(0);
    }

}
