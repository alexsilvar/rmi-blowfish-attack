/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alexsander Ramos da Silva
 */
public class InputOutput {

    public byte[] readFile(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            return null;
        }
        InputStream is = new FileInputStream(file);
        long length = file.length();
// creates array (assumes file length<Integer.MAX_VALUE)
        byte[] data = new byte[(int) length];
        int offset = 0;
        int count = 0;
        while ((offset < data.length) && (count = is.read(data, offset, data.length - offset)) >= 0) {
            offset += count;
        }
        is.close();
        return data;
    }

    public String[] readFileSt(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            return null;
        }
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        List<String> l = new ArrayList<>();
        while (br.ready()) {
            l.add(br.readLine());
        }
        return l.toArray(new String[l.size()]);
    }

    public void saveFile(String filename, byte[] data) throws IOException {
        FileOutputStream out = new FileOutputStream(filename);
        out.write(data);
        out.close();
    }
}
