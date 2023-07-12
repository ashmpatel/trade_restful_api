package com.example.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * A simple csv file reader that calls a handler for each row to process data.
 * A header row is assumed to be present. ofc, can make it an option to have one or not
 * but not required for this.
 */
public class ReadCsv {

    private CallBackListener callWithData;

    public ReadCsv(CallBackListener callWithData) {
        this.callWithData = callWithData;
    }

    public void read(String csvFile) {
        boolean skippedHeader = false;
        try {
            File file = new File(csvFile);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (skippedHeader) {
                    callWithData.callBack(line);
                } else {
                    skippedHeader = true;
                }
            }
            br.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}