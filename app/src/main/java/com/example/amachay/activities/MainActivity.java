package com.example.amachay.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.amachay.R;
import com.example.amachay.activities.Client.MapClientActivity;
import com.example.amachay.activities.Tienda.MapTiendaActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {


    //NOS CREAMOS UNA PROPIEDAD DEL TIPO BUTTON
    Button mButtonSoytienda;
    Button mButttonSoyusuario;

    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //INICIALIZAMOS EL SHARED

        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);

        final SharedPreferences.Editor editor = mPref.edit();
        // AHORA LOS INSTANCIAMOS
        mButttonSoyusuario=findViewById(R.id.btnUsuario);

        mButtonSoytienda=findViewById(R.id.btnTienda);

        //LES AÑADIMOS EL BOTÓN DEL CLICK

        //ESTABLECEMOS EL BOTÓN AL USUARIO
        mButttonSoyusuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //CREAMOS UN PARÁMETRO DE TIPO STRING
                editor.putString("user","client");
                //editor.putString("estado","registro");

                //PROGRAMAMOS LO QUE QUEREMOS QUE HAGA EL BOTÓN

                //LO QUE HACE ESTE MÉTODO ES PASAR DE UNA PANTALLA A OTRA

                editor.apply();
                goToSelectAuth();


            }
        });
        //ESTABLECEMOS EL BOTÓN AL MÉDICO
        mButtonSoytienda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editor.putString("user", "tienda");
                //LE DECIMOS QUE GUARDE ESE VALOR CON EL MÉTODO APPLY
                editor.apply();

                //PROGRAMAMOS LO QUE QUEREMOS QUE HAGA EL BOTÓN

                //LO QUE HACE ESTE MÉTODO ES PASAR DE UNA PANTALLA A OTRA
                goToSelectAuth();



            }
        });

    }

    /*
    //posible causa de que cuando se abra la app siempre te mande al mapa
    @Override
    protected void onStart() {
        super.onStart();

        if(FirebaseAuth.getInstance().getCurrentUser()!= null)
        {
            String user = mPref.getString("estado","");
            if(user.equals("registro"))
            {
                Intent intent = new Intent(MainActivity.this, menu_principal.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
            else
            {
                Intent intent = new Intent(MainActivity.this, MapTiendaActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }

        }
    }

    */
    // EL METODO LO COLOCAMOS DENTRO DEL MAIN ACTIVITY
    private void goToSelectAuth() {
//USAMOS LA PROPIEDAD LLAMADA INTENT
        Intent intent = new Intent(MainActivity.this, SelectOptionAuthActivity.class);
        //NECESITAMOS INICIAR EL INTEN

        startActivity(intent);

    }


}
