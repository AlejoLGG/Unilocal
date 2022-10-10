package com.example.unilocal.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.unilocal.R
import com.example.unilocal.bd.Usuarios
import com.example.unilocal.databinding.ActivityMainBinding

import com.example.unilocal.fragments.FavoritesFragment
import com.example.unilocal.fragments.InicioFragment
import com.example.unilocal.fragments.MyPlacesFragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {
    private lateinit var sharedPreferences:SharedPreferences
    private var MENU_INICIO = "Inicio"
    private var MENU_MIS_LUGARES = "Mis lugares"
    private var MENU_FAVORITOS = "favoritos"
    lateinit var binding: ActivityMainBinding
    var codeUser: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences= this.getSharedPreferences("sesion",Context.MODE_PRIVATE)
        codeUser = sharedPreferences.getInt("id",-1)
        if(codeUser != -1){
            val usuario = Usuarios.getUser(codeUser)
            val header = binding.navigationView.getHeaderView(0)
            header.findViewById<TextView>(R.id.name_user_session).text = usuario!!.nombre
            header.findViewById<TextView>(R.id.email_user_session).text = usuario!!.correo
        }
        changeFragments(2,MENU_INICIO)
        binding.barraInferior.setOnItemSelectedListener {
            when(it.itemId){
                R.id.menu_favorites -> changeFragments(1,MENU_FAVORITOS)
                R.id.menu_home -> changeFragments(2,MENU_INICIO)
                R.id.menu_my_places -> changeFragments(3,MENU_MIS_LUGARES)
            }
            true
        }
        binding.navigationView.setNavigationItemSelectedListener (this)
    }

    fun irCrearLugar(view:View){
        val intent = Intent(this, CrearLugarActivity::class.java)
        startActivity(intent)
    }


    fun cerrarSesion(){
        sharedPreferences.edit().clear().commit()
        finish()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun changeFragments(valor:Int, nombre:String){
        var fragment:Fragment
        if(valor ==  1){
            val bundle:Bundle = Bundle()
            bundle.putInt("code", codeUser)
            fragment = FavoritesFragment()
            fragment.bundle = bundle
        }else if(valor == 2){
            val bundle:Bundle = Bundle()
            bundle.putInt("code", codeUser)
            fragment = InicioFragment()
            fragment.bundle = bundle
        }else{
            val bundle:Bundle = Bundle()
            bundle.putInt("code", codeUser)
            fragment = MyPlacesFragment()
            fragment.bundle = bundle
        }
       supportFragmentManager.beginTransaction().replace(binding.contenidoPrincipal.id,fragment)
           .addToBackStack(nombre)
           .commit()
    }

    fun abrirMenu(){
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.navPerfil -> abrirPerfil()
            R.id.menu_cerrar_sesion -> cerrarSesion()
            R.id.navCategorias -> abrirCategorias()
        }
        item.isChecked = true
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun abrirPerfil(){
        val intent = Intent(this, DetallesUsuarioActivity::class.java)
        startActivity(intent)
    }

    fun abrirCategorias(){
        val intent = Intent(this, CategoriesActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val count = supportFragmentManager.backStackEntryCount
        if(count > 0){
            val nombre = supportFragmentManager.getBackStackEntryAt(count-1).name
            when(nombre){
                MENU_FAVORITOS -> binding.barraInferior.menu.getItem(0).isChecked = true
                MENU_INICIO -> binding.barraInferior.menu.getItem(1).isChecked = true
                MENU_MIS_LUGARES -> binding.barraInferior.menu.getItem(2).isChecked = true
            }
        }
    }

}