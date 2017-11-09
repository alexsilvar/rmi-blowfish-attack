/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blowfish;

import java.io.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Avell B155 MAX
 */
public class Decrypt {

    private static byte[] readFile(String filename) throws IOException {
        File file = new File(filename);
        InputStream is = new FileInputStream(file);
        long length = file.length();
// creates array (assumes file length<Integer.MAX_VALUE)
        byte[] data = new byte[(int) length];
        int offset = 0;
        int count = 0;
        while ((offset < data.length) && (count = is.read(data, offset,
                data.length - offset)) >= 0) {
            offset += count;
        }
        is.close();
        return data;
    }

    private static void saveFile(String filename, byte[] data) throws IOException {
        FileOutputStream out = new FileOutputStream(filename);
        out.write(data);
        out.close();
    }

    public static void decriptografa(String chave, String arquivo) {
        try {
            byte[] key = chave.getBytes();
            SecretKeySpec keySpec = new SecretKeySpec(key, "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] message = readFile(arquivo);
            System.out.println("message size (bytes) = " + message.length);
            byte[] decrypted = cipher.doFinal(message);
            saveFile(chave + ".msg", decrypted);
        } catch (javax.crypto.BadPaddingException e) {
            System.out.println("Invalid key.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
// args[0] é a chave a ser usada
// args[1] é o nome do arquivo de entrada
        try {
            byte[] key = args[0].getBytes();
            SecretKeySpec keySpec = new SecretKeySpec(key, "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] message = readFile(args[1]);
            System.out.println("message size (bytes) = " + message.length);
            byte[] decrypted = cipher.doFinal(message);
            saveFile(args[1] + ".msg", decrypted);
        } catch (javax.crypto.BadPaddingException e) {
// essa exceção é lançada quando a senha está incorreta
// porém não quer dizer que a senha está correta se não lançar
// essa exceção
            System.out.println("Invalid key.");
        } catch (Exception e) {
// don't try this at home
            e.printStackTrace();
        }
    }
}
