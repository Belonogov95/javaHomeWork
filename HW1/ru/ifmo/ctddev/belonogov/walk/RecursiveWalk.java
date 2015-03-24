package ru.ifmo.ctddev.belonogov.walk;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecursiveWalk {
    FastScanner in;
    PrintWriter out;
    public static final long X0 = 2166135261l;
    public static final long P = 16777619l;

    private void rec(Path inputPath) {
        File file = new File(String.valueOf(inputPath));
        //System.out.println(inputPath.toString());
        if (file.isDirectory()) {
            //System.out.println("directory case");
            try {
                DirectoryStream<Path> stream = Files.newDirectoryStream(inputPath);
                for (Path path : stream) {
                    rec(path);
                }
            } catch (SecurityException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        else {
            try {
                //System.out.println("calc hash: " + file.toString());
                FileInputStream stream = new FileInputStream(file);

                int T = (int)100000;
                byte [] buffer = new byte[T];
                long res = X0;
                while (true) {
                    //System.out.println("new step");
                    int cur = 0;
                    int c = 0;
                    while (cur < T && (c = stream.read()) != -1) {
                        buffer[cur++] = (byte)c;
                    }
                    for (int i = 0; i < cur; i++)
                        res = ((res * P) ^ buffer[i]) & ((1l << 32) - 1);
                    if (c == -1)
                        break;
                }
                System.out.println(String.valueOf(res));

                out.print(String.format("%08x", res));
                out.print(" ");
                out.println(inputPath.toString());
            }
            catch (IOException e) {
                out.print(String.format("%08x", 0));
                out.print(" ");
                out.println(inputPath.toString());
                System.out.println(e.getMessage());
            }
        }
    }

    public void solve() {
        String fileName;
        while ((fileName = in.next()) != null) {
            if (fileName.length() > 0) {
                rec(Paths.get(fileName.trim()));
            }
        }
        System.out.println("finish");
        out.close();
    }

    public void run(String[] args) {
        try {
            in = new FastScanner(new File(args[0]));
            out = new PrintWriter(new File(args[1]));
            solve();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        new RecursiveWalk().run(args);
    }

    public class FastScanner {
        BufferedReader bf;

        public FastScanner(File file) throws FileNotFoundException {
            try {
                bf = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                throw e;
            }
        }
        String next() {
            try {
                return bf.readLine();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                //e.printStackTrace();
            }
            return null;
        }
    }

}
