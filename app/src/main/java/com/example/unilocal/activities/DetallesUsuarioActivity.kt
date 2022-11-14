package com.example.unilocal.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.unilocal.R
import com.example.unilocal.bd.Cities
import com.example.unilocal.bd.Usuarios
import com.example.unilocal.databinding.ActivityDetallesUsuarioBinding
import com.example.unilocal.models.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class DetallesUsuarioActivity : AppCompatActivity() {
    lateinit var binding: ActivityDetallesUsuarioBinding
    lateinit var cities: ArrayList<City>
    var user:FirebaseUser? = null
    var cityPosition: Int = -1
    var tipo: String? = ""
    var code: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetallesUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cities = ArrayList()
        user = FirebaseAuth.getInstance().currentUser
        if(user!= null){
            Firebase.firestore
                .collection("users")
                .document(user!!.uid)
                .get()
                .addOnSuccessListener {
                    val userF = it.toObject(User::class.java)
                    binding.nombreUsuario.text = it.toObject(User::class.java)!!.nombre
                    binding.nombreLayout.hint = it.toObject(User::class.java)!!.nombre
                    binding.nicknameLayout.hint = it.toObject(User::class.java)!!.nickname
                    binding.correoLayout.hint = user!!.email
                    binding.btnGuardarCambiosDetallesUsuario.setOnClickListener { updateUser(userF) }
                    loadCities(it.toObject(User::class.java))
                }
        }
    }

    fun loadCities(person:User?){
        cities.clear()
        Firebase.firestore
            .collection("citiesF")
            .get()
            .addOnSuccessListener {
                for (doc in it) {
                    val city = doc.toObject(City::class.java)
                    city.key = doc.id
                    cities.add(city)
                }
                var city = Cities.obtener(person!!.idCity, cities)
                var position = cities.indexOf(city)
                var adapter= ArrayAdapter(this,android.R.layout.simple_spinner_item,cities)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.cityEdit.adapter= adapter
                binding.cityEdit.setSelection(position)
                binding.cityEdit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        cityPosition = p2
                    }
                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }
                }
            }
    }

    fun updateUser(person: User?){
        var nombre = binding.campoNombreUsuario.text.toString()
        var nickname = binding.nicknameUsuario.text.toString()
        var correo = binding.correoUsuario.text.toString()
        var idCity = cities[cityPosition].key

        if(nombre.isEmpty()){
            nombre = binding.nombreLayout.hint.toString()
        }

        if(nickname.isEmpty()){
            nickname = binding.nicknameLayout.hint.toString()
        }

        if(correo.isEmpty()){
            correo = binding.correoLayout.hint.toString()
        }
        if(nombre.isNotEmpty() && nickname.isNotEmpty() && correo.isNotEmpty() && idCity!=""){
            val newUser = User(person!!.id,nombre,nickname,idCity,person.rol)
            var user = FirebaseAuth.getInstance().currentUser
            val credential= EmailAuthProvider.getCredential(user!!.email!!, "12345")
            user!!.reauthenticate(credential)
                .addOnSuccessListener {
                      user!!.updateEmail(correo)
                        .addOnSuccessListener {
                            verificarEmail(user)
                            Firebase.firestore
                                .collection("users")
                                .document(user.uid)
                                .set(newUser)
                                .addOnSuccessListener {
                                    Snackbar.make(binding.root, "Se ha actualizado correctamente tu informacion", Toast.LENGTH_LONG).show()
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        when(person.rol){
                                            Rol.USER -> startActivity(Intent(this, MainActivity::class.java))
                                            Rol.MODERATOR -> startActivity(Intent(this, ModeratorActivity::class.java))
                                            else -> startActivity(Intent(this, GestionModeratorActivity::class.java))
                                        }
                                    },4000)
                                }
                        }.addOnFailureListener {
                                   Snackbar.make(binding.root, "Correo invalido", Toast.LENGTH_LONG).show()
                                }
                }.addOnFailureListener {
                    Snackbar.make(binding.root, "Contraseña equivocada, porfavor revisa", Toast.LENGTH_LONG).show()
                }
        }else{
            Snackbar.make(binding.root, "Llene todos los datos", Toast.LENGTH_LONG).show()
        }

    }

    private fun verificarEmail(user: FirebaseUser){
        user.sendEmailVerification().addOnCompleteListener(this){
            if(it.isSuccessful){
                Toast.makeText(baseContext, R.string.email_enviado, Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(baseContext, R.string.email_error, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun cerrarSesion(){
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity( intent )
        finish()

    }
}