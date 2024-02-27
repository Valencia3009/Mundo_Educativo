package com.example.mundoeducativo

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Pattern

class Registro : AppCompatActivity() {

    private lateinit var txtNombre: EditText
    private lateinit var txtEdad: EditText
    private lateinit var txtCorreoRegistro: EditText
    private lateinit var txtContraseñaRegistro: EditText
    private lateinit var cbTerminos: CheckBox
    private lateinit var btnRegistrarseRegistro: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)
        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()

        txtNombre = findViewById(R.id.txtNombre)
        txtEdad = findViewById(R.id.txtEdad)
        txtCorreoRegistro = findViewById(R.id.txtCorreoRegistro)
        txtContraseñaRegistro = findViewById(R.id.txtContraseñaRegistro)
        cbTerminos = findViewById(R.id.cbTerminos)
        btnRegistrarseRegistro = findViewById(R.id.btnRegistrarseRegistro)

        btnRegistrarseRegistro.isEnabled = false

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("usuarios")

        cbTerminos.setOnCheckedChangeListener { _, isChecked ->
            btnRegistrarseRegistro.isEnabled = isChecked && validarCampos()
        }

        // Listener para los cambios en los campos de texto
        val textFields = listOf(txtNombre, txtCorreoRegistro, txtEdad, txtContraseñaRegistro)
        for (textField in textFields) {
            textField.addTextChangedListener {
                btnRegistrarseRegistro.isEnabled = cbTerminos.isChecked && validarCampos()
            }
        }

        btnRegistrarseRegistro.setOnClickListener {
            val nombre = txtNombre.text.toString()
            val correo = txtCorreoRegistro.text.toString()
            val edad = txtEdad.text.toString()
            val contraseña = txtContraseñaRegistro.text.toString()

            auth.createUserWithEmailAndPassword(correo, contraseña)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.let {
                            // Si el registro en Firebase Authentication es exitoso, guarda los datos en Realtime Database
                            val usuario = HashMap<String, String>()
                            usuario["nombre"] = nombre
                            usuario["correo"] = correo
                            usuario["edad"] = edad
                            myRef.child(it.uid).setValue(usuario)

                            // Enviar correo de verificación
                            it.sendEmailVerification()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this, "Correo de verificación enviado a $correo", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this, "Error al enviar correo de verificación: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }

                            // Limpiar los campos de texto y desmarcar el checkbox después del registro exitoso
                            txtNombre.text.clear()
                            txtEdad.text.clear()
                            txtCorreoRegistro.text.clear()
                            txtContraseñaRegistro.text.clear()
                            cbTerminos.isChecked = false

                            // Mostrar un mensaje emergente indicando que el registro fue exitoso
                            Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Si el registro falla, muestra un mensaje de error
                        Toast.makeText(this, "Error al registrar: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        task.exception?.printStackTrace()

                    }
                }
        }

        val txtTerminos = findViewById<TextView>(R.id.txtTerminos)
        txtTerminos.setOnClickListener {
            mostrarDialogoTerminosCondiciones()
        }

        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // Función para validar si todos los campos están llenos y si el correo y la contraseña tienen el formato adecuado
    private fun validarCampos(): Boolean {
        val nombre = txtNombre.text.toString()
        val correo = txtCorreoRegistro.text.toString()
        val edad = txtEdad.text.toString()
        val contraseña = txtContraseñaRegistro.text.toString()

        val correoValido = isValidEmail(correo)
        val contraseñaValida = isValidPassword(contraseña)

        if (!correoValido) {
            txtCorreoRegistro.error = "Correo no válido"
        }

        if (!contraseñaValida) {
            txtContraseñaRegistro.error = "Contraseña debe tener al menos 8 caracteres"
        }

        return nombre.isNotEmpty() && correoValido && edad.isNotEmpty() && contraseñaValida
    }

    // Función para validar si un correo electrónico tiene el formato adecuado
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "^\\w+@[a-zA-Z_]+?\\.[a-zA-Z]{2,3}\$".toRegex()
        return emailPattern.matches(email)
    }

    // Función para validar si una contraseña tiene al menos 8 caracteres
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    // Función para mostrar un diálogo con los términos y condiciones
    private fun mostrarDialogoTerminosCondiciones() {
        val textoTerminosCondiciones = getString(R.string.terminos_condiciones)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Términos y Condiciones")
        builder.setMessage(textoTerminosCondiciones)
        builder.setPositiveButton("Aceptar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}