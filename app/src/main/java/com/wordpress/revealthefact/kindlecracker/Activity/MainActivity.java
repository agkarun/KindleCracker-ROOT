/***
 * Kindle Cracker Convert kindle book into PDF format for personal use only
 *     Copyright (C) 2018  Karunakaran
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Also add information on how to contact you by electronic and paper mail.
 *
 *   If the program does terminal interaction, make it output a short
 * notice like this when it starts in an interactive mode:
 *
 *     <program>  Copyright (C) <year>  <name of author>
 *     This program comes with ABSOLUTELY NO WARRANTY; for details type `show w'.
 *     This is free software, and you are welcome to redistribute it
 *     under certain conditions; type `show c' for details.
 *
 * The hypothetical commands `show w' and `show c' should show the appropriate
 * parts of the General Public License.  Of course, your program's commands
 * might be different; for a GUI interface, you would use an "about box".
 *
 *   You should also get your employer (if you work as a programmer) or school,
 * if any, to sign a "copyright disclaimer" for the program, if necessary.
 * For more information on this, and how to apply and follow the GNU GPL, see
 * <https://www.gnu.org/licenses/>.
 *
 *   The GNU General Public License does not permit incorporating your program
 * into proprietary programs.  If your program is a subroutine library, you
 * may consider it more useful to permit linking proprietary applications with
 * the library.  If this is what you want to do, use the GNU Lesser General
 * Public License instead of this License.  But first, please read
 * <https://www.gnu.org/licenses/why-not-lgpl.html>.
 */

package com.wordpress.revealthefact.kindlecracker.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.wordpress.revealthefact.kindlecracker.R;
import com.wordpress.revealthefact.kindlecracker.Service.CrackService;

import java.io.File;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity {

    private TextInputLayout continueLay;
    private TextInputLayout compressLay;
    private TextInputEditText xCoord,yCoord,file,pages,prepTime,pageContinue,compressPDF;
    private CheckBox continueChkbox,compressCheckbox;
    private Button startButton,stopBtn;
    private int x,y,time,totalPages,fromPage,compress;
    private String destFolder="KindleCracker";
    private static final int STORAGE_PERMISSION_CODE=1;
    private File sShotfolder;
    private File mergedFolder;
    private String fileName;
    private View snackView;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        xCoord=findViewById(R.id.Xcoord);
        yCoord=findViewById(R.id.Ycoord);
        file=findViewById(R.id.filename);
        pages=findViewById(R.id.pages);
        prepTime=findViewById(R.id.preptime);
        compressPDF=findViewById(R.id.compresspdf);
        continueChkbox=findViewById(R.id.continuechkbox);
        compressCheckbox=findViewById(R.id.compresschkbox);
        pageContinue= findViewById(R.id.continuepage);
        startButton = findViewById(R.id.convertbtn);
        stopBtn=findViewById(R.id.stopConvertbtn);
        continueLay=findViewById(R.id.continuetextLay);
        compressLay=findViewById(R.id.compresspdfLay);
        snackView =findViewById(R.id.rootView);
        preferences = getSharedPreferences("EULA",MODE_PRIVATE);

        if(preferences.getBoolean("NOTAGREED",true)){
            showEULA();
        }

        //checking if device is rooted
        new RootCheck().execute();
        requestStoragePermission();

        continueChkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(continueChkbox.isChecked()){
                    continueLay.setVisibility(View.VISIBLE);
                }
                else {
                    continueLay.setVisibility(View.GONE);
                }
            }
        });

        compressCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(compressCheckbox.isChecked()){
                    compressLay.setVisibility(View.VISIBLE);
                }
                else{
                    compressLay.setVisibility(View.GONE);
                }
            }
        });

        //starting the pdf converting service
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(getValues()){
                    if(continueChkbox.isChecked()&&!compressCheckbox.isChecked())
                    {
                        if (pageContinue.getText().toString().trim().length()<=0||
                                pageContinue.getText().toString().trim().contentEquals("0"))
                        {
                            Snackbar.make(snackView,"All fields are mandatory don't leave fields empty",Snackbar.LENGTH_LONG).show();
                        }
                        else if (pageContinue.getText().toString().trim().length()>0&&
                                !pageContinue.getText().toString().trim().contentEquals("0"))
                        {
                            fromPage=Integer.parseInt(pageContinue.getText().toString().trim());
                            startConvertingService(true,false);
                        }
                    }


                    if(!continueChkbox.isChecked()&&compressCheckbox.isChecked())
                    {
                        if (compressPDF.getText().toString().trim().length()<=0||
                                compressPDF.getText().toString().trim().contentEquals("0"))
                        {
                            Snackbar.make(snackView,"All fields are mandatory don't leave fields empty",Snackbar.LENGTH_LONG).show();
                        }
                        else if (compressPDF.getText().toString().trim().length()>0&&
                                !compressPDF.getText().toString().trim().contentEquals("0"))
                        {
                            compress=Integer.parseInt(compressPDF.getText().toString().trim());
                            startConvertingService(false,true);
                        }
                    }


                    if(continueChkbox.isChecked()&&compressCheckbox.isChecked())
                    {
                        if (compressPDF.getText().toString().trim().length()<=0||
                                compressPDF.getText().toString().trim().contentEquals("0")||
                                pageContinue.getText().toString().trim().length()<=0||
                                pageContinue.getText().toString().trim().contentEquals("0"))
                        {
                            Snackbar.make(snackView,"All fields are mandatory don't leave fields empty",Snackbar.LENGTH_LONG).show();
                        }
                        else if (compressPDF.getText().toString().trim().length()>0&&
                                !compressPDF.getText().toString().trim().contentEquals("0")&&
                                pageContinue.getText().toString().trim().length()>0&&
                                !pageContinue.getText().toString().trim().contentEquals("0"))
                        {
                            fromPage=Integer.parseInt(pageContinue.getText().toString().trim());
                            compress=Integer.parseInt(compressPDF.getText().toString().trim());
                            startConvertingService(true,true);
                        }
                    }

                    else if(!continueChkbox.isChecked()&&!compressCheckbox.isChecked()) {
                        startConvertingService(false,false);
                    }
                }
            }
        });

        //stop pdf converting
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this,CrackService.class));
                Snackbar.make(snackView,"Converting Stopped...",Snackbar.LENGTH_LONG).show();
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start Activity.
                // Permission request was denied.
            } else {
                Toast.makeText(this,"Storage permission need for Save PDF files",Toast.LENGTH_LONG).show();
                finish();
                moveTaskToBack(true);
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)

    }



    private void requestStoragePermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }

        else {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }


    }

    //getting values from user
    public boolean getValues() {
        boolean values=false;
        if (xCoord.getText().toString().trim().length() > 0 &&
                yCoord.getText().toString().trim().length() > 0 &&
                pages.getText().toString().trim().length() > 0 &&
                prepTime.getText().toString().trim().length() > 0 &&
                file.getText().toString().trim().length() > 0)
        {
            try {
                x = Integer.parseInt(xCoord.getText().toString().trim());
                y = Integer.parseInt(yCoord.getText().toString().trim());
                totalPages = Integer.parseInt(pages.getText().toString().trim());
                time = Integer.parseInt(prepTime.getText().toString().trim());
                fileName = file.getText().toString().trim();
                values=true;
            } catch (Exception e) {
                Snackbar.make(snackView,"Some field contains invalid value",Snackbar.LENGTH_LONG).show();
            }

        }
        else {
            Snackbar.make(snackView,"All fields are mandatory  don't leave fields empty...",Snackbar.LENGTH_LONG).show();
        }

        return values;
    }

    /**
     * Starting PDF converting service
     * @param fromPage coverting from already having pdf
     * @param compress compress the pdf file
     */
    public void startConvertingService(boolean fromPage,boolean compress){

        try{
            sShotfolder= new File("/sdcard/"+destFolder+"/");
            mergedFolder= new File("/sdcard/"+destFolder+"/"+"Converted"+"/");
            if(!sShotfolder.exists()&&!mergedFolder.exists())
            {
                sShotfolder.mkdir();
                mergedFolder.mkdir();
            }
        }

        catch (Exception e){
            e.printStackTrace();
        }

        Intent crackService= new Intent(MainActivity.this,CrackService.class);
        if (fromPage&&!compress){
            crackService.putExtra("fromPage",this.fromPage);
            crackService.putExtra("compress",false);

        }
        else if (!fromPage&&compress){
            crackService.putExtra("fromPage",-1);
            crackService.putExtra("compress",true);
            crackService.putExtra("compressRate",this.compress);
        }
        else if (fromPage&&compress){
            crackService.putExtra("fromPage",this.fromPage);
            crackService.putExtra("compress",true);
            crackService.putExtra("compressRate",this.compress);
        }
        else {
            crackService.putExtra("fromPage",-1);
            crackService.putExtra("compress",false);
        }

        crackService.putExtra("file", fileName);
        crackService.putExtra("x",x);
        crackService.putExtra("y",y);
        crackService.putExtra("totalPages", totalPages);
        crackService.putExtra("time",time);
        startService(crackService);
    }

    public class RootCheck extends AsyncTask {
        ProgressDialog suDialog = null;
        //        boolean unsupportedSU = false;
        boolean[] suGranted = {false};
        //        private Context context = null;
        private Shell.Interactive suShell;



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            suDialog=new ProgressDialog(MainActivity.this);
            suDialog.setCancelable(false);
            suDialog.setTitle("Please Wait...");
            suDialog.setMessage("Checking for Root Permission");
            suDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            suShell = (new Shell.Builder())
                    .useSU().addCommand("id", 0, new Shell.OnCommandResultListener() {
                        @Override
                        public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                            synchronized (suGranted) {
                                suGranted[0] = true;
                            }
                        }
                    }).open(new Shell.OnCommandResultListener() {
                        @Override
                        public void onCommandResult(int commandCode, int exitCode, List<String> output) {

                        }
                    });
            suShell.waitForIdle();
//        unsupportedSU = isSuPackage(getPackageManager(), "com.kingouser.com");
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            try {
                if (suDialog != null) {
                    suDialog.dismiss();
                }
            } catch (final Exception e) {
                Log.e("++++"+e,"+++++");
            } finally {
                suDialog = null;
            }

            if (!suGranted[0]&&!isFinishing()) {
                showRootNotFoundMessage();
            }

        }

        public void showRootNotFoundMessage(){

            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Root Permission required")
                    .setTitle("Info!")
                    .setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    moveTaskToBack(true);
                }
            });
            android.support.v7.app.AlertDialog dialog = builder.create();
            dialog.show();
        }
    }


    public void showEULA(){

        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Click AGREE button to Agree the licence agreement.")
                .setTitle("End User Licence Agreement")
                .setCancelable(false)

                .setNeutralButton("View Licence", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/file/d/1tzLFN9Abvlkm6zvt2a6_YUUIGczAER7x/view?usp=sharing"));
                        startActivity(browserIntent);
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        moveTaskToBack(true);
                    }
                })

                .setPositiveButton("AGREE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        preferences.edit().putBoolean("NOTAGREED",false).apply();
                    }
                });
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.guid:
                startActivity(new Intent(this,GuidActivity.class));
                return true;
            case R.id.about:
                startActivity(new Intent(this,AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}