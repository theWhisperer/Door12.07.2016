package thewhispererinc.door12072016;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Nick on 13/07/2016.
 */
//http://programmerguru.com/android-tutorial/android-asynctask-example/
public class ReadPermissionFile extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] params) {

        /*try {
            URL url = new URL("\n" +
                    "https://drive.google.com/a/facca.com/file/d/0B5oZIiu2TcSvTml2LVhpNUxEbE0/view?usp=sharing\n");
            //BufferedReader input = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            Scanner input = new Scanner(url.openStream());

            input.useDelimiter(",");
            while (input.hasNext())
            {
                System.out.print(input.next());
            }


            *//*while ((inputLine = input.readLine())!=null) {

                System.out.println(inputLine);
                // Write data to file
                //output.write(data, 0, count);
            }*//*
            // Flush output
            //output.flush();
            // Close streams
            //output.close();
            input.close();
            return 1;

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }*/
        return null;

    }
}
